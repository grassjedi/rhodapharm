CREATE TABLE usr (
  id BIGSERIAL PRIMARY KEY,
  email TEXT UNIQUE,
  roles TEXT,
  created TIMESTAMP DEFAULT current_timestamp,
  disabled TIMESTAMP,
  token TEXT,
  token_info TEXT,
  token_expiry TIMESTAMP
);

CREATE TABLE usr_session (
  id BIGSERIAL PRIMARY KEY,
  usr_id BIGINT REFERENCES usr(id),
  expiry TIMESTAMP,
  key TEXT UNIQUE
);

CREATE INDEX usr_session_usr_id ON usr_session(usr_id);
CREATE INDEX usr_session_expiry ON usr_session(expiry);
CREATE INDEX usr_session_key ON usr_session(key);