<?php

namespace App\Form;

use App\Entity\Appointment;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
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
                'attr' => ['class' => 'form-control'],
            ])
            ->add('department', TextType::class, [
                'label' => 'Department',
                'attr' => ['class' => 'form-control', 'placeholder' => 'e.g. Cardiology'],
            ])
            ->add('doctor', TextType::class, [
                'label' => 'Doctor',
                'attr' => ['class' => 'form-control', 'placeholder' => 'Doctor name'],
            ])
            ->add('message', TextareaType::class, [
                'label' => 'Message (optional)',
                'required' => false,
                'attr' => ['class' => 'form-control', 'rows' => 5, 'placeholder' => 'Your message'],
            ])
            ->add('status', ChoiceType::class, [
                'label' => 'Status',
                'choices' => [
                    'Pending' => Appointment::STATUS_PENDING,
                    'Confirmed' => Appointment::STATUS_CONFIRMED,
                    'Cancelled' => Appointment::STATUS_CANCELLED,
                    'Completed' => Appointment::STATUS_COMPLETED,
                    'Expired' => Appointment::STATUS_EXPIRED,
                ],
                'attr' => ['class' => 'form-control'],
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
