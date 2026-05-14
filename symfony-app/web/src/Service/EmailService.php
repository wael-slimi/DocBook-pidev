<?php

declare(strict_types=1);

namespace App\Service;

final class EmailService
{
    private const GMAIL_SMTP = 'smtp.gmail.com';
    private const GMAIL_PORT = 587;

    public function __construct(
        private readonly string $fromEmail,
        private readonly string $fromPassword,
        private readonly string $defaultTo,
    ) {
    }

    public function send(string $to, string $subject, string $bodyText, string $bodyHtml = ''): bool
    {
        if ($this->fromEmail === '' || $this->fromPassword === '') {
            return false;
        }

        $context = stream_context_create([
            'ssl' => [
                'verify_peer' => true,
                'verify_peer_name' => true,
            ],
        ]);

        $sock = @stream_socket_client(
            'tcp://' . self::GMAIL_SMTP . ':' . self::GMAIL_PORT,
            $errno,
            $errstr,
            15,
            STREAM_CLIENT_CONNECT,
            $context
        );
        if (!$sock) {
            return false;
        }

        try {
            $this->readLine($sock);
            $this->sendLine($sock, 'EHLO ' . ($_SERVER['SERVER_NAME'] ?? 'localhost'));
            $this->readMulti($sock);
            $this->sendLine($sock, 'STARTTLS');
            $this->readLine($sock);
            stream_socket_enable_crypto($sock, true, STREAM_CRYPTO_METHOD_TLS_CLIENT);
            $this->sendLine($sock, 'EHLO ' . ($_SERVER['SERVER_NAME'] ?? 'localhost'));
            $this->readMulti($sock);
            $this->sendLine($sock, 'AUTH LOGIN');
            $this->readLine($sock);
            $this->sendLine($sock, base64_encode($this->fromEmail));
            $this->readLine($sock);
            $this->sendLine($sock, base64_encode($this->fromPassword));
            $reply = $this->readLine($sock);
            if (strpos($reply, '235') === false) {
                return false;
            }
            $this->sendLine($sock, 'MAIL FROM:<' . $this->fromEmail . '>');
            $this->readLine($sock);
            $this->sendLine($sock, 'RCPT TO:<' . $to . '>');
            $this->readLine($sock);
            $this->sendLine($sock, 'DATA');
            $this->readLine($sock);

            $boundary = '----=_Part_' . bin2hex(random_bytes(8));
            $headers = "From: DocBook <{$this->fromEmail}>\r\n";
            $headers .= "To: $to\r\n";
            $headers .= "Subject: =?UTF-8?B?" . base64_encode($subject) . "?=\r\n";
            $headers .= "MIME-Version: 1.0\r\n";
            if ($bodyHtml !== '') {
                $headers .= "Content-Type: multipart/alternative; boundary=\"$boundary\"\r\n\r\n";
                $headers .= "--$boundary\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n$bodyText\r\n";
                $headers .= "--$boundary\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n$bodyHtml\r\n";
                $headers .= "--$boundary--\r\n";
            } else {
                $headers .= "Content-Type: text/plain; charset=UTF-8\r\n\r\n";
                $headers .= $bodyText . "\r\n";
            }
            $message = $this->dotStuff($headers) . "\r\n.\r\n";
            fwrite($sock, $message);
            $reply = $this->readLine($sock);
            $ok = strpos($reply, '250') === 0;
            $this->sendLine($sock, 'QUIT');
            return $ok;
        } finally {
            fclose($sock);
        }
    }

    public function sendNewDossierNotification(string $numeroDossier, string $patientNom, string $patientPrenom): bool
    {
        if ($this->defaultTo === '') {
            return false;
        }
        $subject = '[DocBook] Nouveau dossier créé – ' . $numeroDossier;
        $text = "Un nouveau dossier médical a été créé dans DocBook.\n\n";
        $text .= "N° dossier : $numeroDossier\n";
        $text .= "Patient : $patientNom $patientPrenom\n";
        $html = '<p>Un nouveau dossier médical a été créé dans DocBook.</p>';
        $html .= '<p><strong>N° dossier :</strong> ' . htmlspecialchars($numeroDossier) . '<br>';
        $html .= '<strong>Patient :</strong> ' . htmlspecialchars($patientNom . ' ' . $patientPrenom) . '</p>';
        return $this->send($this->defaultTo, $subject, $text, $html);
    }

    private function sendLine($sock, string $line): void
    {
        fwrite($sock, $line . "\r\n");
    }

    private function readLine($sock): string
    {
        $line = fgets($sock, 512);
        return $line !== false ? trim($line) : '';
    }

    private function readMulti($sock): void
    {
        while ($line = fgets($sock, 512)) {
            $line = trim($line);
            if (strlen($line) < 4 || $line[3] !== '-') {
                break;
            }
        }
    }

    private function dotStuff(string $data): string
    {
        return str_replace("\r\n.", "\r\n..", $data);
    }
}
