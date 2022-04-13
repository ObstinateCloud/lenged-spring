package com.lenged.spring;

/**
 * 后置处理器
 */
public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(String beanName,Object bean);

    Object postProcessAfterInitialization(String beanName,Object bean);
}
