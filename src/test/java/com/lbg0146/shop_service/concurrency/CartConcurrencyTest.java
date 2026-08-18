package com.lbg0146.shop_service.concurrency;

import com.lbg0146.shop_service.cart.dto.request.CartItemCreateRequest;
import com.lbg0146.shop_service.cart.entity.Cart;
import com.lbg0146.shop_service.cart.entity.CartItem;
import com.lbg0146.shop_service.cart.repository.CartItemRepository;
import com.lbg0146.shop_service.cart.repository.CartRepository;
import com.lbg0146.shop_service.cart.service.CartService;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import com.lbg0146.shop_service.support.TestDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
public class CartConcurrencyTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        // @Transactional을 제거했으므로, 다음 테스트에 영향을 주지 않도록 수동으로 데이터를 비워줍니다.
        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시성 문제 발생: 장바구니 담기를 동시에 10번 요청하면 수량이 10이 되지 않는다.")
    void addItemConcurrencyIssue() throws InterruptedException {
        // given: 테스트용 회원 및 상품 세팅
        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        // 💡 핵심 포인트: 장바구니와 장바구니 상품을 '수량 0'으로 미리 생성해 둡니다.
        // 이렇게 해야 다중 INSERT로 인한 오류 없이 순수하게 UPDATE(수량 더하기) 동시성 문제만 테스트할 수 있습니다.
        Cart cart = testDataFactory.createCart(member);
        testDataFactory.createCartItem(cart, product, 0);

        CartItemCreateRequest request = new CartItemCreateRequest(product.getId(), 1);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when: 10번 동시에 장바구니 담기 요청 (기존 수량 + 1)
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    cartService.addItem(member.getId(), request);
                } catch (Exception e) {
                    log.error("장바구니 담기 실패: ", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then: 최종 수량 검증
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId()).orElseThrow();

        if (cartItem.getQuantity() != 10) {
            log.error("🚨 [동시성 테스트 결과] 동시에 10번 담기를 요청했지만 덮어쓰기가 발생하여 최종 수량은 [{}]개 뿐입니다!", cartItem.getQuantity());
        } else {
            log.info("✅ [동시성 테스트 결과] 10번 담기 요청이 정상적으로 처리되어 최종 수량이 [{}]개입니다.", cartItem.getQuantity());
        }

        // 10번 담았으므로 10이어야 하지만, 덮어쓰기 문제로 인해 10이 되지 않음을 확인 (초록불이 들어와야 정상적인 실패 상태입니다!)
        assertThat(cartItem.getQuantity()).isNotEqualTo(10);
    }
}
