<?php

namespace App\Entity;

use App\Enum\CaregiverPermission;
use App\Enum\CaregiverStatus;
use App\Repository\PatientCaregiverRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PatientCaregiverRepository::class)]
class PatientCaregiver
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'patientCaregivers')]
    private ?patient $patient = null;

    #[ORM\ManyToOne(inversedBy: 'patientCaregivers')]
    private ?Caregiver $caregiver = null;

    #[ORM\Column(enumType: CaregiverPermission::class)]
    private ?CaregiverPermission $permissions = null;

    #[ORM\Column(enumType: CaregiverStatus::class)]
    private ?CaregiverStatus $status = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getPatient(): ?patient
    {
        return $this->patient;
    }

    public function setPatient(?patient $patient): static
    {
        $this->patient = $patient;

        return $this;
    }

    public function getCaregiver(): ?Caregiver
    {
        return $this->caregiver;
    }

    public function setCaregiver(?Caregiver $caregiver): static
    {
        $this->caregiver = $caregiver;

        return $this;
    }

    public function getPermissions(): ?CaregiverPermission
    {
        return $this->permissions;
    }

    public function setPermissions(CaregiverPermission $permissions): static
    {
        $this->permissions = $permissions;

        return $this;
    }

    public function getStatus(): ?CaregiverStatus
    {
        return $this->status;
    }

    public function setStatus(CaregiverStatus $status): static
    {
        $this->status = $status;

        return $this;
    }
}
