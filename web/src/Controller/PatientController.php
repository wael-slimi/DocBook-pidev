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
use App\Repository\UserRepository;
USE App\Enum\Specialty;

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

    #[Route('/patient/search', name: 'app_doctor_search')]
    public function search(Request $request, UserRepository $userRepository): Response
    {
        $searchTerm = $request->query->get('q', '');

        $specialty = $request->query->get('specialty', 'All');

        $minPrice = $request->query->get('minPrice') !== '' ? (float)$request->query->get('minPrice') : null;
        $maxPrice = $request->query->get('maxPrice') !== '' ? (float)$request->query->get('maxPrice') : null;

        $doctors = $userRepository->findDoctorsByFilters($searchTerm, $specialty, $minPrice, $maxPrice);

        return $this->render('patient/search.html.twig', [
            'doctors' => $doctors,
            'searchTerm' => $searchTerm,
            'selectedSpecialty' => $specialty, 
            'minPrice' => $minPrice,
            'maxPrice' => $maxPrice,
            'specialties' => Specialty::cases(), 
        ]);
    }
}