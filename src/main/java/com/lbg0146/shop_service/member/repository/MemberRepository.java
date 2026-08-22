package com.lbg0146.shop_service.member.repository;

import com.lbg0146.shop_service.grade.entity.Grade;
import com.lbg0146.shop_service.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
    SELECT m
    FROM Member m
    JOIN FETCH m.grade
    WHERE m.deletedAt IS NULL
    """)
    List<Member> findAllByDeletedAtIsNullWithGrade();

    @Modifying
    @Query("""
    UPDATE Member m
    SET m.grade = :grade
    WHERE m.id IN :memberIds
    """)
    int updateGradeByIds(
            @Param("memberIds") List<Long> memberIds,
            @Param("grade") Grade grade
    );
}
