package com.neuedu.controller.portal;

import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.ICartService;
import com.neuedu.service.impl.CartServiceImpl;
import org.apache.catalina.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;


@RestController
@RequestMapping(value = "/cart")
public class CartController {
    @Autowired
    ICartService cartService;

    /*
    * 购物车中添加商品
    * */
    @RequestMapping(value = "/add.do")
    public ServerResponse add(HttpSession session,Integer productId,Integer count){
        //判断是否登录，权限问题
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return cartService.add(userInfo.getId(),productId,count);
    }
    /*
    * 购物车列表
    * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session){
        //判断是否登录，权限问题
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return cartService.list(userInfo.getId());
    }

    /*
    * 更新购物车中某个商品的数量
    * */

    @RequestMapping(value = "/update.do")
    public ServerResponse update(HttpSession session,Integer productId,Integer count){
        //判断是否登录，权限问题
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return cartService.update(userInfo.getId(),productId,count);
    }
    /*
    * 移除购物车某个产品(可能是多个商品   注意参数)
    * */
    @RequestMapping(value = "/delete_Product.do")
    public ServerResponse delete_Product(HttpSession session,String productIds){
        //判断是否登录，权限问题
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return cartService.delete_Product(userInfo.getId(),productIds);
    }

    /*
    * 购物车选中某个商品
    * */
    @RequestMapping(value = "/select.do")
    public ServerResponse select(HttpSession session,Integer productId){
        //判断是否登录，权限问题
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return cartService.select(userInfo.getId(),productId
                ,Const.CartCheckedEnum.PRODUCT_CHECKED.getCode());
    }
    /*
    * 取消选中某一商品
    * */
    @RequestMapping(value = "/un_select.do")
    public ServerResponse un_select(HttpSession session,Integer productId){
        //判断是否登录，权限问题
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return cartService.select(userInfo.getId(),productId
                ,Const.CartCheckedEnum.PRODUCT_UNCHECKED.getCode());
    }
    /*
     * 全选 商品
     * */
    @RequestMapping(value = "/select_all.do")
    public ServerResponse select_all(HttpSession session){
        //判断是否登录，权限问题
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return cartService.select(userInfo.getId(),null
                ,Const.CartCheckedEnum.PRODUCT_CHECKED.getCode());
    }
    /*
    * 取消全选
    * */
    @RequestMapping(value = "/un_select_all.do")
    public ServerResponse un_select_all(HttpSession session){
        //判断是否登录，权限问题
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return cartService.select(userInfo.getId(),null
                ,Const.CartCheckedEnum.PRODUCT_UNCHECKED.getCode());
    }

    /*
     * 购物车中产品的数量
     * */
    @RequestMapping(value = "/get_cart_product_count.do")
    public ServerResponse get_cart_product_count(HttpSession session){
        //判断是否登录，权限问题
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return cartService.get_cart_product_count(userInfo.getId());
    }

}
