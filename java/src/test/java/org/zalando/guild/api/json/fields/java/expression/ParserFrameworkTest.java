package org.zalando.guild.api.json.fields.java.expression;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.hamcrest.core.IsNot.not;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import org.junit.jupiter.api.Test;

import org.zalando.guild.api.json.fields.java.model.FieldPredicate;

/**
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   07.09.2015
 */
public class ParserFrameworkTest {

    @Test
    public void field() {
        assertThat("(foo)", matchesFields("foo"));
        assertThat("(foo)", matchesFields("foo", "bar"));
        assertThat("(foo)", not(matchesFields("bar")));
        assertThat("(foo-bar)", matchesFields("foo-bar"));
        assertThat("(foo_bar)", matchesFields("foo_bar"));
    }

    @Test
    public void fieldGroup() {
        assertThat("(foo,baz)", matchesFields("foo"));
        assertThat("(foo, baz)", matchesFields("foo"));
        assertThat("(  foo    , baz)", matchesFields("foo"));
        assertThat("(foo,baz)", matchesFields("foo", "bar"));
        assertThat("(foo,baz)", matchesFields("baz"));
        assertThat("(foo,baz)", matchesFields("baz", "phleem"));
    }

    @Test
    public void negation() {
        assertThat("!(foo)", matchesFields("bar"));
        assertThat("!(foo)", not(matchesFields("foo")));
        assertThat("!(foo, bar)", not(matchesFields("foo")));
        assertThat("!(foo, bar)", not(matchesFields("bar")));
        assertThat("!(foo, bar)", matchesFields("baz"));
    }

    @Test
    public void qualifier() {
        assertThat("(foo(bar))", matchesFields("foo"));
        assertThat("(foo(bar))", matchesFields("foo", "bar"));
        assertThat("(foo(bar))", not(matchesFields("foo", "baz")));
    }

    @Test
    public void complexExpressions() {
        assertThat("(foo(bar(baz)))", matchesFields("foo"));
        assertThat("(foo(bar(baz)))", matchesFields("foo", "bar"));
        assertThat("(foo(bar(baz)))", matchesFields("foo", "bar", "baz"));
        assertThat("(foo!(bar(baz)))", matchesFields("foo"));
        assertThat("(foo!(bar(baz)))", matchesFields("foo", "phleem", "baz"));
        assertThat("(foo!(bar(baz)))", matchesFields("foo", "phleem", "phooey"));
        assertThat("(foo!(bar(baz)))", not(matchesFields("foo", "bar")));
        assertThat("(foo!(bar(baz)))", not(matchesFields("foo", "bar", "baz")));
        assertThat("(foo(bar(baz)))", not(matchesFields("foo", "bar", "phleem")));
        assertThat("(foo(bar(baz)))", not(matchesFields("foo", "phleem")));
    }

    @Test
    public void nestedBlacklist() {
        assertThat("(profile!(age))", matchesFields("profile"));
        assertThat("(profile!(age))", not(matchesFields("profile", "age")));
        assertThat("(profile!(age))", matchesFields("profile", "bio"));
        assertThat("(profile!(age))", matchesFields("profile", "bio", "anything"));
        assertThat("(profile!(age))", not(matchesFields("other")));
    }

    static Matcher<String> matchesFields(final String firstField, final String... moreFields) {

        final List<String> fields;
        if (moreFields.length > 0) {
            fields = new ArrayList<>(moreFields.length + 1);
            fields.add(firstField);
            fields.addAll(asList(moreFields));
        } else {
            fields = singletonList(firstField);
        }

        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(final String item) {
                final FieldPredicate fieldPredicate = ParserFramework.parseFieldsExpression(item);
                return fieldPredicate.test(fields);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("An expression matching ").appendValue(fields);
            }
        };
    }

}
