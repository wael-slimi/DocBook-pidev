<?php

namespace App\Controller\Patient;

use App\Entity\DossierMedical;
use App\Form\DossierMedicalType;
use App\Repository\DossierMedicalRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/patient/dossiers')]
class DossierMedicalController extends AbstractController
{
    public function __construct(
        private readonly DossierMedicalRepository $repository,
        private readonly EntityManagerInterface $em,
    ) {
    }

    #[Route('', name: 'app_patient_dossier_index', methods: ['GET'])]
    public function index(Request $request): Response
    {
        $search = $request->query->get('q');
        $tri = $request->query->get('tri', 'dateCreation');
        $ordre = $request->query->get('ordre', 'DESC');
        $genre = $request->query->get('genre');

        $dossiers = $this->repository->searchAndFilter($search, $tri, $ordre, null, null, $genre);
        $total = $this->repository->countSearchAndFilter($search, null, null, $genre);

        return $this->render('patient/dossier_medical/index.html.twig', [
            'dossiers' => $dossiers,
            'total' => $total,
            'q' => $search,
            'tri' => $tri,
            'ordre' => $ordre,
            'genre' => $genre,
        ]);
    }

    #[Route('/nouveau', name: 'app_patient_dossier_new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        $dossier = new DossierMedical();
        $form = $this->createForm(DossierMedicalType::class, $dossier);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $this->repository->save($dossier, true);
            $this->addFlash('success', 'Dossier créé avec succès.');
            return $this->redirectToRoute('app_patient_dossier_index');
        }

        return $this->render('patient/dossier_medical/form.html.twig', [
            'dossier' => $dossier,
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'app_patient_dossier_show', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function show(DossierMedical $dossier): Response
    {
        return $this->render('patient/dossier_medical/show.html.twig', [
            'dossier' => $dossier,
        ]);
    }

    #[Route('/{id}/modifier', name: 'app_patient_dossier_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(Request $request, DossierMedical $dossier): Response
    {
        $form = $this->createForm(DossierMedicalType::class, $dossier);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $this->repository->save($dossier, true);
            $this->addFlash('success', 'Dossier mis à jour.');
            return $this->redirectToRoute('app_patient_dossier_show', ['id' => $dossier->getId()]);
        }

        return $this->render('patient/dossier_medical/form.html.twig', [
            'dossier' => $dossier,
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'app_patient_dossier_delete', requirements: ['id' => '\d+'], methods: ['POST'])]
    public function delete(Request $request, DossierMedical $dossier): Response
    {
        if ($this->isCsrfTokenValid('delete' . $dossier->getId(), $request->get('_token'))) {
            $this->repository->remove($dossier, true);
            $this->addFlash('success', 'Dossier supprimé.');
        }
        return $this->redirectToRoute('app_patient_dossier_index');
    }
}