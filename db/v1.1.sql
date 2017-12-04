-- Add status column to event table
-------------------------------------------------------------------------------
ALTER TABLE public.event ADD status VARCHAR(25) DEFAULT 'ACTIVE' NOT NULL;
