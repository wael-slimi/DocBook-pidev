<?php

declare(strict_types=1);

namespace App\Service;

use chillerlan\QRCode\QRCode;
use chillerlan\QRCode\QROptions;

final class QrCodeService
{
    public function __construct(
        private readonly string $projectDir
    ) {
    }

    public function getDataUri(string $data, int $size = 120): string
    {
        $options = new QROptions([
            'version'    => 5,
            'outputType' => QRCode::OUTPUT_MARKUP_SVG,
            'eccLevel'  => QRCode::ECC_L,
            'scale'     => 4,
        ]);
        $qrcode = new QRCode($options);
        $svg = $qrcode->render($data);
        return 'data:image/svg+xml;base64,' . base64_encode($svg);
    }

    public function getSvg(string $data): string
    {
        $options = new QROptions([
            'version'    => 5,
            'outputType' => QRCode::OUTPUT_MARKUP_SVG,
            'eccLevel'  => QRCode::ECC_L,
            'scale'     => 4,
        ]);
        return (new QRCode($options))->render($data);
    }

    public function getPngDataUri(string $data): string
    {
        if (!extension_loaded('gd')) {
            return '';
        }
        $options = new QROptions([
            'version'     => 5,
            'outputType'  => QRCode::OUTPUT_IMAGE_PNG,
            'eccLevel'    => QRCode::ECC_L,
            'scale'       => 4,
        ]);
        $qrcode = new QRCode($options);
        $png = $qrcode->render($data);
        return 'data:image/png;base64,' . base64_encode($png);
    }

    public function savePngForPdf(string $data): ?string
    {
        if (!extension_loaded('gd')) {
            return null;
        }
        $dir = $this->projectDir . '/var/qrcode';
        if (!is_dir($dir)) {
            mkdir($dir, 0755, true);
        }
        $filename = 'qr_' . bin2hex(random_bytes(8)) . '.png';
        $fullPath = $dir . '/' . $filename;
        $options = new QROptions([
            'version'     => 5,
            'outputType'  => QRCode::OUTPUT_IMAGE_PNG,
            'eccLevel'    => QRCode::ECC_L,
            'scale'       => 3,
        ]);
        $qrcode = new QRCode($options);
        $png = $qrcode->render($data);
        if (file_put_contents($fullPath, $png) === false) {
            return null;
        }
        return 'var/qrcode/' . $filename;
    }
}
