<?php

namespace App\Enum;

enum CaregiverPermission: string
{
    case READ = 'READ';
    case WRITE = 'WRITE';
    case FULL = 'FULL';
}