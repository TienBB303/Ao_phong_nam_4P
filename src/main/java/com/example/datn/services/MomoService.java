package com.example.datn.services;

import com.example.datn.entities.Bill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

@Service
public class MomoService {
    @Autowired
    BillService billService;

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.returnUrl}")
    private String returnUrl;

    @Value("${momo.notifyUrl}")
    private String notifyUrl;

    public String createQrOrder(Integer cartId, BigDecimal amount) throws Exception {
        Bill bill = billService.findById(cartId);

        String requestId = String.valueOf(System.currentTimeMillis());
        String orderId = "CART" + cartId + "_" + requestId;
        String orderInfo = "Thanh toán đơn hàng " + bill.getCode();
        String extraData = "";

        String amountStr = amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
        // raw data đúng thứ tự
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amountStr +
                "&extraData=" + extraData +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + requestId +
                "&requestType=captureWallet";

        // sinh signature
        String signature = hmacSHA256(rawHash, secretKey);

        // JSON request
        JSONObject json = new JSONObject();
        json.put("partnerCode", partnerCode);
        json.put("accessKey", accessKey);
        json.put("requestId", requestId);
        json.put("amount", amountStr);
        json.put("orderId", orderId);
        json.put("orderInfo", orderInfo);
        json.put("redirectUrl", returnUrl);
        json.put("ipnUrl", notifyUrl);
        json.put("extraData", extraData);
        json.put("requestType", "captureWallet");
        json.put("signature", signature);

        System.out.println("MOMO REQUEST: " + json.toString(2));

        HttpResponse<String> response = Unirest.post(endpoint)
                .header("Content-Type", "application/json")
                .body(json.toString())
                .asString();

        System.out.println("MOMO RESPONSE: " + response.getBody());

        JSONObject res = new JSONObject(response.getBody());
        if (!res.has("payUrl")) {
            throw new RuntimeException("Momo error: " + res.toString());
        }
        return res.getString("payUrl");
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKeySpec);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // MoMo yêu cầu hex lowercase
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }
        return hash.toString();
    }
}
