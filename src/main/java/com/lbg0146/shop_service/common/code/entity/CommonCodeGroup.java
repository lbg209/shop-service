package com.lbg0146.shop_service.common.code.entity;

import com.lbg0146.shop_service.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCodeGroup extends BaseEntity {
    @Id
    @Column(name = "group_code", length = 50)
    private String groupCode;

    @Column(nullable = false, length = 100)
    private String groupName;

    @Column(length = 500)
    private String description;

    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn;
}
