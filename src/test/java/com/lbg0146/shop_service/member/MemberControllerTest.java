package com.lbg0146.shop_service.member;

import com.lbg0146.shop_service.member.dto.request.MemberCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void 회원가입_API_성공() throws Exception {

        String uniqueId = UUID.randomUUID().toString();
        String loginId = "controller_" + uniqueId;
        String email = "test_" + uniqueId + "@test.com";
        String phone = "010" + String.format("%08d", System.nanoTime() % 100_000_000);

        MemberCreateRequest request = new MemberCreateRequest(
                loginId,
                "12345678",
                "테스트",
                "닉네임",
                email,
                phone
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

        String uniqueId = UUID.randomUUID().toString();

        String loginId = "testUser_" + uniqueId;
        String email = "test_" + uniqueId + "@test.com";
        String phone = "010" + String.format("%08d", System.nanoTime() % 100_000_000);

        MemberCreateRequest request = new MemberCreateRequest(
                loginId,
                "12345678",
                "테스트",
                "닉네임",
                email,
                phone
        );

        mockMvc.perform(
                        post("/api/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        MemberCreateRequest duplicateRequest = new MemberCreateRequest(
                loginId,                       // 같은 ID
                "12345678",
                "테스트2",
                "닉네임2",
                "other_" + uniqueId + "@test.com",
                "010" + String.format(
                        "%08d",
                        (System.nanoTime() + 1) % 100_000_000
                )
        );

        mockMvc.perform(
                        post("/api/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateRequest))
                )
                .andExpect(status().isConflict());
    }
}
