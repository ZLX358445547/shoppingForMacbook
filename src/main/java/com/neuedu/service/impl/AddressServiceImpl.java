package com.neuedu.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.ShippingMapper;
import com.neuedu.pojo.Shipping;
import com.neuedu.service.IAddressService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service(value = "iShippingService")
public class AddressServiceImpl implements IAddressService {

    @Autowired
    ShippingMapper shippingMapper;
    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        //step1:参数非空校验
        if (shipping==null){
            return ServerResponse.createServerResponseByError("参数不能为空");
        }

        //step2:添加
        shipping.setUserId(userId);
        shippingMapper.insert(shipping);
        //step3:返回结果
        //新建map集合
        Map<String,Integer> map = Maps.newHashMap();
        map.put("shippingId",shipping.getId());
        return ServerResponse.createServerResponseBySucess(map);
    }
    /*
    * 删除地址
    * */
    @Override
    public ServerResponse del(Integer userId, Integer shippingId) {
        if (shippingId==null){
            return ServerResponse.createServerResponseByError("参数不能为空");
        }

        //step2:删除
        int result=shippingMapper.deleteByUserIdAndShippingId(userId,shippingId);
        //step3:返回结果
        //新建map集合
        if (result>0){
            return ServerResponse.createServerResponseBySucessMsg("删除成功");
        }
        return ServerResponse.createServerResponseByError("删除失败");
    }
    /*
   * 登录状态更新地址
    * */
    @Override
    public ServerResponse update(Shipping shipping) {
        //step1:参数非空校验
        if (shipping == null) {
            return ServerResponse.createServerResponseByError("参数不能为空");
        }

        //step2:添加
          int result  = shippingMapper.updateBySelectiveKey(shipping);
        //step3:返回结果
        if (result >0){
            return ServerResponse.createServerResponseBySucessMsg("更新成功");
        }
        return  ServerResponse.createServerResponseByError("更新失败");
    }
    /*
    * 选中查看具体的地址
    * */
    @Override
    public ServerResponse select(Integer shippingId) {
        //step1:参数非空校验
        if (shippingId == null){
            return ServerResponse.createServerResponseByError("参数不能为空");
        }
        Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
        return ServerResponse.createServerResponseBySucess(shipping);
    }

    /*
     * 分页显示地址列表
     * */
    @Override
    public ServerResponse list(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippings = shippingMapper.selectAll();
        PageInfo pageInfo = new PageInfo(shippings);
        return ServerResponse.createServerResponseBySucess(pageInfo);
    }




}


