<?php

declare(strict_types=1);

namespace App\Repository;

use App\Entity\DossierMedical;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<DossierMedical>
 */
class DossierMedicalRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, DossierMedical::class);
    }

    /**
     * @return DossierMedical[]
     */
    public function searchAndFilter(
        ?string $search = null,
        ?string $tri = 'dateCreation',
        ?string $ordre = 'DESC',
        ?\DateTimeInterface $dateDebut = null,
        ?\DateTimeInterface $dateFin = null,
        ?string $genre = null,
        ?int $limit = 100,
        ?int $offset = 0
    ): array {
        $qb = $this->createQueryBuilder('d');

        if ($search !== null && $search !== '') {
            $qb->andWhere(
                $qb->expr()->orX(
                    $qb->expr()->like('d.numeroDossier', ':search'),
                    $qb->expr()->like('d.patientNom', ':search'),
                    $qb->expr()->like('d.patientPrenom', ':search'),
                    $qb->expr()->like('d.email', ':search')
                )
            )->setParameter('search', '%' . $search . '%');
        }

        if ($dateDebut !== null) {
            $qb->andWhere('d.dateCreation >= :dateDebut')->setParameter('dateDebut', $dateDebut);
        }
        if ($dateFin !== null) {
            $qb->andWhere('d.dateCreation <= :dateFin')->setParameter('dateFin', $dateFin);
        }
        if ($genre !== null && $genre !== '') {
            $qb->andWhere('d.genre = :genre')->setParameter('genre', $genre);
        }

        $allowedSort = ['dateCreation', 'dateModification', 'patientNom', 'patientPrenom', 'numeroDossier'];
        if (\in_array($tri, $allowedSort, true)) {
            $qb->orderBy('d.' . $tri, $ordre === 'ASC' ? 'ASC' : 'DESC');
        } else {
            $qb->orderBy('d.dateCreation', 'DESC');
        }

        $qb->setMaxResults($limit)->setFirstResult($offset);

        return $qb->getQuery()->getResult();
    }

    public function countSearchAndFilter(
        ?string $search = null,
        ?\DateTimeInterface $dateDebut = null,
        ?\DateTimeInterface $dateFin = null,
        ?string $genre = null
    ): int {
        $qb = $this->createQueryBuilder('d')->select('COUNT(d.id)');

        if ($search !== null && $search !== '') {
            $qb->andWhere(
                $qb->expr()->orX(
                    $qb->expr()->like('d.numeroDossier', ':search'),
                    $qb->expr()->like('d.patientNom', ':search'),
                    $qb->expr()->like('d.patientPrenom', ':search'),
                    $qb->expr()->like('d.email', ':search')
                )
            )->setParameter('search', '%' . $search . '%');
        }
        if ($dateDebut !== null) {
            $qb->andWhere('d.dateCreation >= :dateDebut')->setParameter('dateDebut', $dateDebut);
        }
        if ($dateFin !== null) {
            $qb->andWhere('d.dateCreation <= :dateFin')->setParameter('dateFin', $dateFin);
        }
        if ($genre !== null && $genre !== '') {
            $qb->andWhere('d.genre = :genre')->setParameter('genre', $genre);
        }

        return (int) $qb->getQuery()->getSingleScalarResult();
    }

    public function save(DossierMedical $entity, bool $flush = false): void
    {
        $this->getEntityManager()->persist($entity);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    public function remove(DossierMedical $entity, bool $flush = false): void
    {
        $this->getEntityManager()->remove($entity);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }
}
