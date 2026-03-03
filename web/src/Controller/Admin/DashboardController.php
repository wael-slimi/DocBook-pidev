<?php

declare(strict_types=1);

namespace App\Controller\Admin;

use App\Repository\DocumentRepository;
use App\Repository\DossierMedicalRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin')]
class DashboardController extends AbstractController
{
    public function __construct(
        private readonly DossierMedicalRepository $dossierRepository,
        private readonly DocumentRepository $documentRepository,
    ) {
    }

    #[Route('', name: 'app_admin_dashboard', methods: ['GET'])]
    public function index(): Response
    {
        $totalDossiers = $this->dossierRepository->count([]);
        $totalDocuments = $this->documentRepository->count([]);

        $byGenre = $this->dossierRepository->createQueryBuilder('d')
            ->select('d.genre as g, COUNT(d.id) as cnt')
            ->groupBy('d.genre')
            ->getQuery()
            ->getResult();

        $byType = $this->documentRepository->createQueryBuilder('doc')
            ->select('doc.typeDocument as t, COUNT(doc.id) as cnt')
            ->groupBy('doc.typeDocument')
            ->getQuery()
            ->getResult();

        $lastMonths = [];
        for ($i = 5; $i >= 0; $i--) {
            $date = new \DateTimeImmutable('first day of ' . $i . ' months ago');
            $start = $date->modify('first day of this month')->setTime(0, 0);
            $end = $date->modify('last day of this month')->setTime(23, 59, 59);
            $countD = $this->dossierRepository->createQueryBuilder('d')
                ->select('COUNT(d.id)')
                ->where('d.dateCreation >= :start')
                ->andWhere('d.dateCreation <= :end')
                ->setParameter('start', $start)
                ->setParameter('end', $end)
                ->getQuery()
                ->getSingleScalarResult();
            $countDoc = $this->documentRepository->createQueryBuilder('doc')
                ->select('COUNT(doc.id)')
                ->where('doc.dateCreation >= :start')
                ->andWhere('doc.dateCreation <= :end')
                ->setParameter('start', $start)
                ->setParameter('end', $end)
                ->getQuery()
                ->getSingleScalarResult();
            $lastMonths[] = [
                'label' => $start->format('M Y'),
                'dossiers' => (int) $countD,
                'documents' => (int) $countDoc,
            ];
        }

        return $this->render('admin/dashboard/index.html.twig', [
            'total_dossiers' => $totalDossiers,
            'total_documents' => $totalDocuments,
            'by_genre' => $byGenre,
            'by_type' => $byType,
            'last_months' => $lastMonths,
        ]);
    }
}
