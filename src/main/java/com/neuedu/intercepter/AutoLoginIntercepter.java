package com.neuedu.intercepter;

import com.google.gson.Gson;
import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class AutoLoginIntercepter implements HandlerInterceptor {

    //写完实现类之后进行注入
    @Autowired
    IUserService userService;

    /*
    * 第一次发送请求，通过拦截器
    * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Cookie[] cookies = request.getCookies();
        System.out.println("============preHandle==========");
        if(cookies != null){
            for (Cookie cookie : cookies){
                String name = cookie.getName();
                if (name.equals("token")){
                    String value = cookie.getValue();
                    //根据token来查询用户信息
                UserInfo userInfo = userService.getUserInfoByToken(value);
                if (userInfo!=null){
                    request.getSession().setAttribute(Const.CURREBTUSER,userInfo);
                    return true;
                }
                }
            }
        }
        response.reset();
        response.setContentType("text/json.character=utf-8");
        response.setCharacterEncoding("utf-8");
        PrintWriter printWriter = response.getWriter();
        ServerResponse serverResponse = ServerResponse.createServerResponseByError(100,"需要登录");
        Gson gson = new Gson();
        String json = gson.toJson(serverResponse);
        printWriter.write(json);
        printWriter.flush();
        printWriter.close();

        return false;
    }
    /*
    *preHandle返回值为false的时候，不调用

    * 只有preHandle返回值为true的时候，调用postHandle
    * controller处理完之后，返回的时候，通过拦截器
    * */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        System.out.println("==============postHandler===================");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        System.out.println("===============afterComletion==============================");
    }
}
