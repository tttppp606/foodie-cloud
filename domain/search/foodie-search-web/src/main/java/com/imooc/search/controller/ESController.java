package com.imooc.search.controller;

import com.imooc.search.service.ItemsESService;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 有单独的Application+端口（此模块yml文件设置端口为8033）启动此模块
 */
@Api(value = "搜索", tags = {"用于ES搜索商品的相关接口"})
@RestController
@RequestMapping("itemsES")
public class ESController {

    @Autowired
    private ItemsESService itemsESService;

    /**
     * 利用ES进行关键字，排序和分页查询
     * @param keywords
     * @param sort
     * @param page
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "搜索商品", notes = "搜索商品", httpMethod = "GET")
    @GetMapping("/es/search")/* 可以跟没有es的搜索保持一致，不冲突，因为启动的端口不一样*/
    public IMOOCJSONResult search(
            String keywords,
            String sort,
            Integer page,
            Integer pageSize) {

        if (StringUtils.isBlank(keywords)) {
            return IMOOCJSONResult.errorMsg(null);
        }

        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 20;
        }

        //es支持的分页是从第0页开始，前端是从第1页，所以在进入service前--，当回到contoller前再+1
        page --;

        PagedGridResult grid = itemsESService.searhItems(keywords,
                sort,
                page,
                pageSize);

        return IMOOCJSONResult.ok(grid);
    }
}
