package com.lbg0146.shop_service.member.service;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.common.enums.Role;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.grade.entity.Grade;
import com.lbg0146.shop_service.grade.repository.GradeRepository;
import com.lbg0146.shop_service.member.dto.request.MemberCreateRequest;
import com.lbg0146.shop_service.member.dto.request.MemberUpdateRequest;
import com.lbg0146.shop_service.member.dto.response.MemberResponse;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final GradeRepository gradeRepository;
    private final MemberHistoryService memberHistoryService;
    private final CommonCodeDetailRepository commonCodeDetailRepository;

    @Transactional
    public Long join(MemberCreateRequest request) {

        validateDuplicate(request);

        Grade basicGrade = gradeRepository.findByGradeCode("BASIC")
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADE_NOT_FOUND));

        Member member = Member.createMember(
                basicGrade,
                request.loginId(),
                request.password(),
                request.name(),
                request.nickname(),
                request.email(),
                request.phone(),
                Role.USER
        );

        CommonCodeDetail createCode = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue("HISTORY_CHANGE_TYPE", "CREATE")
                .orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_CHANGE_TYPE_NOT_FOUND));

        Member saveMember = memberRepository.save(member);

        memberHistoryService.saveHistory(
                saveMember,
                createCode,
                null
        );

        return saveMember.getId();
    }

    private void validateDuplicate(MemberCreateRequest request) {

        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (memberRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }
    }

    public MemberResponse findMember(Long memberId) {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponse.from(member);
    }

    @Transactional
    public void updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        memberHistoryService.closeCurrentHistory(
                memberId,
                LocalDateTime.now()
        );

        member.update(request.nickname(), request.phone());

        CommonCodeDetail updateCode = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue("HISTORY_CHANGE_TYPE", "UPDATE")
                .orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_CHANGE_TYPE_NOT_FOUND));

        memberHistoryService.saveHistory(
                member,
                updateCode,
                null
        );
    }

    @Transactional
    public void deleteMember(Long memberId) {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        memberHistoryService.closeCurrentHistory(memberId, now);

        member.delete();

        CommonCodeDetail deleteCode = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue("HISTORY_CHANGE_TYPE", "DELETE")
                .orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_CHANGE_TYPE_NOT_FOUND));

        memberHistoryService.saveHistory(
                member,
                deleteCode,
                null
        );
    }

    // N + 1 발생 !!
    public List<MemberResponse> findMembers() {

        /*
        return memberRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(MemberResponse::from)
                .toList();
         */

        return memberRepository.findAllByDeletedAtIsNullWithGrade()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }
}
