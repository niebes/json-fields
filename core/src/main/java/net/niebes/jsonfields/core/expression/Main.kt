package net.niebes.jsonfields.core.expression

import net.niebes.jsonfields.core.expression.ParserFramework.parseFieldsExpression
import kotlin.system.exitProcess

/**
 * Entry point for testing the fields expression engine. Pass in a fields expression and a fields expression to test
 * against this expression.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   07.09.2015
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size < 2) {
            println("Usage: Main <expression> <field> [<field>+]")
            exitProcess(1)
        }

        val fieldPredicate = parseFieldsExpression(args[0])
        val fields: List<String> = listOf(*args).subList(1, args.size)
        println("Generated predicate:     $fieldPredicate")
        println("Field hierarchy to test: $fields")
        println("Result:                  " + fieldPredicate.test(fields))
    }
}
