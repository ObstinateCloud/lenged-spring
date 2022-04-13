package com.lenged.service;

import com.lenged.spring.LengedApplicationContext;

public class Test {

    public static void main(String[] args) {
        //1.创建spring容器
        LengedApplicationContext context = new LengedApplicationContext(AppConfig.class);
// 1.单例、多例测试
//        System.out.println(context.getBean("userService"));
//        System.out.println(context.getBean("userService"));
//   2.依赖注入测试
//        UserService userService = (UserService) context.getBean("userService");
        //代理方法
        UserInterface userService = (UserInterface) context.getBean("userService");
        userService.test();
//        System.out.println(userService.beanName);
    }
}
