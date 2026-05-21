package net.niebes.jsonfields.webflux

import io.micrometer.context.ThreadLocalAccessor
import net.niebes.jsonfields.core.model.FieldPredicate

/**
 * Bridges the FieldPredicate between Reactor Context and ThreadLocal.
 *
 * When context-propagation is enabled (Spring Boot 3.2+ default), this accessor
 * automatically propagates the predicate stored in Reactor Context to the
 * ThreadLocal that Jackson reads during serialization.
 *
 * Registered via META-INF/services/io.micrometer.context.ThreadLocalAccessor.
 */
class JsonFieldsThreadLocalAccessor : ThreadLocalAccessor<FieldPredicate> {

    override fun key(): String = JsonFieldsWebFilter.CONTEXT_KEY

    override fun getValue(): FieldPredicate? = JsonFieldsWebFilter.CURRENT_PREDICATE.get()

    override fun setValue(value: FieldPredicate) {
        JsonFieldsWebFilter.CURRENT_PREDICATE.set(value)
    }

    override fun setValue() {
        JsonFieldsWebFilter.CURRENT_PREDICATE.remove()
    }
}
