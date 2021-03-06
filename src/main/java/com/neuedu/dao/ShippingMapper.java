package com.neuedu.dao;

import com.neuedu.pojo.Shipping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface ShippingMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table neuedu_shopping
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table neuedu_shopping
     *
     * @mbg.generated
     */
    int insert(Shipping record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table neuedu_shopping
     *
     * @mbg.generated
     */
    Shipping selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table neuedu_shopping
     *
     * @mbg.generated
     */
    List<Shipping> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table neuedu_shopping
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Shipping record);

    int updateBySelectiveKey(Shipping shipping);
    /**
     *
     * 删除地址
     * @param shippingId
     * @Param userId
     *
     * */
    int  deleteByUserIdAndShippingId(@Param("userId") Integer userId,@Param("shippingId") Integer shippingId);
}