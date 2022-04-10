package com.lenged.service;

import com.lenged.spring.Autowired;
import com.lenged.spring.BeanNameAware;
import com.lenged.spring.Component;
import com.lenged.spring.Scope;

@Component
//@Scope("prototype")
public class UserService implements BeanNameAware {

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
}
