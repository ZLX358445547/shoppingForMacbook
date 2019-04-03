package com.neuedu.service;

import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;

public interface ICartService {

    /*
    * 添加商品接口
    * */
    public ServerResponse add(Integer userId ,Integer productId,Integer count);


    /*购物车列表*/
    ServerResponse  list(Integer userId);

    /*
    * 更新购物车中某个商品的数量
    * */
    ServerResponse update (Integer userId,Integer productId,Integer count);

    /*
    * 移除购物车某个产品(可能是多个商品   注意参数)
    * */
      ServerResponse delete_Product (Integer userId,String productIds);
     /*
     * 购物车中选中某个商品
     * */
     ServerResponse select(Integer userId,Integer productId,Integer check);

     /*
     * 查询购物车中产品的数量
     * */
      ServerResponse get_cart_product_count(Integer userId);



}

