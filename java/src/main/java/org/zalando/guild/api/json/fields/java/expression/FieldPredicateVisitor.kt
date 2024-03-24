package org.zalando.guild.api.json.fields.java.expression;

import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.and;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.matchIndex;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.not;
import static org.zalando.guild.api.json.fields.java.model.FieldPredicates.or;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.antlr.v4.runtime.misc.NotNull;

import org.zalando.guild.api.json.fields.java.model.FieldPredicate;
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsBaseVisitor;
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsParser.FieldContext;
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsParser.Field_setContext;
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsParser.Fields_expressionContext;
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsParser.Json_fieldsContext;
import org.zalando.guild.api.json.fields.java.parser.JsonFieldsParser.Qualified_fieldContext;

/**
 * A Visitor for parsing a JsonFields expression and turning it into a {@link FieldPredicate}.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   03.09.2015
 */
class FieldPredicateVisitor extends JsonFieldsBaseVisitor<FieldPredicate> {

    private final AtomicInteger depth = new AtomicInteger(0);

    /**
     * This is the root entry point. It's just a pass-through to
     * {@link #visitFields_expression (Fields_expressionContext)}.
     */
    @Override
    public FieldPredicate visitJson_fields(@NotNull final Json_fieldsContext ctx) {
        return visitFields_expression(ctx.fields_expression());
    }

    @Override
    public FieldPredicate visitFields_expression(@NotNull final Fields_expressionContext ctx) {
        final FieldPredicate predicate = visitField_set(ctx.field_set());
        return ctx.negation() == null ? predicate : not(predicate);
    }

    @Override
    public FieldPredicate visitField(@NotNull final FieldContext ctx) {
        return matchIndex(depth.get(), ctx.getText());
    }

    @Override
    public FieldPredicate visitQualified_field(@NotNull final Qualified_fieldContext ctx) {

        final FieldPredicate fieldPredicate = visitField(ctx.field());

        final Fields_expressionContext fieldsExpressionContext = ctx.fields_expression();

        if (fieldsExpressionContext == null) {
            return fieldPredicate;
        } else {
            depth.incrementAndGet();

            final FieldPredicate result = and(fieldPredicate, visitFields_expression(fieldsExpressionContext));
            depth.decrementAndGet();
            return result;
        }
    }

    @Override
    public FieldPredicate visitField_set(@NotNull final Field_setContext ctx) {

        final List<Qualified_fieldContext> fieldContexts = ctx.qualified_field();
        final FieldPredicate first = visitQualified_field(fieldContexts.get(0));
        if (fieldContexts.size() == 1) {
            return first;
        }

        final FieldPredicate[] more = new FieldPredicate[fieldContexts.size() - 1];
        int offset = 0;

        for (final Qualified_fieldContext qualified_fieldContext : fieldContexts.subList(1, fieldContexts.size())) {
            more[offset++] = visitQualified_field(qualified_fieldContext);
        }

        return or(first, more);

    }

}
