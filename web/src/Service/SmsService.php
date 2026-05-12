<?php

namespace App\Service;

use Twilio\Rest\Client;
use Psr\Log\LoggerInterface;

class SmsService
{
    private $sid;
    private $token;
    private $from;
    private $logger;

    public function __construct(string $sid, string $token, string $from, LoggerInterface $logger)
    {
        $this->sid = $sid;
        $this->token = $token;
        $this->from = $from; // This should be +14155238886 in your .env
        $this->logger = $logger;
    }

    public function sendSms(string $to, string $message): void
    {
        try {
            $client = new Client($this->sid, $this->token);
            
            // The 'whatsapp:' prefix is mandatory for both 'from' and 'to'
            $client->messages->create(
                'whatsapp:' . $to,
                [
                    'from' => 'whatsapp:' . $this->from,
                    'body' => $message
                ]
            );
            
            $this->logger->info("WhatsApp sent successfully to $to");
        } catch (\Exception $e) {
            $this->logger->error("WhatsApp failed: " . $e->getMessage());
            // We throw the error so the Controller knows the send failed
            throw $e;
        }
    }
}