<?php

namespace App\Controller;

use App\Entity\Appointment;
use App\Entity\User;
use App\Form\AppointmentType;
use App\Repository\AppointmentRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/appointment')]
class AppointmentController extends AbstractController
{
    #[Route('/', name: 'app_appointment_index', methods: ['GET'])]
    public function index(AppointmentRepository $appointmentRepository): Response
    {
        $user = $this->getUser();
        $appointments = $user
            ? $appointmentRepository->findByUser($user)
            : [];

        return $this->render('appointment/index.html.twig', [
            'appointments' => $appointments,
        ]);
    }

    #[Route('/new', name: 'app_appointment_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        /** @var User $user */
        $user = $this->getUser();
        $appointment = new Appointment();
        $appointment->setPatient($user);
        $appointment->setStatus(Appointment::STATUS_PENDING);
        
        $form = $this->createForm(AppointmentType::class, $appointment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($appointment);
            $em->flush();
            $this->addFlash('success', 'Appointment created successfully!');
            
            return $this->redirectToRoute('app_appointment_show', ['id' => $appointment->getId()]);
        }

        return $this->render('appointment/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'app_appointment_show', methods: ['GET'])]
    public function show(Appointment $appointment): Response
    {
        return $this->render('appointment/show.html.twig', [
            'appointment' => $appointment,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_appointment_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Appointment $appointment, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(AppointmentType::class, $appointment);

        // Only doctors can change the appointment status
        if ($this->isGranted('ROLE_DOCTOR')) {
            $form->add('status', ChoiceType::class, [
                'label' => 'Status',
                'choices' => [
                    'Pending' => Appointment::STATUS_PENDING,
                    'Confirmed' => Appointment::STATUS_CONFIRMED,
                    'Cancelled' => Appointment::STATUS_CANCELLED,
                    'Completed' => Appointment::STATUS_COMPLETED,
                    'Expired' => Appointment::STATUS_EXPIRED,
                ],
                'attr' => ['class' => 'form-control'],
            ]);
        }

        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            $this->addFlash('success', 'Appointment updated successfully!');

            return $this->redirectToRoute('app_appointment_show', ['id' => $appointment->getId()]);
        }

        return $this->render('appointment/edit.html.twig', [
            'appointment' => $appointment,
            'form' => $form->createView(),
        ]);
    }
}
