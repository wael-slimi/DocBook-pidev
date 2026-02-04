<?php

namespace App\Enum;

enum CaregiverStatus: string
{
    case PENDING = 'PENDING';
    case ACTIVE = 'ACTIVE';
}