<?php

namespace App\Controller;

use App\Repository\PatientCaregiverRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

class DashboardController extends AbstractController
{

    #[Route('/dashboard', name: 'app_dashboard')]
    #[IsGranted('IS_AUTHENTICATED_FULLY')] // Ensures user is logged in
    public function index(): Response
    {
        $user = $this->getUser();

        // 1. Check if user is a Doctor
        if ($this->isGranted('ROLE_DOCTOR')) {
            return $this->render('doctor/dashboard.html.twig');
        }

        // 2. Check if user is a Caregiver
        if ($this->isGranted('ROLE_CAREGIVER')) {
            /** @var \App\Entity\Caregiver $caregiver */
            $caregiver = $user;
            $managedPatients = $caregiver->getPatientCaregivers()->map(fn($pc) => $pc->getPatient())->filter(fn($p) => $p !== null)->toArray();

            return $this->render('caregiver/dashboard.html.twig', [
                'managedPatients' => $managedPatients,
            ]);
        }

        // 3. Default to Patient Dashboard
        return $this->render('patient/dashboard.html.twig');
    }

    #[Route('/test/doctor', name: 'test_doctor')]
    public function testDoctor(): Response
    {
        return $this->render('doctor/dashboard.html.twig');
    }

    /**
     * Direct link to test Caregiver UI: http://localhost:8000/test/caregiver
     */
    #[Route('/test/caregiver', name: 'test_caregiver')]
    public function testCaregiver(PatientCaregiverRepository $pcRepo): Response
    {
        /** @var \App\Entity\Caregiver $caregiver */
        $caregiver = $this->getUser();
        $managedPatients = $caregiver->getPatientCaregivers()->map(fn($pc) => $pc->getPatient())->filter(fn($p) => $p !== null)->toArray();

        return $this->render('caregiver/dashboard.html.twig', [
            'managedPatients' => $managedPatients,
        ]);
    }
    #[Route('/test/patient', name: 'test_patient')]
    public function testPatient(): Response
    {
        return $this->render('patient/dashboard.html.twig');
    }

}
