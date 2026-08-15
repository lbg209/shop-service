package com.lbg0146.shop_service.member.entity;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.entity.BaseHistoryEntity;
import com.lbg0146.shop_service.grade.entity.Grade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberHistory extends BaseHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_type_code_id", nullable = false)
    private CommonCodeDetail changeType;

    @Column(nullable = false, length = 50)
    private String loginId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 20)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private Member changedBy;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    public static MemberHistory create(
            Member member,
            CommonCodeDetail changeType,
            String loginId,
            String name,
            String email,
            String phone,
            String role,
            Grade grade,
            Member changedBy,
            LocalDateTime validFrom,
            LocalDateTime validTo
    ) {
        MemberHistory history = new MemberHistory();
        history.member = member;
        history.changeType = changeType;
        history.loginId = loginId;
        history.name = name;
        history.email = email;
        history.phone = phone;
        history.role = role;
        history.grade = grade;
        history.changedBy = changedBy;
        history.validFrom = validFrom;
        history.validTo = validTo;

        return history;
    }

    public void close(LocalDateTime validTo) {
        this.validTo = validTo;
    }
}