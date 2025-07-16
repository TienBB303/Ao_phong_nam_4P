package com.example.datn.services.product_and_other;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class Barcode {

    public static String generateBarcodeImage(String barcodeText) throws IOException, WriterException {
        int width = 300;
        int height = 100;

        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.CODE_128, width, height);

        // Tạo thư mục nếu chưa có
        Path dirPath = Paths.get("D:/barcode");
        Files.createDirectories(dirPath);

        String filename = barcodeText + ".png";
        Path path = dirPath.resolve(filename);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        return filename;
    }
}
