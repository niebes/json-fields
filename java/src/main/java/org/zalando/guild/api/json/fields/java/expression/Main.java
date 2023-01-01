package org.zalando.guild.api.json.fields.java.expression;

import static java.lang.System.out;

import static java.util.Arrays.asList;

import static org.zalando.guild.api.json.fields.java.expression.ParserFramework.parseFieldsExpression;

import java.util.List;

import org.zalando.guild.api.json.fields.java.model.FieldPredicate;

/**
 * Entry point for testing the fields expression engine. Pass in a fields expression and a fields expression to test
 * against this expression.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   07.09.2015
 */
public final class Main {
    public static void main(final String... args) {
        if (args.length < 2) {
            out.println("Usage: Main <expression> <field> [<field>+]");
            System.exit(1);
        }

        final FieldPredicate fieldPredicate = parseFieldsExpression(args[0]);
        final List<String> fields = asList(args).subList(1, args.length);
        out.println("Generated predicate:     " + fieldPredicate);
        out.println("Field hierarchy to test: " + fields);
        out.println("Result:                  " + fieldPredicate.test(fields));
    }
}
