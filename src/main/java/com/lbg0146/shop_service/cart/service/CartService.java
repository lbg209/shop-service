package com.lbg0146.shop_service.cart.service;

import com.lbg0146.shop_service.cart.dto.request.CartItemCreateRequest;
import com.lbg0146.shop_service.cart.dto.request.CartItemUpdateRequest;
import com.lbg0146.shop_service.cart.dto.response.CartItemResponse;
import com.lbg0146.shop_service.cart.dto.response.CartResponse;
import com.lbg0146.shop_service.cart.entity.Cart;
import com.lbg0146.shop_service.cart.entity.CartItem;
import com.lbg0146.shop_service.cart.repository.CartItemRepository;
import com.lbg0146.shop_service.cart.repository.CartRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public CartResponse getCart(Long memberId) {

        Member member = getMember(memberId);

        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow(() ->
                new BusinessException(ErrorCode.CART_NOT_FOUND));

        List<CartItemResponse> items = cartItemRepository.findAllByCartId(cart.getId())
                        .stream()
                        .map(item -> new CartItemResponse(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getProduct().getProductName(),
                                item.getProduct().getPrice(),
                                item.getQuantity()
                        ))
                        .toList();

        return new CartResponse(
                cart.getId(),
                items
        );
    }

    @Transactional
    public void addItem(Long memberId, CartItemCreateRequest request) {

        Member member = getMember(memberId);

        Cart cart = cartRepository.findByMemberId(member.getId()).orElseGet(() ->
                        cartRepository.save(Cart.createCart(member)));

        Product product = productRepository.findByIdAndDeletedAtIsNull(request.productId()).orElseThrow(() ->
                        new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                );

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();

            // 동시성 문제 발생 !!!!!!!!!!!!!
            cartItem.changeQuantity(cartItem.getQuantity() + request.quantity());

        } else {

            CartItem cartItem = CartItem.createCartItem(
                    cart,
                    product,
                    request.quantity()
            );

            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void updateItem(Long memberId, Long cartItemId, CartItemUpdateRequest request) {

        Member member = getMember(memberId);

        Cart cart = cartRepository.findByMemberId(member.getId()).orElseThrow(() ->
                        new BusinessException(ErrorCode.CART_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId()).orElseThrow(() ->
                        new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (request.quantity() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }

        cartItem.changeQuantity(request.quantity());
    }

    @Transactional
    public void deleteItem(Long memberId, Long cartItemId) {

        Member member = getMember(memberId);

        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.CART_NOT_FOUND));

        CartItem cartItem = cartItemRepository
                .findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        cartItemRepository.delete(cartItem);
    }


    private Member getMember(Long memberId) {

        return memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(() ->
                        new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
