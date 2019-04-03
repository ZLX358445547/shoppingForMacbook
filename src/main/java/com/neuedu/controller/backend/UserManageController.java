package com.neuedu.controller.backend;

import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/*
* 后台用户控制类
* */

@RestController
@RequestMapping(value = "/manager")
public class UserManageController {
    @Autowired
    IUserService userService;

    /*
     * 管理员登录
     * */
    @RequestMapping(value = "/login.do")
    public ServerResponse login(HttpSession session,
                                @RequestParam(value = "username") String username,
                                @RequestParam(value = "password") String password
    ){

        ServerResponse serverResponse =  userService.login(username,password);
        //登录成功
        if (serverResponse.isSuccess()){
            UserInfo userInfo =  (UserInfo)serverResponse.getData();
            if(userInfo.getRole() == Const.RoleEnum.ROLE_CUSTMER.getCode()){
                return ServerResponse.createServerResponseByError("抱歉，您没有登录权限");
            }
            session.setAttribute(Const.CURREBTUSER,userInfo);
        }
        return serverResponse;

    }
}
