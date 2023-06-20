package com.imooc.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

// 返回前端的搜索结果，以下字段在数据库中不在一个表中，如果不用es，需要连表查询，效率很慢
@Data
@Document(indexName = "foodie-items-ik", type = "doc"/* es-head查看索引状态中mappings下为doc*/,createIndex = false)
public class Items {
    /**
     * @Id:将itemId字段作为ES的内置ID，两者保持一样
     * index = false：不作为检索字段
     */
    @Id
    @Field(store = true, type = FieldType.Text, index = false)
    private String itemId;//items表

    @Field(store = true, type = FieldType.Text, index = true)
    private String itemName;//items表

    @Field(store = true, type = FieldType.Text, index = false)
    private String imgUrl;//items_img表

    @Field(store = true, type = FieldType.Integer)
    private Integer price;//items_spec表

    @Field(store = true, type = FieldType.Integer)
    private Integer sellCounts;//items表
}
