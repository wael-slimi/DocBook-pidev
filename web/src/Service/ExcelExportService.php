<?php

declare(strict_types=1);

namespace App\Service;

use App\Entity\Document;
use App\Entity\DossierMedical;
use PhpOffice\PhpSpreadsheet\Spreadsheet;
use PhpOffice\PhpSpreadsheet\Writer\Xlsx;
use PhpOffice\PhpSpreadsheet\Style\Fill;
use Symfony\Component\HttpFoundation\StreamedResponse;

final class ExcelExportService
{
    /**
     * Export dossiers list to Excel.
     *
     * @param DossierMedical[] $dossiers
     */
    public function exportDossiers(array $dossiers): StreamedResponse
    {
        $spreadsheet = new Spreadsheet();
        $sheet = $spreadsheet->getActiveSheet();
        $sheet->setTitle('Dossiers médicaux');

        $headers = ['N° dossier', 'Nom', 'Prénom', 'Date naissance', 'Genre', 'Email', 'Téléphone', 'Créé le'];
        $col = 'A';
        foreach ($headers as $h) {
            $sheet->setCellValue($col . '1', $h);
            $sheet->getStyle($col . '1')->getFont()->setBold(true);
            $sheet->getStyle($col . '1')->getFill()
                ->setFillType(Fill::FILL_SOLID)->getStartColor()->setRGB('e2e8f0');
            $col++;
        }

        $row = 2;
        foreach ($dossiers as $d) {
            $sheet->setCellValue('A' . $row, $d->getNumeroDossier());
            $sheet->setCellValue('B' . $row, $d->getPatientNom());
            $sheet->setCellValue('C' . $row, $d->getPatientPrenom());
            $sheet->setCellValue('D' . $row, $d->getDateNaissance() ? $d->getDateNaissance()->format('d/m/Y') : '');
            $sheet->setCellValue('E' . $row, $d->getGenre());
            $sheet->setCellValue('F' . $row, $d->getEmail());
            $sheet->setCellValue('G' . $row, $d->getTelephone());
            $sheet->setCellValue('H' . $row, $d->getDateCreation() ? $d->getDateCreation()->format('d/m/Y H:i') : '');
            $row++;
        }

        foreach (range('A', 'H') as $c) {
            $sheet->getColumnDimension($c)->setAutoSize(true);
        }

        return $this->streamXlsx($spreadsheet, 'dossiers_medicaux.xlsx');
    }

    /**
     * Export documents list to Excel.
     *
     * @param Document[] $documents
     */
    public function exportDocuments(array $documents): StreamedResponse
    {
        $spreadsheet = new Spreadsheet();
        $sheet = $spreadsheet->getActiveSheet();
        $sheet->setTitle('Documents');

        $headers = ['Titre', 'Type', 'Date document', 'Dossier', 'Patient', 'Créé le'];
        $col = 'A';
        foreach ($headers as $h) {
            $sheet->setCellValue($col . '1', $h);
            $sheet->getStyle($col . '1')->getFont()->setBold(true);
            $sheet->getStyle($col . '1')->getFill()
                ->setFillType(Fill::FILL_SOLID)->getStartColor()->setRGB('e2e8f0');
            $col++;
        }

        $row = 2;
        foreach ($documents as $doc) {
            $d = $doc->getDossierMedical();
            $sheet->setCellValue('A' . $row, $doc->getTitre());
            $sheet->setCellValue('B' . $row, $doc->getTypeDocument());
            $sheet->setCellValue('C' . $row, $doc->getDateDocument() ? $doc->getDateDocument()->format('d/m/Y') : '');
            $sheet->setCellValue('D' . $row, $d ? $d->getNumeroDossier() : '');
            $sheet->setCellValue('E' . $row, $d ? trim($d->getPatientNom() . ' ' . $d->getPatientPrenom()) : '');
            $sheet->setCellValue('F' . $row, $doc->getDateCreation() ? $doc->getDateCreation()->format('d/m/Y H:i') : '');
            $row++;
        }

        foreach (range('A', 'F') as $c) {
            $sheet->getColumnDimension($c)->setAutoSize(true);
        }

        return $this->streamXlsx($spreadsheet, 'documents.xlsx');
    }

    private function streamXlsx(Spreadsheet $spreadsheet, string $filename): StreamedResponse
    {
        $response = new StreamedResponse(function () use ($spreadsheet) {
            $writer = new Xlsx($spreadsheet);
            $writer->save('php://output');
        });
        $response->headers->set('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
        $response->headers->set('Content-Disposition', 'attachment; filename="' . $filename . '"');
        return $response;
    }
}
