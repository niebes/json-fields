package org.zalando.guild.api

import com.google.common.base.Strings
import com.google.common.base.Supplier
import org.slf4j.LoggerFactory
import org.zalando.guild.api.json.fields.java.expression.ParserFramework
import org.zalando.guild.api.json.fields.java.model.FieldPredicate
import org.zalando.guild.api.json.fields.java.model.FieldPredicates
import java.io.IOException
import java.text.MessageFormat
import javax.servlet.*

class FieldsFilter @JvmOverloads constructor(
    private val parameter: String = "fields",
    defaultFields: String? = null
) : Filter {
    private val defaultFieldPredicate: FieldPredicate

    init {
        defaultFieldPredicate =
            if (Strings.isNullOrEmpty(defaultFields)) FieldPredicates.alwaysTrue()
            else ParserFramework.parseFieldsExpression(defaultFields!!)
    }

    private fun setFieldPredicate(fields: String?) {
        try {
            if (Strings.isNullOrEmpty(fields)) {
                FIELD_PREDICATE.set(defaultFieldPredicate)
            } else {
                val fieldsExpression = ParserFramework.parseFieldsExpression(fields!!)
                FIELD_PREDICATE.set(fieldsExpression)
            }
        } catch (e: RuntimeException) {
            logger.warn(
                MessageFormat.format("error while filtering fields. skip filter for fields: {0}", fields),
                e
            )
            FIELD_PREDICATE.set(defaultFieldPredicate)
        }
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
        val FIELD_PREDICATE_SUPPLIER = Supplier(FIELD_PREDICATE::get)
    }
}