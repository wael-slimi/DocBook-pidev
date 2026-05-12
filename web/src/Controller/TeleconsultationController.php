<?php

namespace App\Controller;

use App\Entity\Teleconsultation;
use App\Form\TeleconsultationType;
use App\Repository\TeleconsultationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
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

    #[Route('/new', name: 'app_teleconsultation_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $teleconsultation = new Teleconsultation();
        $form = $this->createForm(TeleconsultationType::class, $teleconsultation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($teleconsultation);
            $em->flush();
            $this->addFlash('success', 'Teleconsultation created successfully!');
            
            return $this->redirectToRoute('app_teleconsultation_room', ['id' => $teleconsultation->getId()]);
        }

        return $this->render('teleconsultation/new.html.twig', [
            'form' => $form->createView(),
            'teleconsultation' => $teleconsultation,
        ]);
    }

    #[Route('/{id}/room', name: 'app_teleconsultation_room', methods: ['GET'])]
    public function room(Teleconsultation $teleconsultation): Response
    {
        return $this->render('consultation/video_room.html.twig', [
            'teleconsultation' => $teleconsultation,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_teleconsultation_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Teleconsultation $teleconsultation, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(TeleconsultationType::class, $teleconsultation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            $this->addFlash('success', 'Teleconsultation updated successfully!');
            
            return $this->redirectToRoute('app_teleconsultation_room', ['id' => $teleconsultation->getId()]);
        }

        return $this->render('teleconsultation/edit.html.twig', [
            'teleconsultation' => $teleconsultation,
            'form' => $form->createView(),
        ]);
    }
}
