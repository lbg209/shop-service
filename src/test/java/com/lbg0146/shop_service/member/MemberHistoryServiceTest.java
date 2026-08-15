package com.lbg0146.shop_service.member;

import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.entity.MemberHistory;
import com.lbg0146.shop_service.member.repository.MemberHistoryRepository;
import com.lbg0146.shop_service.member.service.MemberHistoryService;
import com.lbg0146.shop_service.member.service.MemberService;
import com.lbg0146.shop_service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class MemberHistoryServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberHistoryService memberHistoryService;

    @Autowired
    private MemberHistoryRepository memberHistoryRepository;

    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @Test
    void 회원_이력을_저장하면_회원정보가_스냅샷으로_저장() {

        Member member = testDataFactory.createMember();

        memberHistoryService.saveHistory(
                member,
                "CREATE",
                null
        );

        List<MemberHistory> histories = memberHistoryRepository.findAllByMemberId(member.getId());

        MemberHistory history = histories.get(histories.size() - 1);

        assertThat(history.getMember().getId()).isEqualTo(member.getId());

        assertThat(history.getLoginId()).isEqualTo(member.getLoginId());

        assertThat(history.getName()).isEqualTo(member.getName());

        assertThat(history.getEmail()).isEqualTo(member.getEmail());

        assertThat(history.getPhone()).isEqualTo(member.getPhone());

        assertThat(history.getGrade().getId()).isEqualTo(member.getGrade().getId());

        assertThat(history.getChangeType().getCodeValue()).isEqualTo("CREATE");

        assertThat(history.getValidFrom()).isNotNull();

        assertThat(history.getValidTo()).isNull();
    }

    @Test
    void 회원_탈퇴시_기존_이력이_종료되고_DELETE_이력이_생성된다() {

        Member member = testDataFactory.createMember();

        memberHistoryService.saveHistory(
                member,
                "CREATE",
                null
        );

        memberService.deleteMember(member.getId());

        List<MemberHistory> histories = memberHistoryRepository.findAllByMemberId(member.getId());

        assertThat(histories).hasSize(2);

        MemberHistory previousHistory = histories.get(0);
        MemberHistory deleteHistory = histories.get(1);

        // 기존 이력 종료
        assertThat(previousHistory.getValidTo()).isNotNull();

        // DELETE 이력 생성
        assertThat(deleteHistory.getChangeType().getCodeValue()).isEqualTo("DELETE");

        assertThat(deleteHistory.getChangedBy()).isNull();

        assertThat(deleteHistory.getValidFrom()).isNotNull();

        assertThat(deleteHistory.getValidTo()).isNull();

        // 회원 Soft Delete
        assertThat(member.getDeletedAt()).isNotNull();
    }
}
