-- Healthcare Appointments Database Schema for PostgreSQL
-- Run this SQL to create/add necessary columns for the Appointments module

-- Add missing columns to existing appointment table
ALTER TABLE appointment ADD COLUMN IF NOT EXISTS department VARCHAR(255);

-- Create appointment_rating table (for patient ratings)
CREATE TABLE IF NOT EXISTS appointment_rating (
    id SERIAL PRIMARY KEY,
    appointment_id INT NOT NULL,
    patient_id INT,
    stars INT NOT NULL CHECK (stars >= 1 AND stars <= 5),
    comment TEXT,
    rated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE,
    UNIQUE (appointment_id, patient_id)
);

-- Optional: Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_appointment_user ON appointment(patient_id, doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointment_status ON appointment(status);