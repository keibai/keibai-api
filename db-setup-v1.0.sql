-- user table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS "user"
(
  id        SERIAL                       NOT NULL
    CONSTRAINT user_pkey
    PRIMARY KEY,
  name      VARCHAR(255)                 NOT NULL,
  last_name  VARCHAR(255)                 NOT NULL,
  password  VARCHAR(255)                 NOT NULL,
  email     VARCHAR(255)                 NOT NULL,
  country   VARCHAR(255)                 NOT NULL,
  city      VARCHAR(255)                 NOT NULL,
  address   VARCHAR(255)                 NOT NULL,
  zip_code   VARCHAR(255)                 NOT NULL,
  credit    DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
  created_at TIMESTAMP                    NOT NULL,
  updated_at TIMESTAMP                    NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS user_id_uindex
  ON "user" (id);
CREATE UNIQUE INDEX IF NOT EXISTS user_email_uindex
  ON "user" (email);

-- auction_type table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auction_type
(
  id   SERIAL  NOT NULL
    CONSTRAINT auction_type_pkey
    PRIMARY KEY,
  name INTEGER NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS auction_type_id_uindex
  ON auction_type (id);
CREATE UNIQUE INDEX IF NOT EXISTS auction_type_name_uindex
  ON auction_type (name);

-- category table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS category
(
  id   SERIAL       NOT NULL
    CONSTRAINT category_pkey
    PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS category_id_uindex
  ON category (id);
CREATE UNIQUE INDEX IF NOT EXISTS category_name_uindex
  ON category (name);

-- event table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS event
(
  id           SERIAL       NOT NULL
    CONSTRAINT event_pkey
    PRIMARY KEY
    CONSTRAINT auction_type
    REFERENCES auction_type
    CONSTRAINT category
    REFERENCES category
    CONSTRAINT owner
    REFERENCES "user",
  name         VARCHAR(255) NOT NULL,
  auction_time INTEGER DEFAULT 60,
  location     VARCHAR(255) NOT NULL,
  created_at   TIMESTAMP    NOT NULL,
  updated_at   TIMESTAMP    NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS event_id_uindex
  ON event (id);

-- auction_status table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auction_status
(
  id   SERIAL       NOT NULL
    CONSTRAINT auction_status_pkey
    PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS auction_status_id_uindex
  ON auction_status (id);
CREATE UNIQUE INDEX IF NOT EXISTS auction_status_name_uindex
  ON auction_status (name);

-- auction table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auction
(
  id             SERIAL           NOT NULL
    CONSTRAINT auction_pkey
    PRIMARY KEY
    CONSTRAINT event
    REFERENCES event
    CONSTRAINT owner
    REFERENCES "user"
    CONSTRAINT status
    REFERENCES auction_status
    CONSTRAINT winner
    REFERENCES "user",
  name           VARCHAR(255)     NOT NULL,
  starting_price DOUBLE PRECISION NOT NULL,
  start_time     TIMESTAMP        NOT NULL,
  is_valid       BOOLEAN DEFAULT FALSE
);
CREATE UNIQUE INDEX IF NOT EXISTS auction_id_uindex
  ON auction (id);


-- good table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.good
(
  id SERIAL PRIMARY KEY NOT NULL,
  name VARCHAR(255) NOT NULL,
  image BYTEA,
  CONSTRAINT auction FOREIGN KEY (id) REFERENCES auction (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS good_id_uindex ON public.good (id);

-- bid table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bid
(
  id         SERIAL           NOT NULL
    CONSTRAINT bid_pkey
    PRIMARY KEY
    CONSTRAINT auction
    REFERENCES auction
    CONSTRAINT owner
    REFERENCES "user",
  amount     DOUBLE PRECISION NOT NULL,
  created_at TIMESTAMP        NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS bid_id_uindex
  ON bid (id);