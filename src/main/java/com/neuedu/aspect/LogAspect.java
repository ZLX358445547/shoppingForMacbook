/*
package com.neuedu.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.zip.ZipOutputStream;

*/
/**
 * 日志服务切面类
 *//*

@Component
@Aspect
public class LogAspect {

    //定义切点表达式
   @Pointcut("execution(* com.neuedu.service.impl.UserServiceImpl.*(..))")
   public  void pointcut(){

   }
   //前置通知
    @Before("pointcut()")
   public void before(){
       System.out.println("==========前置通知===========");
   }

    //后置通知
    @After("pointcut()")
    public  void after(){
        System.out.println("=============后置通知=========");
   }

   //环绕通知
    @Around("pointcut()")
    public Object arround(ProceedingJoinPoint proceedingJoinPoint){
       //执行切入点的匹配的方法
        Object o = null;
        try {
            System.out.println("===========arround_before===========");
           o =  proceedingJoinPoint.proceed();
            System.out.println("===========arround_after==========");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.out.println("============arround_throwing==========");
        }
        System.out.println("==========arround_after_returning============");
        return o;
   }


}
*/
