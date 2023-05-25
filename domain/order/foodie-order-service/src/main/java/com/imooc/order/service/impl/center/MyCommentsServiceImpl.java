package com.imooc.order.service.impl.center;

import com.imooc.enums.YesOrNo;
import com.imooc.order.fallback.itemservice.ItemCommentsFeignClient;
import com.imooc.order.mapper.OrderItemsMapper;
import com.imooc.order.mapper.OrderStatusMapper;
import com.imooc.order.mapper.OrdersMapper;
import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.order.service.center.MyCommentsService;
import com.imooc.service.BaseService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MyCommentsServiceImpl extends BaseService implements MyCommentsService {

    @Autowired
    public OrderItemsMapper orderItemsMapper;

    @Autowired
    public OrdersMapper ordersMapper;

    @Autowired
    public OrderStatusMapper orderStatusMapper;

    //引用里其他微服务的mapper，不能再pom中添加其他微服务的mapper，再@Autowired
    //需要用eureka进行远程调用
    //  fegin章节再改
//    @Autowired
//    public LoadBalancerClient loadBalancerClient;
//
//    @Autowired
//    public RestTemplate restTemplate;

    /* feign调用 */
    @Autowired
    //private ItemCommmentsService itemCommentsService;
    private ItemCommentsFeignClient itemCommentsService;


    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<OrderItems> queryPendingComment(String orderId) {
        OrderItems query = new OrderItems();
        query.setOrderId(orderId);
        return orderItemsMapper.select(query);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComments(String orderId, String userId,
                             List<OrderItemsCommentBO> commentList) {

        // 1. 保存评价 items_comments
        for (OrderItemsCommentBO oic : commentList) {
            oic.setCommentId(sid.nextShort());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("commentList", commentList);

//        ServiceInstance instance = loadBalancerClient.choose("foodie-item-service");
//        /**
//         * 服务消费者通过eureka的注册中心（LoadBalancerClient）
//         * 获取到类服务客户端的ip和端口，进行通讯
//         */
//        String url = String.format("http://%s:%s/item-comments-api/saveComments",
//                instance.getHost(),
//                instance.getPort());
//        restTemplate.postForLocation(url,map);

        itemCommentsService.saveComments(map);


        // 2. 修改订单表改已评价 orders
        Orders order = new Orders();
        order.setId(orderId);
        order.setIsComment(YesOrNo.YES.type);
        ordersMapper.updateByPrimaryKeySelective(order);

        // 3. 修改订单状态表的留言时间 order_status
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCommentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
    }
}
