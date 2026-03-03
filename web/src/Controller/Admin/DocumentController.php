<?php

declare(strict_types=1);

namespace App\Controller\Admin;

use App\Entity\Document;
use App\Entity\DossierMedical;
use App\Form\DocumentType;
use App\Repository\DocumentRepository;
use App\Repository\DossierMedicalRepository;
use App\Service\ExcelExportService;
use App\Service\PdfExportService;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\StreamedResponse;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/dossiers/{dossierId}/documents', requirements: ['dossierId' => '\d+'])]
class DocumentController extends AbstractController
{
    private const PER_PAGE = 10;

    public function __construct(
        private readonly DocumentRepository $documentRepository,
        private readonly DossierMedicalRepository $dossierRepository,
        private readonly PaginatorInterface $paginator,
        private readonly PdfExportService $pdfExport,
        private readonly ExcelExportService $excelExport,
    ) {
    }

    private function getDossier(int $dossierId): DossierMedical
    {
        $dossier = $this->dossierRepository->find($dossierId);
        if (!$dossier) {
            throw $this->createNotFoundException('Dossier non trouvé.');
        }
        return $dossier;
    }

    #[Route('', name: 'app_admin_document_index', methods: ['GET'])]
    public function index(Request $request, int $dossierId): Response
    {
        $dossier = $this->getDossier($dossierId);
        $search = $request->query->get('q');
        $tri = $request->query->get('tri', 'dateDocument');
        $ordre = $request->query->get('ordre', 'DESC');
        $type = $request->query->get('type');
        $dateDebut = $request->query->get('date_debut') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_debut')) : null;
        $dateFin = $request->query->get('date_fin') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_fin')) : null;

        $qb = $this->documentRepository->getQueryBuilderForSearch($dossier, $search, $tri, $ordre, $type, $dateDebut, $dateFin);
        $pagination = $this->paginator->paginate($qb, $request->query->getInt('page', 1), self::PER_PAGE);

        if ($request->isXmlHttpRequest() || $request->query->get('ajax')) {
            $response = $this->render('admin/document/_list_rows.html.twig', [
                'documents' => $pagination->getItems(),
                'dossier' => $dossier,
                'prefix' => '/admin',
            ]);
            $response->headers->set('X-Total-Count', (string) $pagination->getTotalItemCount());
            return $response;
        }

        return $this->render('admin/document/index.html.twig', [
            'pagination' => $pagination,
            'documents' => $pagination->getItems(),
            'dossier' => $dossier,
            'total' => $pagination->getTotalItemCount(),
            'q' => $search,
            'tri' => $tri,
            'ordre' => $ordre,
            'type' => $type,
            'date_debut' => $request->query->get('date_debut'),
            'date_fin' => $request->query->get('date_fin'),
            'prefix' => '/admin',
        ]);
    }

    #[Route('/export/excel', name: 'app_admin_document_export_excel', methods: ['GET'])]
    public function exportExcel(Request $request, int $dossierId): StreamedResponse
    {
        $dossier = $this->getDossier($dossierId);
        $search = $request->query->get('q');
        $tri = $request->query->get('tri', 'dateDocument');
        $ordre = $request->query->get('ordre', 'DESC');
        $type = $request->query->get('type');
        $dateDebut = $request->query->get('date_debut') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_debut')) : null;
        $dateFin = $request->query->get('date_fin') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_fin')) : null;
        $documents = $this->documentRepository->searchAndFilterByDossier($dossier, $search, $tri, $ordre, $type, $dateDebut, $dateFin, 5000, 0);
        return $this->excelExport->exportDocuments($documents);
    }

    #[Route('/nouveau', name: 'app_admin_document_new', methods: ['GET', 'POST'])]
    public function new(Request $request, int $dossierId): Response
    {
        $dossier = $this->getDossier($dossierId);
        $document = new Document();
        $document->setDossierMedical($dossier);
        $form = $this->createForm(DocumentType::class, $document, ['with_dossier' => false]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $this->documentRepository->save($document, true);
            $this->addFlash('success', 'Document créé.');
            return $this->redirectToRoute('app_admin_document_index', ['dossierId' => $dossierId]);
        }
        return $this->render('admin/document/form.html.twig', [
            'document' => $document,
            'dossier' => $dossier,
            'form' => $form,
            'prefix' => '/admin',
        ]);
    }

    #[Route('/{id}', name: 'app_admin_document_show', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function show(int $dossierId, Document $document): Response
    {
        $dossier = $this->getDossier($dossierId);
        if ($document->getDossierMedical() !== $dossier) {
            throw $this->createNotFoundException();
        }
        return $this->render('admin/document/show.html.twig', [
            'document' => $document,
            'dossier' => $dossier,
            'prefix' => '/admin',
        ]);
    }

    #[Route('/{id}/export/pdf', name: 'app_admin_document_export_pdf', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function exportPdf(int $dossierId, Document $document): Response
    {
        $dossier = $this->getDossier($dossierId);
        if ($document->getDossierMedical() !== $dossier) {
            throw $this->createNotFoundException();
        }
        $pdf = $this->pdfExport->generateDocumentPdf($document);
        $response = new Response($pdf);
        $response->headers->set('Content-Type', 'application/pdf');
        $response->headers->set('Content-Disposition', 'attachment; filename="document-' . $document->getId() . '-' . preg_replace('/[^a-z0-9_-]/i', '-', $document->getTitre() ?? '') . '.pdf"');
        return $response;
    }

    #[Route('/{id}/modifier', name: 'app_admin_document_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(Request $request, int $dossierId, Document $document): Response
    {
        $dossier = $this->getDossier($dossierId);
        if ($document->getDossierMedical() !== $dossier) {
            throw $this->createNotFoundException();
        }
        $form = $this->createForm(DocumentType::class, $document, ['with_dossier' => false]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $this->documentRepository->save($document, true);
            $this->addFlash('success', 'Document mis à jour.');
            return $this->redirectToRoute('app_admin_document_index', ['dossierId' => $dossierId]);
        }
        return $this->render('admin/document/form.html.twig', [
            'document' => $document,
            'dossier' => $dossier,
            'form' => $form,
            'prefix' => '/admin',
        ]);
    }

    #[Route('/{id}', name: 'app_admin_document_delete', requirements: ['id' => '\d+'], methods: ['POST'])]
    public function delete(Request $request, int $dossierId, Document $document): Response
    {
        $dossier = $this->getDossier($dossierId);
        if ($document->getDossierMedical() !== $dossier) {
            throw $this->createNotFoundException();
        }
        $this->documentRepository->remove($document, true);
        $this->addFlash('success', 'Document supprimé.');
        return $this->redirectToRoute('app_admin_document_index', ['dossierId' => $dossierId]);
    }
}
