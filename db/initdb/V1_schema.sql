-- Wordle+ — Schéma PostgreSQL corrigé

-- UUID helper
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- pour gen_random_uuid()

-- === Référentiels ===
CREATE TABLE IF NOT EXISTS languages (
  code        VARCHAR(8) PRIMARY KEY,
  name        TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS lexicon (
  id          BIGSERIAL PRIMARY KEY,
  language    VARCHAR(8) NOT NULL REFERENCES languages(code) ON UPDATE CASCADE,
  word        TEXT NOT NULL,
  length      SMALLINT GENERATED ALWAYS AS (char_length(word)) STORED,
  difficulty  SMALLINT,
  UNIQUE (language, word)
);
CREATE INDEX IF NOT EXISTS idx_lexicon_lang_len ON lexicon(language, length);

-- === Utilisateurs ===
CREATE TABLE IF NOT EXISTS users (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  google_sub     TEXT NOT NULL UNIQUE,
  email          TEXT NOT NULL,
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  display_name   TEXT,
  avatar_url     TEXT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_login_at  TIMESTAMPTZ
);
-- unicité email insensible à la casse
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_ci ON users (lower(email));

-- Réglages utilisateur 1–1
CREATE TABLE IF NOT EXISTS user_settings (
  user_id            UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  ui_theme           TEXT NOT NULL DEFAULT 'system',      -- 'light'|'dark'|'system'
  locale             VARCHAR(16),
  default_lang       VARCHAR(8) REFERENCES languages(code),
  default_word_length SMALLINT,
  default_max_attempts SMALLINT,
  extra              JSONB DEFAULT '{}'::jsonb
);

-- === Sessions & multi ===
CREATE TABLE IF NOT EXISTS sessions (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  host_user_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type          TEXT NOT NULL,                              -- 'friends'|'solo'
  status        TEXT NOT NULL,                              -- 'pending'|'active'|'closed'
  is_private    BOOLEAN NOT NULL DEFAULT TRUE,
  invite_code   TEXT UNIQUE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  started_at    TIMESTAMPTZ,
  closed_at     TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS session_settings (
  session_id         UUID PRIMARY KEY REFERENCES sessions(id) ON DELETE CASCADE,
  language           VARCHAR(8) REFERENCES languages(code),
  word_length        SMALLINT,
  max_attempts       SMALLINT,
  rounds_total       SMALLINT,
  round_duration_sec INTEGER,
  games_per_player   SMALLINT,
  extra              JSONB DEFAULT '{}'::jsonb
);

-- table de jonction N–N
CREATE TABLE IF NOT EXISTS session_participants (
  session_id  UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role        TEXT NOT NULL DEFAULT 'player',
  joined_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  score       INTEGER DEFAULT 0,
  PRIMARY KEY (session_id, user_id)
);

-- Rounds (manches)
CREATE TABLE IF NOT EXISTS rounds (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id     UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  round_no       SMALLINT NOT NULL,
  target_word_id BIGINT NOT NULL REFERENCES lexicon(id),
  status         TEXT NOT NULL,                             -- 'ready'|'running'|'finished'
  start_at       TIMESTAMPTZ,
  end_at         TIMESTAMPTZ,
  UNIQUE (session_id, round_no)
);

-- Games (1 par joueur et par round)
CREATE TABLE IF NOT EXISTS games (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  session_id     UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  round_id       UUID NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
  status         TEXT NOT NULL,                              -- 'in_progress'|'won'|'lost'|'abandoned'
  language       VARCHAR(8) NOT NULL REFERENCES languages(code),
  word_length    SMALLINT NOT NULL,
  max_attempts   SMALLINT NOT NULL,
  attempts_used  SMALLINT DEFAULT 0,
  started_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  finished_at    TIMESTAMPTZ,
  target_word_id BIGINT NOT NULL REFERENCES lexicon(id),
  UNIQUE (round_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_games_user ON games(user_id, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_games_session ON games(session_id);

-- Guesses
CREATE TABLE IF NOT EXISTS guesses (
  id           BIGSERIAL PRIMARY KEY,
  game_id      UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  attempt_no   SMALLINT NOT NULL,
  guess_text   TEXT NOT NULL,
  result_mask  TEXT NOT NULL,                                -- ex: 'GYBBG'
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (game_id, attempt_no)
);
CREATE INDEX IF NOT EXISTS idx_guesses_game ON guesses(game_id);
