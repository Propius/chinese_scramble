-- ============================================================================
-- Flyway Migration V2: Insert Initial Data
-- ============================================================================
-- Description: Inserts default feature flags and sample data
-- Author: Elite Backend Lead Developer
-- Version: 1.0.0
-- ============================================================================

-- ============================================================================
-- Feature Flags: Default Configuration
-- ============================================================================

-- Core game modes
INSERT INTO feature_flags (feature_name, enabled, description, enabled_at)
VALUES
    ('idiom-scramble', TRUE, 'Enable Chinese idiom scramble game mode (成语拼字)', CURRENT_TIMESTAMP),
    ('sentence-crafting', TRUE, 'Enable sentence crafting game mode (造句游戏)', CURRENT_TIMESTAMP);

-- Game features
INSERT INTO feature_flags (feature_name, enabled, description, enabled_at)
VALUES
    ('leaderboard', TRUE, 'Enable leaderboard display and rankings', CURRENT_TIMESTAMP),
    ('hints', TRUE, 'Enable hint system (3 levels with penalties)', CURRENT_TIMESTAMP),
    ('achievements', TRUE, 'Enable achievement system', CURRENT_TIMESTAMP);

-- Optional features (disabled by default)
INSERT INTO feature_flags (feature_name, enabled, description)
VALUES
    ('audio-pronunciation', FALSE, 'Enable audio pronunciation for Chinese characters'),
    ('practice-mode', FALSE, 'Enable practice mode (no score, unlimited hints)'),
    ('daily-challenge', FALSE, 'Enable daily challenge mode with special rewards'),
    ('multiplayer', FALSE, 'Enable multiplayer competitive mode');

-- ============================================================================
-- Admin Account: Default Admin User
-- ============================================================================
-- Password: Admin123! (BCrypt hash)
-- IMPORTANT: Change this password immediately in production!
-- ============================================================================

INSERT INTO players (username, email, password_hash, role, active)
VALUES
    ('admin', 'admin@chinesescramble.gov.sg',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'ADMIN', TRUE);

-- ============================================================================
-- Sample Players: For Development and Testing
-- ============================================================================
-- All sample users have password: Player123!
-- ============================================================================

