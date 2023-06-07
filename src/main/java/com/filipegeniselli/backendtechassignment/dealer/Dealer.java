package com.filipegeniselli.backendtechassignment.dealer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table
public class Dealer {

    @Id
    private UUID id;

    private String name;

    private DealerTierLimit tier;

    private Boolean allowRemovingOldListings;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DealerTierLimit getTier() {
        return tier;
    }

    public void setTier(DealerTierLimit tier) {
        this.tier = tier;
    }

    public Boolean getAllowRemovingOldListings() {
        return allowRemovingOldListings;
    }

    public void setAllowRemovingOldListings(Boolean allowRemovingOldListings) {
        this.allowRemovingOldListings = allowRemovingOldListings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dealer dealer = (Dealer) o;
        return Objects.equals(id, dealer.id) && Objects.equals(name, dealer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static final class DealerBuilder {
        private UUID id;
        private String name;
        private DealerTierLimit tier;
        private Boolean allowRemovingOldListings;

        private DealerBuilder() {
        }

        public static DealerBuilder aDealer() {
            return new DealerBuilder();
        }

        public DealerBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public DealerBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DealerBuilder tier(DealerTierLimit tier) {
            this.tier = tier;
            return this;
        }

        public DealerBuilder allowRemovingOldListings(Boolean allowRemovingOldListings) {
            this.allowRemovingOldListings = allowRemovingOldListings;
            return this;
        }

        public Dealer build() {
            Dealer dealer = new Dealer();
            dealer.setId(id);
            dealer.setName(name);
            dealer.setTier(tier);
            dealer.setAllowRemovingOldListings(allowRemovingOldListings);
            return dealer;
        }
    }
}
