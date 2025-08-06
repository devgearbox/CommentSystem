package com.example.lizhi.service;

import com.example.lizhi.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> getAddressesByUserId(Long userId);
    Address saveAddress(Address address);
    void deleteAddress(Long id);
    boolean isAddressOwnedByUser(Long addressId, Long userId);
    void clearDefaultAddress(Long userId);
    Address getAddressById(Long addressId);
    // 添加方法获取用户的默认地址
    Address findDefaultAddressByUserId(Long userId);
}