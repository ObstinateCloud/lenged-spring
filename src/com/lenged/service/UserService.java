package com.lenged.service;

import com.lenged.spring.*;
import com.lenged.spring.annotation.Autowired;
import com.lenged.spring.annotation.Component;

@Component
//@Scope("prototype")
public class UserService implements BeanNameAware,InitializingBean,UserInterface {

    @Autowired
    private OrderService orderService;

    String beanName;


    public void test(){
       System.out.println(orderService);
   }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("init method");
    }
}
