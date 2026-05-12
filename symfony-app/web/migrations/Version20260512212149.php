<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260512212149 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Clean up leftover medical_document and constraints';
    }

    public function up(Schema $schema): void
    {
        // Drop the unique constraint first
        $this->addSql('ALTER TABLE appointment_rating DROP CONSTRAINT IF EXISTS appointment_rating_appointment_id_patient_id_key');
        // Drop the index if it still exists
        $this->addSql('DROP INDEX IF EXISTS appointment_rating_appointment_id_patient_id_key');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('CREATE UNIQUE INDEX appointment_rating_appointment_id_patient_id_key ON appointment_rating (appointment_id, patient_id)');
    }
}