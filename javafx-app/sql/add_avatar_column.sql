-- Run this SQL to add avatar_url column to the user table
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);