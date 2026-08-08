package com.lbg0146.shop_service.address;

import com.lbg0146.shop_service.address.dto.request.AddressCreateRequest;
import com.lbg0146.shop_service.address.dto.request.AddressUpdateRequest;
import com.lbg0146.shop_service.address.dto.response.AddressResponse;
import com.lbg0146.shop_service.address.service.AddressService;
import com.lbg0146.shop_service.common.enums.Role;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.grade.entity.Grade;
import com.lbg0146.shop_service.grade.repository.GradeRepository;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
public class AddressServiceTest {

    @Autowired
    AddressService addressService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    GradeRepository gradeRepository;

    private static int count = 0;

    private Member createMember() {

        count++;

        Grade grade = gradeRepository.findByGradeCode("BASIC")
                .orElseThrow();

        Member member = Member.createMember(
                grade,
                "testUser" + count,
                "1234",
                "테스트",
                "닉네임" + count,
                "test" + count + "@test.com",
                "0101111111" + count,
                Role.USER
        );

        return memberRepository.save(member);
    }

    @Test
    void 배송지_등록_성공() {

        Member member = createMember();

        AddressCreateRequest request = new AddressCreateRequest(
                "집",
                "호날두",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101동",
                true
        );

        Long addressId = addressService.createAddress(
                member.getId(),
                request
        );

        AddressResponse response = addressService.findAddress(member.getId(), addressId);

        assertThat(response.addressName()).isEqualTo("집");

        assertThat(response.isDefault()).isTrue();
    }

    @Test
    void 새로운_대표배송지를_등록하면_기존대표는_해제() {

        Member member = createMember();

        AddressCreateRequest first = new AddressCreateRequest(
                "집",
                "호날두",
                "01011111111",
                "12345",
                "서울",
                "101동",
                true
        );

        Long firstId = addressService.createAddress(
                member.getId(),
                first
        );

        AddressCreateRequest second = new AddressCreateRequest(
                "회사",
                "호날두",
                "01022222222",
                "54321",
                "경기",
                "202호",
                true
        );

        Long secondId = addressService.createAddress(
                member.getId(),
                second
        );

        // when
        AddressResponse firstAddress =
                addressService.findAddress(member.getId(), firstId);

        AddressResponse secondAddress =
                addressService.findAddress(member.getId(), secondId);

        // then
        assertThat(firstAddress.isDefault())
                .isFalse();

        assertThat(secondAddress.isDefault())
                .isTrue();
    }

    @Test
    void 배송지_수정_성공() {

        Member member = createMember();

        AddressCreateRequest createRequest = new AddressCreateRequest(
                "집",
                "호날두",
                "01011111111",
                "12345",
                "서울",
                "101동",
                true
        );

        Long addressId = addressService.createAddress(
                member.getId(),
                createRequest
        );

        AddressUpdateRequest updateRequest = new AddressUpdateRequest(
                "회사",
                "김철수",
                "01099999999",
                "54321",
                "경기도",
                "202호"
        );

        addressService.updateAddress(
                member.getId(),
                addressId,
                updateRequest
        );

        AddressResponse response =
                addressService.findAddress(member.getId(), addressId);

        assertThat(response.addressName()).isEqualTo("회사");

        assertThat(response.receiverName()).isEqualTo("김철수");

        assertThat(response.phone()).isEqualTo("01099999999");
    }

    @Test
    void 대표배송지_변경_성공() {

        Member member = createMember();

        Long homeId = addressService.createAddress(
                member.getId(),
                new AddressCreateRequest(
                        "집",
                        "홍길동",
                        "01011111111",
                        "12345",
                        "서울",
                        "101동",
                        true
                )
        );


        Long companyId = addressService.createAddress(
                member.getId(),
                new AddressCreateRequest(
                        "회사",
                        "홍길동",
                        "01022222222",
                        "54321",
                        "경기",
                        "202호",
                        false
                )
        );

        addressService.changeDefaultAddress(
                member.getId(),
                companyId
        );

        AddressResponse home = addressService.findAddress(member.getId(), homeId);

        AddressResponse company = addressService.findAddress(member.getId(), companyId);


        assertThat(home.isDefault()).isFalse();

        assertThat(company.isDefault()).isTrue();
    }

    @Test
    void 배송지_삭제_성공() {
        Member member = createMember();

        Long addressId = addressService.createAddress(
                member.getId(),
                new AddressCreateRequest(
                        "집",
                        "홍길동",
                        "01011111111",
                        "12345",
                        "서울",
                        "101동",
                        true
                )
        );

        addressService.deleteAddress(member.getId(), addressId);

        assertThatThrownBy(() -> addressService.findAddress(member.getId(), addressId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 다른회원_배송지_조회_실패() {

        Member memberA = createMember();

        Member memberB = createMember();

        Long addressId = addressService.createAddress(
                memberB.getId(),
                new AddressCreateRequest(
                        "집",
                        "홍길동",
                        "01011111111",
                        "12345",
                        "서울",
                        "101동",
                        true
                )
        );

        assertThatThrownBy(() -> addressService.findAddress(memberA.getId(), addressId))
                .isInstanceOf(BusinessException.class);
    }
}
