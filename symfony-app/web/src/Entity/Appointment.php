<?php

namespace App\Entity;

use App\Repository\AppointmentRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: AppointmentRepository::class)]
class Appointment
{
    public const STATUS_PENDING = 'Pending';
    public const STATUS_CONFIRMED = 'Confirmed';
    public const STATUS_CANCELLED = 'Cancelled';
    public const STATUS_COMPLETED = 'Completed';
    public const STATUS_URGENT = 'URGENT';
    public const STATUS_EXPIRED = 'Expired';

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(type: Types::DATETIME_MUTABLE)]
    private ?\DateTimeInterface $scheduledAt = null;

    #[ORM\Column(length: 50)]
    private ?string $status = self::STATUS_PENDING;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $reason = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false)]
    private ?User $patient = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false)]
    private ?User $doctor = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $department = null;

    #[ORM\OneToOne(mappedBy: 'appointment', cascade: ['persist', 'remove'])]
    private ?Teleconsultation $teleconsultation = null;

    #[ORM\OneToMany(targetEntity: AppointmentRating::class, mappedBy: 'appointment', cascade: ['persist', 'remove'], orphanRemoval: true)]
    private Collection $ratings;

    public function __construct()
    {
        $this->ratings = new ArrayCollection();
    }

    public function getId(): ?int { return $this->id; }

    public function getScheduledAt(): ?\DateTimeInterface { return $this->scheduledAt; }
    public function setScheduledAt(?\DateTimeInterface $scheduledAt): static { $this->scheduledAt = $scheduledAt; return $this; }

    public function getStatus(): ?string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }

    public function getReason(): ?string { return $this->reason; }
    public function setReason(?string $reason): static { $this->reason = $reason; return $this; }

    public function getPatient(): ?User { return $this->patient; }
    public function setPatient(?User $patient): static { $this->patient = $patient; return $this; }

    public function getDoctor(): ?User { return $this->doctor; }
    public function setDoctor(?User $doctor): static { $this->doctor = $doctor; return $this; }

    public function getDepartment(): ?string { return $this->department; }
    public function setDepartment(?string $department): static { $this->department = $department; return $this; }

    public function getTeleconsultation(): ?Teleconsultation { return $this->teleconsultation; }
    public function setTeleconsultation(?Teleconsultation $teleconsultation): static
    {
        if ($teleconsultation !== null && $teleconsultation->getAppointment() !== $this) {
            $teleconsultation->setAppointment($this);
        }
        $this->teleconsultation = $teleconsultation;
        return $this;
    }

    /**
     * @return Collection<int, AppointmentRating>
     */
    public function getRatings(): Collection
    {
        return $this->ratings;
    }

    public function addRating(AppointmentRating $rating): static
    {
        if (!$this->ratings->contains($rating)) {
            $this->ratings->add($rating);
            $rating->setAppointment($this);
        }
        return $this;
    }

    public function removeRating(AppointmentRating $rating): static
    {
        if ($this->ratings->removeElement($rating)) {
            if ($rating->getAppointment() === $this) {
                $rating->setAppointment(null);
            }
        }
        return $this;
    }
}