<?php

namespace App\Form;

use App\Entity\Doctor;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class DoctorType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('specialty', TextType::class, [
                'label' => 'Medical Specialty',
                'attr' => ['placeholder' => 'e.g. Cardiology']
            ])
            ->add('licenseNumber', TextType::class, [
                'label' => 'Medical License Number'
            ])
            ->add('consultationFee', MoneyType::class, [
                'currency' => 'TND', // Use your currency code
                'divisor' => 1,
            ])
            ->add('bio', TextareaType::class, [
                'required' => false,
                'attr' => ['rows' => 5]
            ])
        ;
    }

    public function getParent(): string
    {
        return UserType::class;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Doctor::class,
        ]);
    }
}
