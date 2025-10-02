-- ============================================================================
-- Flyway Migration V1: Create Base Tables
-- ============================================================================
-- Description: Creates core tables for Chinese Word Scramble Game
-- Author: Elite Backend Lead Developer
-- Version: 1.0.0
-- Database: PostgreSQL 14+ / H2 (development)
-- Encoding: UTF-8 (supports Chinese characters)
-- ============================================================================

-- ============================================================================
-- Table: players
-- Description: Stores player accounts and authentication data
-- ============================================================================
CREATE TABLE players (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'PLAYER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_player_role CHECK (role IN ('PLAYER', 'ADMIN', 'MODERATOR'))
);

-- Indexes for players table
CREATE INDEX idx_player_username ON players(username);
CREATE INDEX idx_player_email ON players(email);
CREATE INDEX idx_player_active ON players(active);
CREATE INDEX idx_player_last_login ON players(last_login_at);

-- Comments for players table
COMMENT ON TABLE players IS 'Player accounts with authentication and role management';
COMMENT ON COLUMN players.username IS 'Unique username (supports Chinese characters)';
COMMENT ON COLUMN players.email IS 'Email address for account recovery';
COMMENT ON COLUMN players.password_hash IS 'BCrypt hashed password (60 characters)';
COMMENT ON COLUMN players.role IS 'User role: PLAYER, ADMIN, or MODERATOR';
COMMENT ON COLUMN players.active IS 'Account active status (false = banned/deactivated)';
COMMENT ON COLUMN players.last_login_at IS 'Timestamp of last successful login';
COMMENT ON COLUMN players.version IS 'Optimistic locking version';

-- ============================================================================
-- Table: idiom_scores
-- Description: Records scores from idiom scramble games (成语拼字)
-- ============================================================================
CREATE TABLE idiom_scores (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    idiom VARCHAR(20) NOT NULL,
    score INTEGER NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    time_taken INTEGER NOT NULL,
    hints_used INTEGER NOT NULL DEFAULT 0,
    accuracy_rate DOUBLE PRECISION NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT TRUE,
    played_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_idiom_score_player FOREIGN KEY (player_id)
        REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT chk_idiom_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_idiom_hints CHECK (hints_used BETWEEN 0 AND 3),
    CONSTRAINT chk_idiom_accuracy CHECK (accuracy_rate BETWEEN 0.0 AND 1.0),
    CONSTRAINT chk_idiom_time CHECK (time_taken > 0),
    CONSTRAINT chk_idiom_score CHECK (score >= 0)
);

-- Indexes for idiom_scores table
CREATE INDEX idx_idiom_score_player ON idiom_scores(player_id);
CREATE INDEX idx_idiom_score_difficulty ON idiom_scores(difficulty);
CREATE INDEX idx_idiom_score_played_at ON idiom_scores(played_at);
CREATE INDEX idx_idiom_score_score ON idiom_scores(score DESC);
CREATE INDEX idx_idiom_score_idiom ON idiom_scores(idiom);

-- Comments for idiom_scores table
COMMENT ON TABLE idiom_scores IS 'Score records from idiom scramble games';
COMMENT ON COLUMN idiom_scores.idiom IS 'The Chinese idiom (成语) played, e.g., 一马当先';
COMMENT ON COLUMN idiom_scores.score IS 'Points earned (base points + bonuses - penalties)';
COMMENT ON COLUMN idiom_scores.difficulty IS 'Game difficulty: EASY, MEDIUM, HARD, EXPERT';
COMMENT ON COLUMN idiom_scores.time_taken IS 'Time to complete in seconds';
COMMENT ON COLUMN idiom_scores.hints_used IS 'Number of hints used (0-3)';
COMMENT ON COLUMN idiom_scores.accuracy_rate IS 'Accuracy rate from 0.0 to 1.0';

-- ============================================================================
-- Table: sentence_scores
-- Description: Records scores from sentence crafting games (造句游戏)
-- ============================================================================
CREATE TABLE sentence_scores (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    target_sentence VARCHAR(100) NOT NULL,
    player_sentence VARCHAR(100) NOT NULL,
    score INTEGER NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    time_taken INTEGER NOT NULL,
    hints_used INTEGER NOT NULL DEFAULT 0,
    accuracy_rate DOUBLE PRECISION NOT NULL,
    grammar_score INTEGER NOT NULL,
    similarity_score DOUBLE PRECISION NOT NULL,
    validation_errors TEXT,
    completed BOOLEAN NOT NULL DEFAULT TRUE,
    played_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_sentence_score_player FOREIGN KEY (player_id)
        REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT chk_sentence_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_sentence_hints CHECK (hints_used BETWEEN 0 AND 3),
    CONSTRAINT chk_sentence_accuracy CHECK (accuracy_rate BETWEEN 0.0 AND 1.0),
    CONSTRAINT chk_sentence_grammar CHECK (grammar_score BETWEEN 0 AND 100),
    CONSTRAINT chk_sentence_similarity CHECK (similarity_score BETWEEN 0.0 AND 1.0),
    CONSTRAINT chk_sentence_time CHECK (time_taken > 0),
    CONSTRAINT chk_sentence_score CHECK (score >= 0)
);

