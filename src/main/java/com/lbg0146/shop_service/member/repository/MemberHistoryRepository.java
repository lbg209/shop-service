package com.lbg0146.shop_service.member.repository;

import com.lbg0146.shop_service.member.entity.MemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberHistoryRepository extends JpaRepository<MemberHistory, Long> {

    List<MemberHistory> findAllByMemberId(Long memberId);

    Optional<MemberHistory> findTopByMemberIdAndValidToIsNullOrderByValidFromDesc(Long memberId);

    @Modifying
    @Query("""
    UPDATE MemberHistory mh
    SET mh.validTo = :validTo
    WHERE mh.member.id = :memberId
    AND mh.validTo IS NULL
    """)
    int closeCurrentHistory(
            @Param("memberId") Long memberId,
            @Param("validTo") LocalDateTime validTo
    );


    @Modifying
    @Query("""
    UPDATE MemberHistory mh
    SET mh.validTo = :validTo
    WHERE mh.member.id IN :memberIds
      AND mh.validTo IS NULL
""")
    int closeCurrentHistories(
            @Param("memberIds") List<Long> memberIds,
            @Param("validTo") LocalDateTime validTo
    );
}
