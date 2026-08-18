package com.lbg0146.shop_service.address.service;

import com.lbg0146.shop_service.address.dto.request.AddressCreateRequest;
import com.lbg0146.shop_service.address.dto.request.AddressUpdateRequest;
import com.lbg0146.shop_service.address.dto.response.AddressResponse;
import com.lbg0146.shop_service.address.entity.Address;
import com.lbg0146.shop_service.address.repository.AddressRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createAddress(Long memberId, AddressCreateRequest request) {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (request.isDefault()) {
            addressRepository.findByMemberIdAndIsDefaultTrue(memberId)
                    .ifPresent(address -> address.updateDefault(false));
        }

        Address address = Address.createAddress(
                member,
                request.addressName(),
                request.receiverName(),
                request.phone(),
                request.zipcode(),
                request.address(),
                request.detailAddress(),
                request.isDefault()
        );

        Address saveAddress = addressRepository.save(address);

        return saveAddress.getId();
    }


    public AddressResponse findAddress(Long memberId, Long addressId) {

        Address address = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        return AddressResponse.from(address);
    }


    public List<AddressResponse> findAddresses(Long memberId) {

        memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return addressRepository.findAllByMemberId(memberId)
                .stream()
                .map(AddressResponse::from)
                .toList();
    }


    @Transactional
    public void updateAddress(Long memberId, Long addressId, AddressUpdateRequest request) {

        Address address = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        address.update(
                request.addressName(),
                request.receiverName(),
                request.phone(),
                request.zipcode(),
                request.address(),
                request.detailAddress()
        );
    }


    @Transactional
    public void deleteAddress(Long memberId, Long addressId) {

        Address address = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        addressRepository.delete(address);
    }

    @Transactional
    public void changeDefaultAddress(Long memberId, Long addressId) {

        // 1. 기존 기본 배송지를 모두 false로 벌크 업데이트 (다른 스레드 대기)
        addressRepository.clearDefaultAddress(memberId);

        // 2. 새로운 배송지를 true로 변경
        Address newDefaultAddress = addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        // 동시성 문제 발생 !!
        //addressRepository.findByMemberIdAndIsDefaultTrue(memberId)
        //        .ifPresent(address -> address.updateDefault(false));

        newDefaultAddress.updateDefault(true);
    }
}
