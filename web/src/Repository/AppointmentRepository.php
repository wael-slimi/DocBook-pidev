<?php

namespace App\Repository;

use App\Entity\Appointment;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Appointment>
 */
class AppointmentRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Appointment::class);
    }

    /**
     * Check if the given doctor already has an appointment at the same datetime.
     * Optionally exclude an appointment (e.g. when editing).
     */
    public function findConflict(string $doctor, \DateTimeInterface $scheduledAt, ?int $excludeAppointmentId = null): ?Appointment
    {
        $qb = $this->createQueryBuilder('a')
            ->andWhere('a.doctor = :doctor')
            ->andWhere('a.scheduledAt = :scheduledAt')
            ->setParameter('doctor', $doctor)
            ->setParameter('scheduledAt', $scheduledAt);

        if ($excludeAppointmentId !== null) {
            $qb->andWhere('a.id != :excludeId')->setParameter('excludeId', $excludeAppointmentId);
        }

        return $qb->getQuery()->getOneOrNullResult();
    }

    /**
     * Find all appointments that are in the past and still Pending (for archiving).
     *
     * @return Appointment[]
     */
    public function findPastPending(): array
    {
        return $this->createQueryBuilder('a')
            ->andWhere('a.scheduledAt < :now')
            ->andWhere('a.status = :status')
            ->setParameter('now', new \DateTimeImmutable())
            ->setParameter('status', Appointment::STATUS_PENDING)
            ->getQuery()
            ->getResult();
    }

    /**
     * Find appointments scheduled for today by a given doctor.
     *
     * @return Appointment[]
     */
    public function findTodayByDoctor(\Stringable|string $doctor): array
    {
        $now = new \DateTimeImmutable();
        $startOfDay = $now->setTime(0, 0, 0);
        $endOfDay = $now->setTime(23, 59, 59);

        return $this->createQueryBuilder('a')
            ->andWhere('a.doctor = :doctor')
            ->andWhere('a.scheduledAt >= :start')
            ->andWhere('a.scheduledAt <= :end')
            ->setParameter('doctor', (string) $doctor)
            ->setParameter('start', $startOfDay)
            ->setParameter('end', $endOfDay)
            ->orderBy('a.scheduledAt', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Find all appointments for a given user (as patient or doctor).
     *
     * @return Appointment[]
     */
    public function findByUser($user): array
    {
        return $this->createQueryBuilder('a')
            ->leftJoin('a.patient', 'p')
            ->leftJoin('a.doctor', 'd')
            ->andWhere('p.id = :user OR d.id = :user')
            ->setParameter('user', $user)
            ->orderBy('a.scheduledAt', 'ASC')
            ->getQuery()
            ->getResult();
    }

    //    /**
    //     * @return Appointment[] Returns an array of Appointment objects
    //     */
    //    public function findByExampleField($value): array
    //    {
    //        return $this->createQueryBuilder('a')
    //            ->andWhere('a.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->orderBy('a.id', 'ASC')
    //            ->setMaxResults(10)
    //            ->getQuery()
    //            ->getResult()
    //        ;
    //    }

    //    public function findOneBySomeField($value): ?Appointment
    //    {
    //        return $this->createQueryBuilder('a')
    //            ->andWhere('a.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    }
}
