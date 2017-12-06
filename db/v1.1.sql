-- Add status column to event table
-------------------------------------------------------------------------------
ALTER TABLE public.event ADD status VARCHAR(25) DEFAULT 'ACTIVE' NOT NULL;

-- Change auction is_valid column type from boolean to varchar
-------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION alter_existent_is_valid_auction(boolean) RETURNS varchar
  AS $$
BEGIN
  IF $1 THEN
    RETURN 'ACCEPTED';
  ELSE
    RETURN 'PENDING';
  END IF;
END
$$ language 'plpgsql';

ALTER TABLE public.auction ALTER COLUMN is_valid TYPE VARCHAR(25) USING alter_existent_is_valid_auction(is_valid);