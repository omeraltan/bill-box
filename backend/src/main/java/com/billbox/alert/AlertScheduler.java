package com.billbox.alert;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertScheduler {

    private final AlertService alertService;

    public AlertScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(cron = "0 15 8 * * *")
    public void dailyScan() {
        alertService.scanDueInvoices();
    }
}
