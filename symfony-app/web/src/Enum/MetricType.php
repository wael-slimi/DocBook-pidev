<?php

namespace App\Enum;

enum MetricType: string
{
    case HEART_RATE = 'heart_rate';
    case BLOOD_PRESSURE = 'blood_pressure';
    case BLOOD_GLUCOSE = 'blood_glucose';
    case WEIGHT = 'weight';
    case TEMPERATURE = 'temperature';
    case OXYGEN_SATURATION = 'oxygen_saturation';

    public function getLabel(): string
    {
        return match($this) {
            self::HEART_RATE => 'Heart Rate (bpm)',
            self::BLOOD_PRESSURE => 'Blood Pressure (mmHg)',
            self::BLOOD_GLUCOSE => 'Blood Glucose (mg/dL)',
            self::WEIGHT => 'Weight (kg)',
            self::TEMPERATURE => 'Body Temperature (°C)',
            self::OXYGEN_SATURATION => 'Oxygen Saturation (%)',
        };
    }
}