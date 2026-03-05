CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  google_id VARCHAR(255) UNIQUE,
  email VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  picture VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des sessions multijoueur
CREATE TABLE IF NOT EXISTS sessions (
  id UUID PRIMARY KEY,
  code VARCHAR(16) UNIQUE NOT NULL, -- code d'invitation
  host_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  rounds INT NOT NULL DEFAULT 5,
  time_limit INT NOT NULL DEFAULT 60, -- secondes
  word_length INT NOT NULL DEFAULT 5,
  status VARCHAR(20) NOT NULL DEFAULT 'LOBBY' CHECK (status IN ('LOBBY', 'IN_PROGRESS', 'FINISHED', 'CANCELLED')),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des joueurs dans une session
CREATE TABLE IF NOT EXISTS session_players (
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  is_host BOOLEAN NOT NULL DEFAULT FALSE,
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (session_id, user_id)
);

-- Table optionnelle pour le chat de session
CREATE TABLE IF NOT EXISTS session_chat (
  id BIGSERIAL PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  message TEXT NOT NULL,
  sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS games (
  id UUID PRIMARY KEY,
  user_id BIGINT REFERENCES users(id) ON DELETE CASCADE, -- NULL si multi
  session_id UUID REFERENCES sessions(id) ON DELETE CASCADE, -- NULL si solo
  round_number INT, -- NULL si solo
  game_type VARCHAR(20) NOT NULL CHECK (game_type IN ('DAILY', 'RANDOM', 'SESSION')),
  answer VARCHAR(10) NOT NULL,
  max_attempts INT NOT NULL DEFAULT 6,
  attempts_used INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'WON', 'LOST')),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS guesses (
  id BIGSERIAL PRIMARY KEY,
  game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  attempt_no INT NOT NULL,
  guess VARCHAR(10) NOT NULL,
  result_mask VARCHAR(10) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- Table pour suivre l'état de chaque joueur pour chaque game de session
CREATE TABLE IF NOT EXISTS session_game_player (
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
  score INT DEFAULT 0,
  PRIMARY KEY (session_id, game_id, user_id)
);

CREATE TABLE IF NOT EXISTS friend_requests (
  id BIGSERIAL PRIMARY KEY,
  requester_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(requester_id, receiver_id),
  CHECK (requester_id != receiver_id)
);

CREATE TABLE IF NOT EXISTS friendships (
  id BIGSERIAL PRIMARY KEY,
  user1_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  user2_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user1_id, user2_id),
  CHECK (user1_id < user2_id)
);

-- Indices pour optimiser les requêtes
CREATE INDEX IF NOT EXISTS idx_games_user_id ON games(user_id);
CREATE INDEX IF NOT EXISTS idx_games_game_type ON games(game_type);
CREATE INDEX IF NOT EXISTS idx_games_created_at ON games(created_at);
CREATE INDEX IF NOT EXISTS idx_guesses_game_id ON guesses(game_id);
