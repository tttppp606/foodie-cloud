package com.imooc.item.test;


import com.imooc.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用jemter测试zk分布式锁
 */
@RestController
public class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @PostMapping("zk-lock")
    public void decreaseItemSpecStock(@RequestParam String specId,
                                        @RequestParam int buyCounts){
        itemService.decreaseItemSpecStock(specId,buyCounts);
    }
}
