<?php

namespace App\EventSubscriber;

use App\Entity\User;
use App\Service\SmsService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpKernel\Event\RequestEvent;
use Symfony\Component\HttpKernel\KernelEvents;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;
use Symfony\Component\Security\Http\Event\LoginSuccessEvent;

class Check2faSubscriber implements EventSubscriberInterface
{
    private $urlGenerator;
    private $em;
    private $smsService;

    public function __construct(UrlGeneratorInterface $urlGenerator, EntityManagerInterface $em, SmsService $smsService)
    {
        $this->urlGenerator = $urlGenerator;
        $this->em = $em;
        $this->smsService = $smsService;
    }

    public static function getSubscribedEvents(): array
    {
        return [
            LoginSuccessEvent::class => 'onLoginSuccess',
        ];
    }

    public function onLoginSuccess(LoginSuccessEvent $event): void
    {
        /** @var User $user */
        $user = $event->getUser();
    
        // Check the boolean field in your User entity
        // Note: Make sure the method is exactly as named in User.php (e.g., isIs2faEnabled)
        if (!$user->is2faEnabled()) {
            return; 
        }
    
        $code = (string)random_int(100000, 999999);
        $user->setTempVerificationCode($code);
        $this->em->flush();
    
        try {
            // This will trigger your Twilio/SmsService
            $this->smsService->sendSms($user->getPhone(), "Your DocBook login code is: " . $code);
        } catch (\Exception $e) {
            // Log error if needed
        }
    
        $session = $event->getRequest()->getSession();
        $session->set('pending_2fa_user_id', $user->getId());
        $session->set('2fa_status', 'pending'); // Use this to guard your dashboard
    
        $response = new RedirectResponse($this->urlGenerator->generate('app_2fa_login_verify'));
        $event->setResponse($response);
    }
}