package com.neuedu.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Shipping;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IAddressService;
import com.neuedu.service.IOrderService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping(value = "/order")
public class OrderController {

    @Autowired
    IOrderService orderService;

    /*
     * 创建订单
     * */
    @RequestMapping(value = "/create.do")
    public ServerResponse createOrder(HttpSession session,Integer shippingId){
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURREBTUSER);


        return  orderService.createOrder(userInfo.getId(),shippingId);
    }

    /*
     * 取消订单
     * */
    @RequestMapping(value = "/cancel.do")
    public ServerResponse cancel(HttpSession session,Long orderNo){
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURREBTUSER);


        return  orderService.cancel(userInfo.getId(),orderNo);
    }

    /*
     * 获取 订单 的商品信息
     * */
    @RequestMapping(value = "/get_order_cart_product.do")
    public ServerResponse get_order_cart_product(HttpSession session){
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURREBTUSER);

        return  orderService.get_order_cart_product(userInfo.getId());
    }
    /*
    * 订单list
    * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session,
                            @RequestParam(required = false,defaultValue = "1") Integer pageNum,
                            @RequestParam(required = false,defaultValue = "10") Integer pageSize

    ){
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURREBTUSER);

        return  orderService.list(userInfo.getId(),pageNum,pageSize);
    }
    /*
    * 订单详情
    * */
    @RequestMapping(value = "/detail.do")
    public ServerResponse detail(HttpSession session, Long orderNo
    ){
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURREBTUSER);


        return  orderService.detail(orderNo);
    }

    //========================================支付宝支付===============================================
    /*
    * 支付宝支付接口
    * */
    @RequestMapping(value = "/pay.do")
    public ServerResponse pay(HttpSession session,Long orderNo,HttpServletRequest request) throws IOException {
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURREBTUSER);

        String path = request.getSession().getServletContext().getRealPath("upload");
        return orderService.pay(userInfo.getId(),orderNo,path);
    }


    /*
    * 支付宝回调应用服务器的接口
    *
    * */

    @RequestMapping(value = "/alipay_callback.do")
    public ServerResponse callback(HttpServletRequest request){
        System.out.println("==================支付宝回调应用服务器的接口===================");
        //1.获取回调参数
        Map<String,String[]> params=request.getParameterMap();
        Map<String,String> requestparams= Maps.newHashMap();
        Iterator<String> it=params.keySet().iterator();
        while(it.hasNext()){
            String key=it.next();
            String[] strArr=params.get(key);
            String value="";
            for(int i=0;i<strArr.length;i++){
                value= (i==strArr.length-1)?value+strArr[i]: value+strArr[i]+",";
            }
            requestparams.put(key,value);
        }
        //step1:支付宝验签

        try {
            //一定要做移除，不然支付宝验证签名无效
            requestparams.remove("sign_type");
            boolean result =  AlipaySignature.rsaCheckV2(requestparams, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if (!result){
                return ServerResponse.createServerResponseByError("非法请求，业务不通过");
            }
            //处理业务逻辑
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.println(requestparams);
        return orderService.alipay_callback(requestparams);

     }

    /*
     * 查询订单的支付状态
     * */
    @RequestMapping(value = "/query_order_pay_status.do")
    public ServerResponse query_order_pay_status(HttpSession session,Long orderNo)  {
        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURREBTUSER);
        return orderService.query_order_pay_status(orderNo);
    }


}