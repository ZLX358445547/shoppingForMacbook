package com.neuedu.service.impl;

import com.alipay.api.domain.Keyword;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.CategoryMapper;
import com.neuedu.dao.ProductMapper;
import com.neuedu.pojo.Category;
import com.neuedu.pojo.Product;
import com.neuedu.service.ICategoryService;
import com.neuedu.service.IProductService;
import com.neuedu.utils.FTPUtil;
import com.neuedu.utils.PropertiesUtils;
import com.neuedu.vo.ProductDetailVO;
import com.neuedu.vo.ProductListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Service(value = "iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    ProductMapper productMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    ICategoryService categoryService;

/*
* 新增或者更新产品
* */
    @Override
    public ServerResponse saveOrUpdate(Product product) {

        //step1：参数非空交验
            if(product==null){
                return ServerResponse.createServerResponseByError("参数为空");
        }
        //step2：设置商品的主图  sub_images-->1.jpg  2.jpg  3.png
            String subImages = product.getSubImages();
            if (subImages!=null&&!subImages.equals("")){
                String[]  subIamgeArr = subImages.split(",");
                if (subIamgeArr.length>0){
                    //设置商品的主图
                    product.setMainImage(subIamgeArr[0]);
                }
            }

        //step3：商品的添加或者更新
        if (product.getId()==null){
            //添加
           int result =  productMapper.insert(product);
            if (result>0){
                return  ServerResponse.createServerResponseBySucessMsg("更新成功");
            }else {
                return ServerResponse.createServerResponseByError("更新失败");
            }
        }else{
            //更新

        }
        //step4：返回结果
        return null;
    }
    /*
    * 更新商品状态
    * */
    @Override
    public ServerResponse set_sale_status(Integer productId, Integer status) {
        //step1：参数非空交验
        if(productId==null){
            return ServerResponse.createServerResponseByError("商品ID不能为空");
        }
        if(status==null){
            return ServerResponse.createServerResponseByError("商品状态参数不能为空");
        }
        //step2：更新商品的状态
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int result = productMapper.updateProductKeySelective(product);
        if (result>0){
            return ServerResponse.createServerResponseBySucessMsg("更新成功");
        }

        return ServerResponse.createServerResponseByError("更新失败");
    }
/*
* 查询商品详情
*
* */
    @Override
    public ServerResponse detail(Integer productId) {
        //step1:参数校验
        if(productId==null){
            return ServerResponse.createServerResponseByError("商品ID不能为空");
        }

        //step2:查询product
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createServerResponseByError("商品不存在");
        }
        //step3:product--->productDetailVO
        ProductDetailVO productDetailVO = assembleProductDetailVO(product);
        //step4:返回结果

        return ServerResponse.createServerResponseBySucess(productDetailVO);
    }

