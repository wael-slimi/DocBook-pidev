<?php

declare(strict_types=1);

namespace App\Controller;

use App\Entity\Teleconsultation;
use App\Form\TeleconsultationType;
use App\Repository\TeleconsultationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/teleconsultation')]
#[IsGranted('ROLE_USER')]
class TeleconsultationController extends AbstractController
{
    public function __construct(
        private readonly TeleconsultationRepository $repository,
        private readonly EntityManagerInterface $em,
    ) {
    }

    #[Route('/', name: 'app_teleconsultation_index', methods: ['GET'])]
    public function index(Request $request): Response
    {
        $user = $this->getUser();
        $q = $request->query->get('q');
        $mode = $request->query->get('mode');

        if ($this->isGranted('ROLE_DOCTOR')) {
            $teleconsultations = $this->repository->searchAndFilter($q, $mode, doctor: $user);
        } elseif ($this->isGranted('ROLE_PATIENT')) {
            $teleconsultations = $this->repository->searchAndFilter($q, $mode, patient: $user);
        } else {
            $teleconsultations = $this->repository->searchAndFilter($q, $mode);
        }

        return $this->render('teleconsultation/index.html.twig', [
            'teleconsultations' => $teleconsultations,
            'q' => $q,
            'current_mode' => $mode,
        ]);
    }

    #[Route('/new', name: 'app_teleconsultation_new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        $teleconsultation = new Teleconsultation();
        $options = ['doctor' => $this->isGranted('ROLE_DOCTOR') ? $this->getUser() : null];
        $form = $this->createForm(TeleconsultationType::class, $teleconsultation, $options);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $this->repository->save($teleconsultation, true);
            $this->addFlash('success', 'Teleconsultation created successfully!');
            return $this->redirectToRoute('app_teleconsultation_index');
        }

        return $this->render('teleconsultation/form.html.twig', [
            'teleconsultation' => $teleconsultation,
            'form' => $form,
            'is_edit' => false,
        ]);
    }

    #[Route('/{id}', name: 'app_teleconsultation_show', methods: ['GET'])]
    public function show(Teleconsultation $teleconsultation): Response
    {
        $this->denyAccessUnlessOwner($teleconsultation);
        return $this->render('teleconsultation/show.html.twig', [
            'teleconsultation' => $teleconsultation,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_teleconsultation_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Teleconsultation $teleconsultation): Response
    {
        $this->denyAccessUnlessOwner($teleconsultation);
        $options = ['doctor' => $this->isGranted('ROLE_DOCTOR') ? $this->getUser() : null];
        $form = $this->createForm(TeleconsultationType::class, $teleconsultation, $options);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $this->em->flush();
            $this->addFlash('success', 'Teleconsultation updated successfully!');
            return $this->redirectToRoute('app_teleconsultation_index');
        }

        return $this->render('teleconsultation/form.html.twig', [
            'teleconsultation' => $teleconsultation,
            'form' => $form,
            'is_edit' => true,
        ]);
    }

    #[Route('/{id}', name: 'app_teleconsultation_delete', methods: ['POST'])]
    public function delete(Request $request, Teleconsultation $teleconsultation): Response
    {
        $this->denyAccessUnlessOwner($teleconsultation);
        if ($this->isCsrfTokenValid('delete' . $teleconsultation->getId(), $request->request->get('_token'))) {
            $this->repository->remove($teleconsultation, true);
            $this->addFlash('success', 'Teleconsultation deleted successfully!');
        }
        return $this->redirectToRoute('app_teleconsultation_index');
    }

    #[Route('/{id}/room', name: 'app_teleconsultation_room', methods: ['GET'])]
    public function room(Teleconsultation $teleconsultation): Response
    {
        $this->denyAccessUnlessOwner($teleconsultation);
        return $this->render('consultation/video_room.html.twig', [
            'teleconsultation' => $teleconsultation,
        ]);
    }

    private function denyAccessUnlessOwner(Teleconsultation $teleconsultation): void
    {
        $user = $this->getUser();
        $appointment = $teleconsultation->getAppointment();
        if ($appointment === null || ($appointment->getPatient() !== $user && $appointment->getDoctor() !== $user)) {
            throw $this->createAccessDeniedException('Access denied.');
        }
    }
}
