package org.zalando.guild.api.json.fields.java.model;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.alwaysFalse;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.alwaysTrue;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.and;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.depthLessThan;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.matchIndex;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.not;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.or;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import org.hamcrest.core.IsNot;

import org.junit.jupiter.api.Test;

/**
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   02.09.2015
 */
public class FieldPredicatesTest {
    private static final FieldPredicate True = alwaysTrue();
    private static final FieldPredicate False = alwaysFalse();
    private static final String ANY_FIELD_NAME = "placeholder to use in cases where the field name is irrelevant";

    static Matcher<FieldPredicate> matchesTokens(final String firstToken, final String... tokenArray) {
        final List<String> tokens = new ArrayList<>(tokenArray.length + 1);
        tokens.add(firstToken);
        tokens.addAll(asList(tokenArray));
        return new TypeSafeMatcher<FieldPredicate>() {
            @Override
            protected boolean matchesSafely(final FieldPredicate item) {
                return item.test(tokens);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("A FieldPredicate matching ").appendValue(tokens);
            }
        };
    }

    static Matcher<FieldPredicate> doesntMatchTokens(final String first, final String... tokenArray) {
        return IsNot.not(matchesTokens(first, tokenArray));
    }

    @Test
    public void indexBasedMatch() throws Exception {

        assertThat(matchIndex(0, "foo"), matchesTokens("foo"));
        assertThat(matchIndex(0, "foo"), matchesTokens("foo", "bar"));
        assertThat(matchIndex(0, "foo"), matchesTokens("foo", "bar", "baz"));
        assertThat(matchIndex(0, "foo"), doesntMatchTokens("bar"));

        assertThat(matchIndex(1, "bar"), matchesTokens("foo"));
        assertThat(matchIndex(1, "bar"), matchesTokens("foo", "bar"));
        assertThat(matchIndex(1, "bar"), matchesTokens("foo", "bar", "baz"));
        assertThat(matchIndex(1, "bar"), doesntMatchTokens("foo", "baz"));

    }

    @Test
    public void conjunction() throws Exception {

        assertThat(and(True), matchesTokens(ANY_FIELD_NAME));
        assertThat(and(False), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(and(True, True), matchesTokens(ANY_FIELD_NAME));
        assertThat(and(True, False), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(and(False, False), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(and(True, True, True), matchesTokens(ANY_FIELD_NAME));
        assertThat(and(True, True, False), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(and(True, False, True), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(and(True, False, False), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(and(False, True, True), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(and(False, True, False), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(and(False, False, True), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(and(False, False, False), doesntMatchTokens(ANY_FIELD_NAME));
    }

    @Test
    public void negation() throws Exception {

        assertThat(not(True), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(not(False), matchesTokens(ANY_FIELD_NAME));

    }

    @Test
    public void depthGuard() throws Exception {
        assertThat(depthLessThan(1), doesntMatchTokens("foo"));
        assertThat(depthLessThan(2), matchesTokens("foo"));
        assertThat(depthLessThan(2), doesntMatchTokens("foo", "bar"));
        assertThat(depthLessThan(3), matchesTokens("foo", "bar"));
        assertThat(depthLessThan(3), doesntMatchTokens("foo", "bar", "baz"));
    }

    @Test
    public void disjunction() throws Exception {
        assertThat(or(True), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(False), doesntMatchTokens(ANY_FIELD_NAME));
        assertThat(or(True, True), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(True, False), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(False, False), doesntMatchTokens(ANY_FIELD_NAME));

        assertThat(or(True, True, True), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(True, True, False), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(True, False, True), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(True, False, False), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(False, True, True), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(False, True, False), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(False, False, True), matchesTokens(ANY_FIELD_NAME));
        assertThat(or(False, False, False), doesntMatchTokens(ANY_FIELD_NAME));
    }

}
