package com.greyu.ysj.service.impl;

import com.greyu.ysj.config.ResultStatus;
import com.greyu.ysj.entity.*;
import com.greyu.ysj.mapper.CartDetailMapper;
import com.greyu.ysj.mapper.CartMapper;
import com.greyu.ysj.mapper.GoodMapper;
import com.greyu.ysj.model.ResultModel;
import com.greyu.ysj.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Author: gre_yu@163.com
 * @Date: Created in 23:12 2018/3/11.
 */
@Service
public class CartServiceImpl implements CartService{
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartDetailMapper cartDetailMapper;

    @Autowired
    private GoodMapper goodMapper;

    @Override
    public Cart get(Integer userId) {
        CartExample cartExample = new CartExample();
        CartExample.Criteria criteria = cartExample.createCriteria();
        criteria.andUserIdEqualTo(userId);

        Cart cart;
        try {
            cart = this.cartMapper.selectByExample(cartExample).get(0);
        } catch (Exception e) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setAmount(0.0);
            this.cartMapper.insert(cart);
            cart = this.cartMapper.selectByExample(cartExample).get(0);
            e.printStackTrace();
        }

        // Find out the shopping cart details
        CartDetailExample cartDetailExample = new CartDetailExample();
        CartDetailExample.Criteria cartCriteria = cartDetailExample.createCriteria();
        // Shopping cart id
        cartCriteria.andCartIdEqualTo(cart.getCartId());
        // Not equal to 0
        cartCriteria.andCountNotEqualTo(0);
        List<CartDetail> cartDetails = this.cartDetailMapper.selectByExample(cartDetailExample);

        for (CartDetail c : cartDetails) {
            Good good = this.goodMapper.selectByPrimaryKey(c.getGoodId());
            c.setGood(good);
        }
        cart.setGoods(cartDetails);

