<?php

declare(strict_types=1);

namespace App\Service;

use App\Entity\Document;
use App\Entity\DossierMedical;
use Dompdf\Dompdf;
use Dompdf\Options;
use Twig\Environment;

final class PdfExportService
{
    private const FONT = 'DejaVu Sans';

    public function __construct(
        private readonly QrCodeService $qrCodeService,
        private readonly Environment $twig,
        private readonly string $projectDir
    ) {
    }

    /**
     * Generate PDF for a medical dossier (with authenticity QR at bottom).
     */
    public function generateDossierPdf(DossierMedical $dossier): string
    {
        $qrData = 'DOCBOOK-DOSSIER-' . $dossier->getId() . '-' . ($dossier->getNumeroDossier() ?? '');
        [$qrContent, $qrTempPath] = $this->getQrContentForPdf($qrData);
        try {
            $html = $this->renderDossierHtml($dossier, $qrContent, $qrData);
            return $this->htmlToPdf($html);
        } finally {
            if ($qrTempPath !== null && is_file($qrTempPath)) {
                @unlink($qrTempPath);
            }
        }
    }

    /**
     * Generate PDF for a document (with authenticity QR at bottom).
     */
    public function generateDocumentPdf(Document $document): string
    {
        $dossier = $document->getDossierMedical();
        $qrData = 'DOCBOOK-DOC-' . $document->getId() . '-D' . ($dossier?->getId() ?? '');
        [$qrContent, $qrTempPath] = $this->getQrContentForPdf($qrData);
        try {
            $html = $this->renderDocumentHtml($document, $dossier, $qrContent, $qrData);
            return $this->htmlToPdf($html);
        } finally {
            if ($qrTempPath !== null && is_file($qrTempPath)) {
                @unlink($qrTempPath);
            }
        }
    }

    /**
     * Returns [html fragment for QR, full path to temp file to delete or null].
     * Uses public QR API URL so the QR image always loads in Dompdf (works without GD, no path issues).
     */
    private function getQrContentForPdf(string $data): array
    {
        $encoded = urlencode($data);
        $qrUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=' . $encoded;
        $img = '<img src="' . $qrUrl . '" width="72" height="72" alt="QR authenticité" style="display:block;" />';
        return [$img, null];
    }

    private function renderDossierHtml(DossierMedical $dossier, string $qrContent, string $qrData): string
    {
        $d = $dossier;
        return $this->twig->render('pdf/dossier.html.twig', [
            'numero_dossier' => $this->e($d->getNumeroDossier()),
            'patient_nom' => $this->e($d->getPatientNom()),
            'patient_prenom' => $this->e($d->getPatientPrenom()),
            'date_naissance' => $d->getDateNaissance() ? $d->getDateNaissance()->format('d/m/Y') : '—',
            'genre' => $this->e($d->getGenre()),
            'email' => $this->e($d->getEmail()),
            'telephone' => $this->e($d->getTelephone()),
            'adresse' => $this->e($d->getAdresse()),
            'remarques' => $this->e($d->getRemarques()),
            'date_creation' => $d->getDateCreation() ? $d->getDateCreation()->format('d/m/Y H:i') : '—',
            'qr_content' => $qrContent,
            'qr_data' => $this->e($qrData),
            'generated_at' => (new \DateTimeImmutable())->format('d/m/Y H:i'),
        ]);
    }

    private function renderDocumentHtml(Document $doc, ?DossierMedical $dossier, string $qrContent, string $qrData): string
    {
        $dNum = $dossier ? $dossier->getNumeroDossier() : null;
        $patient = $dossier ? trim($dossier->getPatientNom() . ' ' . $dossier->getPatientPrenom()) : '—';
        $contenuRaw = $doc->getContenu() ?? '';
        $contenu = nl2br($this->e($contenuRaw));

        return $this->twig->render('pdf/document.html.twig', [
            'titre' => $this->e($doc->getTitre()),
            'document_id' => $doc->getId(),
            'dossier_numero' => $this->e($dNum ?? '—'),
            'patient_nom' => $this->e($patient),
            'type_document' => $this->e($doc->getTypeDocument()),
            'date_document' => $doc->getDateDocument() ? $doc->getDateDocument()->format('d/m/Y') : '—',
            'contenu' => $contenu,
            'qr_content' => $qrContent,
            'qr_data' => $this->e($qrData),
            'generated_at' => (new \DateTimeImmutable())->format('d/m/Y H:i'),
        ]);
    }

    private function htmlToPdf(string $html): string
    {
        $options = new Options();
        $options->set('isRemoteEnabled', true);
        $options->set('isHtml5ParserEnabled', true);
        $options->set('defaultFont', self::FONT);
        $dompdf = new Dompdf($options);
        $dompdf->loadHtml($html, 'UTF-8');
        $dompdf->setPaper('A4', 'portrait');
        $dompdf->setBasePath($this->projectDir);
        $dompdf->render();
        return $dompdf->output();
    }

    private function e(?string $s): string
    {
        return $s !== null && $s !== '' ? htmlspecialchars($s, ENT_QUOTES, 'UTF-8') : '—';
    }
}
