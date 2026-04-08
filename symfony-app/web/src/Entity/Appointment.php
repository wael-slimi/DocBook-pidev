<?php

namespace App\Entity;

use App\Repository\AppointmentRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: AppointmentRepository::class)]
class Appointment
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(type: Types::DATETIME_MUTABLE)]
    private ?\DateTimeInterface $scheduledAt = null;

    #[ORM\Column(length: 50)]
    private ?string $status = 'PENDING'; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $reason = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false)]
    private ?User $patient = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false)]
    private ?User $doctor = null;

    #[ORM\OneToOne(mappedBy: 'appointment', cascade: ['persist', 'remove'])]
    private ?Teleconsultation $teleconsultation = null;

    public function getId(): ?int { return $this->id; }

    public function getScheduledAt(): ?\DateTimeInterface { return $this->scheduledAt; }
    public function setScheduledAt(\DateTimeInterface $scheduledAt): static { $this->scheduledAt = $scheduledAt; return $this; }

    public function getStatus(): ?string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }

    public function getReason(): ?string { return $this->reason; }
    public function setReason(?string $reason): static { $this->reason = $reason; return $this; }

    public function getPatient(): ?User { return $this->patient; }
    public function setPatient(?User $patient): static { $this->patient = $patient; return $this; }

    public function getDoctor(): ?User { return $this->doctor; }
    public function setDoctor(?User $doctor): static { $this->doctor = $doctor; return $this; }

    public function getTeleconsultation(): ?Teleconsultation { return $this->teleconsultation; }
    public function setTeleconsultation(?Teleconsultation $teleconsultation): static 
    {
        // Set the owning side of the relation if necessary
        if ($teleconsultation !== null && $teleconsultation->getAppointment() !== $this) {
            $teleconsultation->setAppointment($this);
        }
        $this->teleconsultation = $teleconsultation;
        return $this;
    }
}