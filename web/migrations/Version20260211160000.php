<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260211160000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'DossierMedical and Document tables';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE dossier_medical (
            id INT AUTO_INCREMENT NOT NULL,
            numero_dossier VARCHAR(50) NOT NULL,
            patient_nom VARCHAR(120) NOT NULL,
            patient_prenom VARCHAR(120) NOT NULL,
            date_naissance DATE DEFAULT NULL,
            genre VARCHAR(20) DEFAULT NULL,
            email VARCHAR(180) DEFAULT NULL,
            telephone VARCHAR(30) DEFAULT NULL,
            adresse LONGTEXT DEFAULT NULL,
            remarques LONGTEXT DEFAULT NULL,
            date_creation DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\',
            date_modification DATETIME DEFAULT NULL,
            UNIQUE INDEX UNIQ_dossier_numero (numero_dossier),
            PRIMARY KEY(id)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ENGINE = InnoDB');

        $this->addSql('CREATE TABLE document (
            id INT AUTO_INCREMENT NOT NULL,
            dossier_medical_id INT NOT NULL,
            titre VARCHAR(200) NOT NULL,
            type_document VARCHAR(50) NOT NULL,
            date_document DATE NOT NULL,
            contenu LONGTEXT DEFAULT NULL,
            fichier_path VARCHAR(500) DEFAULT NULL,
            date_creation DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\',
            date_modification DATETIME DEFAULT NULL,
            INDEX IDX_doc_dossier (dossier_medical_id),
            PRIMARY KEY(id),
            CONSTRAINT FK_doc_dossier FOREIGN KEY (dossier_medical_id) REFERENCES dossier_medical (id) ON DELETE CASCADE
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ENGINE = InnoDB');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE document');
        $this->addSql('DROP TABLE dossier_medical');
    }
}
