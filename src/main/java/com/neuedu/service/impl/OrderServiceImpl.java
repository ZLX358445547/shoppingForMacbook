package com.neuedu.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;

import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;

import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;

import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.*;
import com.neuedu.pojo.*;
import com.neuedu.service.IOrderService;

import com.neuedu.utils.*;
import com.neuedu.vo.CartOrderItemVO;
import com.neuedu.vo.OrderItemVO;
import com.neuedu.vo.OrderVO;
import com.neuedu.vo.ShippingVO;

import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements IOrderService {
    @Autowired
    CartMapper cartMapper;
    @Autowired
    ProductMapper productMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    ShippingMapper shippingMapper;
    @Autowired
    PayInfoMapper payInfoMapper;
    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        //step1:参数非空校验

        if(shippingId==null){
            return ServerResponse.createServerResponseByError("地址参数不能为空");
        }

        //step2:根据userId查询购物车中已选中的商品 -》List<Cart>
        List<Cart> cartList= cartMapper.findCartListByUserIdAndChecked(userId);

        //step3:List<Cart>-->List<OrderItem>
        ServerResponse serverResponse= getCartOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return  serverResponse;
        }

        //step4:创建订单order并将其保存到数据库
        //计算订单的价格
        BigDecimal orderTotalPrice=new BigDecimal("0");
        List<OrderItem> orderItemList=(List<OrderItem>)serverResponse.getData();
        if(orderItemList==null||orderItemList.size()==0){
            return  ServerResponse.createServerResponseByError("购物车为空");
        }
        orderTotalPrice=getOrderPrice(orderItemList);
        Order order=createOrder(userId,shippingId,orderTotalPrice);

        /*int a=3/0;*/

        if(order==null){
            return ServerResponse.createServerResponseByError("订单创建失败");
        }
        //step5:将List<OrderItem>保存到数据库
        for(OrderItem orderItem:orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        //批量插入
        orderItemMapper.insertBatch(orderItemList);
        //step6:扣库存
        reduceProductStock(orderItemList);
        //step7:购物车中清空已下单的商品
        cleanCart(cartList);

        //step8:
        // 新建OrderItemVO，ShippingVO写相应逻辑，返回OrderVO
        OrderVO orderVO= assembleOrderVO(order,orderItemList,shippingId);
        return ServerResponse.createServerResponseBySucess(orderVO);
    }


    /*
    * step3:
    * 将List<Cart>转换为List<OrderItem>(订单明细)的方法
    * */
    private  ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList){

        if(cartList==null||cartList.size()==0){
            return ServerResponse.createServerResponseByError("购物车空");
        }
        List<OrderItem> orderItemList= Lists.newArrayList();

        for(Cart cart:cartList){

            OrderItem orderItem=new OrderItem();
            orderItem.setUserId(userId);
            Product product=productMapper.selectByPrimaryKey(cart.getProductId());
            if(product==null){
                return  ServerResponse.createServerResponseByError("id为"+cart.getProductId()+"的商品不存在");
            }
            if(product.getStatus()!= Const.ProductStatusEnum.PRODUCT_ONLINE.getCode()){//商品下架
                return ServerResponse.createServerResponseByError("id为"+product.getId()+"的商品已经下架");
            }
            if(product.getStock()<cart.getQuantity()){//库存不足
                return ServerResponse.createServerResponseByError("id为"+product.getId()+"的商品库存不足");
            }
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductId(product.getId());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setProductName(product.getName());
            orderItem.setTotalPrice(BigDecimalUtils.mul(product.getPrice().doubleValue(),cart.getQuantity().doubleValue()));

            orderItemList.add(orderItem);
        }

        return  ServerResponse.createServerResponseBySucess(orderItemList);
    }





    /**
     * step4：
     * （1）创建订单
     * （3）订单状态，需要定义枚举类型，OrderStatusEnum
     * （4）支付状态定义枚举类型
     *
     * */

    private Order createOrder(Integer userId, Integer shippingId, BigDecimal orderTotalPrice){
        Order order=new Order();
        order.setOrderNo(generateOrderNO());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        //订单状态
        order.setStatus(Const.OrderStatusEnum.ORDER_UN_PAY.getCode());
        //订单金额
        order.setPayment(orderTotalPrice);
        order.setPostage(0);
        order.setPaymentType(Const.PaymentEnum.ONLINE.getCode());

        //保存订单
        int result=orderMapper.insert(order);
        if(result>0){
            return order;
        }
        return  null;
    }
    /**
     *step4：
     * （2）生成订单编号
     * */
    private  Long generateOrderNO(){
        //时间戳+随机数（100以内）
        return System.currentTimeMillis()+new Random().nextInt(100);
    }
    /*
    * step4：
    * （5）计算订单的总价格的方法
    * */
    private  BigDecimal getOrderPrice(List<OrderItem> orderItemList){

        BigDecimal bigDecimal=new BigDecimal("0");

        for(OrderItem orderItem:orderItemList){
            bigDecimal=BigDecimalUtils.add(bigDecimal.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }

        return bigDecimal;
    }

    /**
     * 扣库存
     * */
    private  void  reduceProductStock(List<OrderItem> orderItemList){

        if(orderItemList!=null&&orderItemList.size()>0){

            for(OrderItem orderItem:orderItemList){
                Integer productId= orderItem.getProductId();
                Integer quantity=orderItem.getQuantity();
                Product product= productMapper.selectByPrimaryKey(productId);
                product.setStock(product.getStock()-quantity);
                productMapper.updateByPrimaryKey(product);
            }

        }

    }

    /**step7：
     * 清空购物车中已选中的商品
     * */

    private  void  cleanCart(List<Cart> cartList){

        if(cartList!=null&&cartList.size()>0){
            cartMapper.batchDelete(cartList);
        }

    }


    /*step8
    * （1）建立OrderVO
    *
    * */

    private OrderItemVO assembleOrderItemVO(OrderItem orderItem){
        OrderItemVO orderItemVO=new OrderItemVO();

        if(orderItem!=null){

            orderItemVO.setQuantity(orderItem.getQuantity());
            orderItemVO.setCreateTime(DateUtils.dateToStr(orderItem.getCreateTime()));
            orderItemVO.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
            orderItemVO.setOrderNo(orderItem.getOrderNo());
            orderItemVO.setProductId(orderItem.getProductId());
            orderItemVO.setProductImage(orderItem.getProductImage());
            orderItemVO.setProductName(orderItem.getProductName());
            orderItemVO.setTotalPrice(orderItem.getTotalPrice());

        }

        return orderItemVO;
    }




    /*step8
     *
     * （2）建立OrderItemVO
     * */


    private OrderVO assembleOrderVO(Order order, List<OrderItem> orderItemList, Integer shippingId){
        OrderVO orderVO=new OrderVO();

        List<OrderItemVO> orderItemVOList=Lists.newArrayList();
        for(OrderItem orderItem:orderItemList){
            OrderItemVO orderItemVO= assembleOrderItemVO(orderItem);
            orderItemVOList.add(orderItemVO);
        }
        orderVO.setOrderItemVoList(orderItemVOList);
        orderVO.setImageHost(PropertiesUtils.readByKey("imageHost"));
        Shipping shipping= shippingMapper.selectByPrimaryKey(shippingId);
        if(shipping!=null){
            orderVO.setShippingId(shippingId);
            ShippingVO shippingVO= assmbleShippingVO(shipping);
            orderVO.setShippingVo(shippingVO);
            orderVO.setReceiverName(shipping.getReceiverName());
        }

        orderVO.setStatus(order.getStatus());
        Const.OrderStatusEnum orderStatusEnum= Const.OrderStatusEnum.codeOf(order.getStatus());
        if(orderStatusEnum!=null){
            orderVO.setStatusDesc(orderStatusEnum.getDesc());
        }

        orderVO.setPostage(0);
        orderVO.setPayment(order.getPayment());
        orderVO.setPaymentType(order.getPaymentType());
        //支付类型描述
        Const.PaymentEnum paymentEnum=Const.PaymentEnum.codeOf(order.getPaymentType());
        if(paymentEnum!=null){
            orderVO.setPaymentTypeDesc(paymentEnum.getDesc());
        }
        orderVO.setOrderNo(order.getOrderNo());



        return orderVO;
    }
    /*step8
     *
     * （3）建立ShippingItemVO
     * */
    private ShippingVO assmbleShippingVO(Shipping shipping){
        ShippingVO shippingVO=new ShippingVO();

        if(shipping!=null){
            shippingVO.setReceiverAddress(shipping.getReceiverAddress());
            shippingVO.setReceiverCity(shipping.getReceiverCity());
            shippingVO.setReceiverDistrict(shipping.getReceiverDistrict());
            shippingVO.setReceiverMobile(shipping.getReceiverMobile());
            shippingVO.setReceiverName(shipping.getReceiverName());
            shippingVO.setReceiverPhone(shipping.getReceiverPhone());
            shippingVO.setReceiverProvince(shipping.getReceiverProvince());
            shippingVO.setReceiverZip(shipping.getReceiverZip());
        }
        return shippingVO;
    }


    //================================================


    /*
    * 取消订单
    * */

    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        //step1：参数非空校验
        if(orderNo == null){
            return  ServerResponse.createServerResponseByError("参数不能为空");
        }

        //step2：根据userId和orderNo去查询 订单
         Order order = orderMapper.findOrderByUseridAndOrderNo(userId,orderNo);
        if(order==null){
            return ServerResponse.createServerResponseByError("该用户没有此订单");
        }
        //step3：判断订单状态并且取消
        if(order.getStatus()!=Const.OrderStatusEnum.ORDER_UN_PAY.getCode()){
            return ServerResponse.createServerResponseByError("订单不可取消");
        }
        //step4：返回结果
        order.setStatus(Const.OrderStatusEnum.ORDER_UN_PAY.getCode());
        int result = orderMapper.updateByPrimaryKey(order);
        if (result>0){
            return ServerResponse.createServerResponseBySucessMsg("订单取消成功");
        }

        return ServerResponse.createServerResponseByError("订单取消失败");
    }
    /*
     * 获取购物车中订单的商品明细
     * */
    @Override
    public ServerResponse get_order_cart_product(Integer userId) {
        //step1:查询购物车
        List<Cart> cartList= cartMapper.findCartListByUserIdAndChecked(userId);
        //step2:List<Cart>-->List<OrderItem>
        ServerResponse serverResponse= getCartOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return  serverResponse;
        }
        //step3:组装vo
        CartOrderItemVO cartOrderItemVO=new CartOrderItemVO();
        cartOrderItemVO.setImageHost(PropertiesUtils.readByKey("imageHost"));
        List<OrderItem> orderItemList=(List<OrderItem>) serverResponse.getData();
        List<OrderItemVO> orderItemVOList=Lists.newArrayList();
        if(orderItemList==null||orderItemList.size()==0){
            return ServerResponse.createServerResponseByError("购物车空");
        }
        for(OrderItem orderItem:orderItemList){
            orderItemVOList.add(assembleOrderItemVO(orderItem));
        }
        cartOrderItemVO.setOrderItemVOList(orderItemVOList);
        cartOrderItemVO.setTotalPrice( getOrderPrice(orderItemList));
        // cartOrderItemVO.setTotalPrice();
        //step4:返回结果
        return ServerResponse.createServerResponseBySucess(cartOrderItemVO);
    }
    /*
    * 订单list分页展示
    * */
    @Override
    public ServerResponse list(Integer userId,Integer pageNum,Integer pageSize) {


        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList=Lists.newArrayList();
        if(userId==null){
            //查询所有
            orderList=orderMapper.selectAll();
        }else{//查询当前用户
            orderList=orderMapper.findOrderByUserid(userId);
        }

        if(orderList==null||orderList.size()==0){
            return  ServerResponse.createServerResponseByError("未查询到订单信息");

        }
        List<OrderVO> orderVOList=Lists.newArrayList();
        for(Order order:orderList){
            List<OrderItem> orderItemList=   orderItemMapper.findOrderItemsByOrderno(order.getOrderNo());
            OrderVO orderVO=assembleOrderVO(order,orderItemList,order.getShippingId());
            orderVOList.add(orderVO);
        }

        PageInfo pageInfo=new PageInfo(orderVOList);
        return ServerResponse.createServerResponseBySucess(pageInfo);
    }
    /*
     * 订单详情
     * */
    @Override
    public ServerResponse detail(Long orderNo) {
        //step1:参数非空校验
        if(orderNo==null){
            return  ServerResponse.createServerResponseByError("参数不能为空");
        }
        //step2:查询订单
        Order order=orderMapper.findOrderByOrderNo(orderNo);
        if(order==null){
            return  ServerResponse.createServerResponseByError("订单不存在");
        }
        //step3:获取ordervo
        List<OrderItem> orderItemList= orderItemMapper.findOrderItemsByOrderno(order.getOrderNo());
        OrderVO orderVO= assembleOrderVO(order,orderItemList,order.getShippingId());
        //step4:返回结果

        return ServerResponse.createServerResponseBySucess(orderVO);
    }

    /*
     *关闭订单
     * */
    @Override
    public void closeOrder(String time) {
        //1.查询订单创建时间，<time的未付款的订单
        List<Order> orderList = orderMapper.findOrderByCreateTime(Const.OrderStatusEnum.ORDER_UN_PAY.getCode(),time);
        if (orderList!=null&&orderList.size()>0){
            for (Order order  : orderList){
                List<OrderItem> orderItemList = orderItemMapper.findOrderItemsByOrderno(order.getOrderNo());
                if (orderItemList!=null&&orderItemList.size()>0){
                    for (OrderItem orderItem :orderItemList){
                        Product product=  productMapper.selectByPrimaryKey(orderItem.getProductId());
                        //
                        if (product == null){
                         continue;
                        }
                        product.setStock(product.getStock()+orderItem.getQuantity());
                        productMapper.updateByPrimaryKey(product);
                    }

                }

                //关闭订单
                order.setStatus(Const.OrderStatusEnum.ORDER_CANCELED.getCode());
                order.setCloseTime(new Date());
                orderMapper.updateByPrimaryKey(order);

            }

        }
    }

    //============================================================================================
    /*////////////////////////////////////////支付相关////////////////////////////////////////////*/


    // （1）支付宝当面付2.0服务
    private static AlipayTradeService   tradeService;
    //(2)静态代码块最先被加载而且在jvm运行过程中只加载一次；
    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

    }
    // （3）简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {

            if (StringUtils.isNotEmpty(response.getSubCode())) {
                System.out.println(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            System.out.println("body:" + response.getBody());
        }
    }
    /*
    * 生成二维码的逻辑
    * */
    @Override
    public ServerResponse pay(Integer userId, Long orderNo,String path) throws IOException {
        //==================================================
        //Map<String ,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.findOrderByUseridAndOrderNo(userId,orderNo);
        if (order == null){
            return ServerResponse.createServerResponseByError("订单不存在");
        }
        //resultMap.put("orderNo", order.getOrderNo().toString());
        //====================================================

            // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
            // 需保证商户系统端不能重复，建议通过数据库sequence生成，
            String outTradeNo = orderNo.toString();

            // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
            String subject = "订单"+order.getOrderNo()+"当面付扫码消费"+order.getPayment().intValue();

            // (必填) 订单总金额，单位为元，不能超过1亿元
            // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
            String totalAmount = String.valueOf(order.getPayment().doubleValue());

            // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
            // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
            String undiscountableAmount = "0";

            // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
            // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
            String sellerId = "";

            // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
            String body = "购买商品共"+order.getPayment()+"元";

            // 商户操作员编号，添加此参数可以为商户操作员做销售统计
            String operatorId = "test_operator_id";

            // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
            String storeId = "test_store_id";

            // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
            ExtendParams extendParams = new ExtendParams();
            extendParams.setSysServiceProviderId("2088100200300400500");

            // 支付超时，定义为120分钟
            String timeoutExpress = "120m";

            // 商品明细列表，需填写购买商品详细信息，
            List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
            List<OrderItem> orderItemList = orderItemMapper.findOrderItemsByOrderno(order.getOrderNo());
            orderItemMapper.findOrderItemsByOrderno(order.getOrderNo());
            if (orderItemList !=null&&orderItemList.size()>0) {
                for (OrderItem orderItem : orderItemList) {
                    GoodsDetail goodsDetail  = GoodsDetail.newInstance(String.valueOf(orderItem.getProductId()),
                            orderItem.getProductName(),orderItem.getCurrentUnitPrice().longValue(),orderItem.getQuantity()

                    );
                    goodsDetailList.add(goodsDetail);
                }
            }


            // 创建扫码支付请求builder，设置请求参数
            AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                    .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                    .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                    .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                    .setTimeoutExpress(timeoutExpress)
                    //http://localhost:8080无法访问主机，需要进行一个内网穿透,也就是回调地址，外网能够访问
                    .setNotifyUrl("http://39.96.66.122:8080/order/alipay_callback.do")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                    .setGoodsDetailList(goodsDetailList);
            /*
            * 进行支付宝的下单功能
            * */
            AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
            switch (result.getTradeStatus()) {
                case SUCCESS:


                    AlipayTradePrecreateResponse response = result.getResponse();
                    dumpResponse(response);
                    File folder = new File(path);
                    if (!folder.exists()) {
                        folder.setWritable(true);
                        folder.mkdirs();
                    }

                    // 需要修改为运行机器上的路径
                    String filePath = String.format(path + "/qr-%s.png",
                            response.getOutTradeNo());
                    System.out.println("filePath:" + filePath);
                    //将二维码生成，生成之后写入到文件下面
                    ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                    Map map = Maps.newHashMap();
                    map.put("orderNo",order.getOrderNo());
                    map.put("qrcode",PropertiesUtils.readByKey("imageHost")+"/qr-"+response.getOutTradeNo()+".png");
                    File file = new File(filePath);
                    FTPUtil.uploadFile(Lists.newArrayList(file));

                    return ServerResponse.createServerResponseBySucess(map);
                case FAILED:
                    System.err.println("支付宝预下单失败!!!");
                    break;

                case UNKNOWN:
                    System.err.println("系统异常，预下单状态未知!!!");
                    break;

                default:
                    System.err.println("不支持的交易状态，交易返回异常!!!");
                    break;
            }
        return ServerResponse.createServerResponseByError("下单失败！");
    }

    /*
     * 支付宝回调接口实现
     * */
    @Override
    public ServerResponse alipay_callback(Map<String, String> map) {
        //step1:获取ordrNo
        Long orderNo=Long.parseLong(map.get("out_trade_no"));
        //step2:获取流水号
        String tarde_no=map.get("trade_no");
        //step3:获取支付状态
        String trade_status=map.get("trade_status");
        //step4:获取支付时间
        String payment_time=map.get("gmt_payment");
        Order order=orderMapper.findOrderByOrderNo(orderNo);
        if(order==null){
            System.out.println("bushibendingdan");
            return ServerResponse.createServerResponseByError("订单"+orderNo+"不是本商品的订单");
        }
        if (order.getStatus()>=Const.OrderStatusEnum.ORDER_PAYED.getCode()){
            //防止支付宝重复回调
            System.out.println("zhifubaochognfudiaoyong");
            return ServerResponse.createServerResponseByError("支付宝重复调用");
        }
        if (trade_status.equals("TRADE_SUCCESS")){
            // 支付成功
            // 更改订单的状态，更改支付时间
            order.setStatus(Const.OrderStatusEnum.ORDER_PAYED.getCode());
            order.setPaymentTime(DateUtils.strToDate(payment_time));
            orderMapper.updateByPrimaryKey(order);
        }
        // 保存支付信息
        PayInfo payInfo=new PayInfo();
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PaymentPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformStatus(trade_status);
        payInfo.setPlatformNumber(tarde_no);
        payInfo.setUserId(order.getUserId());

        int result= payInfoMapper.insert(payInfo);
        if(result>0){
            return ServerResponse.createServerResponseBySucessMsg("支付信息保存成功");
        }
        return ServerResponse.createServerResponseByError("支付信息保存失败");

    }
    /*
    * 查询订单状态
    * */
    @Override
    public ServerResponse query_order_pay_status(Long orderNo) {

        if(orderNo==null){
            return  ServerResponse.createServerResponseByError("订单号不能为空");
        }
        Order order= orderMapper.findOrderByOrderNo(orderNo);
        if(order==null){
            return ServerResponse.createServerResponseByError("订单不存在");
        }
        if(order.getStatus()==Const.OrderStatusEnum.ORDER_PAYED.getCode()){
            return ServerResponse.createServerResponseBySucess(true);
        }
        return ServerResponse.createServerResponseBySucess(false);
    }


}
