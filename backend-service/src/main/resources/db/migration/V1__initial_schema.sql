CREATE TABLE runs (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    current_hp INTEGER NOT NULL DEFAULT 3,
    max_hp INTEGER NOT NULL DEFAULT 3,
    round_number INTEGER NOT NULL DEFAULT 0,
    total_wins INTEGER NOT NULL DEFAULT 0,
    total_losses INTEGER NOT NULL DEFAULT 0,
    total_draws INTEGER NOT NULL DEFAULT 0,
    shield INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMP
);

CREATE TABLE rounds (
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    round_number INTEGER NOT NULL,
    player_move VARCHAR(20) NOT NULL,
    bot_move VARCHAR(20) NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE buffs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    buff_type VARCHAR(20) NOT NULL,
    effect_key VARCHAR(255) NOT NULL,
    icon VARCHAR(255) NOT NULL
);

CREATE TABLE run_buffs (
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    buff_id BIGINT NOT NULL REFERENCES buffs(id) ON DELETE CASCADE,
    applied_at TIMESTAMP NOT NULL DEFAULT NOW(),
    used_at TIMESTAMP,
    consumed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    icon VARCHAR(255) NOT NULL,
    criteria_type VARCHAR(50) NOT NULL,
    criteria_value INTEGER NOT NULL,
    unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    unlocked_at TIMESTAMP,
    progress INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value VARCHAR(255) NOT NULL
);

-- Seed buffs
INSERT INTO buffs (name, description, buff_type, effect_key, icon) VALUES
('Max HP Up', 'Gain +1 maximum HP', 'SURVIVAL', 'MAX_HP_UP', 'mdi2h:heart-plus'),
('Heal', 'Heal 1 HP', 'SURVIVAL', 'HEAL', 'mdi2h:heart'),
('Shield', 'Gain shield for next loss', 'SURVIVAL', 'SHIELD', 'mdi2s:shield'),
('Double Reward', 'Double reward for next win', 'SCORING', 'DOUBLE_REWARD', 'mdi2s:star'),
('Streak Bonus', 'Bonus points on streaks', 'SCORING', 'STREAK_BONUS', 'mdi2f:fire'),
('Reroll Token', 'One reroll token', 'UTILITY', 'REROLL', 'mdi2r:refresh'),
('Draw as Win', 'Draw counts as win once', 'UTILITY', 'DRAW_AS_WIN', 'mdi2c:check-circle'),
('Ignore Loss', 'Ignore one future loss', 'UTILITY', 'IGNORE_LOSS', 'mdi2s:shield-check');

-- Seed achievements
INSERT INTO achievements (name, description, icon, criteria_type, criteria_value) VALUES
('First Run', 'Complete your first run', 'mdi2r:run', 'RUNS_COMPLETED', 1),
('First Victory', 'Win a run without losing all HP', 'mdi2t:trophy', 'RUNS_WON', 1),
('Survivor', 'Survive 10 rounds', 'mdi2s:sword', 'ROUNDS_SURVIVED', 10),
('10 Wins', 'Win 10 total rounds', 'mdi2f:fire', 'TOTAL_WINS', 10),
('20 Wins', 'Win 20 total rounds', 'mdi2f:fire', 'TOTAL_WINS', 20),
('30 Wins', 'Win 30 total rounds', 'mdi2f:fire', 'TOTAL_WINS', 30),
('50 Wins', 'Win 50 total rounds', 'mdi2f:fire', 'TOTAL_WINS', 50),
('100 Wins', 'Win 100 total rounds', 'mdi2f:fire', 'TOTAL_WINS', 100),
('Win Streak 3', 'Achieve a win streak of 3', 'mdi2p:plus-circle', 'WIN_STREAK', 3),
('Win Streak 5', 'Achieve a win streak of 5', 'mdi2p:plus-circle', 'WIN_STREAK', 5),
('Round Warrior', 'Complete 50 rounds total', 'mdi2s:sword-cross', 'TOTAL_ROUNDS', 50);

-- Seed default settings
INSERT INTO settings (setting_key, setting_value) VALUES
('fullscreen', 'false'),
('master_volume', '0.7'),
('sound_enabled', 'true'),
('animation_enabled', 'true');
