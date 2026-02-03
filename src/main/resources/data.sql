INSERT INTO "dialects" ("dialect", "standard", "meaning", "origin", "region", "example", "verified", "source", "latitude", "longitude", "created_at")
SELECT '정구지', '부추', '김치나 무침에 쓰이는 채소류', '경상도 지역에서 오래 사용된 방언', '경상도', '정구지 넣고 전 부치자', true, '경상도 방언 사전 (2018)', 35.5, 128.5, NOW()
WHERE NOT EXISTS (SELECT 1 FROM "dialects" WHERE "dialect" = '정구지' AND "region" = '경상도');

INSERT INTO "dialects" ("dialect", "standard", "meaning", "origin", "region", "example", "verified", "source", "latitude", "longitude", "created_at")
SELECT '혼저옵서예', '어서 오세요', '손님을 반갑게 맞이하는 인사말', '제주 방언의 대표 인사말', '제주도', '혼저옵서예, 들어오세요', true, '제주 향토어 사전', 33.5, 126.5, NOW()
WHERE NOT EXISTS (SELECT 1 FROM "dialects" WHERE "dialect" = '혼저옵서예' AND "region" = '제주도');

INSERT INTO "dialects" ("dialect", "standard", "meaning", "origin", "region", "example", "verified", "source", "latitude", "longitude", "created_at")
SELECT '자시고', '드시고', '식사 권유 표현', '충청도 사투리', '충청도', '밥 자시고 가세요', true, '충청도 방언집', 36.5, 127.5, NOW()
WHERE NOT EXISTS (SELECT 1 FROM "dialects" WHERE "dialect" = '자시고' AND "region" = '충청도');
