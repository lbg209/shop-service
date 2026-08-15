package com.lbg0146.shop_service.order;

import com.lbg0146.shop_service.cart.entity.Cart;
import com.lbg0146.shop_service.cart.entity.CartItem;
import com.lbg0146.shop_service.cart.repository.CartItemRepository;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.order.dto.request.OrderCreateRequest;
import com.lbg0146.shop_service.order.dto.request.OrderItemRequest;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.entity.OrderItem;
import com.lbg0146.shop_service.order.entity.OrderStatusHistory;
import com.lbg0146.shop_service.order.repository.OrderItemRepository;
import com.lbg0146.shop_service.order.repository.OrderRepository;
import com.lbg0146.shop_service.order.repository.OrderStatusHistoryRepository;
import com.lbg0146.shop_service.order.service.OrderService;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class OrderServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Test
    void 단건_주문이_정상적으로_생성() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        int beforeStock = product.getStockQuantity();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        Long orderId = orderService.createOrder(
                member.getId(),
                request
        );

        Order order = orderRepository.findById(orderId).orElseThrow();

        assertThat(order.getMember().getId()).isEqualTo(member.getId());

        assertThat(order.getTotalPrice()).isEqualTo(product.getPrice() * 2);

        List<OrderItem> orderItems = orderItemRepository.findAll();

        assertThat(orderItems).hasSize(1);

        assertThat(orderItems.get(0).getProduct().getId()).isEqualTo(product.getId());

        assertThat(orderItems.get(0).getQuantity()).isEqualTo(2);

        assertThat(product.getStockQuantity()).isEqualTo(beforeStock - 2);
    }

    @Test
    void 재고보다_많이_주문하면_실패() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        int stock = product.getStockQuantity();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), stock + 1)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        assertThatThrownBy(() -> orderService.createOrder(member.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("재고가 부족합니다.");
    }
    @Test
    void 정률_쿠폰을_적용하면_할인된_최종금액으로_주문() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();
        Coupon coupon = testDataFactory.createRateCoupon();

        // 회원에게 쿠폰 발급
        MemberCoupon memberCoupon = testDataFactory.createMemberCoupon(member, coupon);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                memberCoupon.getId()
        );

        Long orderId = orderService.createOrder(member.getId(), request);

        Order order = orderRepository.findById(orderId).orElseThrow();

        long totalPrice = product.getPrice() * 2;
        long expectedDiscount = totalPrice * 10 / 100;
        long expectedFinalPrice = totalPrice - expectedDiscount;

        // 원래 주문 금액 검증
        assertThat(order.getTotalPrice()).isEqualTo(totalPrice);

        // 할인 금액 검증
        assertThat(order.getCouponDiscountAmount()).isEqualTo(expectedDiscount);

        // 최종 금액 검증
        assertThat(order.getFinalPrice()).isEqualTo(expectedFinalPrice);
    }

    @Test
    void 정액_쿠폰을_적용하면_할인된_최종금액으로_주문() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();
        Coupon coupon = testDataFactory.createAmountCoupon();

        MemberCoupon memberCoupon = testDataFactory.createMemberCoupon(member, coupon);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                memberCoupon.getId()
        );

        Long orderId = orderService.createOrder(member.getId(), request);

        Order order = orderRepository.findById(orderId).orElseThrow();

        long totalPrice = product.getPrice() * 2;
        long expectedDiscount = 5000L;
        long expectedFinalPrice = totalPrice - expectedDiscount;

        assertThat(order.getTotalPrice()).isEqualTo(totalPrice);

        assertThat(order.getCouponDiscountAmount()).isEqualTo(expectedDiscount);

        assertThat(order.getFinalPrice()).isEqualTo(expectedFinalPrice);
    }

    @Test
    void 최소_주문금액보다_작으면_쿠폰_적용_실패() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        Coupon coupon = testDataFactory.createRateCouponWithMinOrderAmount(30000L);

        MemberCoupon memberCoupon = testDataFactory.createMemberCoupon(member, coupon);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                memberCoupon.getId()
        );

        assertThatThrownBy(() -> orderService.createOrder(member.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("최소 주문 금액");
    }

    @Test
    void 이미_사용한_쿠폰은_주문에_적용_불가능() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();
        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCoupon memberCoupon = testDataFactory.createMemberCoupon(member, coupon);

        // 쿠폰을 사용 상태로 변경
        memberCoupon.use();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                memberCoupon.getId()
        );

        assertThatThrownBy(() -> orderService.createOrder(member.getId(), request)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용할 수 없는 쿠폰");
    }

    @Test
    void 쿠폰을_사용하지_않으면_할인금액은_0이고_최종금액은_총액과_같다() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        Long orderId = orderService.createOrder(member.getId(), request);

        Order order = orderRepository.findById(orderId).orElseThrow();

        long expectedTotalPrice = product.getPrice() * 2;

        assertThat(order.getTotalPrice()).isEqualTo(expectedTotalPrice);

        assertThat(order.getCouponDiscountAmount()).isEqualTo(0L);

        assertThat(order.getFinalPrice()).isEqualTo(expectedTotalPrice);
    }

    @Test
    void VIP_등급_할인이_적용() {

        Member member = testDataFactory.createVipMember();
        Product product = testDataFactory.createProduct();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        Long orderId = orderService.createOrder(member.getId(), request);

        Order order = orderRepository.findById(orderId).orElseThrow();

        long totalPrice = product.getPrice() * 2;
        long expectedGradeDiscount = totalPrice * 5 / 100;
        long expectedFinalPrice = totalPrice - expectedGradeDiscount;

        // 원래 상품 금액
        assertThat(order.getTotalPrice()).isEqualTo(totalPrice);

        // VIP 등급 할인 금액
        assertThat(order.getGradeDiscountAmount()).isEqualTo(expectedGradeDiscount);

        // 쿠폰 미사용
        assertThat(order.getCouponDiscountAmount()).isEqualTo(0L);

        // 전체 할인 금액
        assertThat(order.getDiscountAmount()).isEqualTo(expectedGradeDiscount);

        // 최종 결제 금액
        assertThat(order.getFinalPrice()).isEqualTo(expectedFinalPrice);
    }

    @Test
    void VIP_등급할인과_쿠폰할인이_동시에_적용() {

        Member member = testDataFactory.createVipMember();
        Product product = testDataFactory.createProduct();
        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCoupon memberCoupon = testDataFactory.createMemberCoupon(member, coupon);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                memberCoupon.getId()
        );

        Long orderId = orderService.createOrder(member.getId(), request);

        Order order = orderRepository.findById(orderId).orElseThrow();

        long totalPrice = product.getPrice() * 2;

        // VIP 5%
        long expectedGradeDiscount = totalPrice * 5 / 100;

        // 쿠폰 10%
        long expectedCouponDiscount = totalPrice * 10 / 100;

        long expectedDiscount = expectedGradeDiscount + expectedCouponDiscount;

        long expectedFinalPrice = totalPrice - expectedDiscount;

        // 원래 상품 금액
        assertThat(order.getTotalPrice()).isEqualTo(totalPrice);

        // 등급 할인
        assertThat(order.getGradeDiscountAmount()).isEqualTo(expectedGradeDiscount);

        // 쿠폰 할인
        assertThat(order.getCouponDiscountAmount()).isEqualTo(expectedCouponDiscount);

        // 전체 할인
        assertThat(order.getDiscountAmount()).isEqualTo(expectedDiscount);

        // 최종 결제 금액
        assertThat(order.getFinalPrice()).isEqualTo(expectedFinalPrice);
    }

    @Test
    void 장바구니_주문이_정상적으로_생성되고_장바구니가_비워진다() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        Cart cart = testDataFactory.createCart(member);

        CartItem cartItem = testDataFactory.createCartItem(
                cart,
                product,
                2
        );

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        Long orderId = orderService.createCartOrder(member.getId(), request);

        Order order = orderRepository.findById(orderId).orElseThrow();

        // 주문 생성 확인
        assertThat(order.getMember().getId()).isEqualTo(member.getId());

        assertThat(order.getTotalPrice()).isEqualTo(product.getPrice() * 2);

        // 장바구니 상품 삭제 확인
        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

        assertThat(cartItems).isEmpty();
    }

    @Test
    void 장바구니_주문에_쿠폰이_적용() {
        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();
        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCoupon memberCoupon = testDataFactory.createMemberCoupon(member, coupon);

        Cart cart = testDataFactory.createCart(member);

        testDataFactory.createCartItem(cart, product, 2);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                memberCoupon.getId()
        );

        Long orderId = orderService.createCartOrder(member.getId(), request);

        Order order = orderRepository.findById(orderId).orElseThrow();

        long totalPrice = product.getPrice() * 2;

        long expectedCouponDiscount = totalPrice * 10 / 100;

        assertThat(order.getTotalPrice()).isEqualTo(totalPrice);
        assertThat(order.getCouponDiscountAmount()).isEqualTo(expectedCouponDiscount);
    }

    @Test
    void 주문_생성시_ORDERED_CREATE_상태이력이_저장된다() {
        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        Long orderId = orderService.createOrder(member.getId(), request);

        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findAllByOrderId(orderId);

        assertThat(histories).hasSize(1);

        OrderStatusHistory history = histories.get(0);

        assertThat(history.getStatus().getCodeValue()).isEqualTo("ORDERED");
        assertThat(history.getChangeType().getCodeValue()).isEqualTo("CREATE");
        assertThat(history.getChangedBy().getId()).isEqualTo(member.getId());
    }
}
