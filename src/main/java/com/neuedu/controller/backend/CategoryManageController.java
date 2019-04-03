package com.neuedu.controller.backend;

import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;

import com.neuedu.pojo.UserInfo;
import com.neuedu.service.ICategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/manager/category")
public class CategoryManageController {
    @Autowired
    ICategoryService categoryService;
    /*
    * 查询品类子节点（平级）
    * 根据父类id去找子类id
    * */
    @RequestMapping(value = "/get_category.do")
    public ServerResponse get_category(HttpSession session,Integer parentId){
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
        return categoryService.get_category(parentId);
    }
    /*
    * 增加节点
    * */
    @RequestMapping(value = "/add_category.do")
    public ServerResponse add_category(HttpSession session,
                                       @RequestParam(required = false,defaultValue = "0") Integer parentId,
                                        String categoryName){

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
        return categoryService.add_category(parentId,categoryName);



    }

    /*
     * 修改子节点
     * */
    @RequestMapping(value = "/set_category_name.do")
    public ServerResponse set_category_name(HttpSession session,
                                        Integer categoryId,
                                       String categoryName){

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
        return categoryService.set_category_name(categoryId,categoryName);



    }

    /*
    * 获取当前分类id以及递归子节点catgoryid
    * */
    @RequestMapping(value = "/get_deep_category.do")
    public ServerResponse get_deep_category(HttpSession session,
                                            Integer categoryId){

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
        return categoryService.get_deep_category(categoryId);



    }
}
