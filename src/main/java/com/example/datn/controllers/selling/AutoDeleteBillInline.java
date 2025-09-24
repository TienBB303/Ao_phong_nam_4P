package com.example.datn.controllers.selling;

import com.example.datn.services.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class AutoDeleteBillInline {
    @Autowired
    private BillService billService;

    public AutoDeleteBillInline(BillService billService) {
        this.billService = billService;
    }

    // Chạy 1 lần mỗi ngày lúc 01:00
    @Scheduled(fixedRate = 20000)
//    @Scheduled(cron = "0 0 1 * * *")  // 0 giây - 0 phút - 1 giờ
    public void cleanupUnpaidBills() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        System.out.println("[AutoDeleteBillInline] Running cleanup at " + LocalDateTime.now());
        billService.deleteOldUnpaidBills(cutoff);
    }
}
