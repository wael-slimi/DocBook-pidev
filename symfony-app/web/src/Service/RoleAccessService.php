<?php

declare(strict_types=1);

namespace App\Service;

use Symfony\Component\HttpFoundation\RequestStack;

final class RoleAccessService
{
    public const ROLE_ADMIN = 'admin';
    public const ROLE_MEDECIN = 'medecin';
    public const ROLE_PATIENT = 'patient';

    public function __construct(
        private readonly RequestStack $requestStack
    ) {
    }

    public function getCurrentRole(): ?string
    {
        $request = $this->requestStack->getCurrentRequest();
        if ($request === null) {
            return null;
        }
        $path = $request->getPathInfo();
        if (str_starts_with($path, '/admin')) {
            return self::ROLE_ADMIN;
        }
        if (str_starts_with($path, '/medecin')) {
            return self::ROLE_MEDECIN;
        }
        if (str_starts_with($path, '/patient')) {
            return self::ROLE_PATIENT;
        }
        return null;
    }

    public function isAdmin(): bool
    {
        return $this->getCurrentRole() === self::ROLE_ADMIN;
    }

    public function isMedecin(): bool
    {
        return $this->getCurrentRole() === self::ROLE_MEDECIN;
    }

    public function isPatient(): bool
    {
        return $this->getCurrentRole() === self::ROLE_PATIENT;
    }

    public function canEditDossier(): bool
    {
        return $this->isAdmin() || $this->isPatient();
    }

    public function canDeleteDossier(): bool
    {
        return $this->isAdmin() || $this->isPatient();
    }

    public function canCreateDossier(): bool
    {
        return $this->isAdmin() || $this->isPatient();
    }

    public function canReadDossier(): bool
    {
        return $this->getCurrentRole() !== null;
    }

    public function canManageDocuments(): bool
    {
        return $this->getCurrentRole() !== null;
    }

    public function getPrefix(): string
    {
        $role = $this->getCurrentRole();
        return $role ? '/' . $role : '';
    }
}