-- Indexes for sentence_scores table
CREATE INDEX idx_sentence_score_player ON sentence_scores(player_id);
CREATE INDEX idx_sentence_score_difficulty ON sentence_scores(difficulty);
CREATE INDEX idx_sentence_score_played_at ON sentence_scores(played_at);
CREATE INDEX idx_sentence_score_score ON sentence_scores(score DESC);
CREATE INDEX idx_sentence_score_grammar ON sentence_scores(grammar_score);

-- Comments for sentence_scores table
COMMENT ON TABLE sentence_scores IS 'Score records from sentence crafting games';
COMMENT ON COLUMN sentence_scores.target_sentence IS 'The target Chinese sentence to create';
COMMENT ON COLUMN sentence_scores.player_sentence IS 'The sentence created by the player';
COMMENT ON COLUMN sentence_scores.grammar_score IS 'Grammar correctness score (0-100)';
COMMENT ON COLUMN sentence_scores.similarity_score IS 'Similarity to target sentence (0.0-1.0)';
COMMENT ON COLUMN sentence_scores.validation_errors IS 'JSON array of grammar errors';

-- ============================================================================
-- Table: leaderboard
-- Description: Pre-calculated player rankings for different game modes
-- ============================================================================
CREATE TABLE leaderboard (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    game_type VARCHAR(20) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    total_score INTEGER NOT NULL DEFAULT 0,
    games_played INTEGER NOT NULL DEFAULT 0,
    average_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    rank INTEGER NOT NULL,
    accuracy_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_updated TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_leaderboard_player FOREIGN KEY (player_id)
        REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT uk_leaderboard_player_game UNIQUE (player_id, game_type, difficulty),
    CONSTRAINT chk_leaderboard_game_type CHECK (game_type IN ('IDIOM', 'SENTENCE', 'COMBINED')),
    CONSTRAINT chk_leaderboard_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_leaderboard_rank CHECK (rank >= 1),
    CONSTRAINT chk_leaderboard_games CHECK (games_played >= 0),
    CONSTRAINT chk_leaderboard_accuracy CHECK (accuracy_rate BETWEEN 0.0 AND 1.0)
);

-- Indexes for leaderboard table
CREATE INDEX idx_leaderboard_game_type ON leaderboard(game_type, difficulty);
CREATE INDEX idx_leaderboard_rank ON leaderboard(rank);
CREATE INDEX idx_leaderboard_score ON leaderboard(total_score DESC);

-- Comments for leaderboard table
COMMENT ON TABLE leaderboard IS 'Pre-calculated player rankings for fast leaderboard queries';
COMMENT ON COLUMN leaderboard.game_type IS 'IDIOM, SENTENCE, or COMBINED rankings';
COMMENT ON COLUMN leaderboard.rank IS 'Current rank on this leaderboard (1 = best)';
COMMENT ON COLUMN leaderboard.total_score IS 'Sum of all scores for this game type/difficulty';
COMMENT ON COLUMN leaderboard.average_score IS 'Average score per game';

-- ============================================================================
-- Table: achievements
-- Description: Player achievement tracking
-- ============================================================================
CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    achievement_type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_achievement_player FOREIGN KEY (player_id)
        REFERENCES players(id) ON DELETE CASCADE
);

-- Indexes for achievements table
CREATE INDEX idx_achievement_player ON achievements(player_id);
CREATE INDEX idx_achievement_type ON achievements(achievement_type);
CREATE INDEX idx_achievement_unlocked ON achievements(unlocked_at);

-- Comments for achievements table
COMMENT ON TABLE achievements IS 'Player achievements and milestones';
COMMENT ON COLUMN achievements.achievement_type IS 'Achievement identifier: FIRST_WIN, SPEED_DEMON, etc.';
COMMENT ON COLUMN achievements.title IS 'Achievement title in Chinese';
COMMENT ON COLUMN achievements.description IS 'Achievement description in Chinese';
COMMENT ON COLUMN achievements.metadata IS 'JSON metadata about how achievement was earned';

