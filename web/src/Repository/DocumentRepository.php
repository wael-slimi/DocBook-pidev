<?php

declare(strict_types=1);

namespace App\Repository;

use App\Entity\Document;
use App\Entity\DossierMedical;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Document>
 */
class DocumentRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Document::class);
    }

    /**
     * @return Document[]
     */
    public function searchAndFilterByDossier(
        DossierMedical $dossier,
        ?string $search = null,
        ?string $tri = 'dateDocument',
        ?string $ordre = 'DESC',
        ?string $typeDocument = null,
        ?\DateTimeInterface $dateDebut = null,
        ?\DateTimeInterface $dateFin = null,
        ?int $limit = 100,
        ?int $offset = 0
    ): array {
        $qb = $this->createQueryBuilder('doc')
            ->andWhere('doc.dossierMedical = :dossier')
            ->setParameter('dossier', $dossier);

        if ($search !== null && $search !== '') {
            $qb->andWhere(
                $qb->expr()->orX(
                    $qb->expr()->like('doc.titre', ':search'),
                    $qb->expr()->like('doc.contenu', ':search')
                )
            )->setParameter('search', '%' . $search . '%');
        }
        if ($typeDocument !== null && $typeDocument !== '') {
            $qb->andWhere('doc.typeDocument = :type')->setParameter('type', $typeDocument);
        }
        if ($dateDebut !== null) {
            $qb->andWhere('doc.dateDocument >= :dateDebut')->setParameter('dateDebut', $dateDebut);
        }
        if ($dateFin !== null) {
            $qb->andWhere('doc.dateDocument <= :dateFin')->setParameter('dateFin', $dateFin);
        }

        $allowedSort = ['dateDocument', 'dateCreation', 'titre', 'typeDocument'];
        if (\in_array($tri, $allowedSort, true)) {
            $qb->orderBy('doc.' . $tri, $ordre === 'ASC' ? 'ASC' : 'DESC');
        } else {
            $qb->orderBy('doc.dateDocument', 'DESC');
        }

        $qb->setMaxResults($limit)->setFirstResult($offset);

        return $qb->getQuery()->getResult();
    }

    public function countSearchAndFilterByDossier(
        DossierMedical $dossier,
        ?string $search = null,
        ?string $typeDocument = null,
        ?\DateTimeInterface $dateDebut = null,
        ?\DateTimeInterface $dateFin = null
    ): int {
        $qb = $this->createQueryBuilder('doc')
            ->select('COUNT(doc.id)')
            ->andWhere('doc.dossierMedical = :dossier')
            ->setParameter('dossier', $dossier);

        if ($search !== null && $search !== '') {
            $qb->andWhere(
                $qb->expr()->orX(
                    $qb->expr()->like('doc.titre', ':search'),
                    $qb->expr()->like('doc.contenu', ':search')
                )
            )->setParameter('search', '%' . $search . '%');
        }
        if ($typeDocument !== null && $typeDocument !== '') {
            $qb->andWhere('doc.typeDocument = :type')->setParameter('type', $typeDocument);
        }
        if ($dateDebut !== null) {
            $qb->andWhere('doc.dateDocument >= :dateDebut')->setParameter('dateDebut', $dateDebut);
        }
        if ($dateFin !== null) {
            $qb->andWhere('doc.dateDocument <= :dateFin')->setParameter('dateFin', $dateFin);
        }

        return (int) $qb->getQuery()->getSingleScalarResult();
    }

    public function save(Document $entity, bool $flush = false): void
    {
        $this->getEntityManager()->persist($entity);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    public function remove(Document $entity, bool $flush = false): void
    {
        $this->getEntityManager()->remove($entity);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }
}
