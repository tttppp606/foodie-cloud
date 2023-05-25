package com.imooc.order.fallback.itemservice;

import com.imooc.item.service.ItemCommmentsService;
import org.springframework.cloud.openfeign.FeignClient;

//@FeignClient(value = "foodie-item-service", fallback = ItemCommentsFallback.class)
@FeignClient(value = "foodie-item-service", fallbackFactory = ItemCommentsFallBackFactory.class )
public interface ItemCommentsFeignClient extends ItemCommmentsService {
}
