package com.neuedu.service;

import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Shipping;

import java.io.IOException;
import java.util.Map;

public interface IOrderService {

  /*
  * 创建订单
  * */
  ServerResponse createOrder(Integer userId,Integer shippingId);

  /*
  *取消订单
  * */
  ServerResponse cancel(Integer userId,Long orderNo);

  /*
  * 获取购物车中订单的商品明细
  * */
  ServerResponse get_order_cart_product(Integer userId);
  /*
  * 订单list
  * 需要兼容前台和后台，
  * 前台用户只能查看自己的订单
  * 后台管理员能查看所有订单
  * */
   ServerResponse list(Integer userId,Integer pageNum,Integer pageSize);
  /*
   * 订单详情
   * */
  ServerResponse detail(Long orderNo);

  /*
  * ======================================支付接口============================================
  * */
  /*
  * 支付接口
  * */
  ServerResponse pay(Integer userId,Long orderNo,String path) throws IOException;


  /*
  * 支付宝回调接口
  * */
  ServerResponse alipay_callback(Map<String,String>  map);
  /*
  * 查询订单支付状态状态
  * */

  ServerResponse query_order_pay_status(Long orderNo);


  /*
  * 根据创建时间查询订单
  * */
  void closeOrder(String time);

}

