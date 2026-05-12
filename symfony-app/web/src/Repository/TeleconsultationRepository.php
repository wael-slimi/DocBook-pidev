<?php

namespace App\Repository;

use App\Entity\Teleconsultation;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Teleconsultation>
 */
class TeleconsultationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Teleconsultation::class);
    }

    /**
     * @return Teleconsultation[]
     */
    public function searchAndFilter(?string $q, ?string $mode): array
    {
        $qb = $this->createQueryBuilder('t')
            ->leftJoin('t.appointment', 'a')
            ->addOrderBy('t.id', 'DESC');

        if ($q !== null && ($trimmed = trim($q)) !== '') {
            $or = $qb->expr()->orX('t.meetingUrl LIKE :q');
            if (is_numeric($trimmed)) {
                $or->add('a.id = :qId');
                $qb->setParameter('qId', (int) $trimmed);
            }
            $qb->andWhere($or)->setParameter('q', '%' . $trimmed . '%');
        }

        if ($mode !== null && $mode !== '') {
            $qb->andWhere('t.mode = :mode')->setParameter('mode', $mode);
        }

        return $qb->getQuery()->getResult();
    }

    //    /**
    //     * @return Teleconsultation[] Returns an array of Teleconsultation objects
    //     */
    //    public function findByExampleField($value): array
    //    {
    //        return $this->createQueryBuilder('t')
    //            ->andWhere('t.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->orderBy('t.id', 'ASC')
    //            ->setMaxResults(10)
    //            ->getQuery()
    //            ->getResult()
    //        ;
    //    }

    //    public function findOneBySomeField($value): ?Teleconsultation
    //    {
    //        return $this->createQueryBuilder('t')
    //            ->andWhere('t.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    }
}
