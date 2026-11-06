package rush.domain;

/** A recoverable manager decision error, displayed by the boundary. */
public final class RuleViolation extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public RuleViolation(String message) { super(message); }

    public static void require(boolean condition, String message) {
        if (!condition) { throw new RuleViolation(message); }
    }
}
