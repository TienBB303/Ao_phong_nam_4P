package com.example.datn.services;

import com.example.datn.dto.AddressDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.Customer;
import com.example.datn.entities.ShippingAddress;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.repositories.CustomerRepository;
import com.example.datn.repositories.ShippingAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
public class AddressService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ShippingAddressRepository addressRepository;

    @Transactional
    public void save(AddressDto dto, Principal principal) {
        // 1. Lấy account theo username đang login
        Account account = accountRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Customer customer = customerRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (dto.getIsDefault()) {
            addressRepository.clearDefault(customer.getId());
        }

        // 3. Map DTO -> Entity
        ShippingAddress address = new ShippingAddress();
        address.setAddressDetail(dto.getAddressDetail());
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhoneNumber(dto.getReceiverPhoneNumber());

        address.setProvinceId(dto.getProvinceId());
        address.setProvinceName(dto.getProvinceName());

        address.setDistrictId(dto.getDistrictId());
        address.setDistrictName(dto.getDistrictName());

        address.setWardId(dto.getWardId());
        address.setWardName(dto.getWardName());

        address.setIsDefault(dto.getIsDefault());
        address.setCustomer(customer);

        // 4. Lưu
        addressRepository.save(address);
    }
}
