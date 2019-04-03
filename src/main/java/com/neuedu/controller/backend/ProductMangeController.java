package com.neuedu.controller.backend;

import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Product;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IProductService;
import com.neuedu.service.impl.ProductServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/manage/product")
public class ProductMangeController {

    @Autowired
    IProductService productService;
/*
* 新增OR更新产品
* */
@RequestMapping(value = "/save.do")
public ServerResponse saveOrUpdate(HttpSession session, Product product){
        UserInfo userInfo = (UserInfo)session.getAttribute(Const.CURREBTUSER);
        //用户没登录的情况
        if (userInfo==null){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NEED_LOGIN.getCode(),
                    Const.ResponseCodeEnum.NEED_LOGIN.getDesc());
        }
        //判断用户权限，必须是管理员才能进行操作
        if(userInfo.getRole()!= Const.RoleEnum.ROLE_ADMIN.getCode()){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NO_PRIVILEGE.getCode(),
                    Const.ResponseCodeEnum.NO_PRIVILEGE.getDesc());
        }
        return productService.saveOrUpdate(product);

    }
    /*
     * 产品上下架
     * */
    @RequestMapping(value = "/set_sale_status.do")
    public ServerResponse set_sale_status(HttpSession session, Integer productId,Integer status){
        UserInfo userInfo = (UserInfo)session.getAttribute(Const.CURREBTUSER);
        //用户没登录的情况
        if (userInfo==null){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NEED_LOGIN.getCode(),
                    Const.ResponseCodeEnum.NEED_LOGIN.getDesc());
        }
        //判断用户权限，必须是管理员才能进行操作
        if(userInfo.getRole()!= Const.RoleEnum.ROLE_ADMIN.getCode()){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NO_PRIVILEGE.getCode(),
                    Const.ResponseCodeEnum.NO_PRIVILEGE.getDesc());
        }
        return productService.set_sale_status(productId,status);

    }


    /*
     * 商品详情
     * */
    @RequestMapping(value = "/detail.do")
    public ServerResponse detail(HttpSession session, Integer productId){
        UserInfo userInfo = (UserInfo)session.getAttribute(Const.CURREBTUSER);
        //用户没登录的情况
        if (userInfo==null){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NEED_LOGIN.getCode(),
                    Const.ResponseCodeEnum.NEED_LOGIN.getDesc());
        }
        //判断用户权限，必须是管理员才能进行操作
        if(userInfo.getRole()!= Const.RoleEnum.ROLE_ADMIN.getCode()){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NO_PRIVILEGE.getCode(),
                    Const.ResponseCodeEnum.NO_PRIVILEGE.getDesc());
        }
        return productService.detail(productId);

    }
    /*
    *查看商品列表
    *
    * */
    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize",required = false,defaultValue = "10") Integer pageSize
                               ){

        UserInfo userInfo = (UserInfo)session.getAttribute(Const.CURREBTUSER);
        //用户没登录的情况
        if (userInfo==null){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NEED_LOGIN.getCode(),
                    Const.ResponseCodeEnum.NEED_LOGIN.getDesc());
        }
        //判断用户权限，必须是管理员才能进行操作
        if(userInfo.getRole()!= Const.RoleEnum.ROLE_ADMIN.getCode()){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NO_PRIVILEGE.getCode(),
                    Const.ResponseCodeEnum.NO_PRIVILEGE.getDesc());
        }
        return productService.list(pageNum,pageSize);

    }

    /*
    * 后台  产品搜索接口
    * */

    @RequestMapping(value = "/search.do")
    public ServerResponse search(HttpSession session,
                                 @RequestParam(value = "productId",required = false) Integer productId,
                                 @RequestParam(value = "productName",required = false) String productName,
                               @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize",required = false,defaultValue = "10") Integer pageSize
    ){

        UserInfo userInfo = (UserInfo)session.getAttribute(Const.CURREBTUSER);
        //用户没登录的情况
        if (userInfo==null){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NEED_LOGIN.getCode(),
                    Const.ResponseCodeEnum.NEED_LOGIN.getDesc());
        }
        //判断用户权限，必须是管理员才能进行操作
        if(userInfo.getRole()!= Const.RoleEnum.ROLE_ADMIN.getCode()){
            return ServerResponse.createServerResponseByError(Const.ResponseCodeEnum.NO_PRIVILEGE.getCode(),
                    Const.ResponseCodeEnum.NO_PRIVILEGE.getDesc());
        }
        return productService.search(productId,productName,pageNum,pageSize);

    }


}
