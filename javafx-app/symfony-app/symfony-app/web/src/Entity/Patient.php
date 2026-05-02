<?php

namespace App\Entity;

use App\Repository\PatientRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PatientRepository::class)]
class Patient extends User
{

    /**
     * @var Collection<int, PatientCaregiver>
     */
    #[ORM\OneToMany(targetEntity: PatientCaregiver::class, mappedBy: 'patient')]
    private Collection $patientCaregivers;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $caregiver = null;

    public function __construct()
    {
        $this->patientCaregivers = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
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
            $patientCaregiver->setPatient($this);
        }

        return $this;
    }

    public function removePatientCaregiver(PatientCaregiver $patientCaregiver): static
    {
        if ($this->patientCaregivers->removeElement($patientCaregiver)) {
            // set the owning side to null (unless already changed)
            if ($patientCaregiver->getPatient() === $this) {
                $patientCaregiver->setPatient(null);
            }
        }

        return $this;
    }

    public function getCaregiver(): ?string
    {
        return $this->caregiver;
    }

    public function setCaregiver(?string $caregiver): static
    {
        $this->caregiver = $caregiver;

        return $this;
    }
}
