package com.lenged.service;

import com.lenged.spring.BeanPostProcessor;
import com.lenged.spring.annotation.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class MyBeanPostProcessoer implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        //可以根据bean名称做个性化处理
        if("userService".equals(beanName)){
            System.out.println("before userService create");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {
        //可以根据bean名称做个性化处理
        //此处可以返回代理对象
        if("userService".equals(beanName)){
//            System.out.println("after orderService create");
            Object proxyObj = Proxy.newProxyInstance(MyBeanPostProcessoer.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("切面逻辑");
                    return null;
                }
            });
            return  proxyObj;

        }

        return bean;
    }
}
