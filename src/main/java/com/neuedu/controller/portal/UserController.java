package com.neuedu.controller.portal;

import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.UserInfoMapper;
import com.neuedu.json.ObjectMapperApi;
import com.neuedu.pojo.UserInfo;
import com.neuedu.redis.RedisApi;
import com.neuedu.redis.RedisProperties;
import com.neuedu.service.IUserService;
import com.neuedu.utils.MD5Utils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping(value = "/user")
public class UserController {


    @Autowired
    IUserService userService;
    @Autowired
    RedisProperties redisProperties;
    /*
    * 登录
    * */
    @RequestMapping(value = "/login.do")
   public ServerResponse login(HttpSession session,
                               @RequestParam(value = "username") String username,
                               @RequestParam(value = "password") String password,
                               HttpServletResponse response
                               ){
        //测试redis连接
//        System.out.println(redisProperties.getMaxIdle());

        ServerResponse serverResponse =  userService.login(username,password);
        //登录成功

       if (serverResponse.isSuccess()){
           //得到前台的数据，放入userinfo里面
           UserInfo  userInfo =  (UserInfo)serverResponse.getData();
           //设置userInfo的值
           session.setAttribute(Const.CURREBTUSER,userInfo);
           //=================
           // 设置返回的cookie
           //拼接
           String token = MD5Utils.getMD5Code(username + password);
           //设置cookie
           Cookie cookie = new Cookie(Const.CURREBTUSER,token);
           //设置cookie的相关属性
           cookie.setPath("/");
           cookie.setMaxAge(1800*1000);
           cookie.setHttpOnly(true);
           //添加cookie
           response.addCookie(cookie);


       }

       return serverResponse;

   }
   /*
   * 注册
   * */
   @RequestMapping(value="/register.do")
   public ServerResponse register(HttpSession session,UserInfo userInfo){

       ServerResponse serverResponse =  userService.register(userInfo);
       return serverResponse;

   }

   /*
   * 忘记密码
   * */
    /*1.
     * 根据用户名查询密保问题
     * */
    @RequestMapping(value="/forget_get_question.do")
    public ServerResponse forget_get_question(String username){

        ServerResponse serverResponse =  userService.forget_get_question(username);
        return serverResponse;

    }
    /*2.
    * 提交问题答案
    * */
    @RequestMapping(value = "/forget_check_answer.do")
    public ServerResponse foget_check_answer(String username,String question,String answer){
        ServerResponse serverResponse = userService.forget_check_answer(username,question,answer);
        return serverResponse;
    }

    /*3.
    * 忘记密码重置密码
    * */
    @RequestMapping(value = "/forget_reset_password.do")
    public ServerResponse forget_reset_password(String username,String passwrdNew,String forgetToken){
        ServerResponse serverResponse = userService.forget_reset_password(username,passwrdNew,forgetToken);
        return serverResponse;
    }

    /*
    * 检查用户名字或者邮箱是否有效
    * */
    @RequestMapping(value = "/check_valid.do")
    public ServerResponse check_valid(String str,String type){
        ServerResponse serverResponse = userService.check_valid(str,type);
        return serverResponse;
    }

    /*
    * 获取登录用户的信息
    *
    * */
    @RequestMapping(value = "/get_user_info.do")
    public ServerResponse get_user_info(HttpSession session){
       UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

       userInfo.setPassword("");
        return ServerResponse.createServerResponseBySucess(userInfo);
    }

    /*
    * 登录状态下重置密码
    * */

    @RequestMapping(value = "/reset_password.do")
    public ServerResponse reset_password(HttpSession session,String passwordOld,String passwordNew){
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return userService.reset_password(userInfo.getUsername(),passwordOld,passwordNew);
    }
    /*
     * 登录状态下更新个人信息
     * 注意更新之后的信息要返回到session 然后到前台进行展示
     * 要在写一个按照id查找的方法才行
     * */

    @RequestMapping(value = "/update_information.do")
    public ServerResponse update_information(HttpSession session,UserInfo user){
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        user.setId(userInfo.getId());
        ServerResponse serverResponse =  userService.update_information(user);
        if (serverResponse.isSuccess()){
            //更新session的用户信息
                UserInfo userInfo1 = userService.findUserInfoByUserid(userInfo.getId());
                session.setAttribute(Const.CURREBTUSER,userInfo1);
        }
        return serverResponse;
    }


    /*
     * 获取登录用户的详细的信息
     *
     * */
    @RequestMapping(value = "/get_information.do")
    public ServerResponse get_information(HttpSession session){
        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        userInfo.setPassword("");
        return ServerResponse.createServerResponseBySucess(userInfo);
    }

    /*
     * 退出登录
     *
     * */
    @RequestMapping(value = "/logout.do")
    public ServerResponse logout(HttpSession session){
      session.removeAttribute(Const.CURREBTUSER);
        return ServerResponse.createServerResponseBySucessMsg("成功退出");
    }
    //==========================测试redis===================================
    @Autowired
    private JedisPool jedisPool;
    @RequestMapping(value = "/redis.do")
    public String getJedis(){
        //将连接拿出来
        Jedis jedis = jedisPool.getResource();
         String  value = jedis.set("zlx","zhanglixing");
         //将连接还回去
        jedis.close();
        return value;
    }


    //====测试api====


    @Autowired
    private RedisApi redisApi;

    @RequestMapping(value="key/{key}")
    public  String getkey(@PathVariable("key") String key){
        String value = redisApi.get(key);

        return  value;
    }

    //===============================测试ObjectMapperApi类  将对象转换为字符串的api==================================================

   @Autowired
   ObjectMapperApi objectMapperApi;
    @Autowired
    UserInfoMapper userInfoMapper;

    @RequestMapping(value = "/json/{userid}")
    public ServerResponse<UserInfo> findUserByJson(@PathVariable Integer userid,HttpSession session){

        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userid);
        String json = objectMapperApi.obj2str(userInfo);
        System.out.println("============================="+json);
        return ServerResponse.createServerResponseBySucess(null,userInfo);
    }



    @RequestMapping(value = "/jsonPretty/{userid}")
    public ServerResponse<UserInfo> findUserByJsonPretty(@PathVariable Integer userid,HttpSession session){

        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userid);
        List<UserInfo> userInfoList = new ArrayList<>();
        userInfoList.add(userInfo);
        String json = objectMapperApi.obj2strPretty(userInfoList);
        //难于理解
        List<UserInfo> userInfoList1= objectMapperApi.str2Obj(json, new TypeReference<List<UserInfo>>() {});
         UserInfo userInfo1 = objectMapperApi.str2Obj(json,UserInfo.class);
        System.out.println("============================="+userInfo1);
        return ServerResponse.createServerResponseBySucess(null,userInfo);
    }
}
