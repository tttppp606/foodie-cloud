package com.imooc.order.pojo.bo;

import com.imooc.pojo.ShopcartBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 将SubmitOrderBO和List<ShopcartBO>封装到一起，返回前端
 */
@Data
@AllArgsConstructor/*  全部有参构造器*/
@NoArgsConstructor/*  无参构造器*/
public class PlaceOrderBO {
    private SubmitOrderBO submitOrderBO;
    private List<ShopcartBO> shopcartBOList;
}
