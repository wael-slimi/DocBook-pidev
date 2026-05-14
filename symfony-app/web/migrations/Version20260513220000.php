<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260513220000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add duration column to teleconsultation table';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE teleconsultation ADD duration INT DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE teleconsultation DROP duration');
    }
}
