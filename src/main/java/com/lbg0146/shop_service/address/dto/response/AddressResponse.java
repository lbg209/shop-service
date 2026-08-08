package com.lbg0146.shop_service.address.dto.response;

import com.lbg0146.shop_service.address.entity.Address;

public record AddressResponse(
        Long addressId,
        String addressName,
        String receiverName,
        String phone,
        String zipcode,
        String address,
        String detailAddress,
        boolean isDefault
){
    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getAddressName(),
                address.getReceiverName(),
                address.getPhone(),
                address.getZipcode(),
                address.getAddress(),
                address.getDetailAddress(),
                address.isDefault()
        );
    }
}
