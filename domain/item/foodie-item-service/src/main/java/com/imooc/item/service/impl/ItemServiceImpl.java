package com.imooc.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.enums.CommentLevel;
import com.imooc.enums.YesOrNo;
import com.imooc.item.mapper.*;
import com.imooc.item.pojo.*;
import com.imooc.item.pojo.vo.CommentLevelCountsVO;
import com.imooc.item.pojo.vo.ItemCommentVO;
import com.imooc.item.pojo.vo.ShopcartVO;
import com.imooc.item.service.ItemService;
import com.imooc.utils.DesensitizationUtil;
import com.imooc.pojo.PagedGridResult;
import com.imooc.utils.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
//@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemsMapper itemsMapper;
    @Autowired
    private ItemsImgMapper itemsImgMapper;
    @Autowired
    private ItemsSpecMapper itemsSpecMapper;
    @Autowired
    private ItemsParamMapper itemsParamMapper;
    @Autowired
    private ItemsCommentsMapper itemsCommentsMapper;
    @Autowired
    private ItemsMapperCustom itemsMapperCustom;

//    @Autowired
//    private RedisTemplate redisTemplate;


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Items queryItemById(String itemId) {
        return itemsMapper.selectByPrimaryKey(itemId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsImg> queryItemImgList(String itemId) {
        Example itemsImgExp = new Example(ItemsImg.class);
        Example.Criteria criteria = itemsImgExp.createCriteria();
        criteria.andEqualTo("itemId", itemId);

        return itemsImgMapper.selectByExample(itemsImgExp);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsSpec> queryItemSpecList(String itemId) {
        Example itemsSpecExp = new Example(ItemsSpec.class);
        Example.Criteria criteria = itemsSpecExp.createCriteria();
        criteria.andEqualTo("itemId", itemId);

        return itemsSpecMapper.selectByExample(itemsSpecExp);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public ItemsParam queryItemParam(String itemId) {
        Example itemsParamExp = new Example(ItemsParam.class);
        Example.Criteria criteria = itemsParamExp.createCriteria();
        criteria.andEqualTo("itemId", itemId);

        return itemsParamMapper.selectOneByExample(itemsParamExp);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public CommentLevelCountsVO queryCommentCounts(String itemId) {
        Integer goodCounts = getCommentCounts(itemId, CommentLevel.GOOD.type);
        Integer normalCounts = getCommentCounts(itemId, CommentLevel.NORMAL.type);
        Integer badCounts = getCommentCounts(itemId, CommentLevel.BAD.type);
        Integer totalCounts = goodCounts + normalCounts + badCounts;

        CommentLevelCountsVO countsVO = new CommentLevelCountsVO();
        countsVO.setTotalCounts(totalCounts);
        countsVO.setGoodCounts(goodCounts);
        countsVO.setNormalCounts(normalCounts);
        countsVO.setBadCounts(badCounts);

        return countsVO;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    Integer getCommentCounts(String itemId, Integer level) {
        ItemsComments condition = new ItemsComments();
        condition.setItemId(itemId);
        if (level != null) {
            condition.setCommentLevel(level);
        }
        return itemsCommentsMapper.selectCount(condition);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryPagedComments(String itemId,
                                                  Integer level,
                                                  Integer page,
                                                  Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("level", level);

        // mybatis-pagehelper

        /**
         * page: 第几页
         * pageSize: 每页显示条数
         */
        PageHelper.startPage(page, pageSize);

        List<ItemCommentVO> list = itemsMapperCustom.queryItemComments(map);
        for (ItemCommentVO vo : list) {
            vo.setNickname(DesensitizationUtil.commonDisplay(vo.getNickname()));
        }

        return setterPagedGrid(list, page);
    }
    private PagedGridResult setterPagedGrid(List<?> list, Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(page);
        grid.setRows(list);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());
        return grid;
    }

//    @Transactional(propagation = Propagation.SUPPORTS)
//    @Override
//    public PagedGridResult searhItems(String keywords, String sort, Integer page, Integer pageSize) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("keywords", keywords);
//        map.put("sort", sort);
//
//        PageHelper.startPage(page, pageSize);
//        List<SearchItemsVO> list = itemsMapperCustom.searchItems(map);
//
//        return setterPagedGrid(list, page);
//    }
//
//    @Transactional(propagation = Propagation.SUPPORTS)
//    @Override
//    public PagedGridResult searhItems(Integer catId, String sort, Integer page, Integer pageSize) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("catId", catId);
//        map.put("sort", sort);
//
//        PageHelper.startPage(page, pageSize);
//        List<SearchItemsVO> list = itemsMapperCustom.searchItemsByThirdCat(map);
//
//        return setterPagedGrid(list, page);
//    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ShopcartVO> queryItemsBySpecIds(String specIds) {

        String ids[] = specIds.split(",");
        List<String> specIdsList = new ArrayList<>();
        Collections.addAll(specIdsList, ids);

        return itemsMapperCustom.queryItemsBySpecIds(specIdsList);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public ItemsSpec queryItemSpecById(String specId) {
        return itemsSpecMapper.selectByPrimaryKey(specId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public String queryItemMainImgById(String itemId) {
        ItemsImg itemsImg = new ItemsImg();
        itemsImg.setItemId(itemId);
        itemsImg.setIsMain(YesOrNo.YES.type);
        ItemsImg result = itemsImgMapper.selectOne(itemsImg);
        return result != null ? result.getUrl() : "";
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void decreaseItemSpecStock(String specId, int buyCounts) {

        // synchronized 不推荐使用，集群下无用，性能低下
        // 锁数据库: 不推荐，导致数据库性能低下
        // 分布式锁 zookeeper redis

        // lockUtil.getLock(); -- 加锁

        // 1. 查询库存
//        int stock = 10;

        // 2. 判断库存，是否能够减少到0以下
//        if (stock - buyCounts < 0) {
            // 提示用户库存不够
//            10 - 3 -3 - 5 = -1
//        }

        // lockUtil.unLock(); -- 解锁
        /**
         * 一、zookeeper的curator实现分布式锁，防止多线程并发减少库存，导致超卖现象---start
         */
//        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
//        CuratorFramework client = CuratorFrameworkFactory.newClient("43.154.87.31:2181", retryPolicy);
//        client.start();
//        InterProcessMutex lock = new InterProcessMutex(client, "/foodie-cloud-order");
//
//        try {
//            if(lock.acquire(30, TimeUnit.SECONDS)){
//                int result = itemsMapperCustom.decreaseItemSpecStock(specId, buyCounts);
//                if (result != 1) {
//                    throw new RuntimeException("订单创建失败，原因：库存不足!");
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }finally {
//            try {
//                lock.release();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }


        /**
         * 二、利用原生redis实现分布式锁
         * 缺点：并发10个线程只能成功一个，剩余的全部失败，不能等待
         */
//        try (RedisLock redisLock = new RedisLock(redisTemplate,"redisKey",30)){
//            if (redisLock.getLock()) {
//                //业务逻辑
//                int result = itemsMapperCustom.decreaseItemSpecStock(specId, buyCounts);
//                if (result != 1) {
//                    throw new RuntimeException("订单创建失败，原因：库存不足!");
//                }
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        /**
         * 三、利用redis的redission客户端实现分布式锁
         *
         * 100线程 35%error
         */
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://43.154.166.55:6379")
                .setDatabase(0)
                .setPassword("1990425lc");
        RedissonClient redisson = Redisson.create(config);

        RLock lock = redisson.getLock("redission-order");
        lock.lock(30,TimeUnit.SECONDS);
        int result = itemsMapperCustom.decreaseItemSpecStock(specId, buyCounts);
        if (result != 1) {
            lock.unlock();
            throw new RuntimeException("订单创建失败，原因：库存不足!");
        }
        lock.unlock();
    }
}
