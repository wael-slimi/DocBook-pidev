<?php

namespace App\Form;

use App\Entity\Caregiver;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class CaregiverType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('role')
            ->add('isActive')
            ->add('created_at', null, [
                'widget' => 'single_text',
            ])
            ->add('creationDate', null, [
                'widget' => 'single_text',
            ])
            ->add('name')
            ->add('email')
            ->add('password')
            ->add('phone')
            ->add('relationship_type')
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Caregiver::class,
        ]);
    }
}
