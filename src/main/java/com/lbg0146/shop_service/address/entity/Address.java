package com.lbg0146.shop_service.address.entity;

import com.lbg0146.shop_service.common.entity.BaseEntity;
import com.lbg0146.shop_service.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, length = 50)
    private String addressName;

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 255)
    private String detailAddress;

    @Column(nullable = false)
    private boolean isDefault;

    public static Address createAddress(
            Member member,
            String addressName,
            String receiverName,
            String phone,
            String zipcode,
            String address,
            String detailAddress,
            boolean isDefault
    ) {
        Address addressEntity = new Address();

        addressEntity.member = member;
        addressEntity.addressName = addressName;
        addressEntity.receiverName = receiverName;
        addressEntity.phone = phone;
        addressEntity.zipcode = zipcode;
        addressEntity.address = address;
        addressEntity.detailAddress = detailAddress;
        addressEntity.isDefault = isDefault;

        return addressEntity;
    }

    public void update(
            String addressName,
            String receiverName,
            String phone,
            String zipcode,
            String address,
            String detailAddress
    ) {
        this.addressName = addressName;
        this.receiverName = receiverName;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    public void updateDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
