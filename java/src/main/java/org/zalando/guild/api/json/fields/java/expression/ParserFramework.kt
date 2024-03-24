package org.zalando.guild.api.json.fields.java.expression

import com.google.common.base.Preconditions
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates.alwaysFalse
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsLexer
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsParser
import javax.annotation.Nonnull

/**
 * Entry point for parsing Json field expressions into [FieldPredicate]s.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   07.09.2015
 */
object ParserFramework {
    /**
     * Returns a FieldPredicate consistent with the semantics of the supplied Json Fields expression. If the expression
     * is invalid, the returned predicate will not match anything.
     *
     * @exception  NullPointerException  if null is passed in
     */
    @JvmStatic
    fun parseFieldsExpression(fieldsExpression: String): FieldPredicate {
        return parseFieldsExpression(fieldsExpression, false)
    }

    /**
     * Returns a FieldPredicate consistent with the semantics of the supplied Json Fields expression. If the expression
     * is invalid, an [IllegalArgumentException] will be thrown.
     *
     * @exception  NullPointerException      if null is passed in
     * @exception  IllegalArgumentException  if the expression has invalid syntax
     */
    fun parseFieldsExpressionOrFail(fieldsExpression: String): FieldPredicate {
        return parseFieldsExpression(fieldsExpression, true)
    }

    private fun parseFieldsExpression(
        fieldsExpression: String,
        throwIfInvalid: Boolean
    ): FieldPredicate {
        Preconditions.checkNotNull(fieldsExpression, "FieldsExpression required")
        try {
            val lexer = JsonFieldsLexer(CharStreams.fromString(fieldsExpression))
            lexer.removeErrorListeners()

            lexer.addErrorListener(STRICT_LISTENER)

            val parser = JsonFieldsParser(CommonTokenStream(lexer))
            parser.removeErrorListeners()

            parser.addErrorListener(STRICT_LISTENER)
            parser.errorHandler = BailErrorStrategy()
            return FieldPredicateVisitor().visitJson_fields(parser.json_fields())
        } catch (e: ParseCancellationException) {
            if (throwIfInvalid) {
                throw IllegalArgumentException(e)
            } else {
                return alwaysFalse()
            }
        }
    }

    private val STRICT_LISTENER: BaseErrorListener = object : BaseErrorListener() {
        override fun syntaxError(
            recognizer: Recognizer<*, *>?, offendingSymbol: Any, line: Int,
            charPositionInLine: Int, msg: String, e: RecognitionException
        ) {
            throw ParseCancellationException(e)
        }
    }
}
