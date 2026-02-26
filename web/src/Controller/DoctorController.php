<?php

namespace App\Controller;

use App\Repository\AppointmentRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class DoctorController extends AbstractController
{
    #[Route('/dashboard', name: 'app_dashboard', methods: ['GET'])]
    public function dashboard(AppointmentRepository $appointmentRepository): Response
    {
        $user = $this->getUser();
        $appointments = $user
            ? $appointmentRepository->findTodayByDoctor($user)
            : [];

        return $this->render('doctor/dashboard.html.twig', [
            'appointments' => $appointments,
        ]);
    }

    #[Route('/doctor/search', name: 'app_doctor_search', methods: ['GET'])]
    public function search(): Response
    {
        return $this->render('doctor/search.html.twig');
    }

    #[Route('/doctor/complete-profile', name: 'app_doctor_complete_profile', methods: ['GET'])]
    public function completeProfile(): Response
    {
        return $this->redirectToRoute('app_dashboard');
    }
}
