-- Event status changes
------------------------------------------------------------------------------
UPDATE public.event
  SET status = 'IN_PROGRESS'
  WHERE status = 'CLOSED';

UPDATE public.event
  SET status = 'OPENED'
  WHERE status = 'ACTIVE';

-- Modify Good table to drop in cascade
------------------------------------------------------------------------------
ALTER TABLE public.good DROP CONSTRAINT good_auction_fkey;
ALTER TABLE public.good
  ADD CONSTRAINT good_auction_fkey
FOREIGN KEY (auction) REFERENCES auction (id) ON DELETE CASCADE;

-- Auction status changes
------------------------------------------------------------------------------
UPDATE public.auction
  SET status = 'PENDING'
  WHERE is_valid = 'PENDING';

UPDATE public.auction
  SET status = 'ACCEPTED'
  WHERE status = 'OPENED' AND is_valid = 'ACCEPTED';

UPDATE public.auction
  SET status = 'FINISHED'
  WHERE status = 'CLOSED';

DELETE FROM public.auction
  WHERE is_valid = 'DENIED';

-- Drop Auction is_valid column
------------------------------------------------------------------------------
ALTER TABLE public.auction
  DROP COLUMN IF EXISTS is_valid;
