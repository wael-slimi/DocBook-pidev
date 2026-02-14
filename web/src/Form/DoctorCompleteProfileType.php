<?php

namespace App\Form;

use App\Entity\Doctor;
use App\Enum\Specialty; // Import your Enum
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\EnumType; // Use EnumType
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class DoctorCompleteProfileType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('specialty', EnumType::class, [
                'class' => Specialty::class,
                'choice_label' => fn (Specialty $choice) => ucfirst($choice->value),
                'attr' => [
                    'class' => 'form-select rounded-lg border-slate-200 dark:bg-slate-800 dark:border-slate-700 dark:text-white'
                ]
            ])
            ->add('licenseNumber', TextType::class, [
                'attr' => [
                    'class' => 'form-input rounded-lg border-slate-200 dark:bg-slate-800 dark:border-slate-700 dark:text-white',
                    'placeholder' => 'e.g. MED-123456'
                ]
            ])
            ->add('consultationFee', NumberType::class, [
                'attr' => [
                    'class' => 'form-input rounded-lg border-slate-200 dark:bg-slate-800 dark:border-slate-700 dark:text-white',
                    'placeholder' => 'e.g. 100.00'
                ]
            ])
            ->add('bio', TextareaType::class, [
                'attr' => [
                    'class' => 'form-input rounded-lg border-slate-200 dark:bg-slate-800 dark:border-slate-700 dark:text-white',
                    'rows' => 4, 
                    'placeholder' => 'Tell patients about your experience...'
                ]
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Doctor::class,
        ]);
    }
}