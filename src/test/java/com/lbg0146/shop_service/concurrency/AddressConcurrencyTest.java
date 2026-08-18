package com.lbg0146.shop_service.concurrency;

import com.lbg0146.shop_service.address.entity.Address;
import com.lbg0146.shop_service.address.repository.AddressRepository;
import com.lbg0146.shop_service.address.service.AddressService;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import com.lbg0146.shop_service.support.TestDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
public class AddressConcurrencyTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @AfterEach
    void tearDown() {
        addressRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시성 문제 발생: 두 개의 주소를 동시에 기본 배송지로 설정하면 둘 다 기본 배송지가 될 수 있다.")
    void changeDefaultAddressConcurrencyIssue() throws InterruptedException {
        // given: 테스트용 회원 생성
        Member member = testDataFactory.createMember();

        // 주소 2개 직접 생성 및 저장 (둘 다 기본 배송지가 아닌 상태로 시작)
        Address address1 = Address.createAddress(
                member, "집", "홍길동", "010-1111-2222", "12345", "서울시 강남구", "101호", false
        );
        Address address2 = Address.createAddress(
                member, "회사", "홍길동", "010-3333-4444", "54321", "서울시 서초구", "202호", false
        );
        addressRepository.saveAll(List.of(address1, address2));

        int threadCount = 2; // 동시에 2개의 스레드가 각각 다른 주소를 기본 배송지로 요청
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when: 서로 다른 주소를 "동시에" 기본 배송지로 변경 요청
        executorService.submit(() -> {
            try {
                addressService.changeDefaultAddress(member.getId(), address1.getId()); //
            } catch (Exception e) {
                log.error("주소1 기본 배송지 변경 실패: ", e);
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                addressService.changeDefaultAddress(member.getId(), address2.getId()); //[cite: 2]
            } catch (Exception e) {
                log.error("주소2 기본 배송지 변경 실패: ", e);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        // then: 해당 회원의 전체 배송지를 불러와서 기본 배송지(isDefault = true)인 것만 필터링
        List<Address> defaultAddresses = addressRepository.findAllByMemberId(member.getId())
                .stream()
                .filter(Address::isDefault) // isDefault가 true인 것만 남김
                .toList();

        if (defaultAddresses.size() > 1) {
            log.error("🚨 [동시성 테스트 결과] 기본 배송지가 유일해야 하지만 현재 [{}]개의 기본 배송지가 존재합니다!", defaultAddresses.size());
        } else {
            log.info("✅ [동시성 테스트 결과] 1개의 기본 배송지만 존재합니다.");
        }

        // 기본 배송지가 1개여야 하지만 동시성 문제로 2개가 되어 테스트 실패 재현 (초록불이 들어오면 문제 재현 성공!)
        //assertThat(defaultAddresses).hasSizeGreaterThan(1);
        assertThat(defaultAddresses).hasSize(1);
    }
}
