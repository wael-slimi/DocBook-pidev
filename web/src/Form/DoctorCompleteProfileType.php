<?php

namespace App\Form;

use App\Entity\Doctor;
use App\Enum\Specialty; 
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\EnumType; 
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Validator\Constraints\File;

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
            ])

            ->add('profilePicture', FileType::class, [
                'label' => 'Profile Picture',
                'mapped' => false,
                'required' => false,
                'constraints' => [
                    new File(
                        maxSize: '2M',
                        mimeTypes: ['image/jpeg', 'image/png', 'image/webp'],
                        mimeTypesMessage: 'Please upload a valid image (JPEG, PNG, WEBP)'
                    )
                ],
            ]);


    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Doctor::class,
        ]);
    }
}