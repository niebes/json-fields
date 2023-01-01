package org.zalando.guild.api.json.fields.java.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Factory methods for constructing {@link FieldPredicate}s.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   20.08.2015
 */
public final class FieldPredicates {

    private static final FieldPredicate ALWAYS_FALSE = new AlwaysFalsePredicate();

    private static final FieldPredicate ALWAYS_TRUE = new AlwaysTruePredicate();

    private static final FieldPredicate[] EMPTY_PREDICATES_ARRAY = new FieldPredicate[0];

    /**
     * Return a {@link FieldPredicate} that matches everything.
     */
    @Nonnull
    public static FieldPredicate alwaysTrue() {
        return ALWAYS_TRUE;
    }

    /**
     * Return a {@link FieldPredicate} that matches nothing.
     */
    @Nonnull
    public static FieldPredicate alwaysFalse() {
        return ALWAYS_FALSE;
    }

    /**
     * Return a {@link FieldPredicate} that returns true if all of the supplied {@link FieldPredicate}s return true.
     */
    @Nonnull
    public static FieldPredicate and(@Nonnull final FieldPredicate first, @Nonnull final FieldPredicate... more) {
        checkNotNull(first, "First Predicate required");
        checkNotNull(more, "More Predicates required");
        return new AndPredicate(first, more);
    }

    /**
     * Return a {@link FieldPredicate} that returns true if at least one of the supplied {@link FieldPredicate}s return
     * true.
     */
    public static FieldPredicate or(@Nonnull final FieldPredicate first, @Nonnull final FieldPredicate... more) {
        checkNotNull(first, "First Predicate required");
        checkNotNull(more, "More Predicates required");
        return new OrPredicate(first, more);
    }

    /**
     * Return a {@link FieldPredicate} that inverts the matching of the supplied {@link FieldPredicate}.
     */
    public static FieldPredicate not(@Nonnull final FieldPredicate negatee) {
        checkNotNull(negatee, "Negatee required");
        return new NotPredicate(negatee);
    }

    /**
     * Return a {@link FieldPredicate} that returns true if the field at the supplied offset {@link FieldPredicate}
     * equals the supplied token (or if the list doesn't contain that many items).
     */
    public static FieldPredicate matchIndex(final int index, @Nonnull final String token) {
        checkNotNull(token, "Token required");
        return new MatchIndexPredicate(index, token);

    }

    private FieldPredicates() { }

    private static FieldPredicate[] defensiveCopyOfPredicateArray(final FieldPredicate[] fieldPredicates) {

        return fieldPredicates.length == 0 ? EMPTY_PREDICATES_ARRAY
                                           : Arrays.copyOf(fieldPredicates, fieldPredicates.length);
    }

    private static class AndPredicate implements FieldPredicate {
        private final FieldPredicate first;
        private final FieldPredicate[] more;

        public AndPredicate(final FieldPredicate first, final FieldPredicate... more) {
            this.first = first;
            this.more = defensiveCopyOfPredicateArray(more);
        }

        @Override
        public boolean test(@Nonnull final List<String> tokens) {
            if (!first.test(tokens)) {
                return false;
            }

            for (final FieldPredicate predicate : more) {
                if (!predicate.test(tokens)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("( ").append(first);
            for (final FieldPredicate predicate : more) {
                sb.append(" AND ").append(predicate);
            }

            return sb.append(" )").toString();
        }
    }

    private static class NotPredicate implements FieldPredicate {
        private final FieldPredicate negatee;

        public NotPredicate(final FieldPredicate negatee) {
            this.negatee = negatee;
        }

        @Override
        public boolean test(@Nonnull final List<String> tokens) {
            return !negatee.test(tokens);
        }

        @Override
        public String toString() {
            return String.format("NOT ( %s )", negatee);
        }
    }

    private static class OrPredicate implements FieldPredicate {
        private final FieldPredicate first;
        private final FieldPredicate[] more;

        public OrPredicate(final FieldPredicate first, final FieldPredicate... more) {
            this.first = first;
            this.more = defensiveCopyOfPredicateArray(more);
        }

        @Override
        public boolean test(@Nonnull final List<String> tokens) {
            if (first.test(tokens)) {
                return true;
            }

            for (final FieldPredicate predicate : more) {
                if (predicate.test(tokens)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("( ").append(first);
            for (final FieldPredicate predicate : more) {
                sb.append(" OR ").append(predicate);
            }

            return sb.append(" )").toString();
        }
    }

    private static class AlwaysFalsePredicate implements FieldPredicate {
        @Override
        public boolean test(@Nonnull final List<String> tokens) {
            return false;
        }

        @Override
        public String toString() {
            return "false";
        }
    }

    private static class AlwaysTruePredicate implements FieldPredicate {
        @Override
        public boolean test(@Nonnull final List<String> tokens) {
            return true;
        }

        @Override
        public String toString() {
            return "true";
        }
    }

    private static class MatchIndexPredicate implements FieldPredicate {
        private final int index;
        private final String token;

        public MatchIndexPredicate(final int index, final String token) {
            this.index = index;
            this.token = token;
        }

        @Override
        public boolean test(@Nonnull final List<String> tokens) {
            return tokens.size() <= index || tokens.get(index).equals(token);
        }

        @Override
        public String toString() {
            return String.format("match '%s' at index %d", token, index);
        }
    }
}
