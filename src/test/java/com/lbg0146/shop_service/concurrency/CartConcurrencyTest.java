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

        // 장바구니와 장바구니 상품을 수량 0으로 미리 생성해 둡니다.
        Cart cart = testDataFactory.createCart(member);
        testDataFactory.createCartItem(cart, product, 0);

        CartItemCreateRequest request = new CartItemCreateRequest(product.getId(), 1);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when: 10번 동시에 장바구니 담기 요청
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
            log.error("[동시성 테스트 결과] 동시에 10번 담기를 요청했지만 덮어쓰기가 발생하여 최종 수량은 [{}]개 뿐입니다!", cartItem.getQuantity());
        } else {
            log.info("[동시성 테스트 결과] 10번 담기 요청이 정상적으로 처리되어 최종 수량이 [{}]개입니다.", cartItem.getQuantity());
        }

        // 10번 담았으므로 10이어야 하지만, 덮어쓰기 문제로 인해 10이 되지 않음을 확인
        assertThat(cartItem.getQuantity()).isNotEqualTo(10);
        executorService.shutdown();
    }
}
