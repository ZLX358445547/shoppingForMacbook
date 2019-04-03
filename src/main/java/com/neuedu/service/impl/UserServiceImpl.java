package com.neuedu.service.impl;

import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.UserInfoMapper;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IUserService;
import com.neuedu.utils.MD5Utils;
import com.neuedu.utils.TokenCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.security.provider.MD5;

import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    UserInfoMapper userInfoMapper;

    /*
    * 登录
    * */
    @Override
    public ServerResponse login(String username, String password) {


        //step1参数的非空校验
        if(username==null || username.equals("")){
            return ServerResponse.createServerResponseByError("用户名不能为空");
        }
        if (password==null || password.equals("")){
            return  ServerResponse.createServerResponseByError("密码不能为空");

        }

        //step2检查用户名是否存在
        int  result = userInfoMapper.checkUsername(username);
        if (result == 0){
            return ServerResponse.createServerResponseByError("用户名不存在");
        }

        //step3根据用户名字查找用户信息

        UserInfo userInfo = userInfoMapper.selectUserInfoByUsernameAndPassword(username,MD5Utils.getMD5Code(password));
        if (userInfo == null){
            return  ServerResponse.createServerResponseByError("密码错误");
        }
        //step4返回结果
        //业务需求：设置返回前端的密码为空，
        // 把token放入数据库中
        String token = MD5Utils.getMD5Code(username + password);
        userInfoMapper.updateTokenByUserId(userInfo.getId(),token);
        userInfo.setPassword("");
        //返回登录之后的信息
        return ServerResponse.createServerResponseBySucess(userInfo);
    }

    /*
    * 注册
    * */
    @Override
    public ServerResponse register(UserInfo userinfo) {
        //step1 参数的非空校验
        if(userinfo==null){
            return  ServerResponse.createServerResponseByError("参数是必需的！");
        }
        //step2 校验用户名
        int  result = userInfoMapper.checkUsername(userinfo.getUsername());

        if (result > 0){
            return ServerResponse.createServerResponseByError("用户名已存在");
        }
        //step3 校验邮箱
        int result_email = userInfoMapper.checkEmail(userinfo.getEmail());
        //邮箱已经存在
        if (result_email>0){
            return ServerResponse.createServerResponseByError("邮箱已存在");
        }
        //step4 注册
        userinfo.setRole(Const.RoleEnum.ROLE_CUSTMER.getCode());
        userinfo.setPassword(MD5Utils.getMD5Code(userinfo.getPassword()));
         int count = userInfoMapper.insert(userinfo);
        if (count>0){
            return ServerResponse.createServerResponseBySucess("注册成功");
        }
        //step5 返回结果
        return ServerResponse.createServerResponseBySucessMsg("注册失败");


    }


    /*
     * 根据用户名找密保问题
     * */
    @Override
    public ServerResponse forget_get_question(String username) {
        //step1:参数校验
        if(username ==null||username.equals("")){
            return ServerResponse.createServerResponseByError("用户名不能为空");
        }
        //step2：校验username
        int  result = userInfoMapper.checkUsername(username);
        if (result == 0){
            return ServerResponse.createServerResponseByError("用户名不存在，请重新输入");
        }

        //step3：查找密保问题
        String question = userInfoMapper.selectQuestionByUsername(username);
        if (question==null||question.equals("")){
            return ServerResponse.createServerResponseByError("密保问题为空");
        }
        //返回正确的信息
        return ServerResponse.createServerResponseBySucessMsg(question);
    }
    /*
    * 提交问题答案
    * */
    @Override
    public ServerResponse forget_check_answer(String username, String question, String answer) {
        //step1:参数校验
        if(username ==null||username.equals("")){
            return ServerResponse.createServerResponseByError("用户名不能为空");
        }
        if(question ==null||question.equals("")){
            return ServerResponse.createServerResponseByError("问题不能为空");
        }
        if(answer ==null||answer.equals("")){
            return ServerResponse.createServerResponseByError("回答不能为空");
        }
        //step2：根据username，question，answer查询
        int result_qua= userInfoMapper.selectByUsernameAndQuestionAndAnswer(username,question,answer);
        if(result_qua==0){
            //答案错误
            return ServerResponse.createServerResponseByError("答案错误");
        }
        //step3：服务端生成一个token保存并且返回给客户端
        String forgetToken = UUID.randomUUID().toString();
        //guava  cache   需要将token保存在缓存中
        TokenCache.setKey(username,forgetToken);
        return ServerResponse.createServerResponseBySucess(forgetToken);
    }
    /*
    * 修改密码
    * */
    @Override
    public ServerResponse forget_reset_password(String username, String passwrdNew, String forgetToken) {
        //step1:参数校验
        //step1:参数校验
        if(username ==null||username.equals("")){
            return ServerResponse.createServerResponseByError("用户名不能为空");
        }
        if(passwrdNew ==null||passwrdNew.equals("")){
            return ServerResponse.createServerResponseByError("密码不能为空");
        }
        if(forgetToken ==null||forgetToken.equals("")){
            return ServerResponse.createServerResponseByError("forgetToken不能为空");
        }
        //step2：对forgetToke进行校验
            String token =  TokenCache.getKey(username);
        if(token == null ){
            return ServerResponse.createServerResponseByError("Token过期");
        }
        if(!token.equals(forgetToken)){
            return ServerResponse.createServerResponseByError("无效的Token");
        }

        //step3:修改密码
       int result_reset = userInfoMapper.updateUserPassword(username,MD5Utils.getMD5Code(passwrdNew));
        if (result_reset>0){
            return ServerResponse.createServerResponseBySucessMsg("密码修改成功");
        }
        return ServerResponse.createServerResponseByError("密码修改失败");
    }
    //=================================

    /*
    *检查用户名或者邮箱是否存在
    * */
    @Override
    public ServerResponse check_valid(String str, String type) {
        //1.参数非空校验
        if(str ==null||str.equals("")){
            return ServerResponse.createServerResponseByError("用户名或者邮箱不能为空");
        }
        if(type ==null||type.equals("")){
            return ServerResponse.createServerResponseByError("校验的类型参数不能为空");
        }
        //step2：type：username   校验用户名str
        //              email   校验邮箱str
        if(type.equals("username")){
            int result = userInfoMapper.checkUsername(str);
            if(result>0){
                //用户名已经存在
                return ServerResponse.createServerResponseByError("用户名已经存在");
            }else{
                return ServerResponse.createServerResponseBySucess("用户名可以使用");
            }
        }else if(type.equals("email")){
            int result = userInfoMapper.checkEmail(str);
            if(result>0){
                //邮箱已经存在
                return ServerResponse.createServerResponseByError("邮箱已经存在");
            }else{
                return ServerResponse.createServerResponseBySucessMsg("邮箱可以使用");
            }
        }else{
            return ServerResponse.createServerResponseByError("参数类型错误");
        }


    }
    /*
    *登录状态下修改密码
    * */
    @Override
    public ServerResponse reset_password(String username,String passwordOld, String passwordNew) {
        //step1：参数非空校验
        if(passwordOld ==null||passwordOld.equals("")){
            return ServerResponse.createServerResponseByError("用户的旧密码不能为空");
        }
        if(passwordNew ==null||passwordNew.equals("")){
            return ServerResponse.createServerResponseByError("用户的新密码不能为空");
        }

        //step2:根据username 和password
        UserInfo userInfo =  userInfoMapper.selectUserInfoByUsernameAndPassword(username,MD5Utils.getMD5Code(passwordOld));
        if (userInfo==null){
            return ServerResponse.createServerResponseByError("旧密码错误");
        }
        //step3:修改密码   直接调用上面的方法即可
        //进行新密码的的设置
        userInfo.setPassword(MD5Utils.getMD5Code(passwordNew));

        int result = userInfoMapper.updateByPrimaryKey(userInfo);
        if (result>0){
            return ServerResponse.createServerResponseBySucessMsg("密码修改成功！");
        }
        return ServerResponse.createServerResponseByError("密码修改失败！");
    }
    /*
    * 更新个人信息
    * */
    @Override
    public ServerResponse update_information(UserInfo user) {
        //step1：参数非空校验
        if(user ==null){
            return ServerResponse.createServerResponseByError("参数不能为空");
        }
       //step2:更新用户信息
      int result =   userInfoMapper.updateUserBySelectActive(user);
        if (result>0){
            return ServerResponse.createServerResponseBySucessMsg("更新个人信息成功");
        }
        return ServerResponse.createServerResponseByError("更新个人信息失败！");
    }

    /*
    * 按照id查找
    * */
    @Override
    public UserInfo findUserInfoByUserid(Integer userId) {
        return  userInfoMapper.selectByPrimaryKey(userId);

    }

    @Override
    public UserInfo getUserInfoByToken(String token) {
        //System.out.println(3/0);
        return userInfoMapper.getUserInfoByToken(token);
    }
}
