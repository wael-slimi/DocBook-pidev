<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260513220001 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Migrate old document type values to match JavaFX type set';
    }

    public function up(Schema $schema): void
    {
        $this->addSql("UPDATE document SET type_document = 'consultation' WHERE type_document = 'rapport'");
        $this->addSql("UPDATE document SET type_document = 'imagerie' WHERE type_document = 'examen'");
        $this->addSql("UPDATE document SET type_document = 'certificat' WHERE type_document = 'compte_rendu'");
    }

    public function down(Schema $schema): void
    {
        $this->addSql("UPDATE document SET type_document = 'rapport' WHERE type_document = 'consultation'");
        $this->addSql("UPDATE document SET type_document = 'examen' WHERE type_document = 'imagerie'");
        $this->addSql("UPDATE document SET type_document = 'compte_rendu' WHERE type_document = 'certificat'");
    }
}
