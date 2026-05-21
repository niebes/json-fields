package net.niebes.jsonfields.core.expression

import net.niebes.jsonfields.core.model.FieldPredicate
import net.niebes.jsonfields.core.model.FieldPredicates.and
import net.niebes.jsonfields.core.model.FieldPredicates.depthLessThan
import net.niebes.jsonfields.core.model.FieldPredicates.matchIndex
import net.niebes.jsonfields.core.model.FieldPredicates.not
import net.niebes.jsonfields.core.model.FieldPredicates.or
import net.niebes.jsonfields.core.parser.JsonFieldsBaseVisitor
import net.niebes.jsonfields.core.parser.JsonFieldsParser
import java.util.concurrent.atomic.AtomicInteger

/**
 * A Visitor for parsing a JsonFields expression and turning it into a [FieldPredicate].
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   03.09.2015
 */
internal class FieldPredicateVisitor : JsonFieldsBaseVisitor<FieldPredicate?>() {
    private val depth = AtomicInteger(0)

    /**
     * This is the root entry point. It's just a pass-through to
     * [(Fields_expressionContext)][.visitFields_expression].
     */
    override fun visitJson_fields(ctx: JsonFieldsParser.Json_fieldsContext): FieldPredicate {
        return visitFields_expression(ctx.fields_expression())
    }

    override fun visitFields_expression(ctx: JsonFieldsParser.Fields_expressionContext): FieldPredicate {
        val predicate = visitField_set(ctx.field_set())
        return when {
            ctx.negation() == null -> predicate
            else -> not(predicate)
        }
    }

    override fun visitField(ctx: JsonFieldsParser.FieldContext): FieldPredicate {
        return matchIndex(depth.get(), ctx.text)
    }

    override fun visitQualified_field(ctx: JsonFieldsParser.Qualified_fieldContext): FieldPredicate {
        val fieldPredicate = visitField(ctx.field())

        val fieldsExpressionContext = ctx.fields_expression()

        if (fieldsExpressionContext == null) {
            return fieldPredicate
        } else {
            depth.incrementAndGet()
            val subExpression = visitFields_expression(fieldsExpressionContext)
            val guardedSubExpression = or(depthLessThan(depth.get() + 1), subExpression)
            val result = and(fieldPredicate, guardedSubExpression)
            depth.decrementAndGet()
            return result
        }
    }

    override fun visitField_set(ctx: JsonFieldsParser.Field_setContext): FieldPredicate {
        val fieldContexts = ctx.qualified_field()
        val first = visitQualified_field(fieldContexts.first())

        if (fieldContexts.size == 1) {
            return first
        }

        val more = fieldContexts.drop(1).map {
            visitQualified_field(it)
        }

        return or(first, *more.toTypedArray())
    }
}
