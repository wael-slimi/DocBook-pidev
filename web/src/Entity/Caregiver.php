<?php

namespace App\Entity;

use App\Enum\RelationshipType; // Import the Enum
use App\Repository\CaregiverRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CaregiverRepository::class)]
class Caregiver extends User
{
    // 1. Changed to nullable so registration doesn't crash
    // 2. Added enumType to use your RelationshipType Enum
    #[ORM\Column(type: 'string', length: 50, nullable: true, enumType: RelationshipType::class)]
    private ?RelationshipType $relationship_type = null;

    /**
     * @var Collection<int, PatientCaregiver>
     */
    #[ORM\OneToMany(targetEntity: PatientCaregiver::class, mappedBy: 'caregiver')]
    private Collection $patientCaregivers;

    public function __construct()
    {
        $this->patientCaregivers = new ArrayCollection();
    }

    // Updated return type to use the Enum
    public function getRelationshipType(): ?RelationshipType
    {
        return $this->relationship_type;
    }

    // Updated parameter type to use the Enum and allow null
    public function setRelationshipType(?RelationshipType $relationship_type): static
    {
        $this->relationship_type = $relationship_type;

        return $this;
    }

    /**
     * @return Collection<int, PatientCaregiver>
     */
    public function getPatientCaregivers(): Collection
    {
        return $this->patientCaregivers;
    }

    public function addPatientCaregiver(PatientCaregiver $patientCaregiver): static
    {
        if (!$this->patientCaregivers->contains($patientCaregiver)) {
            $this->patientCaregivers->add($patientCaregiver);
            $patientCaregiver->setCaregiver($this);
        }

        return $this;
    }

    public function removePatientCaregiver(PatientCaregiver $patientCaregiver): static
    {
        if ($this->patientCaregivers->removeElement($patientCaregiver)) {
            if ($patientCaregiver->getCaregiver() === $this) {
                $patientCaregiver->setCaregiver(null);
            }
        }

        return $this;
    }
}