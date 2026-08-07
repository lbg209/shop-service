package com.lbg0146.shop_service.member.repository;

import com.lbg0146.shop_service.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);

    Optional<Member> findByLoginIdAndDeletedAtIsNull(String loginId);

    List<Member> findAllByDeletedAtIsNull();

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
