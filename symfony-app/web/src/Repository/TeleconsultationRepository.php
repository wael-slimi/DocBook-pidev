<?php

declare(strict_types=1);

namespace App\Repository;

use App\Entity\Appointment;
use App\Entity\Teleconsultation;
use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class TeleconsultationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Teleconsultation::class);
    }

    public function getByDoctor(User $doctor): array
    {
        return $this->createQueryBuilder('t')
            ->join('t.appointment', 'a')
            ->where('a.doctor = :doctor')
            ->setParameter('doctor', $doctor)
            ->orderBy('t.id', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function getByPatient(User $patient): array
    {
        return $this->createQueryBuilder('t')
            ->join('t.appointment', 'a')
            ->where('a.patient = :patient')
            ->setParameter('patient', $patient)
            ->orderBy('t.id', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function searchAndFilter(?string $q, ?string $mode, ?User $doctor = null, ?User $patient = null): array
    {
        $qb = $this->createQueryBuilder('t')
            ->join('t.appointment', 'a')
            ->addOrderBy('t.id', 'DESC');

        if ($doctor !== null) {
            $qb->andWhere('a.doctor = :doctor')->setParameter('doctor', $doctor);
        }
        if ($patient !== null) {
            $qb->andWhere('a.patient = :patient')->setParameter('patient', $patient);
        }

        if ($q !== null && ($trimmed = trim($q)) !== '') {
            $qb->andWhere('t.videoLink LIKE :q')->setParameter('q', '%' . $trimmed . '%');
        }

        if ($mode !== null && $mode !== '') {
            $qb->andWhere('t.mode = :mode')->setParameter('mode', $mode);
        }

        return $qb->getQuery()->getResult();
    }

    public function getPendingAppointmentsForDoctor(User $doctor): array
    {
        return $this->getEntityManager()->createQueryBuilder()
            ->select('a')
            ->from(Appointment::class, 'a')
            ->where('a.doctor = :doctor')
            ->andWhere('a.status = :status')
            ->setParameter('doctor', $doctor)
            ->setParameter('status', Appointment::STATUS_PENDING)
            ->orderBy('a.scheduledAt', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function save(Teleconsultation $entity, bool $flush = false): void
    {
        $this->getEntityManager()->persist($entity);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    public function remove(Teleconsultation $entity, bool $flush = false): void
    {
        $this->getEntityManager()->remove($entity);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }
}
