<?php

namespace App\Form;

use App\Entity\Appointment;
use App\Entity\User;
use App\Enum\UserRole;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class AppointmentType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
->add('scheduledAt', DateTimeType::class, [
                 'widget' => 'single_text',
                 'label' => 'Date & Time',
                 'required' => true,
                 'attr' => ['class' => 'form-control'],
             ])
            ->add('doctor', EntityType::class, [
                'class' => User::class,
                'choice_label' => 'name',
                'label' => 'Doctor',
                'placeholder' => 'Select a doctor',
                'query_builder' => function($er) {
                    return $er->createQueryBuilder('u')
                        ->where('u.role = :role')
                        ->setParameter('role', UserRole::DOCTOR);
                },
                'attr' => ['class' => 'form-control'],
            ])
            ->add('department', TextType::class, [
                'label' => 'Department',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => 'e.g., Cardiology'],
            ])
            ->add('reason', TextareaType::class, [
                'label' => 'Message (optional)',
                'required' => false,
                'attr' => ['class' => 'form-control', 'rows' => 5, 'placeholder' => 'Your message'],
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Appointment::class,
        ]);
    }
}
