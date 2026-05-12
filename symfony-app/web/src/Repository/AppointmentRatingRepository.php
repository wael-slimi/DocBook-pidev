<?php

namespace App\Repository;

use App\Entity\AppointmentRating;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<AppointmentRating>
 */
class AppointmentRatingRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, AppointmentRating::class);
    }

    public function findByAppointment(int $appointmentId): array
    {
        return $this->createQueryBuilder('r')
            ->andWhere('r.appointment = :appointment')
            ->setParameter('appointment', $appointmentId)
            ->orderBy('r.ratedAt', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function getAverageRating(int $appointmentId): float
    {
        $result = $this->createQueryBuilder('r')
            ->select('AVG(r.stars) as avgStars')
            ->andWhere('r.appointment = :appointment')
            ->setParameter('appointment', $appointmentId)
            ->getQuery()
            ->getSingleScalarResult();

        return round((float) $result, 2);
    }

    public function hasRated(int $appointmentId, int $patientId): bool
    {
        $result = $this->createQueryBuilder('r')
            ->select('COUNT(r.id)')
            ->andWhere('r.appointment = :appointment')
            ->andWhere('r.patient = :patient')
            ->setParameter('appointment', $appointmentId)
            ->setParameter('patient', $patientId)
            ->getQuery()
            ->getSingleScalarResult();

        return (int) $result > 0;
    }

    public function save(AppointmentRating $entity, bool $flush = false): void
    {
        $this->getEntityManager()->persist($entity);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    public function remove(AppointmentRating $entity, bool $flush = false): void
    {
        $this->getEntityManager()->remove($entity);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }
}