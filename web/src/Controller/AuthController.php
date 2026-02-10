<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;

class AuthController extends AbstractController
{

    #[Route('/login', name: 'app_login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        if ($this->getUser()) {
            return $this->redirectToRoute('app_home'); // Or your dashboard
        }

        $error = $authenticationUtils->getLastAuthenticationError();
        $lastUsername = $authenticationUtils->getLastUsername();

        return $this->render('auth/login.html.twig', [
            'last_username' => $lastUsername,
            'error'         => $error, 
        ]);
    }

    /**
     * STEP 1: Basic Information
     */
    #[Route('/register', name: 'app_register', methods: ['GET', 'POST'])]
    public function register(Request $request): Response
    {
        if ($request->isMethod('POST')) {
            $session = $request->getSession();
            $session->set('reg_data', [
                'full_name' => $request->request->get('full_name'),
                'email' => $request->request->get('email'),
            ]);

            return $this->redirectToRoute('app_register_role');
        }

        return $this->render('auth/register_basic.html.twig');
    }

    /**
     * STEP 2: Role Selection
     */
    #[Route('/register/role', name: 'app_register_role', methods: ['GET', 'POST'])]
    public function registerRole(Request $request): Response
    {
        if ($request->isMethod('POST')) {
            $selectedRole = $request->request->get('selected_role');
            
            // Security check: ensure the role is one of the allowed types
            $allowedRoles = ['ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_CAREGIVER'];
            if (!in_array($selectedRole, $allowedRoles)) {
                $selectedRole = 'ROLE_PATIENT'; // Default fallback
            }

            $session = $request->getSession();
            $data = $session->get('reg_data', []);
            $data['role'] = $selectedRole;
            $session->set('reg_data', $data);

            return $this->redirectToRoute('app_register_security');
        }

        return $this->render('auth/register_role.html.twig');
    }

    /**
     * STEP 3: Password & Security
     */
    #[Route('/register/security', name: 'app_register_security', methods: ['GET', 'POST'])]
    public function registerSecurity(Request $request): Response
    {
        if ($request->isMethod('POST')) {
            $password = $request->request->get('password');
            $confirm = $request->request->get('confirm_password');

            if ($password !== $confirm) {
                // Add an error message if they don't match
                $this->addFlash('error', 'Passwords do not match');
                return $this->redirectToRoute('app_register_security');
            }

            // Temporarily store password in session (Only for demo/dev purposes)
            // Once you merge your User branch, you will replace this with the Database save.
            $session = $request->getSession();
            $data = $session->get('reg_data', []);
            $data['password'] = $password; // Usually you'd hash this before saving
            $session->set('reg_data', $data);

            return $this->redirectToRoute('app_register_verification');
        }

        return $this->render('auth/register_security.html.twig');
    }

    /**
     * STEP 4: Verification (OTP)
     */
    #[Route('/register/verification', name: 'app_register_verification', methods: ['GET', 'POST'])]
    public function verification(Request $request): Response
    {
        if ($request->isMethod('POST')) {
            // Get the OTP array from the form
            $otpArray = $request->request->all('otp');
            $fullCode = implode('', $otpArray);

            // Logic: In a real app, you'd verify $fullCode against a code in your DB or Session.
            
            // Success! Clear the session and go to login.
            $request->getSession()->remove('reg_data');
            $this->addFlash('success', 'Account verified successfully! Please log in.');

            return $this->redirectToRoute('app_login');
        }

        return $this->render('auth/register_verification.html.twig');
    }

    #[Route('/forgot-password', name: 'app_forgot_password', methods: ['GET', 'POST'])]
    public function forgotPassword(Request $request): Response
    {
        $step = $request->query->get('step', 'request'); // Default to the email input step

        if ($request->isMethod('POST')) {
            $currentStep = $request->request->get('current_step');

            if ($currentStep === 'request') {
                // Logic: Send email...
                return $this->redirectToRoute('app_forgot_password', ['step' => 'sent']);
            }

            if ($currentStep === 'reset') {
                // Logic: Update password in DB...
                return $this->redirectToRoute('app_forgot_password', ['step' => 'success']);
            }
        }

        return $this->render('auth/forgot_password.html.twig', [
            'step' => $step
        ]);
    }
}