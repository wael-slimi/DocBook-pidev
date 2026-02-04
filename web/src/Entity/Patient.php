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
    

    #[ORM\Column(type: Types::DATE_MUTABLE)]
    private ?\DateTime $date_of_birth = null;

    /**
     * @var Collection<int, PatientCaregiver>
     */
    #[ORM\OneToMany(targetEntity: PatientCaregiver::class, mappedBy: 'patient')]
    private Collection $patientCaregivers;

    public function __construct()
    {
        $this->patientCaregivers = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDateOfBirth(): ?\DateTime
    {
        return $this->date_of_birth;
    }

    public function setDateOfBirth(\DateTime $date_of_birth): static
    {
        $this->date_of_birth = $date_of_birth;

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
}
