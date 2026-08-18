package com.lbg0146.shop_service.nPlusOne;

import com.lbg0146.shop_service.cart.entity.Cart;
import com.lbg0146.shop_service.cart.repository.CartItemRepository;
import com.lbg0146.shop_service.cart.repository.CartRepository;
import com.lbg0146.shop_service.cart.service.CartService;
import com.lbg0146.shop_service.common.enums.Role;
import com.lbg0146.shop_service.delivery.repository.DeliveryRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.grade.entity.Grade;
import com.lbg0146.shop_service.grade.repository.GradeRepository;
import com.lbg0146.shop_service.member.dto.response.MemberResponse;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.member.service.MemberService;
import com.lbg0146.shop_service.order.dto.response.OrderDetailResponse;
import com.lbg0146.shop_service.order.dto.response.OrderResponse;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.repository.OrderItemRepository;
import com.lbg0146.shop_service.order.repository.OrderRepository;
import com.lbg0146.shop_service.order.repository.OrderStatusHistoryRepository;
import com.lbg0146.shop_service.order.service.OrderService;
import com.lbg0146.shop_service.payment.repository.PaymentRepository;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import com.lbg0146.shop_service.support.TestDataFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
public class NPlusOneTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void tearDown() {
        orderStatusHistoryRepository.deleteAllInBatch();
        orderItemRepository.deleteAllInBatch();

        paymentRepository.deleteAllInBatch();
        deliveryRepository.deleteAllInBatch();

        orderRepository.deleteAllInBatch();

        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();

        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("N+1 문제 확인: 장바구니에 5개의 다른 상품이 있을 때, 조회 시 Product 쿼리가 5번 추가로 나간다.")
    // 여기서는 영속성 컨텍스트를 위해 Transactional을 붙입니다.
    @Transactional
    void cartNPlusOneIssue() {
        // given
        Member member = testDataFactory.createMember();
        Cart cart = testDataFactory.createCart(member);
        Category category = Category.createCategory("테스트카테고리", null);
        categoryRepository.save(category);

        // 5개의 서로 다른 상품을 생성하고 장바구니에 담음
        for (int i = 1; i <= 5; i++) {
            Product product = Product.createProduct(category, "상품" + i, 1000L, 100, "설명");
            productRepository.save(product);
            testDataFactory.createCartItem(cart, product, 1);
        }

        // 💡 핵심 포인트: 실제 서비스 환경과 동일하게 만들기 위해
        // 1차 캐시(메모리)에 쌓인 것들을 DB에 강제로 밀어넣고(flush) 메모리를 비웁니다(clear).
        em.flush();
        em.clear();

        log.info("================= [장바구니 조회 시작] =================");

        // when: 장바구니 조회 메서드 호출
        cartService.getCart(member.getId()); //[cite: 2]

        log.info("================= [장바구니 조회 종료] =================");

        // then: 콘솔 로그에서 "장바구니 조회 시작"과 "종료" 사이에
        // select product ... 쿼리가 5번 찍히는지 눈으로 확인합니다!
    }

    @Test
    @DisplayName("N+1 문제 확인: members를 조회할때 grade 조회 쿼리가 추가로 나간다")
    // 여기서는 영속성 컨텍스트를 위해 Transactional을 붙입니다.
    @Transactional
    void memberNPlusOneIssue() {
        Grade basicGrade = gradeRepository.findByGradeCode("BASIC")
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADE_NOT_FOUND));

        // given
        for (int i = 1; i <= 5; i++) {
            Member member = Member.createMember(basicGrade, "login" + i, "12345678", "testname" + i, "test" + i, "test@test.com" + i, "0101234567" + i, Role.USER);
            memberRepository.save(member);

        }

        // 💡 핵심 포인트: 실제 서비스 환경과 동일하게 만들기 위해
        // 1차 캐시(메모리)에 쌓인 것들을 DB에 강제로 밀어넣고(flush) 메모리를 비웁니다(clear).
        em.flush();
        em.clear();

        System.out.println("================= [member 조회 시작] =================");

        // when: 장바구니 조회 메서드 호출
        List<MemberResponse> members = memberService.findMembers();

        System.out.println("================= [member 조회 종료] =================");

        // then: 콘솔 로그에서 "장바구니 조회 시작"과 "종료" 사이에
        // select product ... 쿼리가 5번 찍히는지 눈으로 확인합니다!
    }

    @Test
    @DisplayName("N+1 문제 확인: 주문 목록 조회 시 주문마다 OrderItem 조회 쿼리가 추가로 발생한다.")
    @Transactional
    void orderNPlusOneIssue() {
        // given
        Member member = testDataFactory.createMember();

        for (int i = 0; i < 5; i++) {
            testDataFactory.createOrder(member);
        }

        em.flush();
        em.clear();

        log.info("================= [주문 목록 조회 시작] =================");

        // when
        List<OrderResponse> orders = orderService.findOrders(member.getId());

        log.info("================= [주문 목록 조회 종료] =================");

        // then
        assertThat(orders).hasSize(5);
    }

    @Test
    @Transactional
    void orderDetailNPlusOneIssue() {

        // given
        Member member = testDataFactory.createMember();
        Order order = testDataFactory.createOrder(member);

        testDataFactory.createDelivery(order);
        testDataFactory.createPayment(order);

        em.flush();
        em.clear();

        log.info("================= [주문 상세 조회 시작] =================");

        // when
        OrderDetailResponse response =
                orderService.findOrderDetail(
                        order.getMember().getId(),
                        order.getId()
                );

        log.info("================= [주문 상세 조회 종료] =================");

        // then
        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.items()).hasSize(1);
        assertThat(response.delivery()).isNotNull();
        assertThat(response.payment()).isNotNull();
    }

}
