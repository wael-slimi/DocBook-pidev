<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class DailyHealthType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('heart_rate', NumberType::class, [
                'label' => 'Heart Rate (bpm)',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => '72']
            ])
            ->add('blood_pressure', TextType::class, [
                'label' => 'Blood Pressure (mmHg)',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => '120/80']
            ])
            ->add('blood_glucose', NumberType::class, [
                'label' => 'Blood Glucose (mg/dL)',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => '95']
            ])
            ->add('weight', NumberType::class, [
                'label' => 'Weight (kg)',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => '70.5']
            ])
            ->add('temperature', NumberType::class, [
                'label' => 'Temperature (°C)',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => '36.6']
            ])
            ->add('save', SubmitType::class, [
                'label' => 'Save Daily Metrics',
                'attr' => ['class' => 'btn btn-primary mt-3']
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
        ]);
    }
}