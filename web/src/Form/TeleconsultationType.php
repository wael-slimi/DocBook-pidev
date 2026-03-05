<?php

namespace App\Form;

use App\Entity\Appointment;
use App\Entity\Teleconsultation;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
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
                    return sprintf('#%d - %s (%s)', $a->getId(), $a->getScheduledAt()?->format('Y-m-d H:i'), $a->getDepartment());
                },
                'attr' => ['class' => 'form-control'],
                'placeholder' => 'Select appointment',
            ])
            ->add('duration', IntegerType::class, [
                'label' => 'Duration (minutes)',
                'attr' => ['class' => 'form-control', 'min' => 1, 'placeholder' => 'e.g. 30'],
            ])
            ->add('meetingUrl', UrlType::class, [
                'label' => 'Meeting URL',
                'attr' => ['class' => 'form-control', 'placeholder' => 'https://meet.example.com/...'],
            ])
            ->add('mode', ChoiceType::class, [
                'label' => 'Mode',
                'choices' => [
                    'Video' => Teleconsultation::MODE_VIDEO,
                    'Chat' => Teleconsultation::MODE_CHAT,
                    'Audio' => Teleconsultation::MODE_AUDIO,
                ],
                'attr' => ['class' => 'form-control'],
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
