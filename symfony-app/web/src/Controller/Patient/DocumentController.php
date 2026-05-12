<?php

namespace App\Controller\Patient;

use App\Entity\Document;
use App\Entity\DossierMedical;
use App\Form\DocumentType;
use App\Repository\DocumentRepository;
use App\Repository\DossierMedicalRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/patient/dossiers/{dossierId}/documents', requirements: ['dossierId' => '\d+'])]
class DocumentController extends AbstractController
{
    public function __construct(
        private readonly DocumentRepository $documentRepository,
        private readonly DossierMedicalRepository $dossierRepository,
        private readonly EntityManagerInterface $em,
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

        $documents = $this->documentRepository->searchAndFilterByDossier(
            $dossier, $search, $tri, $ordre, $type
        );
        $total = $this->documentRepository->countSearchAndFilterByDossier(
            $dossier, $search, $type
        );

        return $this->render('patient/document/index.html.twig', [
            'documents' => $documents,
            'dossier' => $dossier,
            'total' => $total,
            'q' => $search,
            'tri' => $tri,
            'ordre' => $ordre,
            'type' => $type,
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
            'form' => $form->createView(),
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
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'app_patient_document_delete', requirements: ['id' => '\d+'], methods: ['POST'])]
    public function delete(Request $request, int $dossierId, Document $document): Response
    {
        $dossier = $this->getDossier($dossierId);
        if ($document->getDossierMedical() !== $dossier) {
            throw $this->createNotFoundException();
        }

        if ($this->isCsrfTokenValid('delete' . $document->getId(), $request->get('_token'))) {
            $this->documentRepository->remove($document, true);
            $this->addFlash('success', 'Document supprimé.');
        }

        return $this->redirectToRoute('app_patient_document_index', ['dossierId' => $dossierId]);
    }
}