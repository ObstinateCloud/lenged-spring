package com.lenged.spring;

import com.lenged.spring.annotation.Autowired;
import com.lenged.spring.annotation.Component;
import com.lenged.spring.annotation.ComponentScan;
import com.lenged.spring.annotation.Scope;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LengedApplicationContext {

    private Class configClass;

    //保存 BeanDefinition
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //单例池
    private ConcurrentHashMap<String, Object> singletonBeanDefinitionMap = new ConcurrentHashMap<>();
    //
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public LengedApplicationContext(Class configClass) {
        this.configClass = configClass;

        //1.包扫描
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScan.value();
            path = path.replace(".", "/");
            //通过相对路径 组装class文件的相对路径
            ClassLoader classLoader = LengedApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile());
//            System.out.println(file);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File file1 : files) {
                    String absolutePath = file1.getAbsolutePath();
//                    System.out.println(absolutePath);
                    if (absolutePath.endsWith(".class")) {
                        //获取class 的全限定名称
                        String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                        className = className.replace("\\", ".");
//                        System.out.println(className);
                        //根据类的全限定明加载类对象
                        try {
                            Class<?> aClass = classLoader.loadClass(className);
                            if (aClass.isAnnotationPresent(Component.class)) {

                                //判断一个类是否实现了某个接口
                                if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) aClass.newInstance();
                                    beanPostProcessorList.add(beanPostProcessor);
                                }

                                Component component = aClass.getAnnotation(Component.class);
                                String beanName = component.value();
                                //默认bean名称处理
                                if ("".equals(beanName)) {
                                    //首字母小写
                                    beanName = Introspector.decapitalize(aClass.getSimpleName());
                                }
                                //不直接创建bean，先创建一个定义对象 BeanDefinition
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(aClass);
//                                System.out.println(aClass.isAnnotationPresent(Scope.class));
                                if (aClass.isAnnotationPresent(Scope.class)) {
                                    Scope scope = aClass.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scope.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                //把所有的BeanDefinition 保存
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }
        }


        //2.单例 bean 创建
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = createBean(beanName, beanDefinition);
                singletonBeanDefinitionMap.put(beanName, bean);
            }

        }






    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class aClass = beanDefinition.getType();

        try {
            Object instance = null;
            try {
                //要求bean必须要有无参构造方法
                instance = aClass.getConstructor().newInstance();

                //为bean中的属性赋值
                for (Field declaredField : aClass.getDeclaredFields()) {
                    if (declaredField.isAnnotationPresent(Autowired.class)) {
                        declaredField.setAccessible(true);
                        declaredField.set(instance, getBean(declaredField.getName()));
                    }

                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            //1. Aware方法回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            //2.前置方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessBeforeInitialization(beanName,instance);
            }


            //3.初始化方法或参数赋值
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            //4.后置方法 初始化后 执行aop逻辑
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(beanName,instance);
            }
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Object getBean(String beanName) {
        //判断是否有bean的定义
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            if ("singleton".equals(scope)) {
                //优先从单例池中找
                Object bean = singletonBeanDefinitionMap.get(beanName);
                if (bean == null) {
                    bean = createBean(beanName, beanDefinition);
                    //创建完成后再存入单例池
                    singletonBeanDefinitionMap.put(beanName, bean);
                }
                return bean;

            } else {
                //非单例每次都返回一个对象
                return createBean(beanName, beanDefinition);
            }

        }
    }
}
