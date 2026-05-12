<?php

namespace App\Tests\Controller;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class AppointmentControllerTest extends WebTestCase
{
public function testAppointmentNewPageLoadsSuccessfully(): void
     {
         $client = static::createClient();
         $crawler = $client->request('GET', '/appointment/new');

         $this->assertResponseIsSuccessful();

         // Verify the page heading in the main content area
         $mainHeading = $crawler->filter('.max-w-2xl > .flex-col > h1');
         $this->assertCount(1, $mainHeading, 'Main content h1 should exist');
         $this->assertStringContainsString('Schedule New Appointment', $mainHeading->text());

         // Verify doctor dropdown is populated (no "surgery" enum error)
         $doctorSelect = $crawler->filter('#appointment_doctor');
         $this->assertCount(1, $doctorSelect, 'Doctor dropdown should exist');

         // Verify doctor dropdown exists and has at least the placeholder option
         $options = $crawler->filter('#appointment_doctor option');
         $this->assertGreaterThanOrEqual(1, $options->count(), 'Doctor dropdown should have at least the placeholder option');
     }

public function testAppointmentIndexPageLoadsSuccessfully(): void
     {
         $client = static::createClient();
         $crawler = $client->request('GET', '/appointment/');

         $this->assertResponseIsSuccessful();

         // Verify the page heading in the main content area
         $mainHeading = $crawler->filter('.max-w-6xl > .flex-col > h1');
         $this->assertCount(1, $mainHeading, 'Main content h1 should exist');
         $this->assertStringContainsString('My Appointments', $mainHeading->text());
     }
}