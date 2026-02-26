<?php

namespace App\Repository;

use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;
use App\Entity\Doctor;
use App\Enum\UserRole;

/**
 * @extends ServiceEntityRepository<User>
 */
class UserRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, User::class);
    }
    
    public function findDoctorsByFilters(?string $name, ?string $specialty, ?float $minPrice, ?float $maxPrice): array
    {
        $qb = $this->getEntityManager()->createQueryBuilder();
        
        // We query the Doctor entity specifically
        $qb->select('d')
           ->from(\App\Entity\Doctor::class, 'd');
    
        // 1. Name Filter (Case-insensitive)
        if (!empty($name)) {
            $qb->andWhere('LOWER(d.name) LIKE LOWER(:name)')
               ->setParameter('name', '%' . $name . '%');
        }
    
        // 2. Specialty Filter
        // Note: We expect the lowercase 'backed value' here (e.g., 'cardiology')
        if (!empty($specialty) && $specialty !== 'All') {
            $qb->andWhere('d.specialty = :specialty')
               ->setParameter('specialty', $specialty);
        }
    
        // 3. Price Filters
        if ($minPrice !== null) {
            $qb->andWhere('d.consultationFee >= :minPrice')
               ->setParameter('minPrice', $minPrice);
        }
    
        if ($maxPrice !== null) {
            $qb->andWhere('d.consultationFee <= :maxPrice')
               ->setParameter('maxPrice', $maxPrice);
        }
    
        return $qb->getQuery()->getResult();
    }
   

//    /**
//     * @return User[] Returns an array of User objects
//     */
//    public function findByExampleField($value): array
//    {
//        return $this->createQueryBuilder('u')
//            ->andWhere('u.exampleField = :val')
//            ->setParameter('val', $value)
//            ->orderBy('u.id', 'ASC')
//            ->setMaxResults(10)
//            ->getQuery()
//            ->getResult()
//        ;
//    }

//    public function findOneBySomeField($value): ?User
//    {
//        return $this->createQueryBuilder('u')
//            ->andWhere('u.exampleField = :val')
//            ->setParameter('val', $value)
//            ->getQuery()
//            ->getOneOrNullResult()
//        ;
//    }
}
