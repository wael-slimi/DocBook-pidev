<?php

declare(strict_types=1);

namespace App\Form;

use App\Entity\Document;
use App\Entity\DossierMedical;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class DocumentType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class, [
                'label' => 'Titre du document',
            ])
            ->add('typeDocument', ChoiceType::class, [
                'label' => 'Type de document',
                'choices' => array_combine(
                    ['Consultation', 'Ordonnance', 'Certificat', 'Imagerie', 'Autre'],
                    Document::TYPES
                ),
            ])
            ->add('dateDocument', DateType::class, [
                'label' => 'Date du document',
                'widget' => 'single_text',
                'required' => true,
            ])
            ->add('contenu', TextareaType::class, [
                'label' => 'Contenu',
                'required' => false,
            ])
            ->add('fichierPath', TextType::class, [
                'label' => 'Chemin fichier (optionnel)',
                'required' => false,
            ]);

        if ($options['with_dossier'] ?? false) {
            $builder->add('dossierMedical', EntityType::class, [
                'class' => DossierMedical::class,
                'label' => 'Dossier médical',
                'choice_label' => function (DossierMedical $d) {
                    return $d->getNumeroDossier() . ' - ' . $d->getPatientNom() . ' ' . $d->getPatientPrenom();
                },
            ]);
        }
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Document::class,
            'with_dossier' => false,
        ]);
        $resolver->setAllowedTypes('with_dossier', 'bool');
    }
}