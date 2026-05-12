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
    #[Route('/', name: 'app_patient_dashboard')]
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
        // Get search parameters
        $searchTerm = $request->query->get('q', '');
        $specialty = $request->query->get('specialty', 'All');

        // Handle empty strings for price to prevent (float)0 issues
        $minPriceInput = $request->query->get('minPrice');
        $maxPriceInput = $request->query->get('maxPrice');

        // Only apply price filters if the value is greater than 0
        $minPrice = ($request->query->get('minPrice') > 0) ? (float)$request->query->get('minPrice') : null;
        $maxPrice = ($request->query->get('maxPrice') > 0) ? (float)$request->query->get('maxPrice') : null;
        
        // Convert specialty to lowercase to match Enum backed values if it's not "All"
        $searchSpecialty = ($specialty !== 'All') ? strtolower($specialty) : 'All';

        // Fetch doctors
        $doctors = $userRepository->findDoctorsByFilters(
            $searchTerm, 
            $searchSpecialty, 
            $minPrice, 
            $maxPrice
        );

        return $this->render('patient/search.html.twig', [
            'doctors'           => $doctors,
            'searchTerm'        => $searchTerm,
            'selectedSpecialty' => $specialty, // Keep original case for the select dropdown
            'minPrice'          => $minPrice,
            'maxPrice'          => $maxPrice,
            'specialties'       => \App\Enum\Specialty::cases(), 
        ]);
    }
}