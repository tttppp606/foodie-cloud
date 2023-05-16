package com.imooc.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.item.mapper.ItemsCommentsMapperCustom;
import com.imooc.item.pojo.vo.MyCommentVO;
import com.imooc.item.service.ItemCommmentsService;
import com.imooc.pojo.PagedGridResult;
import com.imooc.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class ItemCommentsServiceImpl extends BaseService implements ItemCommmentsService {

    @Autowired
    private ItemsCommentsMapperCustom itemsCommentsMapperCustom;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        PageHelper.startPage(page, pageSize);
        List<MyCommentVO> list = itemsCommentsMapperCustom.queryMyComments(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public void saveComments(Map<String, Object> map) {
        itemsCommentsMapperCustom.saveComments(map);
    }
}
