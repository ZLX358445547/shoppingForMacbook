package com.neuedu.service;

import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Shipping;

public interface IAddressService {

   /*
   * 添加地址
   * @param session
   * @param shipping
   * @return
   * */
   public ServerResponse add(Integer userId, Shipping shipping);

   /*
   * 删除地址
   * */
   public ServerResponse del(Integer userId,Integer shippingId);
   /*
   * 登录状态下更新
   * */
   ServerResponse update (Shipping shipping);

   /*
   * 选中某一个商品
   * */
   ServerResponse  select(Integer shippingId);
   /*
   * 分页查询
   * */
   ServerResponse list(Integer pageNum,Integer pageSize);

}

