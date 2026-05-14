<?php

namespace App\Controller;

use App\Repository\AppointmentRepository;
use App\Repository\PatientCaregiverRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

class DashboardController extends AbstractController
{

    #[Route('/dashboard', name: 'app_dashboard')]
    #[IsGranted('IS_AUTHENTICATED_FULLY')] // Ensures user is logged in
    public function index(AppointmentRepository $appointmentRepository): Response
    {
        $user = $this->getUser();

        // 1. Check if user is a Doctor
        if ($this->isGranted('ROLE_DOCTOR')) {
            /** @var \App\Entity\User $doctor */
            $doctor = $user;
            $today = new \DateTimeImmutable('today');
            $tomorrow = (new \DateTimeImmutable('today'))->modify('+1 day');
            $todayAppointments = $appointmentRepository->createQueryBuilder('a')
                ->where('a.doctor = :doctor')
                ->andWhere('a.scheduledAt BETWEEN :today AND :tomorrow')
                ->setParameter('doctor', $doctor)
                ->setParameter('today', $today)
                ->setParameter('tomorrow', $tomorrow)
                ->orderBy('a.scheduledAt', 'ASC')
                ->getQuery()
                ->getResult();
            $todayRevenue = count($todayAppointments) * (float) ($doctor->getConsultationFee() ?? 0);
            return $this->render('doctor/dashboard.html.twig', [
                'todayAppointments' => $todayAppointments,
                'todayRevenue' => $todayRevenue,
            ]);
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
        /** @var \App\Entity\User $user */
        $upcomingAppointments = $appointmentRepository->createQueryBuilder('a')
            ->where('a.patient = :patient')
            ->andWhere('a.scheduledAt >= :now')
            ->setParameter('patient', $user)
            ->setParameter('now', new \DateTimeImmutable())
            ->orderBy('a.scheduledAt', 'ASC')
            ->setMaxResults(5)
            ->getQuery()
            ->getResult();

        return $this->render('patient/dashboard.html.twig', [
            'upcomingAppointments' => $upcomingAppointments,
        ]);
    }

    #[Route('/test/doctor', name: 'test_doctor')]
    #[IsGranted('ROLE_DOCTOR')]
    public function testDoctor(AppointmentRepository $appointmentRepository): Response
    {
        /** @var \App\Entity\User $doctor */
        $doctor = $this->getUser();
        $today = new \DateTimeImmutable('today');
        $tomorrow = (new \DateTimeImmutable('today'))->modify('+1 day');
        $todayAppointments = $appointmentRepository->createQueryBuilder('a')
            ->where('a.doctor = :doctor')
            ->andWhere('a.scheduledAt BETWEEN :today AND :tomorrow')
            ->setParameter('doctor', $doctor)
            ->setParameter('today', $today)
            ->setParameter('tomorrow', $tomorrow)
            ->orderBy('a.scheduledAt', 'ASC')
            ->getQuery()
            ->getResult();
        $todayRevenue = count($todayAppointments) * (float) ($doctor->getConsultationFee() ?? 0);
        return $this->render('doctor/dashboard.html.twig', [
            'todayAppointments' => $todayAppointments,
            'todayRevenue' => $todayRevenue,
        ]);
    }

    /**
     * Direct link to test Caregiver UI: http://localhost:8000/test/caregiver
     */
    #[Route('/test/caregiver', name: 'test_caregiver')]
    #[IsGranted('ROLE_CAREGIVER')]
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
    #[IsGranted('ROLE_PATIENT')]
    public function testPatient(AppointmentRepository $appointmentRepository): Response
    {
        /** @var \App\Entity\User $patient */
        $patient = $this->getUser();
        $upcomingAppointments = $appointmentRepository->createQueryBuilder('a')
            ->where('a.patient = :patient')
            ->andWhere('a.scheduledAt >= :now')
            ->setParameter('patient', $patient)
            ->setParameter('now', new \DateTimeImmutable())
            ->orderBy('a.scheduledAt', 'ASC')
            ->setMaxResults(5)
            ->getQuery()
            ->getResult();
        return $this->render('patient/dashboard.html.twig', [
            'upcomingAppointments' => $upcomingAppointments,
        ]);
    }

}
