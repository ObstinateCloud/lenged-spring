package com.lenged.spring;

import com.lenged.service.AppConfig;
import com.lenged.service.UserService;
import com.sun.xml.internal.ws.util.StringUtils;

import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class LengedApplicationContext {

    private Class configClass;

    //保存 BeanDefinition
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //单例池
    private ConcurrentHashMap<String,Object> singletonBeanDefinitionMap = new ConcurrentHashMap<>();

    public LengedApplicationContext(Class configClass) {
        this.configClass = configClass;

        //1.包扫描
        if(configClass.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScan = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScan.value();
            path= path.replace(".","/");
            //通过相对路径 组装class文件的相对路径
            ClassLoader classLoader = LengedApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile());
//            System.out.println(file);
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for (File file1 : files) {
                    String absolutePath = file1.getAbsolutePath();
//                    System.out.println(absolutePath);
                    if(absolutePath.endsWith(".class")){
                        //获取class 的全限定名称
                        String className = absolutePath.substring(absolutePath.indexOf("com"),absolutePath.indexOf(".class"));
                        className = className.replace("\\",".");
//                        System.out.println(className);
                        //根据类的全限定明加载类对象
                        try {
                            Class<?> aClass = classLoader.loadClass(className);
                            if(aClass.isAnnotationPresent(Component.class)){
                                Component component = aClass.getAnnotation(Component.class);
                                String beanName =component.value();
                                //默认bean名称处理
                                if("".equals(beanName)){
                                    //首字母小写
                                    beanName = Introspector.decapitalize(aClass.getSimpleName());
                                }
                                //不直接创建bean，先创建一个定义对象 BeanDefinition
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(aClass);
//                                System.out.println(aClass.isAnnotationPresent(Scope.class));
                                if (aClass.isAnnotationPresent(Scope.class)){
                                    Scope scope = aClass.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scope.value());
                                }else{
                                    beanDefinition.setScope("singleton");
                                }
                                //把所有的BeanDefinition 保存
                                beanDefinitionMap.put(beanName,beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }
            


        }
        //2.单例 bean 创建
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if("singleton".equals(beanDefinition.getScope())){
                Object bean = createBean(beanName,beanDefinition);
                singletonBeanDefinitionMap.put(beanName,bean);
            }

        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class aClass = beanDefinition.getType();

        try {
            Object instance = aClass.newInstance();
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Object getBean(String beanName){
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinition ==null){
            throw new NullPointerException();
        }else {
            String scope = beanDefinition.getScope();
            if("singleton".equals(scope)){
                //优先从单例池中找
                Object bean = singletonBeanDefinitionMap.get(beanName);
                if(bean==null){
                    bean = createBean(beanName,beanDefinition);
                    //创建完成后再存入单例池
                    singletonBeanDefinitionMap.put(beanName,bean);
                }
                return bean;

            }else {
                //非单例每次都返回一个对象
                return createBean(beanName,beanDefinition);
            }
            
        }
    }
}
