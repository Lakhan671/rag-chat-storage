-- Create chat_sessions table
CREATE TABLE IF NOT EXISTS chat_sessions (
    id UUID PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL DEFAULT 'New Chat',
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create chat_messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    sender VARCHAR(20) NOT NULL CHECK (sender IN ('USER', 'ASSISTANT')),
    content TEXT NOT NULL,
    context TEXT,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id_updated_at ON chat_sessions(user_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id_favorite ON chat_sessions(user_id, is_favorite);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id_created_at ON chat_messages(session_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages(session_id);