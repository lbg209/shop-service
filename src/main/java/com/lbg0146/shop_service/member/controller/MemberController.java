package com.lbg0146.shop_service.member.controller;

import com.lbg0146.shop_service.member.dto.request.MemberCreateRequest;
import com.lbg0146.shop_service.member.dto.request.MemberUpdateRequest;
import com.lbg0146.shop_service.member.dto.response.MemberResponse;
import com.lbg0146.shop_service.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<Long> createMember(@Valid @RequestBody MemberCreateRequest request) {

        Long memberId = memberService.join(request);

        return ResponseEntity.ok(memberId);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> findMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(
                memberService.findMember(memberId)
        );
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<Void> updateMember(@PathVariable Long memberId, @RequestBody MemberUpdateRequest request) {
        memberService.updateMember(memberId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {

        memberService.deleteMember(memberId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> findMembers() {

        return ResponseEntity.ok(
                memberService.findMembers()
        );
    }
}
