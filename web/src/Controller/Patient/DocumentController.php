<?php

declare(strict_types=1);

namespace App\Controller\Patient;

use App\Entity\Document;
use App\Entity\DossierMedical;
use App\Form\DocumentType;
use App\Repository\DocumentRepository;
use App\Repository\DossierMedicalRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/patient/dossiers/{dossierId}/documents', requirements: ['dossierId' => '\d+'])]
class DocumentController extends AbstractController
{
    public function __construct(
        private readonly DocumentRepository $documentRepository,
        private readonly DossierMedicalRepository $dossierRepository
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

    #[Route('', name: 'app_patient_document_index', methods: ['GET'])]
    public function index(Request $request, int $dossierId): Response
    {
        $dossier = $this->getDossier($dossierId);
        $search = $request->query->get('q');
        $tri = $request->query->get('tri', 'dateDocument');
        $ordre = $request->query->get('ordre', 'DESC');
        $type = $request->query->get('type');
        $dateDebut = $request->query->get('date_debut') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_debut')) : null;
        $dateFin = $request->query->get('date_fin') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_fin')) : null;

        $items = $this->documentRepository->searchAndFilterByDossier($dossier, $search, $tri, $ordre, $type, $dateDebut, $dateFin);
        $total = $this->documentRepository->countSearchAndFilterByDossier($dossier, $search, $type, $dateDebut, $dateFin);

        if ($request->isXmlHttpRequest() || $request->query->get('ajax')) {
            $response = $this->render('patient/document/_list_rows.html.twig', [
                'documents' => $items,
                'dossier' => $dossier,
                'prefix' => '/patient',
            ]);
            $response->headers->set('X-Total-Count', (string) $total);
            return $response;
        }

        return $this->render('patient/document/index.html.twig', [
            'documents' => $items,
            'dossier' => $dossier,
            'total' => $total,
            'q' => $search,
            'tri' => $tri,
            'ordre' => $ordre,
            'type' => $type,
            'date_debut' => $request->query->get('date_debut'),
            'date_fin' => $request->query->get('date_fin'),
            'prefix' => '/patient',
        ]);
    }

    #[Route('/nouveau', name: 'app_patient_document_new', methods: ['GET', 'POST'])]
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
            return $this->redirectToRoute('app_patient_document_index', ['dossierId' => $dossierId]);
        }
        return $this->render('patient/document/form.html.twig', [
            'document' => $document,
            'dossier' => $dossier,
            'form' => $form,
            'prefix' => '/patient',
        ]);
    }

    #[Route('/{id}', name: 'app_patient_document_show', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function show(int $dossierId, Document $document): Response
    {
        $dossier = $this->getDossier($dossierId);
        if ($document->getDossierMedical() !== $dossier) {
            throw $this->createNotFoundException();
        }
        return $this->render('patient/document/show.html.twig', [
            'document' => $document,
            'dossier' => $dossier,
            'prefix' => '/patient',
        ]);
    }

    #[Route('/{id}/modifier', name: 'app_patient_document_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
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
            return $this->redirectToRoute('app_patient_document_index', ['dossierId' => $dossierId]);
        }
        return $this->render('patient/document/form.html.twig', [
            'document' => $document,
            'dossier' => $dossier,
            'form' => $form,
            'prefix' => '/patient',
        ]);
    }

    #[Route('/{id}', name: 'app_patient_document_delete', requirements: ['id' => '\d+'], methods: ['POST'])]
    public function delete(Request $request, int $dossierId, Document $document): Response
    {
        $dossier = $this->getDossier($dossierId);
        if ($document->getDossierMedical() !== $dossier) {
            throw $this->createNotFoundException();
        }
        $this->documentRepository->remove($document, true);
        $this->addFlash('success', 'Document supprimé.');
        return $this->redirectToRoute('app_patient_document_index', ['dossierId' => $dossierId]);
    }
}
