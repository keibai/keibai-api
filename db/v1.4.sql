-----------------------------------
-- Combinatorial auction changes --
-----------------------------------

-- Add foreign key to good table in bid
------------------------------------------------------------------------------
ALTER TABLE public.bid ADD good INT REFERENCES good (id);

-- Add combinatorial winners field in auction
------------------------------------------------------------------------------
ALTER TABLE public.auction ADD combinatorial_winners VARCHAR(100) NULL;