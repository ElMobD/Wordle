-- ============================================================================
-- Base de données Wordle - Script d'initialisation
-- ============================================================================
-- Ce script crée toutes les tables nécessaires pour l'application Wordle
-- avec support du mode solo, multijoueur et fonctionnalités sociales
-- ============================================================================

-- ============================================================================
-- SECTION 1 : GESTION DES UTILISATEURS
-- ============================================================================

-- Table des utilisateurs authentifiés via OAuth2 (Google)
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    google_id       VARCHAR(255) UNIQUE,
    email           VARCHAR(255) UNIQUE NOT NULL,
    name            VARCHAR(255) NOT NULL,
    picture         VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- SECTION 2 : SYSTÈME DE JEUX
-- ============================================================================

-- Table des sessions multijoueur (créée avant games pour éviter les références circulaires)
CREATE TABLE IF NOT EXISTS sessions (
    id              UUID PRIMARY KEY,
    code            VARCHAR(16) UNIQUE NOT NULL,
    host_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rounds          INT NOT NULL DEFAULT 5,
    time_limit      INT NOT NULL DEFAULT 60,
    word_length     INT NOT NULL DEFAULT 5,
    status          VARCHAR(20) NOT NULL DEFAULT 'LOBBY' CHECK (status IN ('LOBBY', 'IN_PROGRESS', 'FINISHED', 'CANCELLED')),
    current_round   INT DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des parties (solo ou multijoueur)
CREATE TABLE IF NOT EXISTS games (
    id              UUID PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_id      UUID REFERENCES sessions(id) ON DELETE CASCADE,
    round_number    INT,
    game_type       VARCHAR(20) NOT NULL CHECK (game_type IN ('DAILY', 'RANDOM', 'SESSION')),
    answer          VARCHAR(10) NOT NULL,
    max_attempts    INT NOT NULL DEFAULT 6,
    attempts_used   INT NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'WON', 'LOST', 'CANCELED')),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP
);

-- Table des tentatives de devinette
CREATE TABLE IF NOT EXISTS guesses (
    id              BIGSERIAL PRIMARY KEY,
    game_id         UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    attempt_no      INT NOT NULL,
    guess           VARCHAR(10) NOT NULL,
    result_mask     VARCHAR(10) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- SECTION 3 : MODE MULTIJOUEUR
-- ============================================================================


-- Table des joueurs participants à une session
CREATE TABLE IF NOT EXISTS session_players (
    session_id      UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_host         BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, user_id)
);

-- Table pour suivre l'état de chaque joueur dans une partie de session
CREATE TABLE IF NOT EXISTS session_game_player (
    session_id      UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    game_id         UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    score           INT DEFAULT 0,
    PRIMARY KEY (session_id, game_id, user_id)
);

-- Table du chat de session
CREATE TABLE IF NOT EXISTS session_chat (
    id              BIGSERIAL PRIMARY KEY,
    session_id      UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message         TEXT NOT NULL,
    sent_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- SECTION 4 : SYSTÈME SOCIAL (AMIS)
-- ============================================================================

-- Table des demandes d'amitié
CREATE TABLE IF NOT EXISTS friend_requests (
    id              BIGSERIAL PRIMARY KEY,
    requester_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (requester_id, receiver_id),
    CHECK (requester_id != receiver_id)
);

-- Table des relations d'amitié établies
CREATE TABLE IF NOT EXISTS friendships (
    id              BIGSERIAL PRIMARY KEY,
    user1_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user2_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user1_id, user2_id),
    CHECK (user1_id < user2_id)
);

-- ============================================================================
-- SECTION 5 : INDICES POUR OPTIMISATION DES PERFORMANCES
-- ============================================================================

-- Indices sur la table games
CREATE INDEX IF NOT EXISTS idx_games_user_id ON games(user_id);
CREATE INDEX IF NOT EXISTS idx_games_session_id ON games(session_id);
CREATE INDEX IF NOT EXISTS idx_games_game_type ON games(game_type);
CREATE INDEX IF NOT EXISTS idx_games_created_at ON games(created_at);
CREATE INDEX IF NOT EXISTS idx_games_status ON games(status);

-- Indices sur la table guesses
CREATE INDEX IF NOT EXISTS idx_guesses_game_id ON guesses(game_id);
CREATE INDEX IF NOT EXISTS idx_guesses_user_id ON guesses(user_id);

-- Indices sur la table sessions
CREATE INDEX IF NOT EXISTS idx_sessions_host_id ON sessions(host_id);
CREATE INDEX IF NOT EXISTS idx_sessions_code ON sessions(code);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON sessions(status);

-- Indices sur la table session_chat
CREATE INDEX IF NOT EXISTS idx_session_chat_session_id ON session_chat(session_id);
CREATE INDEX IF NOT EXISTS idx_session_chat_sent_at ON session_chat(sent_at);

-- Indices sur les tables de relations sociales
CREATE INDEX IF NOT EXISTS idx_friend_requests_receiver_id ON friend_requests(receiver_id);
CREATE INDEX IF NOT EXISTS idx_friend_requests_status ON friend_requests(status);
CREATE INDEX IF NOT EXISTS idx_friendships_user1_id ON friendships(user1_id);
CREATE INDEX IF NOT EXISTS idx_friendships_user2_id ON friendships(user2_id);

-- ============================================================================
-- FIN DU SCRIPT
-- ============================================================================
