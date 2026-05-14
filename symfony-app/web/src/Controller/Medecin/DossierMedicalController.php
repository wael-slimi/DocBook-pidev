<?php

declare(strict_types=1);

namespace App\Controller\Medecin;

use App\Entity\DossierMedical;
use App\Form\DossierMedicalType;
use App\Repository\DossierMedicalRepository;
use App\Service\EmailService;
use App\Service\RoleAccessService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/medecin/dossiers')]
#[IsGranted('ROLE_DOCTOR')]
class DossierMedicalController extends AbstractController
{
    public function __construct(
        private readonly DossierMedicalRepository $repository,
        private readonly RoleAccessService $roleAccess,
        private readonly EmailService $emailService,
    ) {
    }

    #[Route('', name: 'app_medecin_dossier_index', methods: ['GET'])]
    public function index(Request $request): Response
    {
        $search = $request->query->get('q');
        $tri = $request->query->get('tri', 'dateCreation');
        $ordre = $request->query->get('ordre', 'DESC');
        $dateDebut = $request->query->get('date_debut') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_debut')) : null;
        $dateFin = $request->query->get('date_fin') ? \DateTime::createFromFormat('Y-m-d', $request->query->get('date_fin')) : null;
        $genre = $request->query->get('genre');

        $items = $this->repository->searchAndFilter($search, $tri, $ordre, $dateDebut, $dateFin, $genre);
        $total = $this->repository->countSearchAndFilter($search, $dateDebut, $dateFin, $genre);

        if ($request->isXmlHttpRequest() || $request->query->get('ajax')) {
            $response = $this->render('medecin/dossier_medical/_list_rows.html.twig', [
                'dossiers' => $items,
                'can_edit_dossier' => true,
                'can_delete_dossier' => true,
                'prefix' => '/medecin',
            ]);
            $response->headers->set('X-Total-Count', (string) $total);
            return $response;
        }

        return $this->render('medecin/dossier_medical/index.html.twig', [
            'dossiers' => $items,
            'total' => $total,
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

    #[Route('/nouveau', name: 'app_medecin_dossier_new', methods: ['GET', 'POST'])]
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
            return $this->redirectToRoute('app_medecin_dossier_index');
        }
        return $this->render('medecin/dossier_medical/form.html.twig', [
            'dossier' => $dossier,
            'form' => $form,
            'prefix' => '/medecin',
        ]);
    }

    #[Route('/{id}', name: 'app_medecin_dossier_show', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function show(DossierMedical $dossier): Response
    {
        return $this->render('medecin/dossier_medical/show.html.twig', [
            'dossier' => $dossier,
            'prefix' => '/medecin',
            'can_edit_dossier' => true,
        ]);
    }

    #[Route('/{id}/modifier', name: 'app_medecin_dossier_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(Request $request, DossierMedical $dossier): Response
    {
        $form = $this->createForm(DossierMedicalType::class, $dossier);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $this->repository->save($dossier, true);
            $this->addFlash('success', 'Dossier mis à jour.');
            return $this->redirectToRoute('app_medecin_dossier_show', ['id' => $dossier->getId()]);
        }
        return $this->render('medecin/dossier_medical/form.html.twig', [
            'dossier' => $dossier,
            'form' => $form,
            'prefix' => '/medecin',
        ]);
    }

    #[Route('/{id}', name: 'app_medecin_dossier_delete', requirements: ['id' => '\d+'], methods: ['POST'])]
    public function delete(Request $request, DossierMedical $dossier): Response
    {
        $this->repository->remove($dossier, true);
        $this->addFlash('success', 'Dossier supprimé.');
        return $this->redirectToRoute('app_medecin_dossier_index');
    }
}
