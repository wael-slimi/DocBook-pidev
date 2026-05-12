<?php

namespace App\Command;

use App\Entity\Appointment;
use App\Repository\AppointmentRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:archive-appointments',
    description: 'Set status to Expired for all past appointments that are still Pending.',
)]
class ArchiveAppointmentsCommand extends Command
{
    public function __construct(
        private readonly AppointmentRepository $appointmentRepository,
        private readonly EntityManagerInterface $entityManager
    ) {
        parent::__construct();
    }

    protected function configure(): void
    {
        $this->addOption('dry-run', null, InputOption::VALUE_NONE, 'Do not persist changes, only report what would be archived.');
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $dryRun = (bool) $input->getOption('dry-run');

        $appointments = $this->appointmentRepository->findPastPending();
        $count = \count($appointments);

        if ($count === 0) {
            $io->success('No past pending appointments to archive.');
            return Command::SUCCESS;
        }

        if ($dryRun) {
            $io->note(sprintf('Dry run: %d appointment(s) would be set to Expired.', $count));
            foreach ($appointments as $a) {
                $io->listing([sprintf('Appointment #%d — %s with %s at %s', $a->getId(), $a->getDepartment(), $a->getDoctor()?->getName() ?? 'Unknown', $a->getScheduledAt()?->format('Y-m-d H:i'))]);
            }
            return Command::SUCCESS;
        }

        foreach ($appointments as $appointment) {
            $appointment->setStatus(Appointment::STATUS_EXPIRED);
        }
        $this->entityManager->flush();

        $io->success(sprintf('Archived %d appointment(s): status set to Expired.', $count));
        return Command::SUCCESS;
    }
}
