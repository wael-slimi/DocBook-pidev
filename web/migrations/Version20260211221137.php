<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260211221137 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE document (id INT AUTO_INCREMENT NOT NULL, dossier_medical_id INT NOT NULL, titre VARCHAR(200) NOT NULL, type_document VARCHAR(50) NOT NULL, date_document DATE NOT NULL, contenu LONGTEXT DEFAULT NULL, fichier_path VARCHAR(500) DEFAULT NULL, date_creation DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', date_modification DATETIME DEFAULT NULL, INDEX IDX_D8698A767750B79F (dossier_medical_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE dossier_medical (id INT AUTO_INCREMENT NOT NULL, numero_dossier VARCHAR(50) NOT NULL, patient_nom VARCHAR(120) NOT NULL, patient_prenom VARCHAR(120) NOT NULL, date_naissance DATE DEFAULT NULL, genre VARCHAR(20) DEFAULT NULL, email VARCHAR(180) DEFAULT NULL, telephone VARCHAR(30) DEFAULT NULL, adresse LONGTEXT DEFAULT NULL, remarques LONGTEXT DEFAULT NULL, date_creation DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', date_modification DATETIME DEFAULT NULL, UNIQUE INDEX UNIQ_3581EE62FB1CFE96 (numero_dossier), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE `user` (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(255) NOT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE document ADD CONSTRAINT FK_D8698A767750B79F FOREIGN KEY (dossier_medical_id) REFERENCES dossier_medical (id) ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE document DROP FOREIGN KEY FK_D8698A767750B79F');
        $this->addSql('DROP TABLE document');
        $this->addSql('DROP TABLE dossier_medical');
        $this->addSql('DROP TABLE `user`');
    }
}
