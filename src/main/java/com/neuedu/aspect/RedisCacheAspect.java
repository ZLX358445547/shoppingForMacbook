package com.neuedu.aspect;

import com.neuedu.common.ServerResponse;
import com.neuedu.json.ObjectMapperApi;
import com.neuedu.redis.RedisApi;
import com.neuedu.utils.MD5Utils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/*
* redis缓存切面类
* */
@Component
@Aspect
public class RedisCacheAspect {


    @Autowired
    RedisApi redisApi;
    @Autowired
    ObjectMapperApi objectMapperApi;
    /*
    * 定义切入点
    * */
    @Pointcut("execution(* com.neuedu.service.impl.ProductServiceImpl.*(..))")
    public  void pointcut(){
    }

    @Around("pointcut()")
    public Object arround(ProceedingJoinPoint joinPoint){
        Object  o = null;
        try{

            //key：md5（全类名 + 方法的名 + 参数）
            StringBuffer keybuffer = new StringBuffer();
            //全类名
            String className = joinPoint.getTarget().getClass().getName();
            System.out.println("======classname"+ className);

            //获取目标的方法名字
             String methodName =   joinPoint.getSignature().getName();
            System.out.println("======methodName" + methodName);
            //如果增加或者修改的缓存不为空，那就要执行清除缓存的操作
            if (methodName.equals("saveOrUpdate")||methodName.equals("set_sale_status")){
                redisApi.flushCache();
                return joinPoint.proceed();

            }


            //方法中的参数
            Object[]  objects = joinPoint.getArgs();
            if (objects!=null){
                for (Object arg :objects){
                    System.out.println("=======arg===="+arg);
                    //加入arg'
                    keybuffer.append(arg);
                }
            }

            //step1:先读缓存

            String key = MD5Utils.getMD5Code(keybuffer.toString());
           //拿到redisApi中的key，转换成String
            String json = redisApi.get(key);
            if (json!=null&&!json.equals("")){
                System.out.println("======读取到了缓存==========");
                ServerResponse serverResponse = objectMapperApi.str2Obj(json, ServerResponse.class);
                return objectMapperApi.str2Obj(json, ServerResponse.class);
            }



            //执行目标方法
            o = joinPoint.proceed();

            System.out.println("======读取到了数据库==========");

            if (o!=null){
                String jsoncache = objectMapperApi.obj2str(o);
                redisApi.set(key,jsoncache);
             System.out.println("======数据库的内容写入了缓存==========");
            }


            System.out.println("========返回值====="+o);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        return o;
    }


}
