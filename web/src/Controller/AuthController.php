<?php

namespace App\Controller;

use App\Entity\Doctor;
use App\Entity\Patient;
use App\Entity\Caregiver;
use App\Enum\UserRole;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;
use App\Repository\UserRepository;

class AuthController extends AbstractController
{
    #[Route('/login', name: 'app_login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        $user = $this->getUser();
        if ($user) {

            return $this->redirectToRoute('app_dashboard'); 
        }

        $error = $authenticationUtils->getLastAuthenticationError();
        $lastUsername = $authenticationUtils->getLastUsername();

        return $this->render('auth/login.html.twig', [
            'last_username' => $lastUsername,
            'error'         => $error, 
        ]);
    }

    #[Route(path: '/logout', name: 'app_logout')]
    public function logout(): void
    {
        throw new \LogicException('This method can be blank.');
    }

    /**
     * STEP 1: Basic Information
     */
    #[Route('/register', name: 'app_register', methods: ['GET', 'POST'])]
    public function register(Request $request , UserRepository $userRepository): Response
    {
        if ($request->isMethod('POST')) {
            $email = $request->request->get('email');
            
            $existingUser = $userRepository->findOneBy(['email' => $email]);

            if ($existingUser) {
                $this->addFlash('error', 'This email is already registered.');
                return $this->redirectToRoute('app_register');
            }

            $session = $request->getSession();
            $session->set('reg_data', [
                'full_name' => $request->request->get('full_name'),
                'email' => $request->request->get('email'),
                'phone' => $request->request->get('phone'), // Added phone if you have it in Step 1
                'dob' => $request->request->get('dob'),
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
            
            $allowedRoles = ['ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_CAREGIVER'];
            if (!in_array($selectedRole, $allowedRoles)) {
                $selectedRole = 'ROLE_PATIENT';
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
                $this->addFlash('error', 'Passwords do not match');
                return $this->redirectToRoute('app_register_security');
            }

            $session = $request->getSession();
            $data = $session->get('reg_data', []);
            $data['password'] = $password; 
            $session->set('reg_data', $data);

            return $this->redirectToRoute('app_register_verification');
        }

        return $this->render('auth/register_security.html.twig');
    }

    /**
     * STEP 4: Verification (OTP) & Final Database Save
     */
    #[Route('/register/verification', name: 'app_register_verification', methods: ['GET', 'POST'])]
    public function verification(
        Request $request, 
        EntityManagerInterface $entityManager, 
        UserPasswordHasherInterface $userPasswordHasher
    ): Response {
        if ($request->isMethod('POST')) {
            $otpArray = $request->request->all('otp');
            $fullCode = implode('', $otpArray);

            // Fetch session data
            $session = $request->getSession();
            $data = $session->get('reg_data');

            if (!$data) {
                $this->addFlash('error', 'Session expired. Please start over.');
                return $this->redirectToRoute('app_register');
            }

            // 1. Create the specific Entity based on selected role
            $user = match($data['role']) {
                'ROLE_DOCTOR' => new Doctor(),
                'ROLE_CAREGIVER' => new Caregiver(),
                default => new Patient(),
            };
            if ($user instanceof Caregiver) {
                $user->setRelationshipType(\App\Enum\RelationshipType::FAMILY);
            }

            // 2. Hydrate user with session data
            $user->setName($data['full_name']);
            $user->setEmail($data['email']);
            $user->setPhone($data['phone'] ?? null);
            $user->setIsActive(true);
            if (empty($data['dob'])) {
                $this->addFlash('error', 'Date of birth is required.');
                return $this->redirectToRoute('app_register');
            }
            $user->setDateOfBirth(new \DateTime($data['dob'])); //date of birth 
            
            // 3. Set the Enum Role
            $user->setRole(match($data['role']) {
                'ROLE_DOCTOR' => UserRole::DOCTOR,
                'ROLE_CAREGIVER' => UserRole::CAREGIVER,
                default => UserRole::PATIENT,
            });
            $user->setCreationDate(new \DateTimeImmutable());

            // 4. Hash the password
            $hashedPassword = $userPasswordHasher->hashPassword($user, $data['password']);
            $user->setPassword($hashedPassword);


            // 5. Persist and Flush to Database
            $entityManager->persist($user);
            $entityManager->flush();

            // 6. Success! Clear the session
            $session->remove('reg_data');
            $this->addFlash('success', 'Registration successful! You can now log in.');

            return $this->redirectToRoute('app_login');
        }

        return $this->render('auth/register_verification.html.twig');
    }

    #[Route('/forgot-password', name: 'app_forgot_password', methods: ['GET', 'POST'])]
    public function forgotPassword(Request $request): Response
    {
        $step = $request->query->get('step', 'request');

        if ($request->isMethod('POST')) {
            $currentStep = $request->request->get('current_step');

            if ($currentStep === 'request') {
                return $this->redirectToRoute('app_forgot_password', ['step' => 'sent']);
            }

            if ($currentStep === 'reset') {
                return $this->redirectToRoute('app_forgot_password', ['step' => 'success']);
            }
        }

        return $this->render('auth/forgot_password.html.twig', [
            'step' => $step
        ]);
    }
}