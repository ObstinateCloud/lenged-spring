package com.lenged.service;

import com.lenged.spring.LengedApplicationContext;

public class Test {

    public static void main(String[] args) {
        //1.创建spring容器
        LengedApplicationContext context = new LengedApplicationContext(AppConfig.class);

        System.out.println(context.getBean("userService"));
        System.out.println(context.getBean("userService"));
        System.out.println(context.getBean("userService"));
        System.out.println(context.getBean("userService"));
    }
}
