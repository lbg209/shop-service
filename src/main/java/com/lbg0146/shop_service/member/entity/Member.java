package com.lbg0146.shop_service.member.entity;

import com.lbg0146.shop_service.common.entity.BaseEntity;
import com.lbg0146.shop_service.common.enums.Role;
import com.lbg0146.shop_service.grade.entity.Grade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Member createMember(
            Grade grade,
            String loginId,
            String password,
            String name,
            String nickname,
            String email,
            String phone,
            Role role
    ) {
        Member member = new Member();

        member.grade = grade;
        member.loginId = loginId;
        member.password = password;
        member.name = name;
        member.nickname = nickname;
        member.email = email;
        member.phone = phone;
        member.role = role;

        return member;
    }

    public void update(
            String nickname,
            String phone
    ) {
        if (nickname != null) {
            this.nickname = nickname;
        }

        if (phone != null) {
            this.phone = phone;
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
