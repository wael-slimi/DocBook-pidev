<?php

namespace App\Form;

use App\Entity\Appointment;
use App\Entity\Teleconsultation;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\UrlType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class TeleconsultationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('appointment', EntityType::class, [
                'class' => Appointment::class,
                'choice_label' => function (Appointment $a) {
                    return sprintf('Appointment #%d - %s', $a->getId(), $a->getScheduledAt()?->format('Y-m-d H:i'));
                },
                'attr' => ['class' => 'form-control'],
                'placeholder' => 'Select appointment',
            ])
            ->add('videoLink', UrlType::class, [
                'label' => 'Video Meeting Link',
                'attr' => ['class' => 'form-control', 'placeholder' => 'https://meet.example.com/...'],
            ])
            ->add('accessCode', TextType::class, [
                'label' => 'Access Code (optional)',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => 'e.g., ABC123'],
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Teleconsultation::class,
        ]);
    }
}
