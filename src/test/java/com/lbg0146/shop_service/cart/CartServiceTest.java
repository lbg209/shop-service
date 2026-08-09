package com.lbg0146.shop_service.cart;

import com.lbg0146.shop_service.cart.dto.request.CartItemCreateRequest;
import com.lbg0146.shop_service.cart.dto.request.CartItemUpdateRequest;
import com.lbg0146.shop_service.cart.entity.Cart;
import com.lbg0146.shop_service.cart.entity.CartItem;
import com.lbg0146.shop_service.cart.repository.CartItemRepository;
import com.lbg0146.shop_service.cart.repository.CartRepository;
import com.lbg0146.shop_service.cart.service.CartService;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class CartServiceTest {

    @Autowired
    CartService cartService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Test
    void 장바구니에_상품을_추가() {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        Product product = productRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        CartItemCreateRequest request =
                new CartItemCreateRequest(
                        product.getId(),
                        2
                );

        cartService.addItem(member.getId(), request);

        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseThrow();

        Optional<CartItem> cartItem =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                );

        assertThat(cartItem).isPresent();
        assertThat(cartItem.get().getQuantity()).isEqualTo(2);
        assertThat(cartItem.get().getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    void 같은_상품을_다시_추가하면_수량이_증가() {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        Product product = productRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        CartItemCreateRequest firstRequest =
                new CartItemCreateRequest(
                        product.getId(),
                        2
                );

        CartItemCreateRequest secondRequest =
                new CartItemCreateRequest(
                        product.getId(),
                        3
                );

        // 첫 번째 추가
        cartService.addItem(member.getId(), firstRequest);

        // 같은 상품 다시 추가
        cartService.addItem(member.getId(), secondRequest);

        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow();

        CartItem cartItem =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                ).orElseThrow();

        // 2 + 3 = 5
        assertThat(cartItem.getQuantity()).isEqualTo(5);

        // 같은 상품의 CartItem이 하나만 존재하는지 확인
        //assertThat(cartItemRepository.findAllByCartId(cart.getId())).hasSize(1);
    }

    @Test
    void 다른_상품을_추가하면_새로운_CartItem이_생성() {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        Product product1 = productRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        Product product2 = productRepository.findByIdAndDeletedAtIsNull(1L).orElseThrow();

        CartItemCreateRequest request1 =
                new CartItemCreateRequest(
                        product1.getId(),
                        2
                );

        CartItemCreateRequest request2 =
                new CartItemCreateRequest(
                        product2.getId(),
                        1
                );

        cartService.addItem(member.getId(), request1);
        cartService.addItem(member.getId(), request2);

        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow();

        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

        assertThat(cartItems)
                .extracting(item -> item.getProduct().getId())
                .contains(product1.getId(), product2.getId());
    }

    @Test
    void 장바구니_상품_수량을_변경() {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        Product product = productRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        CartItemCreateRequest createRequest =
                new CartItemCreateRequest(
                        product.getId(),
                        2
                );

        cartService.addItem(member.getId(), createRequest);

        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow();

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                ).orElseThrow();

        CartItemUpdateRequest updateRequest = new CartItemUpdateRequest(10);

        cartService.updateItem(member.getId(), cartItem.getId(), updateRequest);

        CartItem updatedCartItem = cartItemRepository.findById(cartItem.getId()).orElseThrow();

        assertThat(updatedCartItem.getQuantity()).isEqualTo(10);
    }

    @Test
    void 상품_수량이_0이면_변경에_실패() {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        Product product = productRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        CartItemCreateRequest createRequest = new CartItemCreateRequest(product.getId(), 2);

        cartService.addItem(member.getId(), createRequest);

        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow();

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()).orElseThrow();

        CartItemUpdateRequest updateRequest = new CartItemUpdateRequest(0);

        assertThatThrownBy(() -> cartService.updateItem(
                        member.getId(),
                        cartItem.getId(),
                        updateRequest
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("상품 수량은 1개 이상이어야 합니다.");
    }

    @Test
    void 장바구니_상품을_삭제() {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        Product product = productRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        CartItemCreateRequest createRequest =
                new CartItemCreateRequest(
                        product.getId(),
                        2
                );

        cartService.addItem(member.getId(), createRequest);

        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow();

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                ).orElseThrow();

        cartService.deleteItem(member.getId(), cartItem.getId());

        assertThat(cartItemRepository.findById(cartItem.getId())).isEmpty();
    }

    @Test
    void 존재하지_않는_상품을_장바구니에_추가하면_실패() {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(2L).orElseThrow();

        CartItemCreateRequest request = new CartItemCreateRequest(
                        9999L,
                        2
                );

        assertThatThrownBy(() -> cartService.addItem(
                        member.getId(),
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }

    @Test
    void 삭제된_회원은_장바구니에_접근_불가능() {

        Long deletedMemberId = 9000L;

        assertThatThrownBy(() ->
                cartService.getCart(deletedMemberId)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("회원을 찾을 수 없습니다.");
    }



}