        System.out.println(cart);
        return cart;
    }

    @Override
    public ResultModel getCartDetailByGoodId(Integer userId, Integer goodId) {
        // First find the user's shopping cart
        CartExample cartExample = new CartExample();
        CartExample.Criteria cartCriteria = cartExample.createCriteria();
        cartCriteria.andUserIdEqualTo(userId);
        Cart cart;
        try {
            cart = this.cartMapper.selectByExample(cartExample).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            cart = null;
        }

        if (null == cart) {
            return ResultModel.error(ResultStatus.CART_NOT_FOUND);
        }

        // Find out the shopping cart details for goodId
        CartDetailExample cartDetailExample = new CartDetailExample();
        CartDetailExample.Criteria criteria = cartDetailExample.createCriteria();

        criteria.andGoodIdEqualTo(goodId);
        criteria.andCartIdEqualTo(cart.getCartId());

        CartDetail cartDetail;

        try {
            cartDetail = this.cartDetailMapper.selectByExample(cartDetailExample).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            cartDetail = null;
        }

        if (null == cartDetail) {
            return ResultModel.error(ResultStatus.CART_NOT_FOUND);
        }

        return ResultModel.ok(cartDetail);
    }

    @Override
    public ResultModel save(Integer userId, Integer goodId, Integer count) {
        Good good = this.goodMapper.selectByPrimaryKey(goodId);
        // Product cannot exist
        if (null == good) {
            return ResultModel.error(ResultStatus.GOOD_NOT_FOUND);
        }

        if (good.getInventory() < count) {
            return ResultModel.error(ResultStatus.GOOD_INSUFFICIENT);
        }

        // Read the shopping cart information of the user in the database
        CartExample cartExample = new CartExample();
        CartExample.Criteria criteria = cartExample.createCriteria();
        criteria.andUserIdEqualTo(userId);
        Cart cart;
        try {
            cart = this.cartMapper.selectByExample(cartExample).get(0);
        } catch (Exception e) {
            cart = null;
        }

        // If the user does not have a shopping cart in the database, set the default Cart property
        if (null == cart) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setAmount(0.0);
            // 新建购物车
            this.cartMapper.insert(cart);
        }

        CartDetailExample cartDetailExample = new CartDetailExample();
        CartDetailExample.Criteria cartDetailCriteria1 = cartDetailExample.createCriteria();
        // Set shopping cart details for shopping cart id
        cartDetailCriteria1.andCartIdEqualTo(cart.getCartId());
        // Set the product id of the shopping cart details
        cartDetailCriteria1.andGoodIdEqualTo(goodId);

        // Find the shopping cart details
        CartDetail cartDetail;
        try {
            cartDetail = this.cartDetailMapper.selectByExample(cartDetailExample).get(0);
        } catch (Exception e) {
            cartDetail = null;
        }

        // The price of the item
        Double price = good.getPrice();

        // If there is no such item in the shopping cart, insert a new message first.
        // If already, update the quantity of the item
        if (null == cartDetail) {
            if (count > 0) {
                cartDetail = new CartDetail();
                cartDetail.setCartId(cart.getCartId());
                cartDetail.setGoodId(goodId);

                // Set the number of items in the shopping cart details
                cartDetail.setCount(count);

                // Calculate the total price of the shopping cart
                cart.setAmount(cart.getAmount() + price * cartDetail.getCount());

                // Update shopping cart
                this.cartMapper.updateByPrimaryKey(cart);
                // Insert shopping cart details
                this.cartDetailMapper.insert(cartDetail);
            }
        } else {
            // If the number in the shopping cart is not enough
            if (cartDetail.getCount() + count < 0) {
                // Set the number of items in the shopping cart details
                cartDetail.setCount(0);

                // Calculate the amount, and update the database
                cart.setAmount(cart.getAmount() + price * cartDetail.getCount() * -1);
            } else {
                // Set the number of items in the shopping cart details, add count
                cartDetail.setCount(cartDetail.getCount() + count);

                // Calculate the amount, and update the database
                cart.setAmount(cart.getAmount() + price * count);
            }
            // Update shopping cart
            this.cartMapper.updateByPrimaryKey(cart);
            // Update shopping cart details
            this.cartDetailMapper.updateByPrimaryKey(cartDetail);
        }

        Cart lastCart = this.cartMapper.getAllCart(userId);
        return ResultModel.ok(lastCart);
    }

    @Override
    public ResultModel delete(Integer userId, Long cartId) {
        CartExample cartExample = new CartExample();
        CartExample.Criteria criteria = cartExample.createCriteria();
        criteria.andUserIdEqualTo(userId);

        Cart cart;
        try {
            cart = this.cartMapper.selectByExample(cartExample).get(0);
        } catch (Exception e) {
            cart = null;
        }

        if (cart == null) {
            return ResultModel.error(ResultStatus.CART_NOT_FOUND);
        }

        this.cartMapper.deleteByPrimaryKey(cart.getCartId());

        return ResultModel.ok();
    }

    @Override
    public ResultModel deleteOne(Integer goodId) {
        return null;
    }

    @Override
    public ResultModel update(Integer userId, Long cartId, Integer goodId, Integer count) {
        Cart cart = this.cartMapper.selectByPrimaryKey(cartId);
        // No cart found
        if (null == cart) {
            return ResultModel.error(ResultStatus.CART_NOT_FOUND);
        }

        // Permission denied
        if (!cart.getUserId().equals(userId)) {
            return ResultModel.error(ResultStatus.USER_NOT_ALLOWED);
        }

        Good good = this.goodMapper.selectByPrimaryKey(goodId);
        // Product not found
        if (null == good) {
            return ResultModel.error(ResultStatus.GOOD_NOT_FOUND);
        }

        // Insufficient inventory
        if ((good.getInventory() + count) < 0) {
            return ResultModel.error(ResultStatus.GOOD_INSUFFICIENT);
        }

        // Find information about the item in the shopping cart
        CartDetailExample cartDetailExample = new CartDetailExample();
        CartDetailExample.Criteria criteria = cartDetailExample.createCriteria();
        criteria.andCartIdEqualTo(cartId);
        criteria.andGoodIdEqualTo(goodId);
        List<CartDetail> cartDetailList = this.cartDetailMapper.selectByExample(cartDetailExample);

        CartDetail cartDetail;
        try {
            cartDetail = cartDetailList.get(0);
        } catch (Exception e) {
            cartDetail = null;
        }

        if (null == cartDetail) { // This item is not in the shopping cart.
            cartDetail = new CartDetail();
            // If you want to reduce the number of items, return directly to the success, because there is no such item in the shopping cart.
            if (count <= 0) {
                return ResultModel.ok();
            }

            cartDetail.setCartId(cartId);
            cartDetail.setGoodId(goodId);
            cartDetail.setCount(count);

            this.cartDetailMapper.insert(cartDetail);
            // Update the total price of the shopping cart
            cart.setAmount(cart.getAmount() + good.getPrice() * count);
            this.cartMapper.updateByPrimaryKey(cart);

            // Find updated shopping cart information
            cart = this.cartMapper.getAllCart(userId);
            return ResultModel.ok(cart);

        } else { // This item is already in the shopping cart.
            // After updating, the number of items in the shopping cart <= 0
            if ((cartDetail.getCount() + count) <= 0) {
                // Update the total price of the shopping cart
                cart.setAmount(cart.getAmount() - cartDetail.getCount() * good.getPrice());
                // Delete this item in the shopping cart
                this.cartDetailMapper.deleteByPrimaryKey(cartDetail.getCartDetailId());

                cart = this.cartMapper.getAllCart(userId);
                return ResultModel.ok(cart);
            }else { // Update the number of items in the shopping cart
                cartDetail.setCount(cartDetail.getCount() + count);
                this.cartDetailMapper.updateByPrimaryKey(cartDetail);

                // Update the total price of the shopping cart
                cart.setAmount(cart.getAmount() + good.getPrice() * count);
                this.cartMapper.updateByPrimaryKey(cart);

                cart = this.cartMapper.getAllCart(userId);
                return ResultModel.ok(cart);
            }
        }
    }
}
