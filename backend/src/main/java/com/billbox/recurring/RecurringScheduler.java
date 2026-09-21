package com.billbox.recurring;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecurringScheduler {

    private final RecurringService recurringService;

    public RecurringScheduler(RecurringService recurringService) {
        this.recurringService = recurringService;
    }

    @Scheduled(cron = "0 5 6 * * *")
    public void generate() {
        recurringService.generateDueRules();
    }
}
