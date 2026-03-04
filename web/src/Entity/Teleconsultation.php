<?php

namespace App\Entity;

use App\Repository\TeleconsultationRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: TeleconsultationRepository::class)]
class Teleconsultation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 500)]
    private ?string $videoLink = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $accessCode = null;

    #[ORM\OneToOne(inversedBy: 'teleconsultation', cascade: ['persist'])]
    #[ORM\JoinColumn(nullable: false)]
    private ?Appointment $appointment = null;

    public function getId(): ?int { return $this->id; }

    public function getVideoLink(): ?string { return $this->videoLink; }
    public function setVideoLink(string $videoLink): static { $this->videoLink = $videoLink; return $this; }

    public function getAccessCode(): ?string { return $this->accessCode; }
    public function setAccessCode(?string $accessCode): static { $this->accessCode = $accessCode; return $this; }

    public function getAppointment(): ?Appointment { return $this->appointment; }
    public function setAppointment(?Appointment $appointment): static { $this->appointment = $appointment; return $this; }
}
