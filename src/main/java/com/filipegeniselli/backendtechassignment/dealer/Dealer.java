package com.filipegeniselli.backendtechassignment.dealer;

import com.filipegeniselli.backendtechassignment.exception.BadRequestException;
import com.filipegeniselli.backendtechassignment.listings.Listing;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The main points for both Entity classes is the lack of Lombok and the UUID as the ID
 *
 * Regarding Lombok, I'm not a fan of the feature, it sure reduces boilerplate code,
 * but with all the variety of IDEs and plugins we have nowadays the boilerplate code is reduced already.
 * Lombok removes one thing that helps me a lot during a debugging session, which is the ability to track usage
 * I understand that it also adds lots of useful features, but I'm okay with not using them and having full control of my code
 *
 * For the UUID, I think they are too big and not really scalable for an ID, it can really mess with the database indexing
 * We can implement some proper configuration that were not added to this code for the sake of simplicity. But the recommended configuration for Postgres is:
 * Use the built-in data type 'uuid' and create a regular b-tree index
 *
 * My suggestion here is to use something really scalable and that can be ordered like an int data-type.
 * Something close to Twitter's snowflake, a 64-bit long id with reserved bits:
 * - 41 bits for timestamp
 * - 10 bits for Node Id/Machine ID (1024 nodes/machines)
 * - 12 bits counter (maximum of 4096)
 * - 1 bit reserved
 *
 * This generates an ID with this format: 711753747487830016
 * It is always unique, and there's some safeguards
 * - If a node reaches the maximum of 4096 ids in 1 millisecond, the code just waits 1 ms to reset the counter and generate a new Id
 *
 * On my opinion this would be a better approach for the id where we could have each API implementing the code to generate the ID
 * Or a central API responsible to generate IDs
 *
 * Suggestions to the Dealer feature:
 *
 * - Add more fields to create a new validation, blocking the registration of duplicated dealers
 *      Adding this with only the name would create a false-positive
 *
 * - Add a rating system, every person that buys from the dealer should be able to post a review and assign a rating to the dealer
 *      All dealers would have a rating from 1 to 5 and would be displayed to the listing
 *
 */
@Entity
@Table
public class Dealer {

    @Id
    private UUID id;

    private String name;

    private DealerTierLimit tier;

    private Boolean allowRemovingOldListings;

    @OneToMany(mappedBy = "dealer")
    private Set<Listing> listings;

    public void checkIsValid() {
        List<String> errorMessages = new ArrayList<>();

        if (this.name == null || this.name.equals("")) {
            errorMessages.add("The field name is required");
        }

        if (this.tier == null) {
            errorMessages.add("The field tier is required");
        }

        if (this.allowRemovingOldListings == null) {
            errorMessages.add("The field allowRemovingOldListings is required");
        }

        if (!errorMessages.isEmpty()) {
            throw new BadRequestException(errorMessages.stream().collect(Collectors.joining(System.lineSeparator())));
        }
    }

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

    public Set<Listing> getListings() {
        return listings;
    }

    public void setListings(Set<Listing> listings) {
        this.listings = listings;
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
        private Set<Listing> listings;

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

        public DealerBuilder listings(Set<Listing> listings) {
            this.listings = listings;
            return this;
        }

        public Dealer build() {
            Dealer dealer = new Dealer();
            dealer.setId(id);
            dealer.setName(name);
            dealer.setTier(tier);
            dealer.setAllowRemovingOldListings(allowRemovingOldListings);
            dealer.setListings(listings);
            return dealer;
        }
    }
}
