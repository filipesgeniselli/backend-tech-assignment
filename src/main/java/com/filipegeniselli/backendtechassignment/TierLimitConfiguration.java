package com.filipegeniselli.backendtechassignment;

import com.filipegeniselli.backendtechassignment.dealer.DealerTierLimit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tier-limits")
public class TierLimitConfiguration {
    private int free;
    private int basic;
    private int premium;
    private int business;

    public int getTierLimit(DealerTierLimit tierLimit) {
        int limit = 0;
        switch (tierLimit) {
            case FREE -> limit = free;
            case BASIC -> limit = basic;
            case PREMIUM -> limit = premium;
            case BUSINESS -> limit = business;
        }

        return limit;
    }
}
