package org.zalando.guild.api

import org.slf4j.LoggerFactory
import org.zalando.guild.api.json.fields.java.expression.ParserFramework
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates
import java.io.IOException
import java.text.MessageFormat
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class FieldsFilter @JvmOverloads constructor(
    private val parameter: String = "fields",
    defaultFields: String? = null
) : Filter {
    private val defaultFieldPredicate: FieldPredicate = parseOrDefault(defaultFields, FieldPredicates.alwaysTrue())

    private fun parseOrDefault(defaultFields: String?, default: FieldPredicate) =
        when (defaultFields?.takeIf(String::isNotBlank)) {
            null -> default
            else -> ParserFramework.parseFieldsExpression(defaultFields)
        }

    private fun setFieldPredicate(fields: String?) {
        runCatching {
            parseOrDefault(fields, defaultFieldPredicate)
        }.onFailure { e ->
            logger.warn(
                MessageFormat.format("error while filtering fields. skip filter for fields: {0}", fields),
                e
            )
        }.fold(
            { it },
            { defaultFieldPredicate }
        ).let(FIELD_PREDICATE::set)
    }

    override fun init(filterConfig: FilterConfig) {
        // This constructor is intentionally empty, because something something
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest, response: ServletResponse,
        chain: FilterChain
    ) {
        val fields = request.getParameter(parameter)
        setFieldPredicate(fields)
        chain.doFilter(request, response)
    }

    override fun destroy() {
        // This constructor is intentionally empty, because something something
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private val ALL_FIELDS_PREDICATE = FieldPredicates.alwaysTrue()
        private val FIELD_PREDICATE: ThreadLocal<FieldPredicate> = object : InheritableThreadLocal<FieldPredicate>() {
            override fun initialValue(): FieldPredicate = ALL_FIELDS_PREDICATE
        }
    }
}