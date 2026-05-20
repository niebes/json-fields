package org.zalando.guild.api.json.fields.java.model

/**
 * Factory methods for constructing [FieldPredicate]s.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   20.08.2015
 */
object FieldPredicates {
    private val ALWAYS_FALSE: FieldPredicate = AlwaysFalsePredicate()

    private val ALWAYS_TRUE: FieldPredicate = AlwaysTruePredicate()


    /**
     * Return a [FieldPredicate] that matches everything.
     */
    @JvmStatic
    fun alwaysTrue(): FieldPredicate {
        return ALWAYS_TRUE
    }

    /**
     * Return a [FieldPredicate] that matches nothing.
     */
    @JvmStatic
    fun alwaysFalse(): FieldPredicate {
        return ALWAYS_FALSE
    }

    /**
     * Return a [FieldPredicate] that returns true if all of the supplied [FieldPredicate]s return true.
     */
    @JvmStatic
    fun and(
        first: FieldPredicate,
        vararg more: FieldPredicate
    ): FieldPredicate = AndPredicate(first, *more)

    /**
     * Return a [FieldPredicate] that returns true if at least one of the supplied [FieldPredicate]s return
     * true.
     */
    @JvmStatic
    fun or(
        first: FieldPredicate,
        vararg more: FieldPredicate
    ): FieldPredicate = OrPredicate(first, *more)

    /**
     * Return a [FieldPredicate] that inverts the matching of the supplied [FieldPredicate].
     */
    @JvmStatic
    fun not(
        negatee: FieldPredicate
    ): FieldPredicate = NotPredicate(negatee)

    /**
     * Return a [FieldPredicate] that returns true if the field at the supplied offset [FieldPredicate]
     * equals the supplied token (or if the list doesn't contain that many items).
     */
    @JvmStatic
    fun matchIndex(
        index: Int,
        token: String
    ): FieldPredicate = MatchIndexPredicate(index, token)


    private class AndPredicate(
        private val first: FieldPredicate,
        private vararg val more: FieldPredicate
    ) : FieldPredicate {


        override fun test(fieldHierarchy: List<String>): Boolean {
            if (!first.test(fieldHierarchy)) {
                return false
            }

            for (predicate in more) {
                if (!predicate.test(fieldHierarchy)) {
                    return false
                }
            }

            return true
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("( ").append(first)
            for (predicate in more) {
                sb.append(" AND ").append(predicate)
            }

            return sb.append(" )").toString()
        }
    }

    private class NotPredicate(private val negatee: FieldPredicate) : FieldPredicate {
        override fun test(fieldHierarchy: List<String>): Boolean {
            return !negatee.test(fieldHierarchy)
        }

        override fun toString(): String {
            return String.format("NOT ( %s )", negatee)
        }
    }

    private class OrPredicate(
        private val first: FieldPredicate,
        private vararg val more: FieldPredicate
    ) : FieldPredicate {


        override fun test(fieldHierarchy: List<String>): Boolean {
            if (first.test(fieldHierarchy)) {
                return true
            }

            for (predicate in more) {
                if (predicate.test(fieldHierarchy)) {
                    return true
                }
            }

            return false
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("( ").append(first)
            for (predicate in more) {
                sb.append(" OR ").append(predicate)
            }

            return sb.append(" )").toString()
        }
    }

    private class AlwaysFalsePredicate : FieldPredicate {
        override fun test(fieldHierarchy: List<String>): Boolean {
            return false
        }

        override fun toString(): String {
            return "false"
        }
    }

    private class AlwaysTruePredicate : FieldPredicate {
        override fun test(fieldHierarchy: List<String>): Boolean {
            return true
        }

        override fun toString(): String {
            return "true"
        }
    }

    private class MatchIndexPredicate(private val index: Int, private val token: String) : FieldPredicate {
        override fun test(fieldHierarchy: List<String>): Boolean {
            return fieldHierarchy.size <= index || fieldHierarchy[index] == token
        }

        override fun toString(): String {
            return String.format("match '%s' at index %d", token, index)
        }
    }
}