-- ============================================================================
-- Table: feature_flags
-- Description: Runtime feature toggles
-- ============================================================================
CREATE TABLE feature_flags (
    id BIGSERIAL PRIMARY KEY,
    feature_name VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    enabled_at TIMESTAMP,
    disabled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_feature_name_format CHECK (feature_name ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);

-- Indexes for feature_flags table
CREATE INDEX idx_feature_flag_name ON feature_flags(feature_name);
CREATE INDEX idx_feature_flag_enabled ON feature_flags(enabled);

-- Comments for feature_flags table
COMMENT ON TABLE feature_flags IS 'Runtime feature toggles for A/B testing and gradual rollout';
COMMENT ON COLUMN feature_flags.feature_name IS 'Kebab-case feature identifier';
COMMENT ON COLUMN feature_flags.enabled IS 'Whether feature is currently active';

-- ============================================================================
-- Table: game_sessions
-- Description: Active and completed game sessions
-- ============================================================================
CREATE TABLE game_sessions (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    game_type VARCHAR(20) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    final_score INTEGER,
    session_data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_session_player FOREIGN KEY (player_id)
        REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT chk_session_game_type CHECK (game_type IN ('IDIOM', 'SENTENCE')),
    CONSTRAINT chk_session_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_session_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'ABANDONED', 'EXPIRED'))
);

-- Indexes for game_sessions table
CREATE INDEX idx_session_player ON game_sessions(player_id);
CREATE INDEX idx_session_status ON game_sessions(status);
CREATE INDEX idx_session_started ON game_sessions(started_at);

-- Comments for game_sessions table
COMMENT ON TABLE game_sessions IS 'Active and completed game sessions with state tracking';
COMMENT ON COLUMN game_sessions.status IS 'ACTIVE, COMPLETED, ABANDONED, or EXPIRED';
COMMENT ON COLUMN game_sessions.session_data IS 'JSON game state for resume capability';

-- ============================================================================
-- Table: hint_usage
-- Description: Hint usage tracking for analytics
-- ============================================================================
CREATE TABLE hint_usage (
    id BIGSERIAL PRIMARY KEY,
    game_session_id BIGINT NOT NULL,
    hint_level INTEGER NOT NULL,
    penalty_applied INTEGER NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hint_content VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_hint_session FOREIGN KEY (game_session_id)
        REFERENCES game_sessions(id) ON DELETE CASCADE,
    CONSTRAINT chk_hint_level CHECK (hint_level BETWEEN 1 AND 3),
    CONSTRAINT chk_hint_penalty CHECK (penalty_applied >= 0)
);

-- Indexes for hint_usage table
CREATE INDEX idx_hint_session ON hint_usage(game_session_id);
CREATE INDEX idx_hint_level ON hint_usage(hint_level);
CREATE INDEX idx_hint_used_at ON hint_usage(used_at);

-- Comments for hint_usage table
COMMENT ON TABLE hint_usage IS 'Tracks hint usage for analytics and scoring';
COMMENT ON COLUMN hint_usage.hint_level IS 'Hint level: 1 (-10pts), 2 (-20pts), 3 (-30pts)';
COMMENT ON COLUMN hint_usage.penalty_applied IS 'Score penalty for using this hint';
COMMENT ON COLUMN hint_usage.hint_content IS 'The actual hint shown (Chinese text)';

-- ============================================================================
-- Table: config_cache
-- Description: Cached configuration data with hot-reload support
-- ============================================================================
CREATE TABLE config_cache (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    config_type VARCHAR(20) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    last_loaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_config_type CHECK (config_type IN ('IDIOM', 'SENTENCE', 'FEATURE_FLAG', 'GAME_SETTING')),
    CONSTRAINT chk_config_key_format CHECK (config_key ~ '^[a-z0-9]+(-[a-z0-9]+)*\.(json|yml|yaml|properties)$'),
    CONSTRAINT chk_config_checksum CHECK (checksum ~ '^[a-f0-9]{64}$')
);

-- Indexes for config_cache table
CREATE INDEX idx_config_cache_key ON config_cache(config_key);
CREATE INDEX idx_config_cache_type ON config_cache(config_type);
CREATE INDEX idx_config_cache_loaded ON config_cache(last_loaded_at);

-- Comments for config_cache table
COMMENT ON TABLE config_cache IS 'Cached configuration files with hot-reload capability';
COMMENT ON COLUMN config_cache.config_key IS 'Configuration file name (e.g., idioms.json)';
COMMENT ON COLUMN config_cache.checksum IS 'SHA-256 checksum for change detection';
COMMENT ON COLUMN config_cache.last_loaded_at IS 'Last time configuration was loaded from file';

-- ============================================================================
-- End of Migration V1
-- ============================================================================
