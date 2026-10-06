-- 개발용 인기 메뉴 확인 데이터입니다. 기본 initial-data.sql 실행 후 사용합니다.
-- 샘플 사용자 ID 100001과 주문 ID 100001~100012를 사용합니다.
-- 완료된 결제 내역을 준비하며, 기존 사용자 잔액과 주문을 변경하지 않습니다.
BEGIN;

INSERT INTO member (id, name, created_at)
SELECT 100001, '샘플 주문 사용자', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM member WHERE id = 100001);

INSERT INTO point (member_id, balance, updated_at)
SELECT 100001, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM point WHERE member_id = 100001);

INSERT INTO orders (id, member_id, total_price, status, ordered_at)
SELECT sample.order_id, member.id, menu.price, 'COMPLETED', CURRENT_TIMESTAMP
FROM (
    SELECT 100001 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100002 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100003 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100004 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100005 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100006 AS order_id, 2 AS menu_id
    UNION ALL SELECT 100007 AS order_id, 2 AS menu_id
    UNION ALL SELECT 100008 AS order_id, 2 AS menu_id
    UNION ALL SELECT 100009 AS order_id, 3 AS menu_id
    UNION ALL SELECT 100010 AS order_id, 3 AS menu_id
    UNION ALL SELECT 100011 AS order_id, 4 AS menu_id
    UNION ALL SELECT 100012 AS order_id, 5 AS menu_id
) sample
JOIN menu ON menu.id = sample.menu_id
JOIN member ON member.id = 100001 AND member.name = '샘플 주문 사용자'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE id = sample.order_id);

INSERT INTO order_item (order_id, menu_id, menu_name, menu_price, quantity)
SELECT orders.id, menu.id, menu.name, orders.total_price, 1
FROM (
    SELECT 100001 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100002 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100003 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100004 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100005 AS order_id, 1 AS menu_id
    UNION ALL SELECT 100006 AS order_id, 2 AS menu_id
    UNION ALL SELECT 100007 AS order_id, 2 AS menu_id
    UNION ALL SELECT 100008 AS order_id, 2 AS menu_id
    UNION ALL SELECT 100009 AS order_id, 3 AS menu_id
    UNION ALL SELECT 100010 AS order_id, 3 AS menu_id
    UNION ALL SELECT 100011 AS order_id, 4 AS menu_id
    UNION ALL SELECT 100012 AS order_id, 5 AS menu_id
) sample
JOIN orders ON orders.id = sample.order_id AND orders.member_id = 100001
JOIN member ON member.id = orders.member_id AND member.name = '샘플 주문 사용자'
JOIN menu ON menu.id = sample.menu_id
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = orders.id);

COMMIT;
