<?php

namespace App\Controller;

use App\Entity\Doctor;
use App\Form\DoctorCompleteProfileType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/doctor')]
#[IsGranted('ROLE_DOCTOR')] 
class DoctorController extends AbstractController
{
    #[Route('/complete-profile', name: 'app_doctor_complete_profile')]
    public function completeProfile(Request $request, EntityManagerInterface $entityManager): Response
    {
        /** @var Doctor $doctor */
        $doctor = $this->getUser();

        $form = $this->createForm(DoctorCompleteProfileType::class, $doctor);
        
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $doctor->setIsVerified(true);
            
            $entityManager->flush();

            $this->addFlash('success', 'Professional profile updated successfully!');
            
            return $this->redirectToRoute('app_dashboard');
        }

        return $this->render('doctor/complete_profile.html.twig', [
            'form' => $form->createView(),
        ]);
    }
}