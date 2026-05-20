package org.zalando.guild.api.json.fields.jackson.servlet

import jakarta.servlet.http.HttpServletRequest
import org.zalando.guild.api.json.fields.java.expression.ParserFramework
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates
import java.util.function.Function

/**
 * A Predicate Function that parses a FieldPredicate from a request parameter.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
open class ParamBasedPredicateFunction
/**
 * Protected to allow subclassing.
 */ protected constructor(private val paramName: String = DEFAULT_PARAM_NAME) :
    Function<HttpServletRequest, FieldPredicate> {
    /**
     * Protected to allow subclassing.
     */
    /**
     * Open-closed principle.
     */

    override fun apply(request: HttpServletRequest): FieldPredicate {
        val parameterValue = request.getParameter(paramName)
        return if (parameterValue != null) {
            try {
                ParserFramework.parseFieldsExpressionOrFail(parameterValue)
            } catch (e: IllegalArgumentException) {
                handleExpressionFailure(parameterValue, e)
            }
        } else {
            FieldPredicates.alwaysTrue()
        }
    }

    /**
     * If you want custom error handling for illegal expressions, override this method. The default behavior is to
     * return a FieldPredicate that never matches.
     */

    protected fun handleExpressionFailure(
        expression: String?,
        e: IllegalArgumentException?
    ): FieldPredicate {
        return FieldPredicates.alwaysFalse()
    }

    companion object {
        private const val DEFAULT_PARAM_NAME = "fields"

        /**
         * Return a Predicate Function that maps the query parameter "fields" to a FieldPredicate.
         */

        fun paramBasedPredicateFunctionWithDefaultName(): ParamBasedPredicateFunction {
            return ParamBasedPredicateFunction()
        }

        /**
         * Return a Predicate Function that maps the query parameter "fields" to a FieldPredicate.
         */

        fun paramBasedPredicateFunctionWithCustomName(
            paramName: String
        ): ParamBasedPredicateFunction {
            return ParamBasedPredicateFunction(paramName)
        }
    }
}