--------------------------
-- Auction time changes --
--------------------------

-- Delete auction_time
------------------------------------------------------------------------------
ALTER TABLE public.event
    DROP COLUMN IF EXISTS auction_time;

-- Add ending_time
------------------------------------------------------------------------------
ALTER TABLE public.auction ADD ending_time TIMESTAMP NULL;
