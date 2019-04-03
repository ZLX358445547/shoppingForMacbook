package com.neuedu.service.impl;

import com.google.common.collect.Lists;
import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.CartMapper;
import com.neuedu.dao.ProductMapper;
import com.neuedu.pojo.Cart;
import com.neuedu.pojo.Product;
import com.neuedu.service.ICartService;
import com.neuedu.utils.BigDecimalUtils;
import com.neuedu.vo.CartProductVO;
import com.neuedu.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service(value = "iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    CartMapper cartMapper;
    @Autowired
    ProductMapper productMapper;

/*
* 购物车中添加商品
* */
    @Override
    public ServerResponse add(Integer userId,Integer productId, Integer count) {
        //step1:参数非空校验
        if (productId==null||count==null){
            return ServerResponse.createServerResponseByError("参数不能为空");
        }
        //通过商品id查找出来相关信息
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product==null){
            return ServerResponse.createServerResponseByError("要添加的商品不存在");
        }
        //step2：根据productId 和userId查询购物信息
        Cart cart= cartMapper.selectCartByUseridAndProductId(userId,productId);
         if (cart == null){
             //执行添加操作
             Cart cart1 = new Cart();
             cart1.setUserId(userId);
             cart1.setProductId(productId);
             cart1.setQuantity(count);
             cart1.setChecked(Const.CartCheckedEnum.PRODUCT_CHECKED.getCode());
             cartMapper.insert(cart1);
         }else {
             //执行更新操作
             //更新
             Cart cart1=new Cart();
             cart1.setId(cart.getId());
             cart1.setProductId(productId);
             cart1.setUserId(userId);
             cart1.setQuantity(cart.getQuantity()+count);
             cart1.setChecked(cart.getChecked());
             cartMapper.updateByPrimaryKey(cart1);
         }
         CartVO  cartVO = getCartVOLimit(userId);
         return ServerResponse.createServerResponseBySucess(cartVO);

    }



    /*
    * 获取cartVO的方法
    * */
    private CartVO getCartVOLimit(Integer userId){
        CartVO cartVO=new CartVO();
        //step1: 根据userId查询购物信息--》List<Cart>
        List<Cart> cartList=  cartMapper.selectCartByUserid(userId);

        //step2:List<Cart> -->List<CartProductVO>
        List<CartProductVO> cartProductVOList= Lists.newArrayList();

        //购物车总价格，初始值为0
        BigDecimal carttotalprice=new BigDecimal("0");

        if(cartList!=null&&cartList.size()>0){
            for(Cart cart:cartList){
                CartProductVO cartProductVO=new CartProductVO();
                cartProductVO.setId(cart.getId());
                cartProductVO.setQuantity(cart.getQuantity());
                cartProductVO.setUserId(userId);
                cartProductVO.setProductChecked(cart.getChecked());
                //查询商品
                Product product=  productMapper.selectByPrimaryKey(cart.getProductId());
                if(product!=null){
                    cartProductVO.setProductId(cart.getProductId());
                    cartProductVO.setProductMainImage(product.getMainImage());
                    cartProductVO.setProductName(product.getName());
                    cartProductVO.setProductPrice(product.getPrice());
                    cartProductVO.setProductStatus(product.getStatus());
                    cartProductVO.setProductStock(product.getStock());
                    cartProductVO.setProductSubtitle(product.getSubtitle());
                    //商品库存的逻辑
                    //拿到库存的数量
                    int  stock=product.getStock();
                    //定义一个受限制的数量
                    int limitProductCount=0;
                    //库存大于等购物车的商品数量时
                    if(stock>=cart.getQuantity()){
                        //将购买商品数量赋给限制数量，即可以购买
                        limitProductCount=cart.getQuantity();
                        cartProductVO.setLimitQuantity("LIMIT_NUM_SUCCESS");
                    }else{
                        //商品库存不足，购买的最大数量就是库存的数量
                        limitProductCount=stock;
                        //更新购物车中商品的数量，按照id去更新
                        Cart cart1=new Cart();
                        cart1.setId(cart.getId());
                        cart1.setQuantity(stock);
                        cart1.setProductId(cart.getProductId());
                        cart1.setChecked(cart.getChecked());
                        cart1.setUserId(userId);
                        cartMapper.updateByPrimaryKey(cart1);
                        cartProductVO.setLimitQuantity("LIMIT_NUM_FAIL");
                    }
                    //更新之后，显示的可购买的数量
                    cartProductVO.setQuantity(limitProductCount);
                    //一件商品总价格
                    cartProductVO.setProductTotalPrice(BigDecimalUtils.mul(product.getPrice().doubleValue(),Double.valueOf(cartProductVO.getQuantity())));
                }
                //被选中的商品，计算总价
                if(cartProductVO.getProductChecked()==Const.CartCheckedEnum.PRODUCT_CHECKED.getCode() ){
                    carttotalprice= BigDecimalUtils.add(carttotalprice.doubleValue(),cartProductVO.getProductTotalPrice().doubleValue());
                }
                cartProductVOList.add(cartProductVO);
            }
        }

        cartVO.setCartProductVOList(cartProductVOList);
        //step3: 计算购物车商品总价
        cartVO.setCarttotalprice(carttotalprice);

        //step4:判断购物车是否全选
        int count=cartMapper.isCheckedAll(userId);
        //count>0表示有商品处于未选中状态
        if(count>0){
            cartVO.setIsallchecked(false);
        }else {
            cartVO.setIsallchecked(true);
        }

        //step5:返回结果
        return cartVO;
    }
    //查看购物车商品列表
    @Override
    public ServerResponse list(Integer userId) {
       CartVO cartVO = getCartVOLimit(userId);
       return ServerResponse.createServerResponseBySucess(cartVO);
    }

    //更新购物车中某个商品的数量
    @Override
    public ServerResponse update(Integer userId, Integer productId, Integer count) {

        //step1:参数判定
        if(productId==null||count==null){
            return ServerResponse.createServerResponseByError("参数不能为空");
        }

        //step2:查询购物车中商品
        Cart cart= cartMapper.selectCartByUseridAndProductId(userId,productId);
        if(cart!=null){
            //step3:更新数量
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }

        //step4：返回cartVO
        return ServerResponse.createServerResponseBySucess(getCartVOLimit(userId));
    }

    //删除购物车中某个商品
    @Override
    public ServerResponse delete_Product(Integer userId, String productIds) {

        //step1：参数非空校验
        if (productIds==null || productIds.equals("")){
            return ServerResponse.createServerResponseByError("参数不能为空");
        }
        //step2：productIds---》List<Integer>
        List<Integer> productIdList = Lists.newArrayList();

        String[] productIdsArr = productIds.split(",");
        if (productIdsArr!=null &&productIdsArr.length>0){
            for (String productIdStr: productIdsArr){
                Integer productId = Integer.parseInt(productIdStr);
                productIdList.add(productId);
            }
        }
        //step3：调用dao层
        cartMapper.deleteByUseridAndProductIds(userId,productIdList);
        //step4：返回结果

        return ServerResponse.createServerResponseBySucess(getCartVOLimit(userId));
    }
    /*
    * 选择商品
    * */
    @Override
    public ServerResponse select(Integer userId, Integer productId,Integer check) {
        //step1:参数非空校验
      /*  if (productId==null || productId.equals("")){
            return ServerResponse.createServerResponseByError("参数不能为空");
        }*/
        //step2:dao接口
        cartMapper.selectOrUnselectProduct(userId,productId,check);
        //step3:返回结果
        return ServerResponse.createServerResponseBySucess(getCartVOLimit(userId));

    }
    /*
    * 统计购物车中商品个数
    * */
    @Override
    public ServerResponse get_cart_product_count(Integer userId) {

        int quantity = cartMapper.get_cart_product_count(userId);
        return ServerResponse.createServerResponseBySucess(quantity);
    }


}
