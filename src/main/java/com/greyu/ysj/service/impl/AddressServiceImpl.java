package com.greyu.ysj.service.impl;

import com.github.pagehelper.PageHelper;
import com.greyu.ysj.config.ResultStatus;
import com.greyu.ysj.entity.Address;
import com.greyu.ysj.entity.AddressExample;
import com.greyu.ysj.entity.User;
import com.greyu.ysj.mapper.AddressMapper;
import com.greyu.ysj.mapper.UserMapper;
import com.greyu.ysj.model.ResultModel;
import com.greyu.ysj.service.AddressService;
import com.greyu.ysj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Author: gre_yu@163.com
 * @Date: Created in 17:25 2018/3/9.
 */
@Service
public class AddressServiceImpl implements AddressService{
    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public ResultModel getUserAllAddress(Integer userId, Integer page, Integer rows) {
        User user = this.userMapper.selectByPrimaryKey(userId);

        // User is empty, return user does not exist
        if (null == user) {
            return ResultModel.error(ResultStatus.USER_NOT_FOUND);
        }

        if (null != page && null != rows) {
            PageHelper.startPage(page, rows);
        }

        AddressExample addressExample = new AddressExample();
        AddressExample.Criteria criteria = addressExample.createCriteria();

        criteria.andUserIdEqualTo(userId);
        addressExample.setOrderByClause("is_default desc");
        List<Address> addressList = this.addressMapper.selectByExample(addressExample);

        return ResultModel.ok(addressList);
    }

    @Override
    public ResultModel getOne(Integer userId, Integer addressId) {
        Address address = this.addressMapper.selectByPrimaryKey(addressId);

        if (null == address || !address.getUserId().equals(userId)) {
            return ResultModel.error(ResultStatus.ADDRESS_NOT_FOUND);
        }

        return ResultModel.ok(address);
    }

    @Override
    public ResultModel save(Address address) {
        // User id, recipient, mobile number, city, full address, house number cannot be empty
        if (null == address.getUserId() || null == address.getConsignee() ||
                null == address.getCity() || null == address.getPhone() ||
                null == address.getAddress() || null == address.getStreetNumber()) {
            return ResultModel.error(ResultStatus.DATA_NOT_NULL);
        }

        // Determine if user exists
        User user = this.userMapper.selectByPrimaryKey(address.getUserId());
        if (null == user) {
            return ResultModel.error(ResultStatus.USER_NOT_FOUND);
        }

        // Set isDefault default
        if (null == address.getIsDefault()) {
            address.setIsDefault(false);
        }

        // Add userId query criteria
        List<Address> addressList;
        AddressExample addressExample = new AddressExample();
        AddressExample.Criteria criteria = addressExample.createCriteria();
        criteria.andUserIdEqualTo(address.getUserId());

        // When isDefault is true, set the isDefault of the other address of the user to false
        if (address.getIsDefault() == true) {
            addressList  = this.addressMapper.selectByExample(addressExample);

            for (Address addr : addressList) {
                Integer addressId = addr.getAddressId();
                addr.setIsDefault(false);
                this.addressMapper.updateByPrimaryKey(addr);
            }
        }

        // Insert data
        this.addressMapper.insert(address);

        // I found the address information I just added.
        addressExample.setOrderByClause("address_id Desc");
        addressList = this.addressMapper.selectByExample(addressExample);
        address = addressList.get(0);

        return ResultModel.ok(address);
    }

    /**
     * 删除user的address
     * @param address
     * @return
     */
    @Override
    public ResultModel delete(Address address) {
        Address newAddress = this.addressMapper.selectByPrimaryKey(address.getAddressId());
        AddressExample addressExample = new AddressExample();
        AddressExample.Criteria criteria = addressExample.createCriteria();

        if (null == newAddress || !newAddress.getUserId().equals(address.getUserId())) {
            return ResultModel.error(ResultStatus.ADDRESS_NOT_FOUND);
        }

        // If the default address is deleted, select the address with the largest id of the user as the default address. If there is no other address, do not use it.
        if (newAddress.getIsDefault() == true) {
            //  userId == this.userId
            criteria.andUserIdEqualTo(address.getUserId());
            // addressId != this.addressId
            criteria.andAddressIdNotEqualTo(address.getAddressId());
            // order by address_id desc
            addressExample.setOrderByClause("address_id desc");
            List<Address> addressList = this.addressMapper.selectByExample(addressExample);

            Address otherAddress;
            try {
                otherAddress = addressList.get(0);
            } catch (Exception e) {
                otherAddress = null;
            }

            // If the user has another address
            if (null != otherAddress) {
                otherAddress.setIsDefault(true);
                this.addressMapper.updateByPrimaryKey(otherAddress);
            }
        }

        this.addressMapper.deleteByPrimaryKey(address.getAddressId());

        return ResultModel.ok();
    }

    @Override
    public ResultModel update(Address address) {
        // Judge that address is empty or the userId of the isolated address does not match the userId of the url
        Address newAddress = this.addressMapper.selectByPrimaryKey(address.getAddressId());
        if (null == newAddress || !newAddress.getUserId().equals(address.getUserId())) {
            return ResultModel.error(ResultStatus.ADDRESS_NOT_FOUND);
        }

        AddressExample addressExample = new AddressExample();
        AddressExample.Criteria criteria = addressExample.createCriteria();
        criteria.andUserIdEqualTo(address.getUserId());

        if (null != address.getConsignee()) {
            newAddress.setConsignee(address.getConsignee());
        }

        if (null != address.getPhone()) {
            newAddress.setPhone(address.getPhone());
        }

        if (null != address.getAddress()) {
            newAddress.setAddress(address.getAddress());
        }

        if (null != address.getCity()) {
            newAddress.setCity(address.getCity());
        }

        if (null != address.getStreetNumber()) {
            newAddress.setStreetNumber(address.getStreetNumber());
        }

        if (null != address.getIsDefault()) {
            // If isDefault is at, set the other isDefault to false
            if (address.getIsDefault() == true) {
                List<Address> addressList  = this.addressMapper.selectByExample(addressExample);

                for (Address addr : addressList) {
                    Integer addressId = addr.getAddressId();
                    addr.setIsDefault(false);
                    this.addressMapper.updateByPrimaryKey(addr);
                }
            }
            newAddress.setIsDefault(address.getIsDefault());
        }

        this.addressMapper.updateByPrimaryKey(newAddress);

        return ResultModel.ok(newAddress);
    }
}
