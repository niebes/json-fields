package org.zalando.guild.api.json.fields.java.expression;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import org.zalando.guild.api.json.fields.java.model.FieldPredicate;
import org.zalando.guild.api.json.fields.java.model.FieldPredicates;
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsLexer;
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsParser;

/**
 * Entry point for parsing Json field expressions into {@link FieldPredicate}s.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   07.09.2015
 */
public final class ParserFramework {

    /**
     * Returns a FieldPredicate consistent with the semantics of the supplied Json Fields expression. If the expression
     * is invalid, the returned predicate will not match anything.
     *
     * @exception  NullPointerException  if null is passed in
     */
    public static FieldPredicate parseFieldsExpression(@Nonnull final String fieldsExpression) {
        return parseFieldsExpression(fieldsExpression, false);
    }

    /**
     * Returns a FieldPredicate consistent with the semantics of the supplied Json Fields expression. If the expression
     * is invalid, an {@link IllegalArgumentException} will be thrown.
     *
     * @exception  NullPointerException      if null is passed in
     * @exception  IllegalArgumentException  if the expression has invalid syntax
     */
    public static FieldPredicate parseFieldsExpressionOrFail(@Nonnull final String fieldsExpression) {
        return parseFieldsExpression(fieldsExpression, true);
    }

    private static FieldPredicate parseFieldsExpression(@Nonnull final String fieldsExpression,
            final boolean throwIfInvalid) {
        checkNotNull(fieldsExpression, "FieldsExpression required");
        try {

            final JsonFieldsLexer lexer = new JsonFieldsLexer(CharStreams.fromString(fieldsExpression));
            lexer.removeErrorListeners();

            lexer.addErrorListener(STRICT_LISTENER);

            final JsonFieldsParser parser = new JsonFieldsParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();

            parser.addErrorListener(STRICT_LISTENER);
            parser.setErrorHandler(new BailErrorStrategy());
            return new FieldPredicateVisitor().visitJson_fields(parser.json_fields());
        } catch (ParseCancellationException e) {
            if (throwIfInvalid) {
                throw new IllegalArgumentException(e);
            } else {
                return FieldPredicates.alwaysFalse();
            }
        }
    }

    private static final BaseErrorListener STRICT_LISTENER = new BaseErrorListener() {
        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
                final int charPositionInLine, final String msg, final RecognitionException e) {
            throw new ParseCancellationException(e);
        }

    };

    private ParserFramework() { }
}
