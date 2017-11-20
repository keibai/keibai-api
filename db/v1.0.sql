-- triggers
------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_modified_at()
  RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ language 'plpgsql';

-- user table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS "user"
(
  id        SERIAL                       NOT NULL
    CONSTRAINT user_pkey
    PRIMARY KEY,
  name      VARCHAR(255)                 NOT NULL,
  last_name  VARCHAR(255)                ,
  password  VARCHAR(255)                 NOT NULL,
  email     VARCHAR(255)                 NOT NULL,
  credit    DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS user_id_uindex
  ON "user" (id);
CREATE UNIQUE INDEX IF NOT EXISTS user_email_uindex
  ON "user" (email);

CREATE TRIGGER update_user_modified_at BEFORE UPDATE ON "user" FOR EACH ROW EXECUTE PROCEDURE update_modified_at();

-- event table
------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS event
(
  id           SERIAL       NOT NULL
    CONSTRAINT event_pkey
    PRIMARY KEY
    CONSTRAINT owner
    REFERENCES "user",
  name         VARCHAR(255) NOT NULL,
  auction_time INTEGER DEFAULT 60,
  location     VARCHAR(255),
  auction_type VARCHAR(20) NOT NULL,
  category     VARCHAR(50),
  owner        INTEGER NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS event_id_uindex
  ON event (id);

CREATE TRIGGER update_event_modified_at BEFORE UPDATE ON event FOR EACH ROW EXECUTE PROCEDURE update_modified_at();

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
    CONSTRAINT winner
    REFERENCES "user",
  name           VARCHAR(255)     NOT NULL,
  starting_price DOUBLE PRECISION NOT NULL,
  start_time     TIMESTAMP        NOT NULL,
  status         VARCHAR(25)      NOT NULL,
  is_valid       BOOLEAN DEFAULT FALSE,
  event          INTEGER NOT NULL,
  owner          INTEGER NOT NULL,
  winner         INTEGER NOT NULL
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
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS bid_id_uindex
  ON bid (id);