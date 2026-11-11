package rush.domain;

/** Applies restaurant pricing after the customer's own discount. */
public interface PricingPolicy {
    PricingPolicy STANDARD = new PricingPolicy() {
        @Override public long apply(long customerDiscountedCents) {
            return customerDiscountedCents;
        }
    };

    long apply(long customerDiscountedCents);
}
