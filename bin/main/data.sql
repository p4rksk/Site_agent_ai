-- Admin 더미
INSERT INTO admin_tb (company_name, login_id, password, business_number, phone, created_at, updated_at) VALUES
('현대건설', 'hyundai_admin', '$2a$12$nV3ZfUNbFlHtNQOVM6KOn.dTd9OgL/zmxfH8DveKTdvb0Vhxhr3IO', '123-45-67890', '051-1234-5678', NOW(), NOW()),
('삼성물산', 'samsung_admin', '$2a$12$nV3ZfUNbFlHtNQOVM6KOn.dTd9OgL/zmxfH8DveKTdvb0Vhxhr3IO', '234-56-78901', '051-2345-6789', NOW(), NOW()),
('대우건설', 'daewoo_admin', '$2a$12$nV3ZfUNbFlHtNQOVM6KOn.dTd9OgL/zmxfH8DveKTdvb0Vhxhr3IO', '345-67-89012', '051-3456-7890', NOW(), NOW());

-- SiteAdmin 더미
INSERT INTO site_admin_tb (admin_id, login_id, password, created_at, updated_at) VALUES
(1, 'hyundai_site', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnIbiqixRQO', NOW(), NOW()),
(2, 'samsung_site', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnIbiqixRQO', NOW(), NOW()),
(3, 'daewoo_site', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnIbiqixRQO', NOW(), NOW());

-- Site 더미
INSERT INTO site_tb (admin_id, name, address, lat, lng, manager_name, manager_phone, created_at, updated_at) VALUES
(1, '해운대 주상복합 신축공사', '부산광역시 해운대구 우동 123', 35.1631, 129.1635, '김철수', '010-1234-5678', NOW(), NOW()),
(1, '기장 물류센터 공사', '부산광역시 기장군 기장읍 456', 35.2447, 129.2225, '이영희', '010-2345-6789', NOW(), NOW()),
(2, '서면 오피스텔 신축', '부산광역시 부산진구 서면 789', 35.1579, 129.0597, '박민수', '010-3456-7890', NOW(), NOW()),
(2, '남포동 상업시설 리모델링', '부산광역시 중구 남포동 101', 35.0979, 129.0278, '최지현', '010-4567-8901', NOW(), NOW()),
(3, '사상 공장 증축공사', '부산광역시 사상구 삼락동 202', 35.1504, 128.9922, '정대호', '010-5678-9012', NOW(), NOW()),
(3, '광안리 호텔 신축', '부산광역시 수영구 광안동 303', 35.1531, 129.1186, '한소희', '010-6789-0123', NOW(), NOW());

-- User 더미
INSERT INTO user_tb (kakao_id, username, profile_image, created_at, updated_at) VALUES
('kakao_001', '홍길동', 'https://via.placeholder.com/150', NOW(), NOW()),
('kakao_002', '김근로', 'https://via.placeholder.com/150', NOW(), NOW()),
('kakao_003', '이현장', 'https://via.placeholder.com/150', NOW(), NOW()),
('kakao_004', '박신입', 'https://via.placeholder.com/150', NOW(), NOW()),
('kakao_005', '최안전', 'https://via.placeholder.com/150', NOW(), NOW());

-- SitePdf 더미
INSERT INTO site_pdf_tb (site_id, file_name, file_path, is_active, created_at, updated_at) VALUES
(1, '해운대현장_안전교육.pdf', '/uploads/site1/safety.pdf', true, NOW(), NOW()),
(2, '기장물류_작업지침.pdf', '/uploads/site2/guide.pdf', true, NOW(), NOW()),
(3, '서면오피스텔_안전수칙.pdf', '/uploads/site3/safety.pdf', true, NOW(), NOW()),
(4, '남포동_작업절차서.pdf', '/uploads/site4/guide.pdf', true, NOW(), NOW()),
(5, '사상공장_안전교육.pdf', '/uploads/site5/safety.pdf', true, NOW(), NOW()),
(6, '광안리호텔_현장안내.pdf', '/uploads/site6/guide.pdf', true, NOW(), NOW());

-- ChatLog 더미
INSERT INTO chat_log_tb (site_id, user_id, question, answer, created_at, updated_at) VALUES
(1, 1, '안전모 착용 기준이 어떻게 되나요?', '본 현장은 모든 작업자가 안전모를 착용해야 합니다.', NOW(), NOW()),
(1, 2, '화재 발생시 대피 경로는?', '화재 발생 시 1층 비상구를 통해 대피하시고, 집결지는 정문 앞 주차장입니다.', NOW(), NOW()),
(2, 1, '지게차 운행 구역이 어디인가요?', '지게차 운행 구역은 물류센터 1동과 2동 사이 통로입니다.', NOW(), NOW()),
(2, 4, '안전장비 지급은 어디서 받나요?', '안전장비는 현장 사무소 옆 장비실에서 수령 가능합니다.', NOW(), NOW()),
(3, 2, '추락 방지 시설은 어디에 있나요?', '3층 이상 작업 구역에는 안전망과 안전난간이 설치되어 있습니다.', NOW(), NOW()),
(4, 1, '작업 전 안전점검 절차는?', '작업 전 TBM을 실시하고, 작업 구역 안전점검 후 작업을 시작합니다.', NOW(), NOW()),
(5, 2, '전기 작업 안전 수칙은?', '전기 작업 시 반드시 전원을 차단하고, 검전기로 확인 후 작업합니다.', NOW(), NOW()),
(6, 3, '야간 작업 시 조명 기준은?', '야간 작업 시 작업면 조도 150lux 이상을 유지해야 합니다.', NOW(), NOW());