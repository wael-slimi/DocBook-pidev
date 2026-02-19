<?php

namespace App\Entity;

use App\Repository\DoctorRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use App\Enum\Specialty;

#[ORM\Entity(repositoryClass: DoctorRepository::class)]
class Doctor extends User
{
    #[ORM\Column(type: 'string', enumType: Specialty::class, nullable: true)]   
    private ?Specialty $specialty = null;

    #[ORM\Column(length: 50, nullable: true)] // Made nullable for initial registration
    private ?string $licenseNumber = null;

    #[ORM\Column(type: Types::DECIMAL, precision: 10, scale: 2, nullable: true)]
    private ?string $consultationFee = null;

    #[ORM\Column(type: Types::DECIMAL, precision: 3, scale: 2, nullable: true)]
    private ?string $averageRating = "0.00";

    #[ORM\Column]
    private ?int $totalReviews = 0; // Default to 0

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $bio = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $profileImage = null;

    // --- Constructor to ensure defaults are set ---
    public function __construct()
    {
        $this->totalReviews = 0;
        $this->averageRating = "0.00";
    }

    public function getSpecialty(): ?Specialty
    {
        return $this->specialty;
    }

    public function setSpecialty(?Specialty $specialty): static
    {
        $this->specialty = $specialty;
        return $this;
    }

    public function getLicenseNumber(): ?string
    {
        return $this->licenseNumber;
    }

    public function setLicenseNumber(?string $licenseNumber): static
    {
        $this->licenseNumber = $licenseNumber;
        return $this;
    }

    public function getConsultationFee(): ?string
    {
        return $this->consultationFee;
    }

    public function setConsultationFee(?string $consultationFee): static
    {
        $this->consultationFee = $consultationFee;
        return $this;
    }

    public function getAverageRating(): ?string
    {
        return $this->averageRating;
    }

    public function setAverageRating(?string $averageRating): static
    {
        $this->averageRating = $averageRating;
        return $this;
    }

    public function getTotalReviews(): ?int
    {
        return $this->totalReviews;
    }

    public function setTotalReviews(int $totalReviews): static
    {
        $this->totalReviews = $totalReviews;
        return $this;
    }

    public function getBio(): ?string
    {
        return $this->bio;
    }

    public function setBio(?string $bio): static
    {
        $this->bio = $bio;
        return $this;
    }

    public function getProfileImage(): ?string
    {
        return $this->profileImage;
    }

    public function setProfileImage(?string $profileImage): static
    {
        $this->profileImage = $profileImage;
        return $this;
    }

    
    public function isProfileComplete(): bool
    {
        return $this->specialty !== null && 
               !empty($this->licenseNumber) && 
               !empty($this->consultationFee);
    }
}