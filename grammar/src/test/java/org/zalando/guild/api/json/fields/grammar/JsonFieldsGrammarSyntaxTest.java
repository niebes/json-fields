package org.zalando.guild.api.json.fields.grammar;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.Test;
import org.zalando.guild.api.json.fields.grammar.parser.JsonFieldsLexer;
import org.zalando.guild.api.json.fields.grammar.parser.JsonFieldsParser;

import java.util.BitSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

/**
 * This test validates legality of expressions acccording to the grammar, without actually evaluating them.
 *
 * @author Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since 26.08.2015
 */
public class JsonFieldsGrammarSyntaxTest {

    @Test
    public void simpleField() {
        assertThat("(foo)", is(aValidFieldsExpression()));
        assertThat("   (    foo    )   ", is(aValidFieldsExpression()));
        assertThat("(foo    )   ", is(aValidFieldsExpression()));
        assertThat("(  foo)", is(aValidFieldsExpression()));
        assertThat("foo", is(not(aValidFieldsExpression())));
    }

    @Test
    public void numerics() {
        assertThat("(foo123)", is(aValidFieldsExpression()));
        assertThat("   (    foo,bar123    )   ", is(aValidFieldsExpression()));
        assertThat("(f0o,b4r)", is(aValidFieldsExpression()));
    }

    @Test
    public void uppercase() {
        assertThat("(FOO)", is(aValidFieldsExpression()));
        assertThat("   (    fOo, bar, BAZ    )   ", is(aValidFieldsExpression()));
    }

    @Test
    public void dashesAndUnderscores() {
        assertThat("(foo-bar)", is(aValidFieldsExpression()));
        assertThat("(foo_bar)   ", is(aValidFieldsExpression()));
        assertThat("(foo_)   ", is(aValidFieldsExpression()));
        assertThat("(foo__bar)   ", is(aValidFieldsExpression()));
        assertThat("(_foo)   ", is(aValidFieldsExpression()));
        assertThat("(foo_-bar)   ", is(aValidFieldsExpression()));
    }

    @Test
    public void negation() {
        assertThat("!(foo)", is(aValidFieldsExpression()));
        assertThat("!foo", is(not(aValidFieldsExpression())));
        assertThat("!(foo, bar)", (is(aValidFieldsExpression())));
    }

    @Test
    public void qualifier() {
        assertThat("(foo(bar))", is(aValidFieldsExpression()));
        assertThat("(foo!(bar))", is(aValidFieldsExpression()));
        assertThat("foo(bar)", is(not(aValidFieldsExpression())));
        assertThat("(foo(bar(baz)))", is(aValidFieldsExpression()));
        assertThat("(foo!(bar(baz)))", is(aValidFieldsExpression()));
        assertThat("!(foo(bar(baz)))", is(aValidFieldsExpression()));
        assertThat("!(foo!(bar!(baz)))", is(aValidFieldsExpression()));
    }

    @Test
    public void syntaxError() {

        assertThat("", is(not(aValidFieldsExpression())));
        assertThat("foo, bar", is(not(aValidFieldsExpression())));
        assertThat("<<<foo", is(not(aValidFieldsExpression())));
    }

    static Matcher<String> aValidFieldsExpression() {
        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(final String expression, final Description mismatchDescription) {
                try {

                    final JsonFieldsLexer lexer = lexer(expression);
                    final JsonFieldsParser parser = parser(lexer);

                    parser.compileParseTreePattern(expression, 0, lexer);
                } catch (ParseCancellationException | RecognitionException | IllegalArgumentException e) {
                    mismatchDescription.appendValue(e);
                    return false;
                }

                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("a valid field expression");
            }
        };
    }

    private static JsonFieldsLexer lexer(final String fieldsExpression) {
        return new JsonFieldsLexer(CharStreams.fromString(fieldsExpression));
    }

    private static JsonFieldsParser parser(final JsonFieldsLexer lexer) {
        lexer.removeErrorListeners();

        final ANTLRErrorListener listener = antlrErrorListener();
        lexer.addErrorListener(listener);

        final JsonFieldsParser parser = new JsonFieldsParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();

        parser.addErrorListener(listener);
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private static ANTLRErrorListener antlrErrorListener() {
        return new ANTLRErrorListener() {
            @Override
            public void syntaxError(
                    final Recognizer<?, ?> recognizer,
                    final Object offendingSymbol,
                    final int line,
                    final int charPositionInLine,
                    final String msg,
                    final RecognitionException e
            ) {
                throw new ParseCancellationException(e);
            }

            @Override
            public void reportAmbiguity(
                    final Parser recognizer,
                    final DFA dfa,
                    final int startIndex,
                    final int stopIndex,
                    final boolean exact,
                    final BitSet ambigAlts,
                    final ATNConfigSet configs
            ) {
            }

            @Override
            public void reportAttemptingFullContext(
                    final Parser recognizer,
                    final DFA dfa,
                    final int startIndex,
                    final int stopIndex,
                    final BitSet conflictingAlts,
                    final ATNConfigSet configs
            ) {
            }

            @Override
            public void reportContextSensitivity(
                    final Parser recognizer,
                    final DFA dfa,
                    final int startIndex,
                    final int stopIndex,
                    final int prediction,
                    final ATNConfigSet configs
            ) {
            }
        };
    }
}
