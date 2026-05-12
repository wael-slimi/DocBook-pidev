<?php

namespace App\Controller;

use App\Entity\Appointment;
use App\Entity\AppointmentRating;
use App\Form\AppointmentRatingType;
use App\Repository\AppointmentRatingRepository;
use App\Repository\AppointmentRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/appointment')]
class AppointmentRatingController extends AbstractController
{
    public function __construct(
        private readonly AppointmentRatingRepository $ratingRepository,
        private readonly AppointmentRepository $appointmentRepository,
        private readonly EntityManagerInterface $em,
    ) {
    }

    #[Route('/{id}/rate', name: 'app_appointment_rate', methods: ['GET', 'POST'])]
    #[IsGranted('ROLE_USER')]
    public function rate(Request $request, Appointment $appointment): Response
    {
        // Check if already rated
        $user = $this->getUser();
        if ($this->ratingRepository->hasRated($appointment->getId(), $user->getId())) {
            $this->addFlash('warning', 'Vous avez déjà noté ce rendez-vous.');
            return $this->redirectToRoute('app_appointment_show', ['id' => $appointment->getId()]);
        }

        $rating = new AppointmentRating();
        $rating->setAppointment($appointment);
        $rating->setPatient($user);

        $form = $this->createForm(AppointmentRatingType::class, $rating);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $rating->setRatedAt(new \DateTimeImmutable());
            $this->ratingRepository->save($rating, true);

            $this->addFlash('success', 'Votre note a été enregistrée.');
            return $this->redirectToRoute('app_appointment_show', ['id' => $appointment->getId()]);
        }

        return $this->render('appointment_rating/rate.html.twig', [
            'form' => $form->createView(),
            'appointment' => $appointment,
        ]);
    }
}