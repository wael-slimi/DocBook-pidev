<?php

namespace App\Enum;

enum UserRole: string
{
    case PATIENT = 'patient';
    case DOCTOR = 'doctor';
    case CAREGIVER = 'caregiver';
}