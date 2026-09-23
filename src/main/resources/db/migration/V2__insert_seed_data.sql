-- ========================================
-- 种子数据
-- 超级管理员账号（用户名: admin，初始密码: admin123，请登录后修改）
-- 注意：下方 password 字段为 BCrypt 占位哈希，如需实际登录请用程序生成的 admin123 哈希替换
-- ========================================

-- 6种宠物类型
INSERT INTO pet_type (code, name, sort_order) VALUES
('cat', '小猫', 1),
('dog', '小狗', 2),
('rabbit', '小兔子', 3),
('panda', '小熊猫', 4),
('penguin', '小企鹅', 5),
('dragon', '小龙', 6);

-- 超管账号（密码: admin123 的 BCrypt 哈希占位符，请登录后修改）
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68uJ6w0tru8u', '超级管理员', 'admin', 1);

-- 30条宠物等级配置（6种 × 5级）
-- cat (pet_type_id=1)
INSERT INTO pet_level_config (pet_type_id, level, level_name, required_score, image_url) VALUES
(1, 1, '蛋', 0, '/images/pets/cat_lv1.png'),
(1, 2, '幼崽', 50, '/images/pets/cat_lv2.png'),
(1, 3, '成长', 150, '/images/pets/cat_lv3.png'),
(1, 4, '成熟', 350, '/images/pets/cat_lv4.png'),
(1, 5, '传说', 600, '/images/pets/cat_lv5.png'),
-- dog (pet_type_id=2)
(2, 1, '蛋', 0, '/images/pets/dog_lv1.png'),
(2, 2, '幼崽', 50, '/images/pets/dog_lv2.png'),
(2, 3, '成长', 150, '/images/pets/dog_lv3.png'),
(2, 4, '成熟', 350, '/images/pets/dog_lv4.png'),
(2, 5, '传说', 600, '/images/pets/dog_lv5.png'),
-- rabbit (pet_type_id=3)
(3, 1, '蛋', 0, '/images/pets/rabbit_lv1.png'),
(3, 2, '幼崽', 50, '/images/pets/rabbit_lv2.png'),
(3, 3, '成长', 150, '/images/pets/rabbit_lv3.png'),
(3, 4, '成熟', 350, '/images/pets/rabbit_lv4.png'),
(3, 5, '传说', 600, '/images/pets/rabbit_lv5.png'),
-- panda (pet_type_id=4)
(4, 1, '蛋', 0, '/images/pets/panda_lv1.png'),
(4, 2, '幼崽', 50, '/images/pets/panda_lv2.png'),
(4, 3, '成长', 150, '/images/pets/panda_lv3.png'),
(4, 4, '成熟', 350, '/images/pets/panda_lv4.png'),
(4, 5, '传说', 600, '/images/pets/panda_lv5.png'),
-- penguin (pet_type_id=5)
(5, 1, '蛋', 0, '/images/pets/penguin_lv1.png'),
(5, 2, '幼崽', 50, '/images/pets/penguin_lv2.png'),
(5, 3, '成长', 150, '/images/pets/penguin_lv3.png'),
(5, 4, '成熟', 350, '/images/pets/penguin_lv4.png'),
(5, 5, '传说', 600, '/images/pets/penguin_lv5.png'),
-- dragon (pet_type_id=6)
(6, 1, '蛋', 0, '/images/pets/dragon_lv1.png'),
(6, 2, '幼崽', 50, '/images/pets/dragon_lv2.png'),
(6, 3, '成长', 150, '/images/pets/dragon_lv3.png'),
(6, 4, '成熟', 350, '/images/pets/dragon_lv4.png'),
(6, 5, '传说', 600, '/images/pets/dragon_lv5.png');