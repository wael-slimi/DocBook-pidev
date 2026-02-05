<?php

namespace App\Controller;

use App\Entity\Caregiver;
use App\Form\CaregiverType;
use App\Repository\CaregiverRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/caregiver')]
final class CaregiverController extends AbstractController
{
    #[Route(name: 'app_caregiver_index', methods: ['GET'])]
    public function index(CaregiverRepository $caregiverRepository): Response
    {
        return $this->render('caregiver/index.html.twig', [
            'caregivers' => $caregiverRepository->findAll(),
        ]);
    }

    #[Route('/new', name: 'app_caregiver_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $caregiver = new Caregiver();
        $form = $this->createForm(CaregiverType::class, $caregiver);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($caregiver);
            $entityManager->flush();

            return $this->redirectToRoute('app_caregiver_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('caregiver/new.html.twig', [
            'caregiver' => $caregiver,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_caregiver_show', methods: ['GET'])]
    public function show(Caregiver $caregiver): Response
    {
        return $this->render('caregiver/show.html.twig', [
            'caregiver' => $caregiver,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_caregiver_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Caregiver $caregiver, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(CaregiverType::class, $caregiver);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_caregiver_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('caregiver/edit.html.twig', [
            'caregiver' => $caregiver,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_caregiver_delete', methods: ['POST'])]
    public function delete(Request $request, Caregiver $caregiver, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$caregiver->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($caregiver);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_caregiver_index', [], Response::HTTP_SEE_OTHER);
    }
}
