package com.lbg0146.shop_service.address;

import com.lbg0146.shop_service.address.dto.request.AddressCreateRequest;
import com.lbg0146.shop_service.address.dto.request.AddressUpdateRequest;
import com.lbg0146.shop_service.address.entity.Address;
import com.lbg0146.shop_service.address.repository.AddressRepository;
import com.lbg0146.shop_service.common.enums.Role;
import com.lbg0146.shop_service.grade.entity.Grade;
import com.lbg0146.shop_service.grade.repository.GradeRepository;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AddressControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    AddressRepository addressRepository;

    private Member createMember() {

        Grade grade = gradeRepository.findByGradeCode("BASIC")
                .orElseThrow();

        Member member = Member.createMember(
                grade,
                "controllerUser",
                "1234",
                "컨트롤러테스트",
                "닉네임",
                "controller@test.com",
                "01011111111",
                Role.USER
        );

        return memberRepository.save(member);
    }

    @Test
    void 배송지_등록_API_성공() throws Exception {

        Member member = createMember();

        AddressCreateRequest request = new AddressCreateRequest(
                "집",
                "홍길동",
                "01012345678",
                "12345",
                "서울 강남구",
                "101동",
                true
        );

        mockMvc.perform(
                        post("/api/members/{memberId}/addresses", member.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void 배송지_조회_API_성공() throws Exception {

        Member member = createMember();

        Address address = addressRepository.save(
                Address.createAddress(
                        member,
                        "집",
                        "테스트",
                        "01012345678",
                        "12345",
                        "서울 강남구",
                        "101동",
                        true
                )
        );

        mockMvc.perform(
                        get("/api/members/{memberId}/addresses/{addressId}",
                                member.getId(),
                                address.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId")
                        .value(address.getId()))
                    .andExpect(jsonPath("$.addressName")
                            .value("집"))
                    .andExpect(jsonPath("$.receiverName")
                            .value("홍길동"))
                    .andExpect(jsonPath("$.isDefault")
                            .value(true));
        }

    @Test
    void 배송지_목록_조회_API_성공() throws Exception {

        Member member = createMember();

        addressRepository.save(
                Address.createAddress(
                        member,
                        "집",
                        "테스트",
                        "01011111111",
                        "12345",
                        "서울",
                        "101동",
                        true
                )
        );

        addressRepository.save(
                Address.createAddress(
                        member,
                        "회사",
                        "테스트",
                        "01022222222",
                        "54321",
                        "경기",
                        "202호",
                        false
                )
        );

        mockMvc.perform(
                        get("/api/members/{memberId}/addresses",
                                member.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].addressName")
                        .value("집"))
                .andExpect(jsonPath("$[1].addressName")
                        .value("회사"));
    }

    @Test
    void 배송지_수정_API_성공() throws Exception {

        Member member = createMember();

        Address address = addressRepository.save(
                Address.createAddress(
                        member,
                        "집",
                        "테스트",
                        "01011111111",
                        "12345",
                        "서울",
                        "101동",
                        true
                )
        );

        AddressUpdateRequest request = new AddressUpdateRequest(
                "회사",
                "호날두",
                "01099999999",
                "54321",
                "경기",
                "202호"
        );

        mockMvc.perform(
                        put("/api/members/{memberId}/addresses/{addressId}",
                                member.getId(),
                                address.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        Address updatedAddress = addressRepository.findById(address.getId()).orElseThrow();

        assertThat(updatedAddress.getAddressName()).isEqualTo("회사");

        assertThat(updatedAddress.getReceiverName()).isEqualTo("호날두");
    }

    @Test
    void 대표배송지_변경_API_성공() throws Exception {

        Member member = createMember();

        Address home = addressRepository.save(
                Address.createAddress(
                        member,
                        "집",
                        "테스트",
                        "01011111111",
                        "12345",
                        "서울",
                        "101동",
                        true
                )
        );

        Address company = addressRepository.save(
                Address.createAddress(
                        member,
                        "회사",
                        "테스트",
                        "01022222222",
                        "54321",
                        "경기",
                        "202호",
                        false
                )
        );

        mockMvc.perform(
                        patch("/api/members/{memberId}/addresses/{addressId}/default",
                                member.getId(),
                                company.getId())
                )
                .andExpect(status().isOk());

        Address updatedHome = addressRepository.findById(home.getId()).orElseThrow();

        Address updatedCompany = addressRepository.findById(company.getId()).orElseThrow();

        assertThat(updatedHome.isDefault()).isFalse();

        assertThat(updatedCompany.isDefault()).isTrue();
    }
}
