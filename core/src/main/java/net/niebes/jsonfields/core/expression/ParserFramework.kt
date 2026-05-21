package net.niebes.jsonfields.core.expression

import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.misc.ParseCancellationException
import net.niebes.jsonfields.core.model.FieldPredicate
import net.niebes.jsonfields.core.model.FieldPredicates.alwaysFalse
import net.niebes.jsonfields.core.parser.JsonFieldsLexer
import net.niebes.jsonfields.core.parser.JsonFieldsParser
import java.util.concurrent.ConcurrentHashMap

object ParserFramework {
    private const val MAX_CACHE_SIZE = 1000
    private const val MAX_CACHEABLE_LENGTH = 256
    private val cache = ConcurrentHashMap<String, FieldPredicate>()

    @JvmStatic
    fun parseFieldsExpression(fieldsExpression: String): FieldPredicate {
        if (fieldsExpression.length <= MAX_CACHEABLE_LENGTH) {
            cache[fieldsExpression]?.let { return it }
        }
        val result = try {
            doParse(fieldsExpression)
        } catch (_: ParseCancellationException) {
            return alwaysFalse()
        }
        if (fieldsExpression.length <= MAX_CACHEABLE_LENGTH) {
            evictIfFull()
            cache.putIfAbsent(fieldsExpression, result)
        }
        return result
    }

    fun parseFieldsExpressionOrFail(fieldsExpression: String): FieldPredicate {
        if (fieldsExpression.length <= MAX_CACHEABLE_LENGTH) {
            cache[fieldsExpression]?.let { return it }
        }
        val result = try {
            doParse(fieldsExpression)
        } catch (e: ParseCancellationException) {
            throw IllegalArgumentException("Invalid fields expression", e)
        }
        if (fieldsExpression.length <= MAX_CACHEABLE_LENGTH) {
            evictIfFull()
            cache.putIfAbsent(fieldsExpression, result)
        }
        return result
    }

    private fun evictIfFull() {
        if (cache.size >= MAX_CACHE_SIZE) {
            cache.clear()
        }
    }

    private fun doParse(fieldsExpression: String): FieldPredicate {
        val lexer = JsonFieldsLexer(CharStreams.fromString(fieldsExpression))
        lexer.removeErrorListeners()
        lexer.addErrorListener(STRICT_LISTENER)

        val parser = JsonFieldsParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(STRICT_LISTENER)
        parser.errorHandler = BailErrorStrategy()
        return FieldPredicateVisitor().visitJson_fields(parser.json_fields())
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
