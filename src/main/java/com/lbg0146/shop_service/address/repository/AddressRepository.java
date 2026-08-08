package com.lbg0146.shop_service.address.repository;

import com.lbg0146.shop_service.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByIdAndMemberId(Long addressId, Long memberId);

    List<Address> findAllByMemberId(Long memberId);

    Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId);
}
