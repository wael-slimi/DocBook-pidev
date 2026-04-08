<?php

namespace App\Controller;

use App\Entity\User;
use App\Service\SmsService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class TwoFactorSettingsController extends AbstractController
{
    /**
     * 1. HANDLES 2FA DURING LOGIN
     */
    #[Route('/login/2fa-verify', name: 'app_2fa_login_verify')]
    public function verifyLogin(Request $request, EntityManagerInterface $em): Response
    {
        $userId = $request->getSession()->get('pending_2fa_user_id');

        if (!$userId) {
            return $this->redirectToRoute('app_login');
        }

        if ($request->isMethod('POST')) {
            $user = $em->getRepository(User::class)->find($userId);
            $enteredCode = $request->request->get('verification_code');

            if ($user && $enteredCode === $user->getTempVerificationCode()) {
                $user->setTempVerificationCode(null);
                $em->flush();
                
                $request->getSession()->remove('pending_2fa_user_id');
                $request->getSession()->set('2fa_status', 'verified');

                return in_array('ROLE_DOCTOR', $user->getRoles()) 
                    ? $this->redirectToRoute('app_doctor_dashboard') 
                    : $this->redirectToRoute('home'); 
            }

            $this->addFlash('danger', 'Invalid code. Please try again.');
        }

        return $this->render('verify_2fa.html.twig');
    }

    /**
     * 2. TRIGGERS THE 2FA ACTIVATION FROM SETTINGS
     */
    #[Route('/settings/2fa/toggle', name: 'app_2fa_toggle_settings', methods: ['POST'])]
    public function toggle2fa(Request $request, EntityManagerInterface $em, SmsService $smsService): Response
    {
        /** @var User $user */
        $user = $this->getUser();
        $phone = $request->request->get('phone');
        $wantsToEnable = $request->request->has('enable_2fa');

        if (!$wantsToEnable) {
            $user->setIs2faEnabled(false);
            $em->flush();
            $this->addFlash('success', '2FA has been disabled.');
            return $this->redirect($request->headers->get('referer'));
        }

        // Generate code to verify the phone number BEFORE fully enabling
        $code = (string)random_int(100000, 999999);
        $user->setPhone($phone);
        $user->setTempVerificationCode($code);
        $em->flush();

        try {
            $smsService->sendSms($phone, "Your DocBook activation code is: " . $code);
            $this->addFlash('success', 'A verification code has been sent to your phone.');
        } catch (\Exception $e) {
            $this->addFlash('danger', 'Error sending SMS. Please check the phone format.');
            return $this->redirect($request->headers->get('referer'));
        }

        // Send to verification page to confirm the switch
        return $this->render('verify_2fa.html.twig', [
            'is_activation' => true // Pass a flag to change the form action in Twig
        ]);
    }

    /**
     * 3. CONFIRMS THE ACTIVATION (The missing piece!)
     */
    #[Route('/settings/2fa/confirm', name: 'app_2fa_confirm', methods: ['POST'])]
    public function confirmEnable(Request $request, EntityManagerInterface $em): Response
    {
        /** @var User $user */
        $user = $this->getUser();
        $enteredCode = $request->request->get('verification_code');

        if ($user->getTempVerificationCode() && $enteredCode === $user->getTempVerificationCode()) {
            $user->setIs2faEnabled(true);
            $user->setTempVerificationCode(null);
            $em->flush();
            
            $this->addFlash('success', '2FA is now active on your account!');
            
            return in_array('ROLE_DOCTOR', $user->getRoles()) 
                ? $this->redirectToRoute('app_doctor_settings') 
                : $this->redirectToRoute('app_patient_settings'); 
        }

        $this->addFlash('danger', 'Wrong code. Verification failed.');
        return $this->render('verify_2fa.html.twig', ['is_activation' => true]);
    }
}