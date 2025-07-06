package com.example.datn.controllers.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController // Đánh dấu đây là một REST Controller
@RequestMapping("/api/geo") // Tất cả các endpoint trong controller này sẽ bắt đầu bằng /api/geo
public class GeoProxyController {

    private final RestTemplate restTemplate;

    public GeoProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ✅ Đọc dữ liệu tỉnh/huyện/xã từ file tĩnh
    @GetMapping("/provinces")
    public ResponseEntity<?> getProvincesFromLocalFile() {
        try {
            InputStream is = new ClassPathResource("data/provinces.json").getInputStream();
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể đọc dữ liệu địa phương từ file provinces.json");
        }
    }

    // ❌ Nếu vẫn muốn test gọi trực tiếp API online (sẽ lỗi nếu SSL không tin cậy)
    @GetMapping("/districts/{provinceCode}")
    public String getDistricts(@PathVariable String provinceCode) {
        String url = "https://provinces.open-api.vn/api/p/" + provinceCode + "?depth=2";
        return restTemplate.getForObject(url, String.class);
    }

    @GetMapping("/wards/{districtCode}")
    public String getWards(@PathVariable String districtCode) {
        String url = "https://provinces.open-api.vn/api/d/" + districtCode + "?depth=2";
        return restTemplate.getForObject(url, String.class);
    }
}
@Configuration
class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
