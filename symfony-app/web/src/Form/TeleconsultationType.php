<?php

declare(strict_types=1);

namespace App\Form;

use App\Entity\Appointment;
use App\Entity\Teleconsultation;
use App\Repository\AppointmentRepository;
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
                'label' => 'Associated Appointment',
                'query_builder' => function (AppointmentRepository $repo) use ($options) {
                    $qb = $repo->createQueryBuilder('a')->orderBy('a.scheduledAt', 'DESC');
                    if ($options['doctor'] !== null) {
                        $qb->where('a.doctor = :doctor')
                           ->andWhere('a.status = :status')
                           ->setParameter('doctor', $options['doctor'])
                           ->setParameter('status', Appointment::STATUS_PENDING);
                    }
                    return $qb;
                },
                'choice_label' => function (Appointment $a) {
                    return sprintf('%s - %s',
                        $a->getPatient()?->getName() ?? 'Unknown',
                        $a->getScheduledAt()?->format('Y-m-d H:i') ?? 'N/A'
                    );
                },
                'placeholder' => 'Select an appointment',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('mode', ChoiceType::class, [
                'label' => 'Consultation Mode',
                'choices' => [
                    'Video' => Teleconsultation::MODE_VIDEO,
                    'Audio' => Teleconsultation::MODE_AUDIO,
                    'Chat' => Teleconsultation::MODE_CHAT,
                ],
                'attr' => ['class' => 'form-control'],
            ])
            ->add('duration', IntegerType::class, [
                'label' => 'Duration (minutes)',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => 'e.g. 30'],
            ])
            ->add('videoLink', UrlType::class, [
                'label' => 'Meeting URL',
                'attr' => ['class' => 'form-control', 'placeholder' => 'https://meet.example.com/...'],
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Teleconsultation::class,
            'doctor' => null,
        ]);
        $resolver->setAllowedTypes('doctor', ['null', \App\Entity\User::class]);
    }
}
