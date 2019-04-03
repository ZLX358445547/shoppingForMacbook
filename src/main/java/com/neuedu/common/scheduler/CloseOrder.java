package com.neuedu.common.scheduler;
/*
*
* 定时关闭订单
* */

import com.neuedu.service.impl.OrderServiceImpl;
import com.neuedu.service.impl.ProductServiceImpl;
import com.neuedu.utils.PropertiesUtils;


import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class CloseOrder {

    @Autowired
    OrderServiceImpl orderService;

    //注解含义是：每个一分钟去执行方法
    @Scheduled(cron = "0 */1 * * * *")
    public void closeOrder(){
        //下单之后一个小时没有付款的，进行关闭
        System.out.println("================订单已经被关闭============");


        //读取配置文件内容
        Integer hour = Integer.parseInt(PropertiesUtils.readByKey("close.order.time"));
        String date = com.neuedu.utils.DateUtils.dateToStr(DateUtils.addHours(new Date(),-hour));

        orderService.closeOrder(date);

    }
}
