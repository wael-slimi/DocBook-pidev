<?php

namespace App\Entity;

use App\Enum\UserRole;
use App\Repository\UserRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Gedmo\Mapping\Annotation as Gedmo;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Scheb\TwoFactorBundle\Model\TwoFactorInterface;

#[ORM\Entity(repositoryClass: UserRepository::class)]
#[ORM\Table(name: '`user`')]
#[ORM\InheritanceType('JOINED')]
#[ORM\DiscriminatorMap([
    'user' => User::class,
    'doctor' => Doctor::class,
    'patient' => Patient::class,
    'caregiver' => Caregiver::class,
])]
#[UniqueEntity(fields: ['email'], message: 'There is already an account with this email')] 
class User implements UserInterface, PasswordAuthenticatedUserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    protected ?int $id = null;

    #[ORM\Column(enumType: UserRole::class)]
    private ?UserRole $role = null;

    #[ORM\Column]
    private ?bool $isActive = true;

    #[Gedmo\Timestampable(on: 'create')]
    #[ORM\Column]
    private ?\DateTimeImmutable $creationDate = null;

    #[ORM\Column(length: 255)]
    private ?string $name = null;

    #[ORM\Column(length: 255, unique: true)]
    private ?string $email = null;

    #[ORM\Column(length: 255)]
    private ?string $password = null;

    #[ORM\Column(length: 255, nullable: true, unique: true)]
    private ?string $phone = null;

    #[ORM\Column(type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateOfBirth = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $profilePicture = null;

    #[ORM\Column(type: 'boolean')]
    private bool $isVerified = false;

    #[ORM\Column(type: 'boolean')]
    private bool $is2faEnabled = false;

#[ORM\Column(length: 6, nullable: true)]
     private ?string $tempVerificationCode = null;

     // --- FIELDS TO MATCH JAVAFX DATABASE ---

     #[ORM\Column(length: 20, options: ['default' => 'pending'])]
     private ?string $status = 'pending';

     #[ORM\Column(length: 255, nullable: true)]
     private ?string $resetToken = null;

     #[ORM\Column(type: Types::DATETIME_MUTABLE, nullable: true)]
     private ?\DateTimeInterface $resetTokenExpiry = null;

     #[ORM\Column(length: 10, nullable: true)]
     private ?string $verificationCode = null;

     #[ORM\Column(length: 500, nullable: true)]
     private ?string $avatarUrl = null;

     #[ORM\Column(length: 20, options: ['default' => 'light'])]
     private ?string $themePreference = 'light';

     // --- AUTH METHODS ---

    public function getUserIdentifier(): string
    {
        return (string) $this->email;
    }

    public function getRoles(): array
    {
        $roles = ['ROLE_USER'];
        if ($this->role) {
            $roles[] = 'ROLE_' . strtoupper($this->role->value);
        }
        return array_unique($roles);
    }

    public function getPassword(): ?string
    {
        return $this->password;
    }

    public function setPassword(string $password): static
    {
        $this->password = $password;
        return $this;
    }

    public function eraseCredentials(): void
    {
    }

    // --- GETTERS & SETTERS ---

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getName(): ?string
    {
        return $this->name;
    }

    public function setName(string $name): static
    {
        $this->name = $name;
        return $this;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(string $email): static
    {
        $this->email = $email;
        return $this;
    }

    public function getPhone(): ?string
    {
        return $this->phone;
    }

    public function setPhone(?string $phone): static
    {
        $this->phone = $phone;
        return $this;
    }

    public function is2faEnabled(): bool
    {
        return $this->is2faEnabled;
    }

    public function setIs2faEnabled(bool $is2faEnabled): static
    {
        $this->is2faEnabled = $is2faEnabled;
        return $this;
    }

    public function isTwilioTwoFactorAuthenticationEnabled(): bool
    {
        return $this->is2faEnabled;
    }

    public function isVerified(): bool
    {
        return $this->isVerified;
    }

    public function setIsVerified(bool $isVerified): static
    {
        $this->isVerified = $isVerified;
        return $this;
    }

    public function getRole(): ?UserRole
    {
        return $this->role;
    }

    public function setRole(?UserRole $role): static
    {
        $this->role = $role;
        return $this;
    }

    public function isActive(): ?bool
    {
        return $this->isActive;
    }

    public function setIsActive(bool $isActive): static
    {
        $this->isActive = $isActive;
        return $this;
    }

    public function getDateOfBirth(): ?\DateTimeInterface
    {
        return $this->dateOfBirth;
    }

    public function setDateOfBirth(?\DateTimeInterface $dateOfBirth): static
    {
        $this->dateOfBirth = $dateOfBirth;
        return $this;
    }

    public function getCreationDate(): ?\DateTimeImmutable
    {
        return $this->creationDate;
    }

    public function setCreationDate(?\DateTimeImmutable $creationDate): static
    {
        $this->creationDate = $creationDate;
        return $this;
    }

    public function getProfilePicture(): ?string
    {
        return $this->profilePicture;
    }

    public function setProfilePicture(?string $profilePicture): static
    {
        $this->profilePicture = $profilePicture;
        return $this;
    }

    public function getTempVerificationCode(): ?string
    {
        return $this->tempVerificationCode;
    }

public function setTempVerificationCode(?string $tempVerificationCode): static
     {
         $this->tempVerificationCode = $tempVerificationCode;

         return $this;
     }

     // --- GETTERS & SETTERS FOR JAVAFX FIELDS ---

     public function getStatus(): ?string
     {
         return $this->status;
     }

     public function setStatus(string $status): static
     {
         $this->status = $status;
         return $this;
     }

     public function getResetToken(): ?string
     {
         return $this->resetToken;
     }

     public function setResetToken(?string $resetToken): static
     {
         $this->resetToken = $resetToken;
         return $this;
     }

     public function getResetTokenExpiry(): ?\DateTimeInterface
     {
         return $this->resetTokenExpiry;
     }

     public function setResetTokenExpiry(?\DateTimeInterface $resetTokenExpiry): static
     {
         $this->resetTokenExpiry = $resetTokenExpiry;
         return $this;
     }

     public function getVerificationCode(): ?string
     {
         return $this->verificationCode;
     }

     public function setVerificationCode(?string $verificationCode): static
     {
         $this->verificationCode = $verificationCode;
         return $this;
     }

     public function getAvatarUrl(): ?string
     {
         return $this->avatarUrl;
     }

     public function setAvatarUrl(?string $avatarUrl): static
     {
         $this->avatarUrl = $avatarUrl;
         return $this;
     }

     public function getThemePreference(): ?string
     {
         return $this->themePreference;
     }

     public function setThemePreference(string $themePreference): static
     {
         $this->themePreference = $themePreference;
         return $this;
     }
 }