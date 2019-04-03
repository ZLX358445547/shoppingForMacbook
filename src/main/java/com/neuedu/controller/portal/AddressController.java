package com.neuedu.controller.portal;

import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Shipping;
import com.neuedu.pojo.UserInfo;
import com.neuedu.service.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/shipping")
public class AddressController {
    @Autowired
    IAddressService addressService;
    /*
    * TIANJIADIZHIMOKUAIGIT
    * 添加地址模块
    * */
    @RequestMapping(value = "/add.do")
    public ServerResponse add(HttpSession session, Shipping shipping){

          UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

          return addressService.add(userInfo.getId(),shipping);
    }

    /*
    * 删除地址模块
    * */
    @RequestMapping(value = "/del.do")
    public ServerResponse del(HttpSession session, Integer shippingId){

        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        return addressService.del(userInfo.getId(),shippingId);
    }

    /*
     *
     * 更新地址模块
     * */
    @RequestMapping(value = "/update.do")
    public ServerResponse update(HttpSession session, Shipping shipping){

        UserInfo userInfo = (UserInfo) session.getAttribute(Const.CURREBTUSER);

        shipping.setUserId(userInfo.getId());
        return addressService.update(shipping);
    }

    /**
     * 选中查看具体的地址
     * */

    @RequestMapping("/select.do")
    public ServerResponse select(HttpSession session,Integer shippingId){

        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURREBTUSER);


        return  addressService.select(shippingId);

    }



    /**
     * 分页查询
     * */

    @RequestMapping("/list.do")
    public ServerResponse list(HttpSession session,
                               @RequestParam(required = false,defaultValue = "1") Integer pageNum,
                               @RequestParam(required = false,defaultValue = "10")Integer pageSize){

        UserInfo userInfo=(UserInfo) session.getAttribute(Const.CURREBTUSER);


        return  addressService.list(pageNum,pageSize);

    }

}
