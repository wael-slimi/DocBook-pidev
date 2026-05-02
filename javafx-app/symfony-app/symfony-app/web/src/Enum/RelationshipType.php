<?php

namespace App\Enum;

enum RelationshipType: string
{
    case FAMILY = 'family';
    case PROFESSIONAL = 'professional';
    case FRIEND = 'friend';
    case OTHER = 'other';
}