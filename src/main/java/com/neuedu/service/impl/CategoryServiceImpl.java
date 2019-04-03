package com.neuedu.service.impl;

import com.google.common.collect.Sets;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.CategoryMapper;
import com.neuedu.pojo.Category;
import com.neuedu.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service(value = "iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public ServerResponse get_category(Integer categoryId) {
        //step1:非空校验
        if (categoryId==null){
           return ServerResponse.createServerResponseByError("参数不能为空");
        }
        //step2:根据categoryId查询类别
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category==null){
            return ServerResponse.createServerResponseByError("查询类别不存在");
        }
        //step3:查询子类别
         List<Category> categoryList  = categoryMapper.findChildCategory(categoryId);
        //step4:返回解雇
        return  ServerResponse.createServerResponseBySucess(categoryList);

    }

    @Override
    public ServerResponse add_category(Integer parentId, String categoryName) {
        //step1:参数校验
        if(categoryName==null||categoryName.equals("")){
            return ServerResponse.createServerResponseByError("类别不能为空");
        }
        //step2:添加节点
        Category category  = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(1);
        int result = categoryMapper.insert(category);
        //step3:返回结果
        if(result>0){
            //添加成功
          return ServerResponse.createServerResponseBySucessMsg("添加成功");
        }

        return ServerResponse.createServerResponseByError("添加失败");
    }

    @Override
    public ServerResponse set_category_name(Integer categoryId, String categoryName) {
        //step1:参数非空校验
        if(categoryId==null||categoryId.equals("")){
            return ServerResponse.createServerResponseByError("类别ID不能为空");
        }
        if(categoryName==null||categoryName.equals("")){
            return ServerResponse.createServerResponseByError("类别名称不能为空");
        }

        //step2:根据categoryid进行查询
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category==null){
            return ServerResponse.createServerResponseByError("要修改的类别不存在");
        }
        //step3：修改
        category.setName(categoryName);
        int result = categoryMapper.updateByPrimaryKey(category);
        if(result>0){
            //添加成功
            return ServerResponse.createServerResponseBySucessMsg("修改成功");
        }

        return ServerResponse.createServerResponseByError("修改失败");
    }
    /*
     * 获取当前分类id以及递归查询子节点catgoryid
     * */
    @Override
    public ServerResponse get_deep_category(Integer categoryId) {
        //step1:参数非空校验
        if (categoryId==null){
            return ServerResponse.createServerResponseByError("类别id不能为空");
        }
        //step2:查询
        Set<Category> categorySet = Sets.newHashSet();
        categorySet = findAllChildCategory(categorySet,categoryId);


        Set<Integer> integerSet = Sets.newHashSet();

        Iterator<Category> categoryIterator = categorySet.iterator();
        while (categoryIterator.hasNext()){
            Category category = categoryIterator.next();
            integerSet.add(category.getId());
        }



        return ServerResponse.createServerResponseBySucess(integerSet);
    }
    /*
    * 利用递归查询，将查询结果放入set集合中
    * */
    private Set<Category> findAllChildCategory(Set<Category> categorySet, Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null){
            categorySet.add(category);//通过类别id判断，两个类别id一样的，就认为一类
        }
        //查找categoryId下的子节点(平级)
        List<Category> categorieList = categoryMapper.findChildCategory(categoryId);
        if (categorieList!=null&&categorieList.size()>0){
            for(Category category1:categorieList){
                findAllChildCategory(categorySet,category1.getId());
            }
        }


        return categorySet;
    }


}
