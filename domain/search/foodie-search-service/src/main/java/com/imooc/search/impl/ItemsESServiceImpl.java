package com.imooc.search.impl;

import com.imooc.search.pojo.Items;
import com.imooc.search.service.ItemsESService;
import com.imooc.pojo.PagedGridResult;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemsESServiceImpl implements ItemsESService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Override
    public PagedGridResult searhItems(String keywords, String sort, Integer page, Integer pageSize) {

        //排序
        SortBuilder sortBuilder = null;
        if (sort.equals("c")) {
            sortBuilder = new FieldSortBuilder("sellCounts")
                    .order(SortOrder.DESC);
        } else if (sort.equals("p")) {
            sortBuilder = new FieldSortBuilder("price")
                    .order(SortOrder.ASC);
        } else {
            sortBuilder = new FieldSortBuilder("itemName.keyword")
                    .order(SortOrder.ASC);
        }

        //分页
        Pageable pageable = PageRequest.of(page, pageSize);

        //高亮的前后缀
        String preTag = "<font color='red'>";
        String postTag = "</font>";

        //itemName字段搜索时高亮
        String itemNameFiled = "itemName";
        SearchResultMapper searchResultMapper = new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                List<Items> itemsListHighlight = new ArrayList<>();

                for (SearchHit hit : response.getHits()) {
                    //根据断点数据得来
                    HighlightField description = hit.getHighlightFields().get(itemNameFiled);
                    String itemName = description.getFragments()[0].toString();

                    String itemId = (String) hit.getSourceAsMap().get("itemId");
                    String imgUrl = (String)hit.getSourceAsMap().get("imgUrl");
                    Integer price = (Integer)hit.getSourceAsMap().get("price");
                    Integer sellCounts = (Integer)hit.getSourceAsMap().get("sellCounts");

                    Items itemsHL = new Items();

                    itemsHL.setItemId(itemId);
                    itemsHL.setItemName(itemName);
                    itemsHL.setImgUrl(imgUrl);
                    itemsHL.setPrice(price);
                    itemsHL.setSellCounts(sellCounts);

                    itemsListHighlight.add(itemsHL);
                }

                return new AggregatedPageImpl<>((List<T>)itemsListHighlight,
                            pageable,
                            response.getHits().totalHits);
            }
        };

        //SearchQuery包含了搜索的各种条件
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                /**   实现分页  */
                .withQuery(QueryBuilders.matchQuery(itemNameFiled, keywords))
                /**   实现高亮  */
                .withHighlightFields(new HighlightBuilder.Field(itemNameFiled)
                        .preTags(preTag)
                        .postTags(postTag))
                /**   实现排序  */
                .withSort(sortBuilder)
                .withPageable(pageable)
                .build();

        /**
         * ES实现条件搜索的核心方法
         * 1、searchQuery为搜索条件
         * 2、Items.class为搜索对象
         * 3、searchResultMapper为高亮和分页的配置
         */
        AggregatedPage<Items> pagedItems = esTemplate.queryForPage(searchQuery, Items.class, searchResultMapper);

        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(pagedItems.getContent());//内容
        gridResult.setPage(page + 1);//当前页面，由于controller里--1，这里就+1
        //searchResultMapper的实现方法中必须带有分页的信息，否则利用searchResultMapper构建的pagedItems获取不到TotalPages
        gridResult.setTotal(pagedItems.getTotalPages());//总页数
        gridResult.setRecords(pagedItems.getTotalElements());//总记录数

        return gridResult;

    }
}
