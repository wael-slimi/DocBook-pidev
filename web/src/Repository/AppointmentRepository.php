<?php

namespace App\Repository;

use App\Entity\Appointment;
use App\Entity\User;
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
     * @return Appointment[]
     */
    public function findTodayByDoctor(User $doctor): array
    {
        $today = (new \DateTime())->setTime(0, 0, 0);
        $tomorrow = (clone $today)->modify('+1 day');

        return $this->createQueryBuilder('a')
            ->andWhere('a.doctor = :doctor')
            ->andWhere('a.scheduledAt >= :start')
            ->andWhere('a.scheduledAt < :end')
            ->setParameter('doctor', $doctor)
            ->setParameter('start', $today)
            ->setParameter('end', $tomorrow)
            ->orderBy('a.scheduledAt', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
