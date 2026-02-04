<?php

namespace App\Entity;

use App\Repository\CaregiverRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CaregiverRepository::class)]
class Caregiver extends User
{

    #[ORM\Column(length: 50)]
    private ?string $relationship_type = null;

    /**
     * @var Collection<int, PatientCaregiver>
     */
    #[ORM\OneToMany(targetEntity: PatientCaregiver::class, mappedBy: 'caregiver')]
    private Collection $patientCaregivers;

    public function __construct()
    {
        $this->patientCaregivers = new ArrayCollection();
    }


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
            // set the owning side to null (unless already changed)
            if ($patientCaregiver->getCaregiver() === $this) {
                $patientCaregiver->setCaregiver(null);
            }
        }

        return $this;
    }

    
}
