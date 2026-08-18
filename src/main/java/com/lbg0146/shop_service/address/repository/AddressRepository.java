package com.lbg0146.shop_service.address.repository;

import com.lbg0146.shop_service.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByIdAndMemberId(Long addressId, Long memberId);

    List<Address> findAllByMemberId(Long memberId);

    Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId);

    // 벌크 연산을 통한 기본 배송지 해제 (DB 락 자동 획득)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.member.id = :memberId AND a.isDefault = true")
    void clearDefaultAddress(@Param("memberId") Long memberId);
}
