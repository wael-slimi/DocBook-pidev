<?php

namespace App\Controller;

use App\Repository\UserRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class HomeController extends AbstractController
{
    #[Route('/', name: 'home')]
    public function index(UserRepository $userRepository): Response
    {
        $doctors = $userRepository->findByRole('ROLE_DOCTOR');

        return $this->render('landing/index.html.twig', [
            'doctors' => $doctors,
        ]);
    }
}