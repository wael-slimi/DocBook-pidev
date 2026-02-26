<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260226000406 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // 1. Add the column as nullable first (or just add it)
        $this->addSql('ALTER TABLE "user" ADD is_verified BOOLEAN DEFAULT FALSE NOT NULL');
        $this->addSql('ALTER TABLE "user" ADD is2fa_enabled BOOLEAN DEFAULT FALSE NOT NULL');
        
        // IF the migration was generated differently, ensure you run this:
        $this->addSql('UPDATE "user" SET is_verified = FALSE WHERE is_verified IS NULL');
        $this->addSql('UPDATE "user" SET is2fa_enabled = FALSE WHERE is2fa_enabled IS NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE "user" DROP temp_verification_code');
        $this->addSql('ALTER TABLE "user" ALTER is_verified DROP NOT NULL');
    }
}
