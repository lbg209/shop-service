package com.lbg0146.shop_service.concurrency;

import com.lbg0146.shop_service.cart.repository.CartItemRepository;
import com.lbg0146.shop_service.cart.repository.CartRepository;
import com.lbg0146.shop_service.cart.service.CartService;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.order.dto.request.OrderCreateRequest;
import com.lbg0146.shop_service.order.dto.request.OrderItemRequest;
import com.lbg0146.shop_service.order.service.OrderService;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import com.lbg0146.shop_service.support.TestDataFactory;
import lombok.extern.slf4j.Slf4j;
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
public class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("동시성 문제 발생: 재고가 10개인 상품에 동시에 10번 주문을 요청하면 재고가 0이 되지 않는다.")
    void decreaseStockConcurrencyIssue() throws InterruptedException {

        Member member = testDataFactory.createMember();

        Category category = Category.createCategory("테스트카테고리", null);
        categoryRepository.save(category);

        Product product = Product.createProduct(
                category,
                "인기폭발 한정판 상품",
                10000L,
                10, // 재고 10개 세팅
                "테스트 상품"
        );

        productRepository.save(product);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 1)),
                "수령인", "010-1234-5678", "12345", "서울시", "어딘가", null
        );

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 10번 동시에 주문 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.createOrder(member.getId(), request);
                } catch (Exception e) {
                    log.error("주문 실패: ", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 최종 상품 재고 검증
        Product finalProduct = productRepository.findById(product.getId()).orElseThrow();

        if (finalProduct.getStockQuantity() != 0) {
            log.error("[동시성 테스트 결과] 동시에 10번 주문을 요청했지만 초과 판매(Lost Update)가 발생하여 남은 재고가 [{}]개 입니다!", finalProduct.getStockQuantity());
        } else {
            log.info("[동시성 테스트 결과] 10번의 주문 요청이 정상적으로 처리되어 최종 재고가 [{}]개입니다.", finalProduct.getStockQuantity());
        }

        // 10개 중 10개를 주문했으므로 0이어야 하지만 동시성 문제로 인해 0이 아님
        //assertThat(finalProduct.getStockQuantity()).isNotEqualTo(0);
        assertThat(finalProduct.getStockQuantity()).isEqualTo(0);

        executorService.shutdown();
    }
}
