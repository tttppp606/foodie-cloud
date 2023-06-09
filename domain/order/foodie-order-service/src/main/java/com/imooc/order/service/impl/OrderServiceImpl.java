package com.imooc.order.service.impl;

import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.YesOrNo;
import com.imooc.item.pojo.Items;
import com.imooc.item.pojo.ItemsSpec;
import com.imooc.item.service.ItemService;
import com.imooc.order.mapper.OrderItemsMapper;
import com.imooc.order.mapper.OrderStatusMapper;
import com.imooc.order.mapper.OrdersMapper;
import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.bo.PlaceOrderBO;
import com.imooc.order.pojo.bo.SubmitOrderBO;
import com.imooc.pojo.DecreaseStock;
import com.imooc.pojo.ShopcartBO;
import com.imooc.order.pojo.vo.MerchantOrdersVO;
import com.imooc.order.pojo.vo.OrderVO;
import com.imooc.user.pojo.UserAddress;
import com.imooc.order.service.OrderService;
import com.imooc.user.service.AddressService;
import com.imooc.utils.DateUtil;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RabbitSender;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private AddressService addressService;
    @Autowired
    private ItemService itemService;

//    @Autowired
//    private LoadBalancerClient loadBalancerClient;
//
//    @Autowired
//    private RestTemplate restTemplate;

      @Autowired
      private RabbitSender rabbitSender;

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(PlaceOrderBO placeOrderBO) {
        SubmitOrderBO submitOrderBO = placeOrderBO.getSubmitOrderBO();
        List<ShopcartBO> shopcartList = placeOrderBO.getShopcartBOList();

        String userId = submitOrderBO.getUserId();
        String addressId = submitOrderBO.getAddressId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        Integer payMethod = submitOrderBO.getPayMethod();
        String leftMsg = submitOrderBO.getLeftMsg();
        // 包邮费用设置为0
        Integer postAmount = 0;

        //分布式全局唯一ID
        String orderId = sid.nextShort();

        //fegin调用
        UserAddress address = addressService.queryUserAddres(userId, addressId);
//        ServiceInstance instance = loadBalancerClient.choose("foodie-user-service");
//        String url = String.format("http://%s:%s/address-api/queryAddress"+
//                        "?userId=%s&addressId=%s",
//                instance.getHost(),
//                instance.getPort(),
//                userId,addressId);
//        UserAddress address = restTemplate.getForObject(url,UserAddress.class);

        // 1. 新订单数据保存
        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);

        newOrder.setReceiverName(address.getReceiver());
        newOrder.setReceiverMobile(address.getMobile());
        newOrder.setReceiverAddress(address.getProvince() + " "
                                    + address.getCity() + " "
                                    + address.getDistrict() + " "
                                    + address.getDetail());

