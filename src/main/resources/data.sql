-- users 테이블 더미 데이터
INSERT INTO users (email, nickname, region, role, provider, provider_id, created_at)
SELECT 'testuser@example.com', 'testmom', '서울', 'USER', 'google', 'google-1234', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'testuser@example.com'
);

INSERT INTO users (email, nickname, region, role, provider, provider_id, created_at)
SELECT 'admin@moretale.com', 'admin', '서울', 'ADMIN', 'google', 'google-admin', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@moretale.com'
);

-- user_profiles 테이블 더미 데이터
INSERT INTO user_profiles (
    user_id,
    child_name,
    child_age,
    child_nationality,
    parent_country,
    primary_language,
    secondary_language,
    created_at
)
SELECT
    u.user_id,
    '민준',
    6,
    '대한민국',
    '베트남',
    'ko',
    'vi',
    NOW()
FROM users u
WHERE u.email = 'testuser@example.com'
  AND NOT EXISTS (SELECT 1 FROM user_profiles WHERE user_id = u.user_id);