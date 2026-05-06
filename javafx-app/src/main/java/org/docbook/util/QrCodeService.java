package org.docbook.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.docbook.entities.records.Document;
import org.docbook.entities.records.DossierMedical;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class QrCodeService {

    public Image generateDocumentQrImage(Document document, int size) {
        try {
            String payload = buildPayload(document);
            return generateQrFromPayload(payload, size);
        } catch (WriterException e) {
            return transparentPngFallback();
        }
    }

    public Image generatePatientQrImage(DossierMedical dossier, int size) {
        try {
            String payload = buildPatientPayload(dossier);
            return generateQrFromPayload(payload, size);
        } catch (WriterException e) {
            return transparentPngFallback();
        }
    }

    private Image generateQrFromPayload(String payload, int size) throws WriterException {
        BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size);
        WritableImage image = new WritableImage(size, size);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                pixelWriter.setArgb(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    public byte[] generateDocumentQrPngBytes(Document document, int size) {
        try {
            String payload = buildPayload(document);
            BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size);
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                int offset = y * size;
                for (int x = 0; x < size; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            return PngEncoder.encodeArgb(size, size, pixels);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private String buildPayload(Document document) {
        String baseUrl = LocalWebServerService.getInstance().getServerBaseUrl();
        return baseUrl + "/document?id=" + document.getId();
    }

    private String buildPatientPayload(DossierMedical dossier) {
        String baseUrl = LocalWebServerService.getInstance().getServerBaseUrl();
        return baseUrl + "/patient?id=" + dossier.getId();
    }

    private Image transparentPngFallback() {
        return new Image(new ByteArrayInputStream(PngEncoder.transparent1x1()));
    }

    private static final class PngEncoder {
        private static final byte[] PNG_SIG = new byte[]{
                (byte) 137, 80, 78, 71, 13, 10, 26, 10
        };

        static byte[] transparent1x1() {
            return new byte[]{
                    (byte) 137, 80, 78, 71, 13, 10, 26, 10,
                    0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1,
                    8, 6, 0, 0, 0, 31, 21, (byte) -60, (byte) -119,
                    0, 0, 0, 12, 73, 68, 65, 84, 120, (byte) -100, 99, 0, 1, 0, 0, 5, 0, 1,
                    13, 10, 45, (byte) -76, 0, 0, 0, 0, 73, 69, 78, 68, (byte) -82, 66, 96, (byte) -126
            };
        }

        static byte[] encodeArgb(int width, int height, int[] argb) throws Exception {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            out.write(PNG_SIG);

            java.io.ByteArrayOutputStream ihdr = new java.io.ByteArrayOutputStream();
            writeInt(ihdr, width);
            writeInt(ihdr, height);
            ihdr.write(8);
            ihdr.write(6);
            ihdr.write(0);
            ihdr.write(0);
            ihdr.write(0);
            writeChunk(out, "IHDR", ihdr.toByteArray());

            java.io.ByteArrayOutputStream raw = new java.io.ByteArrayOutputStream();
            for (int y = 0; y < height; y++) {
                raw.write(0);
                int row = y * width;
                for (int x = 0; x < width; x++) {
                    int p = argb[row + x];
                    raw.write((p >> 16) & 0xFF);
                    raw.write((p >> 8) & 0xFF);
                    raw.write(p & 0xFF);
                    raw.write((p >> 24) & 0xFF);
                }
            }

            java.io.ByteArrayOutputStream compressed = new java.io.ByteArrayOutputStream();
            try (java.util.zip.DeflaterOutputStream deflater = new java.util.zip.DeflaterOutputStream(compressed)) {
                deflater.write(raw.toByteArray());
            }
            writeChunk(out, "IDAT", compressed.toByteArray());
            writeChunk(out, "IEND", new byte[0]);
            return out.toByteArray();
        }

        private static void writeChunk(java.io.ByteArrayOutputStream out, String type, byte[] data) throws Exception {
            writeInt(out, data.length);
            byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
            out.write(typeBytes);
            out.write(data);
            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
            crc.update(typeBytes);
            crc.update(data);
            writeInt(out, (int) crc.getValue());
        }

        private static void writeInt(java.io.ByteArrayOutputStream out, int value) {
            out.write((value >>> 24) & 0xFF);
            out.write((value >>> 16) & 0xFF);
            out.write((value >>> 8) & 0xFF);
            out.write(value & 0xFF);
        }
    }
}