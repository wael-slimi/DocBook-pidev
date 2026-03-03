<?php

declare(strict_types=1);

namespace App\Controller\Admin;

use App\Entity\DossierMedical;
use App\Form\DossierMedicalType;
use App\Repository\DossierMedicalRepository;
use App\Service\ExcelExportService;
use App\Service\GeminiService;
use App\Service\EmailService;
use App\Service\PdfExportService;
use App\Service\RoleAccessService;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\StreamedResponse;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/dossiers')]
class DossierMedicalController extends AbstractController
{
    private const PER_PAGE = 10;

    public function __construct(
        private readonly DossierMedicalRepository $repository,
        private readonly RoleAccessService $roleAccess,
        private readonly PaginatorInterface $paginator,
        private readonly PdfExportService $pdfExport,
        private readonly ExcelExportService $excelExport,
        private readonly GeminiService $geminiService,
        private readonly EmailService $emailService,
    ) {
    }

    #[Route('', name: 'app_admin_dossier_index', methods: ['GET'])]
    public function index(Request $request): Response
    {
        $search = $request->query->get('q');
        $tri = $request->query->get('tri', 'dateCreation');
        $ordre = $request->query->get('ordre', 'DESC');
        $dateDebut = $request->query->get('date_debut') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_debut')) : null;
        $dateFin = $request->query->get('date_fin') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_fin')) : null;
        $genre = $request->query->get('genre');

        $qb = $this->repository->getQueryBuilderForSearch($search, $tri, $ordre, $dateDebut, $dateFin, $genre);
        $pagination = $this->paginator->paginate($qb, $request->query->getInt('page', 1), self::PER_PAGE);

        if ($request->isXmlHttpRequest() || $request->query->get('ajax')) {
            $response = $this->render('admin/dossier_medical/_list_rows.html.twig', [
                'dossiers' => $pagination->getItems(),
                'can_edit_dossier' => true,
                'can_delete_dossier' => true,
                'prefix' => '/admin',
            ]);
            $response->headers->set('X-Total-Count', (string) $pagination->getTotalItemCount());
            return $response;
        }

        return $this->render('admin/dossier_medical/index.html.twig', [
            'pagination' => $pagination,
            'dossiers' => $pagination->getItems(),
            'total' => $pagination->getTotalItemCount(),
            'q' => $search,
            'tri' => $tri,
            'ordre' => $ordre,
            'date_debut' => $request->query->get('date_debut'),
            'date_fin' => $request->query->get('date_fin'),
            'genre' => $genre,
            'can_edit_dossier' => true,
            'can_delete_dossier' => true,
        ]);
    }

    #[Route('/export/excel', name: 'app_admin_dossier_export_excel', methods: ['GET'])]
    public function exportExcel(Request $request): StreamedResponse
    {
        $search = $request->query->get('q');
        $tri = $request->query->get('tri', 'dateCreation');
        $ordre = $request->query->get('ordre', 'DESC');
        $dateDebut = $request->query->get('date_debut') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_debut')) : null;
        $dateFin = $request->query->get('date_fin') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_fin')) : null;
        $genre = $request->query->get('genre');
        $dossiers = $this->repository->searchAndFilter($search, $tri, $ordre, $dateDebut, $dateFin, $genre, 5000, 0);
        return $this->excelExport->exportDossiers($dossiers);
    }

    #[Route('/nouveau', name: 'app_admin_dossier_new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        $dossier = new DossierMedical();
        $form = $this->createForm(DossierMedicalType::class, $dossier);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $this->repository->save($dossier, true);
            $this->emailService->sendNewDossierNotification(
                $dossier->getNumeroDossier() ?? '',
                $dossier->getPatientNom() ?? '',
                $dossier->getPatientPrenom() ?? ''
            );
            $this->addFlash('success', 'Dossier créé.');
            return $this->redirectToRoute('app_admin_dossier_index');
        }
        return $this->render('admin/dossier_medical/form.html.twig', [
            'dossier' => $dossier,
            'form' => $form,
            'prefix' => '/admin',
        ]);
    }

    #[Route('/{id}', name: 'app_admin_dossier_show', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function show(DossierMedical $dossier): Response
    {
        return $this->render('admin/dossier_medical/show.html.twig', [
            'dossier' => $dossier,
            'prefix' => '/admin',
            'can_edit_dossier' => true,
        ]);
    }

    #[Route('/{id}/resume-ia', name: 'app_admin_dossier_resume_ia', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function resumeIa(DossierMedical $dossier): Response
    {
        $parts = [];
        $parts[] = sprintf(
            "Dossier %s – %s %s – Naissance %s – Genre %s – Email %s – Tél %s – Adresse %s – Remarques : %s",
            $dossier->getNumeroDossier() ?? '',
            $dossier->getPatientNom() ?? '',
            $dossier->getPatientPrenom() ?? '',
            $dossier->getDateNaissance() ? $dossier->getDateNaissance()->format('d/m/Y') : '—',
            $dossier->getGenre() ?? '—',
            $dossier->getEmail() ?? '—',
            $dossier->getTelephone() ?? '—',
            $dossier->getAdresse() ?? '—',
            $dossier->getRemarques() ?? '—'
        );
        foreach ($dossier->getDocuments() as $doc) {
            $parts[] = sprintf(
                "[Document %s – %s – %s] %s",
                $doc->getTypeDocument(),
                $doc->getDateDocument() ? $doc->getDateDocument()->format('d/m/Y') : '',
                $doc->getTitre() ?? '',
                $doc->getContenu() ?? ''
            );
        }
        $text = implode("\n\n", $parts);
        $summary = $this->geminiService->summarizeDossier($text);

        return $this->json([
            'resume' => $summary ?? 'Impossible de générer le résumé (vérifier GEMINI_API_KEY ou connexion).',
        ]);
    }

    #[Route('/{id}/export/pdf', name: 'app_admin_dossier_export_pdf', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function exportPdf(DossierMedical $dossier): Response
    {
        $pdf = $this->pdfExport->generateDossierPdf($dossier);
        $response = new Response($pdf);
        $response->headers->set('Content-Type', 'application/pdf');
        $response->headers->set('Content-Disposition', 'attachment; filename="dossier-' . ($dossier->getNumeroDossier() ?? $dossier->getId()) . '.pdf"');
        return $response;
    }

    #[Route('/{id}/modifier', name: 'app_admin_dossier_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(Request $request, DossierMedical $dossier): Response
    {
        $form = $this->createForm(DossierMedicalType::class, $dossier);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $this->repository->save($dossier, true);
            $this->addFlash('success', 'Dossier mis à jour.');
            return $this->redirectToRoute('app_admin_dossier_show', ['id' => $dossier->getId()]);
        }
        return $this->render('admin/dossier_medical/form.html.twig', [
            'dossier' => $dossier,
            'form' => $form,
            'prefix' => '/admin',
        ]);
    }

    #[Route('/{id}', name: 'app_admin_dossier_delete', requirements: ['id' => '\d+'], methods: ['POST'])]
    public function delete(Request $request, DossierMedical $dossier): Response
    {
        $this->repository->remove($dossier, true);
        $this->addFlash('success', 'Dossier supprimé.');
        return $this->redirectToRoute('app_admin_dossier_index');
    }
}
