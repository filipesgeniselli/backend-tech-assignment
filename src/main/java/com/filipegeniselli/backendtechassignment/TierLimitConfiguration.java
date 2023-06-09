package com.filipegeniselli.backendtechassignment;

import com.filipegeniselli.backendtechassignment.dealer.DealerTierLimit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "tier-limits")
public class TierLimitConfiguration {
    private final int free;
    private final int basic;
    private final int premium;
    private final int business;

    @ConstructorBinding
    public TierLimitConfiguration(int free, int basic, int premium, int business) {
        this.free = free;
        this.basic = basic;
        this.premium = premium;
        this.business = business;
    }

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
