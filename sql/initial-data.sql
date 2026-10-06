-- 애플리케이션이 테이블을 생성한 뒤 개발 DB에서 실행합니다.
-- 기존 사용자 정보, 포인트 잔액, 메뉴 가격은 변경하지 않습니다.
BEGIN;

INSERT INTO member (id, name, created_at)
SELECT 1, '테스트 사용자', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM member WHERE id = 1);

INSERT INTO point (member_id, balance, updated_at)
SELECT 1, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM point WHERE member_id = 1);

INSERT INTO menu (id, name, price, created_at)
SELECT 1, '아메리카노', 4500, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 1);

INSERT INTO menu (id, name, price, created_at)
SELECT 2, '카페라테', 5000, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 2);

INSERT INTO menu (id, name, price, created_at)
SELECT 3, '바닐라라테', 5500, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 3);

INSERT INTO menu (id, name, price, created_at)
SELECT 4, '카푸치노', 5000, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 4);

INSERT INTO menu (id, name, price, created_at)
SELECT 5, '카페모카', 5500, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE id = 5);

COMMIT;
