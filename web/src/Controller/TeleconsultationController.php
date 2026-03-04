<?php

namespace App\Controller;

use App\Entity\Teleconsultation;
use App\Repository\TeleconsultationRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/teleconsultation')]
class TeleconsultationController extends AbstractController
{
    #[Route('/', name: 'app_teleconsultation_index', methods: ['GET'])]
    public function index(TeleconsultationRepository $teleconsultationRepository): Response
    {
        return $this->render('teleconsultation/index.html.twig', [
            'teleconsultations' => $teleconsultationRepository->findAll(),
        ]);
    }

    #[Route('/{id}/room', name: 'app_teleconsultation_room', methods: ['GET'])]
    public function room(Teleconsultation $teleconsultation): Response
    {
        return $this->render('consultation/video_room.html.twig', [
            'teleconsultation' => $teleconsultation,
        ]);
    }
}