/*
    private ProductDetailVO assemableProductDetailVO(Product product){
        ProductDetailVO  productDetailVO = new ProductDetailVO();
        productDetailVO.setCategoryId(product.getCategoryId());
        //时间需要转成字符串类型    需要定义一个工具类

        return productDetailVO;
    }*/



    private ProductDetailVO assembleProductDetailVO(Product product){
        ProductDetailVO productDetailVo = new ProductDetailVO();
        productDetailVo.setId(product.getId());
        productDetailVo.setName(product.getName());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setSubtitle(product.getSubtitle());
        //todo propertiesUtils
        //setImageHost是图片服务器，需要配置db.properties配置文件才能读取图片信息
        productDetailVo.setImageHost(PropertiesUtils.readByKey("imageHost"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);//默认根节点
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        productDetailVo.setCreateTime(product.getCreateTime().toString());
        productDetailVo.setUpdateTime(product.getUpdateTime().toString());
        return productDetailVo;
    }




    /*
    * 分页逻辑
    * */
    @Override
    public ServerResponse list(Integer pageNum, Integer pagesize) {
        PageHelper.startPage(pageNum,pagesize);
        //step1:查询商品数据  SELECT * FROM prduct limit(pageNum-1)*pageSize,pageSize
        List<Product> productList = productMapper.selectAll();
        List<ProductListVo> productListVoList = Lists.newArrayList();
        if(productList!=null&&productList.size()>0){
            for (Product product :productList){
                ProductListVo productListVo = assembleProductListVo(product);
                productListVoList.add(productListVo);

            }
        }


        PageInfo pageInfo  = new PageInfo(productListVoList);

        return ServerResponse.createServerResponseBySucess(pageInfo);
    }


    /*
    * 分页逻辑的私有防方法
    * */
    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtils.readByKey("imageHost"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }
    /*
    * 商品搜索接口实现
    * */
    @Override
    public ServerResponse search(Integer productId, String productName, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);


        if (productName!=null&&!productName.equals("")) {
            productName = "%" + productName + "%";
        }

         List<Product> productList = productMapper.findProductByProductIdOrProductName(productId,productName);
         List<ProductListVo> productListVoList = Lists.newArrayList();

            if(productList!=null&&productList.size()>0){
                for (Product product :productList){
                    ProductListVo productListVo = assembleProductListVo(product);
                    productListVoList.add(productListVo);

                }
            }

        PageInfo pageInfo = new PageInfo(productListVoList);

        return ServerResponse.createServerResponseBySucess(pageInfo);

    }
    /*
    * 图片上传
    * */
    @Override
    public ServerResponse upload(MultipartFile file, String path) {
        if(file == null){
            return ServerResponse.createServerResponseByError("文件为空，请重新上传");
        }
        //step1,获取图片的名字
        String orignalFileName = file.getOriginalFilename();
        //获取图片的扩展名字
        String exName = orignalFileName.substring(orignalFileName.lastIndexOf("."));//.jpg
        //为图片生成唯一的名字
        String newFileName = UUID.randomUUID().toString()+exName;


        File pathFile = new File(path);
        if(!pathFile.exists()){
            pathFile.setWritable(true);
            pathFile.mkdirs();
        }
        File file1 = new File(path,newFileName);
         try {
             file.transferTo(file1);
             //上传到图片服务器
             //。。。。。。
             FTPUtil.uploadFile(Lists.newArrayList(file1));
             //图片上传成功之后返回的逻辑
             Map<String,String> map = Maps.newHashMap();
             map.put("uri",newFileName);
             map.put("url",PropertiesUtils.readByKey("imageHost")+newFileName);
             return ServerResponse.createServerResponseBySucess(map);
         } catch (IOException e) {
             e.printStackTrace();
         }


        return null;
    }
    /*
    * 前台商品接口---商品详情
    * */
    @Override
    public ServerResponse detial_portal(Integer productId) {
        //step1：参数校验
        if (productId==null){
            return ServerResponse.createServerResponseByError("商品id不能为空");
        }
        //step2：查询product
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product ==null){
            return ServerResponse.createServerResponseByError("商品不存在");
        }

        //step3：校验商品状态
        if (product.getStatus()!= Const.ProductStatusEnum.PRODUCT_ONLINE.getCode()){
            return ServerResponse.createServerResponseByError("商品已经下架或者删除");
        }

        //step4：获取productDetialVO
        ProductDetailVO  productDetailVO = assembleProductDetailVO(product);

        //step5：
        return ServerResponse.createServerResponseBySucess(productDetailVO);

    }

    /**
     * 前台商品搜索
     * @param  categoryId
     * @param keyword
     * @param  pageNum
     * @param  pageSize
     * @param  orderBy 排序字段
     * */
    @Override
    public ServerResponse list_portal(Integer categoryId, String keyword, Integer pageNum, Integer pageSize, String orderBy) {
        //step1:参数校验， categoryId和keyword不能同时为空
        if(categoryId==null&&keyword==null||"".equals(keyword)){
            return ServerResponse.createServerResponseByError("参数错误");
        }
        //step2:categoryId
        Set<Integer>  integerSet = Sets.newHashSet();
        if(categoryId!=null){
            Category  category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && keyword.equals("")){
                //说明没有商品数据
                //PageHelper三步骤：第一步：PageHelper.startPage
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo  = new PageInfo(productListVoList);
                return ServerResponse.createServerResponseBySucess(pageInfo);
            }

            ServerResponse serverResponse = categoryService.get_deep_category(categoryId);

              if (serverResponse.isSuccess()){
                  integerSet = (Set<Integer>) serverResponse.getData();

              }
        }

        //step3: keyword
        if(keyword!=null&&!keyword.equals("")){
            keyword="%"+keyword+"%";
        }

        if(orderBy.equals("")){
            PageHelper.startPage(pageNum,pageSize);
        }else{
            String[] orderByArr=   orderBy.split("_");
            if(orderByArr.length>1){
                PageHelper.startPage(pageNum,pageSize,orderByArr[0]+" "+orderByArr[1]);
            }else{
                PageHelper.startPage(pageNum,pageSize);
            }
        }
        //step4: List<Product>-->List<ProductListVO>
        //PageHelper三步骤：第二步：查询，执行sql语句
        List<Product> productList=productMapper.searchProduct(integerSet,keyword);
        List<ProductListVo> productListVOList=Lists.newArrayList();
        if(productList!=null&&productList.size()>0){
            for(Product product:productList){
                ProductListVo productListVO=  assembleProductListVo(product);
                productListVOList.add(productListVO);
            }
        }
        //step5:分页
        //PageHelper三步骤：第三步new PageInfo(productList);，传入需要分页的对象
        PageInfo pageInfo=new PageInfo(productList);
        pageInfo.setList(productListVOList);
        //step6:返回
        return ServerResponse.createServerResponseBySucess(pageInfo);
    }
}
