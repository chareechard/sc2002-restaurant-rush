package rush.domain;

/** Integer-cent arithmetic; positive half cents round up. */
public final class Money {
    private Money() { }

    public static long percentage(long cents, int percent) {
        RuleViolation.require(cents >= 0 && percent >= 0 && percent <= 100, "Invalid money percentage.");
        if (percent != 0 && cents > Long.MAX_VALUE / percent) {
            throw new ArithmeticException("Money multiplication overflow.");
        }
        long product = cents * percent;
        if (product > Long.MAX_VALUE - 50) {
            throw new ArithmeticException("Money rounding overflow.");
        }
        return (product + 50) / 100;
    }

    public static String format(long cents) {
        long remainder = cents % 100;
        return "$" + (cents / 100) + "." + (remainder < 10 ? "0" : "") + remainder;
    }
}
