package com.greyu.ysj.service.impl;

import com.github.pagehelper.PageHelper;
import com.greyu.ysj.config.Constants;
import com.greyu.ysj.config.ResultStatus;
import com.greyu.ysj.entity.*;
import com.greyu.ysj.mapper.*;
import com.greyu.ysj.model.ResultModel;
import com.greyu.ysj.model.StatisticsOrder;
import com.greyu.ysj.service.OrderService;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.xml.transform.Result;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Author: gre_yu@163.com
 * @Date: Created in 22:14 2018/3/11.
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private GoodMapper goodMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartDetailMapper cartDetailMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Order> getAllOrders(Integer page, Integer rows, String orderBy, Order order, String userName, String start, String end) {
        if (null != page && null != rows) {
            PageHelper.startPage(page, rows);
        }

        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();

        if (null != order.getOrderId()) {
            criteria.andOrderIdEqualTo(order.getOrderId());
        }

        // Order creation time is between [start, end]
        if (null != start && null != end) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startTime;
            Date endTime;

            try {
                startTime = sdf.parse(start);
                endTime = sdf.parse(end);
            } catch (ParseException e) {
                startTime = null;
                endTime = null;
                e.printStackTrace();
            }

            if (null != startTime && null != endTime) {
                criteria.andCreateTimeBetween(startTime, endTime);
            }
        }

        // Query by username
        if (null != userName) {
            UserExample userExample = new UserExample();
            UserExample.Criteria userCriteria = userExample.createCriteria();
            userCriteria.andUserNameEqualTo(userName);
            List<User> users = this.userMapper.selectByExample(userExample);

            User user;
            try {
                user = users.get(0);
                criteria.andUserIdEqualTo(user.getUserId());
            } catch (Exception e) {
                user = null;
            }
        }

        // Set query conditions status
        if (null != order.getStatus()) {
            criteria.andStatusEqualTo(order.getStatus());
        }

        orderExample.setOrderByClause("create_time desc");
        List<Order> orderList = this.orderMapper.selectByExample(orderExample);

        for (Order temp: orderList) {
            Address address = this.addressMapper.selectByPrimaryKey(temp.getAddressId());
            temp.setAddress(address);

//            OrderDetailExample orderDetailExample = new OrderDetailExample();
//            OrderDetailExample.Criteria orderDetailCriteria = orderDetailExample.createCriteria();
//            orderDetailCriteria.andOrderIdEqualTo(temp.getOrderId());

            List<OrderDetail> orderDetails = this.orderDetailMapper.getAllByOrderId(temp.getOrderId());
            temp.setOrderDetails(orderDetails);

            System.out.println(temp);
        }

        return orderList;
    }

    /**
     * Get order information
     * @return
     */
    @Override
    public ResultModel orderStatistics() {
        Integer orderWaiting = countWait();

        Integer orderWaitingToday = countWaitToday();

        Integer orderRefunding = countRefunding();

        Integer orderSuccess = countSuccess();

        Integer orderSuccessToday = countSuccessToday();

        Integer orderDispatching = countDispatching();

        Double totalSale = countTotalSale();

        Double todaySale = countTodaySale();

        Integer collection = countCollection();

        Integer userCount = countUserCount();

        StatisticsOrder statisticsOrder = new StatisticsOrder();
        // Transaction order
        statisticsOrder.setSuccess(orderSuccess);
        // Today's deal
        statisticsOrder.setSuccessToday(orderSuccessToday);
        // to be delivered
        statisticsOrder.setWait(orderWaiting);
        // New pending delivery today
        statisticsOrder.setWaitToday(orderWaitingToday);
        // In distribution
        statisticsOrder.setDispatching(orderDispatching);
        // Pending refund
        statisticsOrder.setRefunding(orderRefunding);

        // Total sales
        statisticsOrder.setTotalSale(totalSale);

        // Sales today
        statisticsOrder.setTodaySale(todaySale);

        // Number of collections
        statisticsOrder.setCollection(collection);

        // amount of users
        statisticsOrder.setUserCount(userCount);

        return ResultModel.ok(statisticsOrder);
    }

    private Integer countUserCount() {
        UserExample userExample = new UserExample();
        Integer userCount = this.userMapper.countByExample(userExample);

        return userCount;
    }

    private Integer countCollection() {
        CartDetailExample cartExample = new CartDetailExample();
        CartDetailExample.Criteria criteria = cartExample.createCriteria();
        Integer collection = this.cartDetailMapper.countByExample(cartExample);

        return collection;
    }

    private Double countTotalSale() {
        Double totalSale = 0.0;
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();
        criteria.andStatusEqualTo(Constants.ORDER_FINISH);

        List<Order> lists = this.orderMapper.selectByExample(orderExample);

        for (Order order: lists) {
            totalSale = totalSale + order.getAmount();
        }

        return totalSale;
    }

    private Double countTodaySale() {
        Double totalSale = 0.0;
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();
        criteria.andStatusEqualTo(Constants.ORDER_FINISH);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start = new Date();
        Date end = new Date();

        Date startTime;
        try {
            startTime = sdf.parse(sdf.format(start));
        } catch (ParseException e) {
            startTime = null;
            e.printStackTrace();
        }

        criteria.andCreateTimeBetween(startTime, end);

        List<Order> lists = this.orderMapper.selectByExample(orderExample);

        for (Order order: lists) {
            totalSale += order.getAmount();
        }

        return totalSale;
    }

    private Integer countDispatching() {
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();
        criteria.andStatusEqualTo(Constants.ORDER_DISPATCHING);
        Integer count = this.orderMapper.countByExample(orderExample);
        return count;
    }

    private Integer countWait() {
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();
        criteria.andStatusEqualTo(Constants.ORDER_WAIT);
        Integer count = this.orderMapper.countByExample(orderExample);
        return count;
    }

    private Integer countWaitToday() {
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();
        criteria.andStatusEqualTo(Constants.ORDER_WAIT);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start = new Date();
        Date end = new Date();

        Date startTime;
        try {
            startTime = sdf.parse(sdf.format(start));
        } catch (ParseException e) {
            startTime = null;
            e.printStackTrace();
        }

        criteria.andCreateTimeBetween(startTime, end);
        Integer count = this.orderMapper.countByExample(orderExample);
        return count;
    }

    private Integer countRefunding() {
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();
        criteria.andStatusEqualTo(Constants.ORDER_REFUNDING);
        Integer count = this.orderMapper.countByExample(orderExample);
        return count;
    }

    private Integer countSuccess() {
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();
        criteria.andStatusEqualTo(Constants.ORDER_FINISH);
        Integer count = this.orderMapper.countByExample(orderExample);
        return count;
    }

    private Integer countSuccessToday() {
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();
        criteria.andStatusEqualTo(Constants.ORDER_FINISH);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start = new Date();
        Date end = new Date();

        Date startTime;
        try {
            startTime = sdf.parse(sdf.format(start));
        } catch (ParseException e) {
            startTime = null;
            e.printStackTrace();
        }

        criteria.andCreateTimeBetween(startTime, end);
        Integer count = this.orderMapper.countByExample(orderExample);
        return count;
    }

    @Override
    public ResultModel getOrderByUserId(Integer userId, Integer status) {
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria();

        if (null != status) {
            criteria.andStatusEqualTo(status);
        }

        criteria.andUserIdEqualTo(userId);
        orderExample.setOrderByClause("create_time DESC");
        List<Order> orders = this.orderMapper.selectByExample(orderExample);

        // Shopping cart details
        for (Order order: orders) {
            OrderDetailExample orderDetailExample = new OrderDetailExample();
            OrderDetailExample.Criteria detailCriteria = orderDetailExample.createCriteria();
            detailCriteria.andOrderIdEqualTo(order.getOrderId());
            List<OrderDetail> orderDetails = this.orderDetailMapper.selectByExample(orderDetailExample);

            // Product details
            for (OrderDetail orderDetail: orderDetails) {
                Good good = this.goodMapper.selectByPrimaryKey(orderDetail.getGoodId()) ;
                orderDetail.setGood(good);
            }
            order.setOrderDetails(orderDetails);
        }

        return ResultModel.ok(orders);
    }

    /**
     * Get order information according to orderId
     * @param orderId
     * @return
     */
    @Override
    public ResultModel getOneOrder(Long orderId) {
        Order order = this.orderMapper.selectByPrimaryKey(orderId);

        if (null == order) {
            return ResultModel.error(ResultStatus.ORDER_NOT_FOUND);
        }

        return ResultModel.ok(order);
    }

    /**
     * Create a new order
     * @param userId
     * @param addressId
     * @param remarks
     * @param ids  Shopping cart details id
     * @return
     */
    @Override
    public ResultModel create(Integer userId, Integer addressId, String remarks, String ids) {
        String[] str = ids.split(",");
        int length = str.length;
        Long[] cartDetailIds = new Long[length];
        for (int i = 0; i < length ; i++) {
            cartDetailIds[i] = Long.parseLong(str[i]);
        }

        Long oneCartDetailId = cartDetailIds[0];
        // Get user cart
        CartDetail oneCartDetail = this.cartDetailMapper.selectByPrimaryKey(oneCartDetailId);
        Cart userCart;
        if (null != oneCartDetail) {
            userCart = this.cartMapper.selectByPrimaryKey(oneCartDetail.getCartId());
        } else {
            userCart = new Cart();
        }

        // Create a new order
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setAmount(0.0);
        order.setCreateTime(new Date());
        order.setRemarks(remarks);
        order.setStatus(Constants.ORDER_WAIT);
        // Insert database
        this.orderMapper.insert(order);
        long orderId = order.getOrderId();
        System.out.println(666);

        for(Long cartDetailId: cartDetailIds) {
            CartDetail cartDetail = this.cartDetailMapper.selectByPrimaryKey(cartDetailId);
            // No shopping cart found
            if (null == cartDetail) {
                System.out.println("meiyou");
                // Failed to create order, delete inserted shopping cart information
                this.orderMapper.deleteByPrimaryKey(orderId);
                return ResultModel.error(ResultStatus.CART_NOT_FOUND);
            }

            int goodId = cartDetail.getGoodId();
            Good good = this.goodMapper.selectByPrimaryKey(goodId);
            // Inventory shortage
            if (good.getInventory() < cartDetail.getCount()) {
                // Failed to create shopping cart, delete inserted shopping cart information
                this.orderMapper.deleteByPrimaryKey(orderId);
                return ResultModel.error(ResultStatus.GOOD_INSUFFICIENT);
            }

            // Calculate the total price of the commodity
            double price = good.getPrice();
            int count = cartDetail.getCount();
            double total = price * count;

            // Update product sales
            good.setSoldCount(good.getSoldCount() + count);
            // Update product inventory
            good.setInventory(good.getInventory() - count);
            this.goodMapper.updateByPrimaryKey(good);

            // Insert shopping cart details
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setGoodId(goodId);
            orderDetail.setCount(count);
            this.orderDetailMapper.insert(orderDetail);

            // Update the total amount of the order
            order.setAmount(order.getAmount() + total);

            // Update the total amount of the shopping cart
            userCart.setAmount(userCart.getAmount() - total);
            // Delete shopping cart details
            this.cartDetailMapper.deleteByPrimaryKey(cartDetailId);
        }

        // Update shopping cart information
        this.cartMapper.updateByPrimaryKey(userCart);
        // Update order information
        this.orderMapper.updateByPrimaryKey(order);

        return ResultModel.ok(order);
    }

    @Override
    public ResultModel updateOrder(Order order) {
        return null;
    }

    /**
     * Request a refund
     * @param orderId
     * @return
     */
    @Override
    public ResultModel refund(Long orderId) {
        Order order = this.orderMapper.selectByPrimaryKey(orderId);

        if (null == order) {
            return ResultModel.error(ResultStatus.ORDER_NOT_FOUND);
        }

        order.setStatus(Constants.ORDER_REFUNDING);
        this.orderMapper.updateByPrimaryKey(order);

        return ResultModel.ok();
    }

    /**
     * Ship
     * @param orderId
     * @return
     */
    @Override
    public ResultModel deliver(Long orderId) {
        Order order = this.orderMapper.selectByPrimaryKey(orderId);

        if (null == order) {
            return ResultModel.error(ResultStatus.ORDER_NOT_FOUND);
        }

        order.setStatus(Constants.ORDER_DISPATCHING);
        this.orderMapper.updateByPrimaryKey(order);

        return ResultModel.ok();
    }

    /**
     * Confirm order delivery completed
     * @param orderId
     * @return
     */
    @Override
    public ResultModel confirm(Long orderId) {
        Order order = this.orderMapper.selectByPrimaryKey(orderId);

        if (null == order) {
            return ResultModel.error(ResultStatus.ORDER_NOT_FOUND);
        }

        order.setStatus(Constants.ORDER_FINISH);
        this.orderMapper.updateByPrimaryKey(order);

        return ResultModel.ok();
    }

    /**
     * Confirm refund success
     * @param orderId
     * @return
     */
    @Override
    public ResultModel confirmRefund(Long orderId) {
        Order order = this.orderMapper.selectByPrimaryKey(orderId);

        if (null == order) {
            return ResultModel.error(ResultStatus.ORDER_NOT_FOUND);
        }

        order.setStatus(Constants.ORDER_REFUND_SUCCESS);
        this.orderMapper.updateByPrimaryKey(order);

        // Find the cart details for the shopping cart id
        OrderDetailExample orderDetailExample = new OrderDetailExample();
        OrderDetailExample.Criteria criteria = orderDetailExample.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List<OrderDetail> orderDetails = this.orderDetailMapper.selectByExample(orderDetailExample);

        for (OrderDetail orderDetail: orderDetails) {
            int goodId = orderDetail.getGoodId();
            Good good = this.goodMapper.selectByPrimaryKey(goodId);
            int count = orderDetail.getCount();
            // Update product inventory
            good.setInventory(good.getInventory() + count);
            // Update product sales
            good.setSoldCount(good.getSoldCount() - count);

            this.goodMapper.updateByPrimaryKey(good);
        }

        return ResultModel.ok();
    }

    /**
     * Refuse to refund
     * @param orderId
     * @return
     */
    @Override
    public ResultModel refuseRefund(Long orderId) {
        Order order = this.orderMapper.selectByPrimaryKey(orderId);

        if (null == order) {
            return ResultModel.error(ResultStatus.ORDER_NOT_FOUND);
        }

        order.setStatus(Constants.ORDER_REFUNDING_FAILURE);
        this.orderMapper.updateByPrimaryKey(order);

        return ResultModel.ok();
    }

    @Override
    public ResultModel delete(Long orderId) {
        return null;
    }
}
