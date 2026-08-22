package com.lbg0146.shop_service.member.service;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.entity.MemberHistory;
import com.lbg0146.shop_service.member.repository.MemberHistoryBulkRepository;
import com.lbg0146.shop_service.member.repository.MemberHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberHistoryService {

    private final MemberHistoryRepository memberHistoryRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;
    private final MemberHistoryBulkRepository memberHistoryBulkRepository;

    @Transactional
    public void saveHistory(Member member, CommonCodeDetail changeTypeCode, Member changedBy) {

        LocalDateTime now = LocalDateTime.now();

        MemberHistory history = MemberHistory.create(
                member,
                changeTypeCode,
                member.getLoginId(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getRole().name(),
                member.getGrade(),
                changedBy,
                now,
                null
        );

        memberHistoryRepository.save(history);
    }

    @Transactional
    public void testsaveHistory(Member member, String changeTypeCode, Member changedBy) {

        CommonCodeDetail changeType = commonCodeDetailRepository
                .findByGroupGroupCodeAndCodeValue(
                        "HISTORY_CHANGE_TYPE",
                        changeTypeCode
                ).orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_CHANGE_TYPE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        MemberHistory history = MemberHistory.create(
                member,
                changeType,
                member.getLoginId(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getRole().name(),
                member.getGrade(),
                changedBy,
                now,
                null
        );

        memberHistoryRepository.save(history);
    }

    @Transactional
    public void closeCurrentHistory(Long memberId, LocalDateTime validTo) {

        memberHistoryRepository.findTopByMemberIdAndValidToIsNullOrderByValidFromDesc(memberId)
                .ifPresent(history -> history.close(validTo));
    }
}
