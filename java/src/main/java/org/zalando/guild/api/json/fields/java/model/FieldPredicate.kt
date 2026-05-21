package org.zalando.guild.api.json.fields.java.model

import java.util.function.Predicate

/**
 * The core abstraction for matching a hierarchy of fields. Based on this, a hierarchy of fields can be tested against a
 * fields expression.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   20.08.2015
 */
interface FieldPredicate : Predicate<List<String>> {
    /**
     *
     * Return true if the supplied field hierarchy should be rendered. A list of strings represents a hierarchy of
     * objects, where each element is the parent element of the next. E.g. a list `["foo", "bar", "baz"]` would
     * map to a JSON object hierarchy like this: `{"foo":{"bar":{"baz":"some value"}}}`. I.e. a FieldPredicate
     * matching this list would cause the "baz" field to be rendered.
     *
     *
     * Implementations of this interface will not accept null values. They may throw [IllegalArgumentException]
     * when confronted with empty lists.
     *
     * @exception  NullPointerException      if the supplied list is `null` or contains `null` values
     * @exception  IllegalArgumentException  if the supplied list is empty
     */
    override fun test(fieldHierarchy: List<String>): Boolean
}
