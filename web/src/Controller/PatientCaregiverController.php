<?php

namespace App\Controller;

use App\Entity\PatientCaregiver;
use App\Form\PatientCaregiverType;
use App\Repository\PatientCaregiverRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/patient/caregiver')]
final class PatientCaregiverController extends AbstractController
{
    #[Route(name: 'app_patient_caregiver_index', methods: ['GET'])]
    public function index(PatientCaregiverRepository $patientCaregiverRepository): Response
    {
        return $this->render('patient_caregiver/index.html.twig', [
            'patient_caregivers' => $patientCaregiverRepository->findAll(),
        ]);
    }

    #[Route('/new', name: 'app_patient_caregiver_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $patientCaregiver = new PatientCaregiver();
        $form = $this->createForm(PatientCaregiverType::class, $patientCaregiver);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($patientCaregiver);
            $entityManager->flush();

            return $this->redirectToRoute('app_patient_caregiver_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('patient_caregiver/new.html.twig', [
            'patient_caregiver' => $patientCaregiver,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_patient_caregiver_show', methods: ['GET'])]
    public function show(PatientCaregiver $patientCaregiver): Response
    {
        return $this->render('patient_caregiver/show.html.twig', [
            'patient_caregiver' => $patientCaregiver,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_patient_caregiver_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, PatientCaregiver $patientCaregiver, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(PatientCaregiverType::class, $patientCaregiver);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_patient_caregiver_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('patient_caregiver/edit.html.twig', [
            'patient_caregiver' => $patientCaregiver,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_patient_caregiver_delete', methods: ['POST'])]
    public function delete(Request $request, PatientCaregiver $patientCaregiver, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$patientCaregiver->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($patientCaregiver);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_patient_caregiver_index', [], Response::HTTP_SEE_OTHER);
    }
}
