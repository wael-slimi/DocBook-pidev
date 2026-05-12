<?php

namespace App\Controller;

use App\Entity\User; // Ensure this matches your User entity
use App\Form\DoctorCompleteProfileType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use App\Form\DoctorSettingsType;

#[Route('/doctor')]
#[IsGranted('ROLE_DOCTOR')] 
class DoctorController extends AbstractController
{
    #[Route('/dashboard', name: 'app_doctor_dashboard')]
    public function index(): Response
    {
        return $this->render('doctor/dashboard.html.twig');
    }
    
    #[Route('/medical-records', name: 'app_medecin_dossier_index')]
    public function listRecords(): Response
    {
        return $this->render('doctor/records/index.html.twig');
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