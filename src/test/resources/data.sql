INSERT INTO grade
(grade_code, grade_name, min_purchase_amount, discount_rate, created_at, updated_at)
VALUES
    ('BASIC', '일반회원', 0, 0, NOW(), NOW()),
    ('VIP', 'VIP회원', 1000000, 5, NOW(), NOW()),
    ('VVIP', 'VVIP회원', 3000000, 10, NOW(), NOW());

INSERT INTO common_code_group
(group_code, group_name, description, use_yn, created_at, updated_at)
VALUES
    ('DELIVERY_STATUS', '배송 상태', '배송 처리 상태를 관리한다.', 'Y', NOW(), NOW()),
    ('DISCOUNT_TYPE', '할인 방식', '쿠폰 할인 방식을 관리한다.', 'Y', NOW(), NOW()),
    ('HISTORY_CHANGE_TYPE', '변경 유형', '데이터 변경 유형을 관리한다.', 'Y', NOW(), NOW()),
    ('ORDER_STATUS', '주문 상태', '주문의 처리 상태를 관리한다.', 'Y', NOW(), NOW()),
    ('PAYMENT_METHOD', '결제 수단', '결제 수단을 관리한다.', 'Y', NOW(), NOW()),
    ('PAYMENT_STATUS', '결제 상태', '결제 처리 상태를 관리한다.', 'Y', NOW(), NOW());


-- 할인 타입
INSERT INTO common_code_detail
(group_code, code_value, code_name, sort_order, use_yn, created_at, updated_at)
VALUES
    ('DISCOUNT_TYPE', 'RATE', '정률 할인', 1, 'Y', NOW(), NOW()),
    ('DISCOUNT_TYPE', 'AMOUNT', '정액 할인', 2, 'Y', NOW(), NOW());


-- 주문 상태
INSERT INTO common_code_detail
(group_code, code_value, code_name, sort_order, use_yn, created_at, updated_at)
VALUES
    ('ORDER_STATUS', 'ORDERED', '주문완료', 1, 'Y', NOW(), NOW()),
    ('ORDER_STATUS', 'PAID', '결제완료', 2, 'Y', NOW(), NOW()),
    ('ORDER_STATUS', 'SHIPPING', '배송중', 3, 'Y', NOW(), NOW()),
    ('ORDER_STATUS', 'DELIVERED', '배송완료', 4, 'Y', NOW(), NOW()),
    ('ORDER_STATUS', 'CANCELLED', '주문취소', 5, 'Y', NOW(), NOW());


-- 결제 상태
INSERT INTO common_code_detail
(group_code, code_value, code_name, sort_order, use_yn, created_at, updated_at)
VALUES
    ('PAYMENT_STATUS', 'READY', '결제대기', 1, 'Y', NOW(), NOW()),
    ('PAYMENT_STATUS', 'PAID', '결제완료', 2, 'Y', NOW(), NOW()),
    ('PAYMENT_STATUS', 'FAILED', '결제실패', 3, 'Y', NOW(), NOW()),
    ('PAYMENT_STATUS', 'CANCELLED', '결제취소', 4, 'Y', NOW(), NOW());


-- 결제 수단
INSERT INTO common_code_detail
(group_code, code_value, code_name, sort_order, use_yn, created_at, updated_at)
VALUES
    ('PAYMENT_METHOD', 'CARD', '카드', 1, 'Y', NOW(), NOW()),
    ('PAYMENT_METHOD', 'CASH', '현금', 2, 'Y', NOW(), NOW()),
    ('PAYMENT_METHOD', 'TRANSFER', '계좌이체', 3, 'Y', NOW(), NOW());


-- 배송 상태
INSERT INTO common_code_detail
(group_code, code_value, code_name, sort_order, use_yn, created_at, updated_at)
VALUES
    ('DELIVERY_STATUS', 'READY', '배송준비', 1, 'Y', NOW(), NOW()),
    ('DELIVERY_STATUS', 'SHIPPING', '배송중', 2, 'Y', NOW(), NOW()),
    ('DELIVERY_STATUS', 'DELIVERED', '배송완료', 3, 'Y', NOW(), NOW());


-- 이력 변경 타입
INSERT INTO common_code_detail
(group_code, code_value, code_name, sort_order, use_yn, created_at, updated_at)
VALUES
    ('HISTORY_CHANGE_TYPE', 'CREATE', '생성', 1, 'Y', NOW(), NOW()),
    ('HISTORY_CHANGE_TYPE', 'UPDATE', '수정', 2, 'Y', NOW(), NOW()),
    ('HISTORY_CHANGE_TYPE', 'DELETE', '삭제', 3, 'Y', NOW(), NOW());


