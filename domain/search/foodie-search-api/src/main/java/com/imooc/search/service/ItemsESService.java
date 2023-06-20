package com.imooc.search.service;

import com.imooc.pojo.PagedGridResult;

/**
 * 利用ES进行关键字，排序和分页查询
 */
public interface ItemsESService {

    public PagedGridResult searhItems(String keywords,
                                      String sort,
                                      Integer page,
                                      Integer pageSize);

}
