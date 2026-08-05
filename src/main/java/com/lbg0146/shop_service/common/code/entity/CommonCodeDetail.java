package com.lbg0146.shop_service.common.code.entity;

import com.lbg0146.shop_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCodeDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_code", nullable = false)
    private CommonCodeGroup group;

    @Column(nullable = false, length = 50)
    private String codeValue;

    @Column(nullable = false, length = 100)
    private String codeName;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn;
}
