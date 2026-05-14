<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260513225000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Normalize appointment status values to match JavaFX casing';
    }

    public function up(Schema $schema): void
    {
        $this->addSql("UPDATE appointment SET status = 'Pending' WHERE status = 'PENDING'");
        $this->addSql("UPDATE appointment SET status = 'Confirmed' WHERE status = 'CONFIRMED'");
        $this->addSql("UPDATE appointment SET status = 'Cancelled' WHERE status = 'CANCELLED'");
        $this->addSql("UPDATE appointment SET status = 'Completed' WHERE status = 'COMPLETED'");
        $this->addSql("UPDATE appointment SET status = 'Expired' WHERE status = 'EXPIRED'");
        $this->addSql("UPDATE appointment SET status = 'Pending' WHERE status IS NULL OR status = ''");
    }

    public function down(Schema $schema): void
    {
        $this->addSql("UPDATE appointment SET status = 'PENDING' WHERE status = 'Pending'");
        $this->addSql("UPDATE appointment SET status = 'CONFIRMED' WHERE status = 'Confirmed'");
        $this->addSql("UPDATE appointment SET status = 'CANCELLED' WHERE status = 'Cancelled'");
        $this->addSql("UPDATE appointment SET status = 'COMPLETED' WHERE status = 'Completed'");
        $this->addSql("UPDATE appointment SET status = 'EXPIRED' WHERE status = 'Expired'");
    }
}