INSERT INTO players (username, email, password_hash, role, active, last_login_at)
VALUES
    ('玩家001', 'player1@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'PLAYER', TRUE, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),

    ('张伟', 'zhangwei@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'PLAYER', TRUE, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),

    ('李娜', 'lina@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'PLAYER', TRUE, DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),

    ('王芳', 'wangfang@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'PLAYER', TRUE, DATEADD('HOUR', -5, CURRENT_TIMESTAMP));

-- ============================================================================
-- Sample Idiom Scores: For Leaderboard Testing
-- ============================================================================

-- Player 1 scores (玩家001)
INSERT INTO idiom_scores (player_id, idiom, score, difficulty, time_taken, hints_used, accuracy_rate, completed)
VALUES
    (2, '一马当先', 450, 'EASY', 45, 0, 1.0, TRUE),
    (2, '井底之蛙', 380, 'MEDIUM', 85, 1, 0.9, TRUE),
    (2, '画蛇添足', 520, 'MEDIUM', 65, 0, 1.0, TRUE),
    (2, '刻舟求剑', 680, 'HARD', 120, 2, 0.85, TRUE);

-- Player 2 scores (张伟)
INSERT INTO idiom_scores (player_id, idiom, score, difficulty, time_taken, hints_used, accuracy_rate, completed)
VALUES
    (3, '守株待兔', 470, 'EASY', 38, 0, 1.0, TRUE),
    (3, '亡羊补牢', 550, 'MEDIUM', 72, 0, 1.0, TRUE),
    (3, '南辕北辙', 720, 'HARD', 95, 1, 0.92, TRUE),
    (3, '滥竽充数', 950, 'EXPERT', 150, 1, 0.88, TRUE);

-- Player 3 scores (李娜)
INSERT INTO idiom_scores (player_id, idiom, score, difficulty, time_taken, hints_used, accuracy_rate, completed)
VALUES
    (4, '掩耳盗铃', 490, 'EASY', 42, 0, 1.0, TRUE),
    (4, '拔苗助长', 510, 'MEDIUM', 68, 1, 0.95, TRUE),
    (4, '东施效颦', 850, 'HARD', 110, 0, 0.95, TRUE);

-- Player 4 scores (王芳)
INSERT INTO idiom_scores (player_id, idiom, score, difficulty, time_taken, hints_used, accuracy_rate, completed)
VALUES
    (5, '杯弓蛇影', 440, 'EASY', 50, 1, 0.92, TRUE),
    (5, '叶公好龙', 420, 'MEDIUM', 95, 2, 0.82, TRUE);

-- ============================================================================
-- Sample Sentence Scores: For Leaderboard Testing
-- ============================================================================

-- Player 1 sentence scores
INSERT INTO sentence_scores (player_id, target_sentence, player_sentence, score, difficulty, time_taken, hints_used, accuracy_rate, grammar_score, similarity_score, completed)
VALUES
    (2, '我喜欢学习中文', '我喜欢学习中文', 500, 'EASY', 50, 0, 1.0, 100, 1.0, TRUE),
    (2, '今天天气很好', '今天天气很好', 480, 'EASY', 55, 1, 0.95, 98, 0.98, TRUE);

-- Player 2 sentence scores
INSERT INTO sentence_scores (player_id, target_sentence, player_sentence, score, difficulty, time_taken, hints_used, accuracy_rate, grammar_score, similarity_score, completed)
VALUES
    (3, '他每天都运动', '他每天都运动', 520, 'EASY', 45, 0, 1.0, 100, 1.0, TRUE),
    (3, '我们一起去图书馆', '我们一起去图书馆', 650, 'MEDIUM', 80, 0, 1.0, 100, 1.0, TRUE);

-- Player 3 sentence scores
INSERT INTO sentence_scores (player_id, target_sentence, player_sentence, score, difficulty, time_taken, hints_used, accuracy_rate, grammar_score, similarity_score, completed)
VALUES
    (4, '这个问题很复杂', '这个问题很复杂', 510, 'EASY', 48, 0, 1.0, 100, 1.0, TRUE),
    (4, '学好中文需要时间', '学好中文需要时间', 720, 'MEDIUM', 75, 1, 0.95, 95, 0.96, TRUE);

-- ============================================================================
-- Sample Leaderboard Entries: Pre-calculated Rankings
-- ============================================================================

-- Idiom - Easy Leaderboard
INSERT INTO leaderboard (player_id, game_type, difficulty, total_score, games_played, average_score, rank, accuracy_rate, last_updated)
VALUES
    (4, 'IDIOM', 'EASY', 490, 1, 490.0, 1, 1.0, CURRENT_TIMESTAMP),
    (3, 'IDIOM', 'EASY', 470, 1, 470.0, 2, 1.0, CURRENT_TIMESTAMP),
    (2, 'IDIOM', 'EASY', 450, 1, 450.0, 3, 1.0, CURRENT_TIMESTAMP),
    (5, 'IDIOM', 'EASY', 440, 1, 440.0, 4, 0.92, CURRENT_TIMESTAMP);

-- Idiom - Medium Leaderboard
INSERT INTO leaderboard (player_id, game_type, difficulty, total_score, games_played, average_score, rank, accuracy_rate, last_updated)
VALUES
    (3, 'IDIOM', 'MEDIUM', 550, 1, 550.0, 1, 1.0, CURRENT_TIMESTAMP),
    (2, 'IDIOM', 'MEDIUM', 520, 1, 520.0, 2, 1.0, CURRENT_TIMESTAMP),
    (4, 'IDIOM', 'MEDIUM', 510, 1, 510.0, 3, 0.95, CURRENT_TIMESTAMP),
    (5, 'IDIOM', 'MEDIUM', 420, 1, 420.0, 4, 0.82, CURRENT_TIMESTAMP);

-- Idiom - Hard Leaderboard
INSERT INTO leaderboard (player_id, game_type, difficulty, total_score, games_played, average_score, rank, accuracy_rate, last_updated)
VALUES
    (4, 'IDIOM', 'HARD', 850, 1, 850.0, 1, 0.95, CURRENT_TIMESTAMP),
    (3, 'IDIOM', 'HARD', 720, 1, 720.0, 2, 0.92, CURRENT_TIMESTAMP),
    (2, 'IDIOM', 'HARD', 680, 1, 680.0, 3, 0.85, CURRENT_TIMESTAMP);

-- Sentence - Easy Leaderboard
INSERT INTO leaderboard (player_id, game_type, difficulty, total_score, games_played, average_score, rank, accuracy_rate, last_updated)
VALUES
    (3, 'SENTENCE', 'EASY', 520, 1, 520.0, 1, 1.0, CURRENT_TIMESTAMP),
    (4, 'SENTENCE', 'EASY', 510, 1, 510.0, 2, 1.0, CURRENT_TIMESTAMP),
    (2, 'SENTENCE', 'EASY', 500, 1, 500.0, 3, 1.0, CURRENT_TIMESTAMP);

-- Sentence - Medium Leaderboard
INSERT INTO leaderboard (player_id, game_type, difficulty, total_score, games_played, average_score, rank, accuracy_rate, last_updated)
VALUES
    (4, 'SENTENCE', 'MEDIUM', 720, 1, 720.0, 1, 0.95, CURRENT_TIMESTAMP),
    (3, 'SENTENCE', 'MEDIUM', 650, 1, 650.0, 2, 1.0, CURRENT_TIMESTAMP);

-- ============================================================================
-- Sample Achievements: For Testing
-- ============================================================================

-- FIRST_WIN achievements
INSERT INTO achievements (player_id, achievement_type, title, description, unlocked_at, metadata)
VALUES
    (2, 'FIRST_WIN', '首次胜利', '完成第一个游戏', DATEADD('DAY', -2, CURRENT_TIMESTAMP), '{"game_type":"IDIOM","score":450}'),
    (3, 'FIRST_WIN', '首次胜利', '完成第一个游戏', DATEADD('DAY', -1, CURRENT_TIMESTAMP), '{"game_type":"IDIOM","score":470}'),
    (4, 'FIRST_WIN', '首次胜利', '完成第一个游戏', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), '{"game_type":"IDIOM","score":490}'),
    (5, 'FIRST_WIN', '首次胜利', '完成第一个游戏', DATEADD('HOUR', -5, CURRENT_TIMESTAMP), '{"game_type":"IDIOM","score":440}');

-- PERFECT_SCORE achievements (100% accuracy, no hints)
INSERT INTO achievements (player_id, achievement_type, title, description, unlocked_at, metadata)
VALUES
    (2, 'PERFECT_SCORE', '完美主义者', '100%准确率且不使用提示完成游戏', DATEADD('DAY', -1, CURRENT_TIMESTAMP), '{"idiom":"画蛇添足","score":520}'),
    (3, 'PERFECT_SCORE', '完美主义者', '100%准确率且不使用提示完成游戏', DATEADD('DAY', -1, CURRENT_TIMESTAMP), '{"idiom":"亡羊补牢","score":550}'),
    (4, 'PERFECT_SCORE', '完美主义者', '100%准确率且不使用提示完成游戏', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), '{"idiom":"掩耳盗铃","score":490}');

-- ============================================================================
-- Sample Game Sessions: Active and Completed
-- ============================================================================

-- Completed sessions
INSERT INTO game_sessions (player_id, game_type, difficulty, status, started_at, completed_at, final_score)
VALUES
    (2, 'IDIOM', 'EASY', 'COMPLETED',
     DATEADD('DAY', -2, CURRENT_TIMESTAMP),
     DATEADD('SECOND', 45, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
     450),
    (3, 'IDIOM', 'MEDIUM', 'COMPLETED',
     DATEADD('DAY', -1, CURRENT_TIMESTAMP),
     DATEADD('SECOND', 72, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
     550);

-- Active session (for testing session expiry)
INSERT INTO game_sessions (player_id, game_type, difficulty, status, started_at, session_data)
VALUES
    (5, 'IDIOM', 'MEDIUM', 'ACTIVE',
     DATEADD('MINUTE', -10, CURRENT_TIMESTAMP),
     '{"idiom":"自相矛盾","scrambled":["矛","自","盾","相"],"timeRemaining":110}');

-- ============================================================================
-- Sample Hint Usage: For Analytics
-- ============================================================================

-- Hints used in completed sessions
INSERT INTO hint_usage (game_session_id, hint_level, penalty_applied, used_at, hint_content)
VALUES
    (1, 1, 10, DATEADD('SECOND', 30, DATEADD('DAY', -2, CURRENT_TIMESTAMP)), '第一个字：一'),
    (2, 1, 10, DATEADD('SECOND', 45, DATEADD('DAY', -1, CURRENT_TIMESTAMP)), '第一个字：亡');

-- ============================================================================
-- End of Migration V2
-- ============================================================================