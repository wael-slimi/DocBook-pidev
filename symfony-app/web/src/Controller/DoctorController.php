<?php

namespace App\Controller;

use App\Entity\User;
use App\Form\DoctorCompleteProfileType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use App\Form\DoctorSettingsType;
use App\Entity\Appointment;
use App\Repository\AppointmentRepository;

#[Route('/doctor')]
#[IsGranted('ROLE_DOCTOR')]
class DoctorController extends AbstractController
{
    #[Route('/dashboard', name: 'app_doctor_dashboard')]
    public function index(AppointmentRepository $appointmentRepository): Response
    {
        /** @var User $doctor */
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

        $pendingCount = count(array_filter($todayAppointments, fn($a) => $a->getStatus() === Appointment::STATUS_PENDING));
        $todayRevenue = count($todayAppointments) * (float) ($doctor->getConsultationFee() ?? 0);

        return $this->render('doctor/dashboard.html.twig', [
            'todayAppointments' => $todayAppointments,
            'pendingCount' => $pendingCount,
            'todayRevenue' => $todayRevenue,
        ]);
    }
    
    #[Route('/complete-profile', name: 'app_doctor_complete_profile')]
    public function completeProfile(Request $request, EntityManagerInterface $entityManager): Response
    {
        /** @var User $doctor */
        $doctor = $this->getUser();

        $form = $this->createForm(DoctorCompleteProfileType::class, $doctor);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            
            /** @var UploadedFile $pictureFile */
            $pictureFile = $form->get('profilePicture')->getData();

            if ($pictureFile) {
                $newFilename = uniqid('doc_').'.'.$pictureFile->guessExtension();

                try {
                    $pictureFile->move(
                        $this->getParameter('profile_pictures_directory'),
                        $newFilename
                    );
                    
                    $doctor->setProfilePicture($newFilename);
                } catch (\Exception $e) {
                    $this->addFlash('error', 'Failed to upload image.');
                }
            }

            // Mark as verified/completed
            $doctor->setIsVerified(true);
            
            $entityManager->flush();

            $this->addFlash('success', 'Professional profile updated successfully!');
            
            return $this->redirectToRoute('app_dashboard');
        }

        return $this->render('doctor/complete_profile.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/settings', name: 'app_doctor_settings')]
    public function settings(Request $request, EntityManagerInterface $em): Response
    {
        /** @var User $doctor */
        $doctor = $this->getUser();

        $form = $this->createForm(DoctorSettingsType::class, $doctor);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            $this->addFlash('success', 'Settings updated!');
            return $this->redirectToRoute('app_doctor_settings');
        }

        return $this->render('doctor/settings.html.twig', [
            // CHANGE THIS LINE:
            'form' => $form->createView(), 
        ]);
    }
}