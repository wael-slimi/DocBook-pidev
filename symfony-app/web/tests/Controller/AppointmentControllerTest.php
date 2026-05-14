<?php

namespace App\Tests\Controller;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class AppointmentControllerTest extends WebTestCase
{
    private function createAuthenticatedClient(): \Symfony\Bundle\FrameworkBundle\KernelBrowser
    {
        $client = static::createClient();
        $container = static::getContainer();
        $em = $container->get(EntityManagerInterface::class);

        $user = $em->getRepository(User::class)->findOneBy([]);
        if (!$user) {
            $user = new User();
            $user->setEmail('test@example.com');
            $user->setPassword('$2y$13$dummy');
            $user->setName('Test User');
            $user->setRole(\App\Enum\UserRole::PATIENT);
            $user->setIsVerified(true);
            $user->setCreationDate(new \DateTimeImmutable());
            $em->persist($user);
            $em->flush();
        }

        $client->loginUser($user);
        return $client;
    }

    public function testAppointmentNewPageLoadsSuccessfully(): void
    {
        $client = $this->createAuthenticatedClient();
        $crawler = $client->request('GET', '/appointment/new');

        $this->assertResponseIsSuccessful();

        $mainHeading = $crawler->filter('.max-w-2xl > .flex-col > h1');
        $this->assertCount(1, $mainHeading, 'Main content h1 should exist');
        $this->assertStringContainsString('Schedule New Appointment', $mainHeading->text());

        $doctorSelect = $crawler->filter('#appointment_doctor');
        $this->assertCount(1, $doctorSelect, 'Doctor dropdown should exist');

        $options = $crawler->filter('#appointment_doctor option');
        $this->assertGreaterThanOrEqual(1, $options->count(), 'Doctor dropdown should have at least the placeholder option');
    }

    public function testAppointmentIndexPageLoadsSuccessfully(): void
    {
        $client = $this->createAuthenticatedClient();
        $crawler = $client->request('GET', '/appointment/');

        $this->assertResponseIsSuccessful();

        $mainHeading = $crawler->filter('.max-w-6xl > .flex-col > h1');
        $this->assertCount(1, $mainHeading, 'Main content h1 should exist');
        $this->assertStringContainsString('My Appointments', $mainHeading->text());
    }
}
