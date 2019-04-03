package com.neuedu;

import com.neuedu.intercepter.AutoLoginIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@SpringBootConfiguration
public class RegisterIntercepter implements WebMvcConfigurer {

    @Autowired
    AutoLoginIntercepter autoLoginIntercepter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {


        //自动登录    /portal/user/get_information.do
        //           /portal/cart/add.do
        //           /portal/order/create.do

        //要进行拦截的集合
        List<String> excludeList = new ArrayList<>();
        excludeList.add("/back/**");
        excludeList.add("/user/login.do");
        excludeList.add("/user/register.do");
        excludeList.add("/user/logout.do");
        excludeList.add("/product/**");

        //注册拦截器，设置拦截与不拦截的url
        //registry.addInterceptor(autoLoginIntercepter).addPathPatterns("/**").excludePathPatterns(excludeList);

    }
}
