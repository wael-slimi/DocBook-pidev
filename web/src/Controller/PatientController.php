<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Symfony\Component\HttpFoundation\Request;
use Doctrine\ORM\EntityManagerInterface;
use App\Entity\User;
use App\Form\PatientSettingsType;

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

    #[Route('/settings', name: 'app_patient_settings')] 
    public function settings(Request $request, EntityManagerInterface $entityManager): Response
    {
        /** @var User $patient */
        $patient = $this->getUser();
        
        $form = $this->createForm(PatientSettingsType::class, $patient);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $pictureFile = $form->get('profilePicture')->getData();

            if ($pictureFile) {
                $newFilename = uniqid('pat_').'.'.$pictureFile->guessExtension();
                
                $pictureFile->move(
                    $this->getParameter('profile_pictures_directory'), 
                    $newFilename
                );
                
                $patient->setProfilePicture($newFilename);
            }

            $entityManager->flush();
            
            $this->addFlash('success', 'Settings updated successfully!');
            return $this->redirectToRoute('app_patient_settings');
        }

        return $this->render('patient/settings.html.twig', [
            'form' => $form->createView(),
        ]);
    }
}