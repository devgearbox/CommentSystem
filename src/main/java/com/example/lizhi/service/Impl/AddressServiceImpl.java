package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.Address;
import com.example.lizhi.repository.AddressRepository;
import com.example.lizhi.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        // 查询用户当前默认地址
        Address defaultAddr = addressRepository.findDefaultByUserId(userId);

        for (Address addr : addresses) {
            // 标记是否为默认地址
            addr.setDefault(addr.getId().equals(defaultAddr != null ? defaultAddr.getId() : -1L));
        }
        return addresses;
    }

    @Override
    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    public boolean isAddressOwnedByUser(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId).orElse(null);
        return address != null && address.getUserId().equals(userId);
    }

    @Override
    public void clearDefaultAddress(Long userId) {
        List<Address> userAddresses = addressRepository.findByUserId(userId);
        for (Address addr : userAddresses) {
            addr.setDefault(false);
        }
        addressRepository.saveAll(userAddresses);
    }
    @Override
    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId).orElse(null);
    }
}