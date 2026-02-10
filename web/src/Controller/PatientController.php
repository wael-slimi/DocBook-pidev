<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/patient')]
#[IsGranted('IS_AUTHENTICATED_FULLY')]
class PatientController extends AbstractController
{
    #[Route('/', name: 'patient_dashboard')]
    public function dashboard(): Response
    {
        return $this->render('patient/dashboard.html.twig');
    }
    
    #[Route('/records', name: 'patient_records')]
    public function records(): Response
    {
        return $this->render('patient/records.html.twig');
    }
}