<?php

namespace App\Entity;

use App\Repository\CaregiverRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CaregiverRepository::class)]
class Caregiver extends User
{

    #[ORM\Column(length: 50)]
    private ?string $relationship_type = null;


    public function getId(): ?int
    {
        return $this->id;
    }

    public function getRelationshipType(): ?string
    {
        return $this->relationship_type;
    }

    public function setRelationshipType(string $relationship_type): static
    {
        $this->relationship_type = $relationship_type;

        return $this;
    }

    
}
