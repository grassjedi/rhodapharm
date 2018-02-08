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
  user_id BIGINT REFERENCES usr(id),
  expiry TIMESTAMP,
  session_key TEXT UNIQUE
);

CREATE INDEX usr_session_usr_id ON usr_session(user_id);
CREATE INDEX usr_session_expiry ON usr_session(expiry);
CREATE INDEX usr_session_key ON usr_session(session_key);

CREATE TABLE raw_material (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name TEXT NOT NULL,
  units TEXT NOT NULL,
  disabled BOOLEAN NOT NULL DEFAULT FALSE
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
  quantity FLOAT NOT NULL,
  disabled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX formulation_product_foreign_key_index ON formulation(product_id);
CREATE INDEX formulation_raw_material_foreign_key_index ON formulation(raw_material_id);

CREATE TABLE raw_material_receipt (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  user_id BIGINT NOT NULL REFERENCES usr(id),
  raw_material_id BIGINT REFERENCES raw_material(id),
  date_captured TIMESTAMP NOT NULL DEFAULT current_timestamp,
  invoice_date TIMESTAMP NOT NULL,
  supplier TEXT NOT NULL,
  quantity BIGINT NOT NULL,
  value BIGINT NOT NULL
);

CREATE INDEX raw_material_receipt_raw_material_fk on raw_material_receipt(raw_material_id);