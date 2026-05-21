package org.zalando.guild.api.json.fields.java.expression

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
import java.util.concurrent.ConcurrentHashMap

object ParserFramework {
    private const val MAX_CACHE_SIZE = 1000
    private val cache = ConcurrentHashMap<String, FieldPredicate>()

    @JvmStatic
    fun parseFieldsExpression(fieldsExpression: String): FieldPredicate {
        return cache.getOrPut(fieldsExpression) {
            doParse(fieldsExpression) ?: alwaysFalse()
        }
    }

    fun parseFieldsExpressionOrFail(fieldsExpression: String): FieldPredicate {
        cache[fieldsExpression]?.let { return it }
        val result = doParse(fieldsExpression)
            ?: throw IllegalArgumentException("Invalid fields expression: $fieldsExpression")
        cache.putIfAbsent(fieldsExpression, result)
        return result
    }

    private fun doParse(fieldsExpression: String): FieldPredicate? {
        if (cache.size >= MAX_CACHE_SIZE) {
            cache.clear()
        }
        return try {
            val lexer = JsonFieldsLexer(CharStreams.fromString(fieldsExpression))
            lexer.removeErrorListeners()
            lexer.addErrorListener(STRICT_LISTENER)

            val parser = JsonFieldsParser(CommonTokenStream(lexer))
            parser.removeErrorListeners()
            parser.addErrorListener(STRICT_LISTENER)
            parser.errorHandler = BailErrorStrategy()
            FieldPredicateVisitor().visitJson_fields(parser.json_fields())
        } catch (_: ParseCancellationException) {
            null
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
