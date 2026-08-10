package com.lbg0146.shop_service.address;

import com.lbg0146.shop_service.address.dto.request.AddressCreateRequest;
import com.lbg0146.shop_service.address.dto.request.AddressUpdateRequest;
import com.lbg0146.shop_service.address.dto.response.AddressResponse;
import com.lbg0146.shop_service.address.service.AddressService;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AddressServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    AddressService addressService;

    @Test
    void 배송지_수정_성공() {

        Member member = testDataFactory.createMember();

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

        Member member = testDataFactory.createMember();

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

        addressService.changeDefaultAddress(member.getId(), companyId);

        AddressResponse home = addressService.findAddress(member.getId(), homeId);

        AddressResponse company = addressService.findAddress(member.getId(), companyId);


        assertThat(home.isDefault()).isFalse();

        assertThat(company.isDefault()).isTrue();
    }

    @Test
    void 배송지_삭제_성공() {

        Member member = testDataFactory.createMember();

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
}
