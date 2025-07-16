package com.example.datn.controllers.product_and_other;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


@Controller
public class BarcodeController {

    @GetMapping("/barcode-images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getBarcodeImage(@PathVariable String filename) throws IOException {
        Path barcodePath = Paths.get("D:/barcode").resolve(filename);
        Resource resource = new UrlResource(barcodePath.toUri()); // CHÍNH XÁC: Resource là của Spring Core IO

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource); // KHÔNG cần ép kiểu nữa
    }
}
