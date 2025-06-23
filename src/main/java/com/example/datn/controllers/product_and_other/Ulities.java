package com.example.datn.controllers.product_and_other;

import com.example.datn.services.product_and_other.ProductService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Path;

public class Ulities {

    public static void generateBarcodeImage(String barcodeText, String path) throws Exception {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(barcodeText, BarcodeFormat.CODE_128, 300, 100);
        Path outputPath = new File(path).toPath();
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputPath);
    }
}
