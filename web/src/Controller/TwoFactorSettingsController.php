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

    #[Route('/login/2fa-verify', name: 'app_2fa_login_verify')]
    public function verifyLogin(Request $request, EntityManagerInterface $em): Response
    {
        $userId = $request->getSession()->get('pending_2fa_user_id');

        if (!$userId) {
            return $this->redirectToRoute('app_login');
        }

        if ($request->isMethod('POST')) {
            // Use the Patient entity since that is what your app is using
            $user = $em->getRepository(\App\Entity\Patient::class)->find($userId);
            $enteredCode = $request->request->get('verification_code');

            if ($user && $enteredCode === $user->getTempVerificationCode()) {
                // Success: Clean up
                $user->setTempVerificationCode(null);
                $em->flush();
                $request->getSession()->remove('pending_2fa_user_id');

                // Redirect to your patient dashboard/settings
                return $this->redirectToRoute('app_patient_settings'); 
            }

            $this->addFlash('danger', 'Invalid code. Check your WhatsApp.');
        }

        // This uses the template we already styled
        return $this->render('verify_2fa.html.twig');
    }

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
            $this->addFlash('success', '2FA disabled.');
            return $this->redirect($request->headers->get('referer'));
        }

        // 1. Generate and save the code
        $code = (string)random_int(100000, 999999);
        $user->setPhone($phone);
        $user->setTempVerificationCode($code);
        $em->flush();

        // 2. Try to send the REAL WhatsApp
        try {
            $smsService->sendSms($phone, "Your DocBook verification code is: " . $code);
            $this->addFlash('success', 'A verification code has been sent to your WhatsApp.');
        } catch (\Exception $e) {
            $this->addFlash('danger', 'Could not send WhatsApp. Did you join the sandbox?');
            return $this->redirect($request->headers->get('referer'));
        }

        return $this->render('verify_2fa.html.twig');
    }

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
            $this->addFlash('success', '2FA is now active!');
            
            return $this->redirectToRoute('app_patient_settings'); 
        }

        $this->addFlash('danger', 'Wrong code. Please check your WhatsApp.');
        return $this->render('verify_2fa.html.twig');
    }
}