package com.lbg0146.shop_service.grade.entity;

import com.lbg0146.shop_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String gradeCode;

    @Column(nullable = false, length = 50)
    private String gradeName;

    @Column(
            precision = 10,
            scale = 0,
            nullable = false
    )
    private BigDecimal minPurchaseAmount;

    @Column(nullable = false)
    private Integer discountRate;

    public long calculateDiscount(long totalPrice) {

        return totalPrice * discountRate / 100;
    }
}
