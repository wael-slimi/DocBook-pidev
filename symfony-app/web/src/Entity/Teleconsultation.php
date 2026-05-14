<?php

declare(strict_types=1);

namespace App\Entity;

use App\Repository\TeleconsultationRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: TeleconsultationRepository::class)]
class Teleconsultation
{
    public const MODE_VIDEO = 'video';
    public const MODE_CHAT = 'chat';
    public const MODE_AUDIO = 'audio';

    public const MODES = [self::MODE_VIDEO, self::MODE_CHAT, self::MODE_AUDIO];

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 500)]
    #[Assert\NotBlank]
    #[Assert\Url]
    private ?string $videoLink = null;

    #[ORM\Column(name: 'access_code', length: 50, nullable: true)]
    #[Assert\Choice(choices: Teleconsultation::MODES)]
    private ?string $mode = null;

    #[ORM\Column(type: Types::INTEGER, nullable: true)]
    #[Assert\Positive]
    private ?int $duration = null;

    #[ORM\OneToOne(inversedBy: 'teleconsultation', cascade: ['persist'])]
    #[ORM\JoinColumn(nullable: false)]
    #[Assert\NotNull]
    private ?Appointment $appointment = null;

    public function getId(): ?int { return $this->id; }

    public function getVideoLink(): ?string { return $this->videoLink; }
    public function setVideoLink(string $videoLink): static { $this->videoLink = $videoLink; return $this; }

    public function getMode(): ?string { return $this->mode; }
    public function setMode(?string $mode): static { $this->mode = $mode; return $this; }

    public function getDuration(): ?int { return $this->duration; }
    public function setDuration(?int $duration): static { $this->duration = $duration; return $this; }

    public function getAppointment(): ?Appointment { return $this->appointment; }
    public function setAppointment(?Appointment $appointment): static { $this->appointment = $appointment; return $this; }
}
