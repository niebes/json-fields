package org.zalando.guild.api.json.fields.jackson.servlet

import com.google.common.base.Preconditions
import com.google.common.base.Supplier
import javax.servlet.http.HttpServletRequest

/**
 * Request Provider that uses ThreadLocals. This obviously won't work in an asynchronous environment.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
class ThreadLocalRequestProvider private constructor() : Supplier<HttpServletRequest> {
    override fun get(): HttpServletRequest = REQUEST.get()

    /**
     * Lazy initialization through static holder class.
     * https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
     */
    internal object Holder {
        /**
         * It's a singleton. The Design Pattern your teachers warned you about.
         */
        val instance = ThreadLocalRequestProvider()
    }

    companion object {
        private val REQUEST = ThreadLocal<HttpServletRequest>()

        /**
         * Assign the HttpServletRequest to the ThreadLocal. This will usually be called from a Filter or Interceptor. Don't
         * forget to call [.removeRequest] at the end of the request, or you'll have a memory leak.
         */
        fun assignRequest(request: HttpServletRequest) {
            REQUEST.set(request)
        }

        /**
         * Clear the HttpServletRequest from the ThreadLocal. This will usually be called from a Filter or Interceptor.
         */
        fun removeRequest() {
            REQUEST.remove()
        }
    }
}