//        newOrder.setTotalAmount();
//        newOrder.setRealPayAmount();
        newOrder.setPostAmount(postAmount);

        newOrder.setPayMethod(payMethod);
        newOrder.setLeftMsg(leftMsg);

        newOrder.setIsComment(YesOrNo.NO.type);
        newOrder.setIsDelete(YesOrNo.NO.type);
        newOrder.setCreatedTime(new Date());
        newOrder.setUpdatedTime(new Date());


        // 2. 循环根据itemSpecIds保存订单商品信息表
        String itemSpecIdArr[] = itemSpecIds.split(",");
        Integer totalAmount = 0;    // 商品原价累计
        Integer realPayAmount = 0;  // 优惠后的实际支付价格累计
        List<ShopcartBO> toBeRemovedShopcatdList = new ArrayList<>();
        for (String itemSpecId : itemSpecIdArr) {
            ShopcartBO cartItem = getBuyCountsFromShopcart(shopcartList, itemSpecId);
            // 整合redis后，商品购买的数量重新从redis的购物车中获取
            int buyCounts = cartItem.getBuyCounts();
            toBeRemovedShopcatdList.add(cartItem);

            // 2.1 根据规格id，查询规格的具体信息，主要获取价格
            //等学习fegin再优化
            ItemsSpec itemSpec = itemService.queryItemSpecById(itemSpecId);
//            ServiceInstance instanceItem = loadBalancerClient.choose("foodie-item-service");
//            url = String.format("http://%s:%s/item-api/singleItemSpec"+
//                            "?specId=%s",
//                    instanceItem.getHost(),
//                    instanceItem.getPort(),
//                    itemSpecId);
//            ItemsSpec itemSpec = restTemplate.getForObject(url,ItemsSpec.class);

            totalAmount += itemSpec.getPriceNormal() * buyCounts;
            realPayAmount += itemSpec.getPriceDiscount() * buyCounts;

            // 2.2 根据商品id，获得商品信息以及商品图片
            String itemId = itemSpec.getItemId();
            // 等学习fegin再优化
            Items item = itemService.queryItemById(itemId);
//            url = String.format("http://%s:%s/item-api/item"+
//                            "?itemId=%s",
//                    instanceItem.getHost(),
//                    instanceItem.getPort(),
//                    itemSpecId);
//            Items item = restTemplate.getForObject(url,Items.class);

            // 等学习fegin再优化
            String imgUrl = itemService.queryItemMainImgById(itemId);
//            url = String.format("http://%s:%s/item-api/primaryImage"+
//                            "?itemId=%s",
//                    instanceItem.getHost(),
//                    instanceItem.getPort(),
//                    itemId);
//            String imgUrl = restTemplate.getForObject(url,String.class);

            // 2.3 循环保存子订单数据到数据库
            String subOrderId = sid.nextShort();
            OrderItems subOrderItem = new OrderItems();
            subOrderItem.setId(subOrderId);
            subOrderItem.setOrderId(orderId);
            subOrderItem.setItemId(itemId);
            subOrderItem.setItemName(item.getItemName());
            subOrderItem.setItemImg(imgUrl);
            subOrderItem.setBuyCounts(buyCounts);
            subOrderItem.setItemSpecId(itemSpecId);
            subOrderItem.setItemSpecName(itemSpec.getName());
            subOrderItem.setPrice(itemSpec.getPriceDiscount());
            orderItemsMapper.insert(subOrderItem);

            // 2.4 在用户提交订单以后，规格表中需要扣除库存
            // 等学习fegin再优化
            /**
             * 防止超卖两种解决方案
             * 1、redis/zk的分布式锁：在减库存那里加锁，防止超卖,无法消峰
             * itemService.decreaseItemSpecStock(itemSpecId, buyCounts);
             * 2、mq的消息队列：
             *    原理：订单中心的减库存的信息放入mq中，商品中心的减库存服务消费
             *    优点：1、实现order和item两个微服务的服务解耦；
             *         2、秒杀场景高并发用于消峰；
             *        （3、当库存可以后续补上时，没有库存也可以继续让客户进行下单后面的操作；）
             */

            this.decreaseItemSpecStock(itemSpecId, buyCounts);//消息队列

            //itemService.decreaseItemSpecStock(itemSpecId, buyCounts);//分布式锁


//            url = String.format("http://%s:%s/item-api/decreaseStock"+
//                            "?specId=%s&buyCounts=%s",
//                    instanceItem.getHost(),
//                    instanceItem.getPort(),
//                    itemSpecId,buyCounts);
//            restTemplate.delete(url);
        }

        newOrder.setTotalAmount(totalAmount);
        newOrder.setRealPayAmount(realPayAmount);
        ordersMapper.insert(newOrder);

        // 3. 保存订单状态表
        OrderStatus waitPayOrderStatus = new OrderStatus();
        waitPayOrderStatus.setOrderId(orderId);
        waitPayOrderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        waitPayOrderStatus.setCreatedTime(new Date());
        orderStatusMapper.insert(waitPayOrderStatus);

        // 4. 构建商户订单，用于传给支付中心
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        merchantOrdersVO.setAmount(realPayAmount + postAmount);
        merchantOrdersVO.setPayMethod(payMethod);

        // 5. 构建自定义订单vo
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(orderId);
        orderVO.setMerchantOrdersVO(merchantOrdersVO);
        orderVO.setToBeRemovedShopcatdList(toBeRemovedShopcatdList);

        return orderVO;
    }


    private void decreaseItemSpecStock(String itemSpecId, Integer buyCounts){
        DecreaseStock decreaseStock = DecreaseStock.builder()
                .ItemSpecId(itemSpecId)
                .buyCounts(buyCounts).build();
        String msg = JsonUtils.objectToJson(decreaseStock);

        HashMap<String, Object> map = new HashMap<>();
        map.put("exchange","exchange-decreaseStock");
        map.put("routingKey","routingKey.decreaseStock");

        rabbitSender.send(msg,map);
    }

    /**
     * 从redis中的购物车里获取商品，目的：counts
     * @param shopcartList
     * @param specId
     * @return
     */
    private ShopcartBO getBuyCountsFromShopcart(List<ShopcartBO> shopcartList, String specId) {
        for (ShopcartBO cart : shopcartList) {
            if (cart.getSpecId().equals(specId)) {
                return cart;
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {

        OrderStatus paidStatus = new OrderStatus();
        paidStatus.setOrderId(orderId);
        paidStatus.setOrderStatus(orderStatus);
        paidStatus.setPayTime(new Date());

        orderStatusMapper.updateByPrimaryKeySelective(paidStatus);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatus queryOrderStatusInfo(String orderId) {
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void closeOrder() {

        // 查询所有未付款订单，判断时间是否超时（1天），超时则关闭交易
        OrderStatus queryOrder = new OrderStatus();
        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> list = orderStatusMapper.select(queryOrder);
        for (OrderStatus os : list) {
            // 获得订单创建时间
            Date createdTime = os.getCreatedTime();
            // 和当前时间进行对比
            int days = DateUtil.daysBetween(createdTime, new Date());
            if (days >= 1) {
                // 超过1天，关闭订单
                doCloseOrder(os.getOrderId());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void doCloseOrder(String orderId) {
        OrderStatus close = new OrderStatus();
        close.setOrderId(orderId);
        close.setOrderStatus(OrderStatusEnum.CLOSE.type);
        close.setCloseTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(close);
    }
}
