<?php

namespace App\Controller;

use App\Entity\Doctor;
use App\Entity\Patient;
use App\Entity\Caregiver;
use App\Enum\UserRole;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;
use App\Repository\UserRepository;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Address;
use Symfony\Component\Mime\Email;

class AuthController extends AbstractController
{
    #[Route('/login', name: 'app_login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        if ($this->getUser()) {
            return $this->redirectToRoute('app_dashboard'); 
        }

        return $this->render('auth/login.html.twig', [
            'last_username' => $authenticationUtils->getLastUsername(),
            'error'         => $authenticationUtils->getLastAuthenticationError(), 
        ]);
    }

    #[Route(path: '/logout', name: 'app_logout')]
    public function logout(): void {}

    #[Route('/register', name: 'app_register', methods: ['GET', 'POST'])]
    public function register(Request $request, UserRepository $userRepository): Response
    {
        if ($request->isMethod('POST')) {
            $email = $request->request->get('email');
            $dobString = $request->request->get('dob');

            if ($dobString) {
                $dob = new \DateTime($dobString);
                $age = (new \DateTime())->diff($dob)->y;

                if ($age < 16) {
                    $this->addFlash('error', 'You must be at least 16 years old to register.');
                    return $this->redirectToRoute('app_register');
                }
            }
            
            if ($userRepository->findOneBy(['email' => $email])) {
                $this->addFlash('error', 'This email is already registered.');
                return $this->redirectToRoute('app_register');
            }

            $request->getSession()->set('reg_data', [
                'full_name' => $request->request->get('full_name'),
                'email' => $email,
                'phone' => $request->request->get('phone'),
                'dob' => $dobString,
            ]);

            return $this->redirectToRoute('app_register_role');
        }

        return $this->render('auth/register_basic.html.twig');
    }

    #[Route('/register/role', name: 'app_register_role', methods: ['GET', 'POST'])]
    public function registerRole(Request $request): Response
    {
        if ($request->isMethod('POST')) {
            $session = $request->getSession();
            $data = $session->get('reg_data', []);
            $data['role'] = $request->request->get('selected_role', 'ROLE_PATIENT');
            $session->set('reg_data', $data);

            return $this->redirectToRoute('app_register_security');
        }

        return $this->render('auth/register_role.html.twig');
    }

    #[Route('/register/security', name: 'app_register_security', methods: ['GET', 'POST'])]
    public function registerSecurity(Request $request, MailerInterface $mailer): Response
    {
        $session = $request->getSession();
        $data = $session->get('reg_data', []);

        if ($request->isMethod('POST')) {
            $password = $request->request->get('password');
            $confirm = $request->request->get('confirm_password');

            if ($password !== $confirm) {
                $this->addFlash('error', 'Passwords do not match');
                return $this->redirectToRoute('app_register_security');
            }

            $regex = '/^(?=.*[0-9])(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/';
            if (!preg_match($regex, $password)) {
                $this->addFlash('error', 'Password must be at least 8 characters long and include a number and special character.');
                return $this->redirectToRoute('app_register_security');
            }

            $emailAddress = $data['email'] ?? null;
            if (!$emailAddress) {
                $this->addFlash('error', 'Registration data lost.');
                return $this->redirectToRoute('app_register');
            }

            $data['password'] = $password; 
            $session->set('reg_data', $data);

            // Generate Verification Code
            $verificationCode = (string)random_int(100000, 999999);
            $session->set('verification_code', $verificationCode);

            // Send Inline HTML Email (No external template needed)
            $email = (new Email())
                ->from(new Address('wslimi35@gmail.com', 'DocBook Support'))
                ->to($emailAddress)
                ->subject('Your DOCBOOK Verification Code')
                ->html("
                    <div style='font-family: sans-serif; max-width: 500px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;'>
                        <h2 style='color: #2563eb; text-align: center;'>DOCBOOK</h2>
                        <p>Hello, use the code below to verify your account:</p>
                        <div style='background: #f3f4f6; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #1e293b; border-radius: 8px;'>
                            $verificationCode
                        </div>
                        <p style='color: #64748b; font-size: 12px; text-align: center; margin-top: 20px;'>If you didn't request this, please ignore this email.</p>
                    </div>
                ");
            
            $mailer->send($email);

            return $this->redirectToRoute('app_register_verification');
        }

        return $this->render('auth/register_security.html.twig');
    }

    #[Route('/register/verification', name: 'app_register_verification', methods: ['GET', 'POST'])]
    public function verification(
        Request $request, 
        EntityManagerInterface $entityManager, 
        UserPasswordHasherInterface $userPasswordHasher
    ): Response {
        $session = $request->getSession();
        
        if ($request->isMethod('POST')) {
            $otpArray = $request->request->all('otp');
            $userOtp = implode('', $otpArray);
            $storedCode = (string)$session->get('verification_code');

            if ($userOtp !== $storedCode) {
                dd(['typed' => $userOtp, 'session' => $storedCode]);
                $this->addFlash('error', 'Invalid verification code.');
                return $this->redirectToRoute('app_register_verification');
            }

            $data = $session->get('reg_data');
            if (!$data) return $this->redirectToRoute('app_register');

            // Entity Creation match logic
            $user = match($data['role']) {
                'ROLE_DOCTOR' => new Doctor(),
                'ROLE_CAREGIVER' => new Caregiver(),
                default => new Patient(),
            };

            $user->setName($data['full_name']);
            $user->setEmail($data['email']);
            $user->setPhone($data['phone'] ?? null);
            $user->setIsActive(true);
            $user->setDateOfBirth(new \DateTime($data['dob']));
            $user->setRole(match($data['role']) {
                'ROLE_DOCTOR' => UserRole::DOCTOR,
                'ROLE_CAREGIVER' => UserRole::CAREGIVER,
                default => UserRole::PATIENT,
            });
            
            $user->setPassword($userPasswordHasher->hashPassword($user, $data['password']));
            $user->setCreationDate(new \DateTimeImmutable());
            
            $entityManager->persist($user);
            $entityManager->flush();

            // Clear registration session
            $session->remove('reg_data');
            $session->remove('verification_code');
            
            $this->addFlash('success', 'Registration successful! You can now log in.');
            return $this->redirectToRoute('app_login');
        }

        return $this->render('auth/register_verification.html.twig');
    }

    #[Route('/register/resend-code', name: 'app_register_resend_code')]
    public function resendCode(Request $request, MailerInterface $mailer): Response
    {
        $session = $request->getSession();
        $data = $session->get('reg_data');

        if (!$data || !isset($data['email'])) {
            $this->addFlash('error', 'Session expired. Please restart.');
            return $this->redirectToRoute('app_register');
        }

        $newCode = (string)random_int(100000, 999999);
        $session->set('verification_code', $newCode);

        $email = (new Email())
            ->from(new Address('wslimi35@gmail.com', 'DocBook Support'))
            ->to($data['email'])
            ->subject('Your NEW DOCBOOK Verification Code')
            ->html("<p>Your new verification code is: <b>$newCode</b></p>");

        $mailer->send($email);
        $this->addFlash('success', 'A new code has been sent!');

        return $this->redirectToRoute('app_register_verification');
    }
}