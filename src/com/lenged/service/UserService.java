package com.lenged.service;

import com.lenged.spring.Autowired;
import com.lenged.spring.Component;
import com.lenged.spring.Scope;

@Component
//@Scope("prototype")
public class UserService {

    @Autowired
    private OrderService orderService;


    private void test(){
       System.out.println(orderService);
   }
}
