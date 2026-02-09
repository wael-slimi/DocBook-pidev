<?php

namespace App\Form;

use App\Entity\Caregiver;
use App\Entity\Patient;
use App\Entity\PatientCaregiver;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class PatientCaregiverType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            
            ->add('patient', EntityType::class, [
                'class' => Patient::class,
                'choice_label' => 'name', 
                'placeholder' => 'Select a patient',
            ])
            ->add('caregiver', EntityType::class, [
                'class' => Caregiver::class,
                'choice_label' => 'name',
                'placeholder' => 'Select a caregiver',
            ])
            
            ->add('permissions', ChoiceType::class, [
                'choices'  => [
                    'View Records' => 'view',
                    'Edit Records' => 'edit',
                    'Full Access'  => 'full',
                ],
                'multiple' => true, 
                'expanded' => false, 
            ])
            
            
            ->add('status', ChoiceType::class, [
                'choices' => [
                    'Pending' => 'pending',
                    'Active' => 'active',
                    'Revoked' => 'revoked',
                ]
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => PatientCaregiver::class,
        ]);
    }
}
