package com.lbg0146.shop_service.cart.repository;

import com.lbg0146.shop_service.cart.entity.CartItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findAllByCartId(Long cartId);

    Optional<CartItem> findByIdAndCartId(Long cartItemId, Long cartId);

    // 동시성 제어가 필요한 곳에 사용할 락 메서드 추가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CartItem c WHERE c.cart.id = :cartId AND c.product.id = :productId")
    Optional<CartItem> findByCartIdAndProductIdWithLock(@Param("cartId") Long cartId, @Param("productId") Long productId);

    // ✅ N+1 문제 해결을 위한 Fetch Join 쿼리 추가!
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart.id = :cartId")
    List<CartItem> findAllByCartIdWithProduct(@Param("cartId") Long cartId);
}
