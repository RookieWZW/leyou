package com.leyou.order.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.AddressMapper;
import com.leyou.order.pojo.Address;
import com.leyou.order.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by RookieWangZhiWei on 2019/4/27.
 */
@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    public void deleteAddress(Long addressId) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        Example example = new Example(Address.class);
        example.createCriteria().andEqualTo("userId", userInfo.getId()).andEqualTo("id", addressId);
        this.addressMapper.deleteByExample(example);
    }


    public void updateAddressByUserId(Address address) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        address.setUserId(userInfo.getId());
        setDefaultAddress(address);
        this.addressMapper.updateByPrimaryKeySelective(address);
    }

    public List<Address> queryAddressByUserId() {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        Example example = new Example(Address.class);
        example.createCriteria().andEqualTo("userId", userInfo.getId());
        return this.addressMapper.selectByExample(example);
    }

    public void addAddressByUserId(Address address) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        address.setUserId(userInfo.getId());
        setDefaultAddress(address);
        this.addressMapper.insert(address);
    }

    public Address queryAddressById(Long addressId) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        Example example = new Example(Address.class);
        example.createCriteria().andEqualTo("id", addressId).andEqualTo("userId", userInfo.getId());
        return this.addressMapper.selectByExample(example).get(0);
    }

    public void setDefaultAddress(Address address) {
        if (address.getDefaultAddress()) {
            List<Address> addresses = this.queryAddressByUserId();
            addresses.forEach(addressTemp -> {
                if (addressTemp.getDefaultAddress()) {
                    addressTemp.setDefaultAddress(false);
                    this.addressMapper.updateByPrimaryKey(addressTemp);
                }
            });
        }
    }
}
