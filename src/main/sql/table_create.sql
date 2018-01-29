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

CREATE TABLE raw_material (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name TEXT NOT NULL,
  units TEXT NOT NULL
);

CREATE TABLE product (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name TEXT NOT NULL,
  disabled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE formulation (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  raw_material_id BIGINT NOT NULL REFERENCES raw_material(id),
  product_id BIGINT NOT NULL REFERENCES product(id),
  quantity BIGINT NOT NULL,
  disabled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX formulation_product_foreign_key_index ON formulation(product_id);
CREATE INDEX formulation_raw_material_foreign_key_index ON formulation(raw_material_id);
