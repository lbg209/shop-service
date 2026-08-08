package com.lbg0146.shop_service.member;

import com.lbg0146.shop_service.member.dto.request.MemberCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void 회원가입_API_성공() throws Exception {

        MemberCreateRequest request = new MemberCreateRequest(
                "testUser",
                "12345678",
                "테스트",
                "닉네임",
                "test@test.com",
                "01077777777"
        );

        mockMvc.perform(
                        post("/api/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void 중복_회원가입_API_실패() throws Exception {

        MemberCreateRequest request = new MemberCreateRequest(
                "testUser",
                "12345678",
                "테스트",
                "닉네임",
                "test@test.com",
                "01088888888"
        );

        // 먼저 가입
        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // 같은 loginId로 다시 가입
        MemberCreateRequest duplicateRequest = new MemberCreateRequest(
                "testUser",
                "12345678",
                "테스트2",
                "닉네임2",
                "test2@test.com",
                "01099999999"
        );

        mockMvc.perform(
                        post("/api/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateRequest))
                )
                .andExpect(status().isBadRequest());
    }
}
