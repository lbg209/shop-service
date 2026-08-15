package com.lbg0146.shop_service.member;

import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.member.dto.request.MemberCreateRequest;
import com.lbg0146.shop_service.member.dto.request.MemberUpdateRequest;
import com.lbg0146.shop_service.member.dto.response.MemberResponse;
import com.lbg0146.shop_service.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Test
    void 회원가입_성공() {

        MemberCreateRequest request = new MemberCreateRequest(
                "testUser1",
                "1234",
                "테스트1",
                "테스터1",
                "test1@test.com",
                "01011123411"
        );

        Long memberId = memberService.join(request);

        MemberResponse member = memberService.findMember(memberId);

        assertThat(member.loginId()).isEqualTo("testUser1");
        assertThat(member.nickname()).isEqualTo("테스터1");
        assertThat(member.gradeName()).isEqualTo("일반회원");
    }

    @Test
    void 중복_로그인아이디_회원가입_실패() {

        MemberCreateRequest request = new MemberCreateRequest(
                "testUser3",
                "1234",
                "테스트3",
                "테스터3",
                "test3@test.com",
                "01013331111"
        );

        memberService.join(request);

        MemberCreateRequest duplicateRequest = new MemberCreateRequest(
                "testUser3",
                "5678",
                "테스트3",
                "테스터3",
                "test3@test.com",
                "01022562222"
        );

        assertThatThrownBy(() -> memberService.join(duplicateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 존재하는 아이디입니다.");
    }

    @Test
    void 탈퇴한_회원은_조회_불가능() {

        MemberCreateRequest request = new MemberCreateRequest(
                "testUser4",
                "1234",
                "탈퇴4",
                "탈퇴자4",
                "delete@test.com",
                "01033421333"
        );

        Long memberId = memberService.join(request);

        memberService.deleteMember(memberId);

        assertThatThrownBy(() -> memberService.findMember(memberId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("회원을 찾을 수 없습니다.");
    }

    @Test
    void 회원정보_수정_성공() {

        MemberCreateRequest request = new MemberCreateRequest(
                "testUser5",
                "1234",
                "수정테스트5",
                "닉네임5",
                "update@test.com",
                "01044514214"
        );

        Long memberId = memberService.join(request);

        MemberUpdateRequest updateRequest = new MemberUpdateRequest(
                "변경닉네임",
                "01099999999"
        );

        memberService.updateMember(memberId, updateRequest);

        MemberResponse response = memberService.findMember(memberId);

        assertThat(response.nickname()).isEqualTo("변경닉네임");

        assertThat(response.phone()).isEqualTo("01099999999");
    }

    @Test
    void 회원목록_조회_탈퇴회원_제외() {

        Long member1 = memberService.join(
                new MemberCreateRequest(
                        "userA",
                        "1234",
                        "회원A",
                        "닉A",
                        "a@test.com",
                        "01055555555"
                )
        );

        Long member2 = memberService.join(
                new MemberCreateRequest(
                        "userB",
                        "1234",
                        "회원B",
                        "닉B",
                        "b@test.com",
                        "01066666666"
                )
        );

        memberService.deleteMember(member1);

        List<MemberResponse> members = memberService.findMembers();

        assertThat(members)
                .extracting(MemberResponse::loginId)
                .contains("userB")
                .doesNotContain("userA");
    }
}
