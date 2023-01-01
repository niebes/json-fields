package org.zalando.guild.api.json.fields.jackson.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Supplier;

/**
 * Request Provider that uses ThreadLocals. This obviously won't work in an asynchronous environment.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
public final class ThreadLocalRequestProvider implements Supplier<HttpServletRequest> {

    private static final ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<>();

    @Override
    @Nonnull
    public HttpServletRequest get() {
        return REQUEST.get();
    }

    /**
     * Assign the HttpServletRequest to the ThreadLocal. This will usually be called from a Filter or Interceptor. Don't
     * forget to call {@link #removeRequest()} at the end of the request, or you'll have a memory leak.
     */
    public static void assignRequest(@Nonnull final HttpServletRequest request) {

        checkNotNull(request, "Request required");
        REQUEST.set(request);
    }

    /**
     * Clear the HttpServletRequest from the ThreadLocal. This will usually be called from a Filter or Interceptor.
     */
    public static void removeRequest() {
        REQUEST.remove();
    }

    private ThreadLocalRequestProvider() { }

    /**
     * It's a singleton. The Design Pattern your teachers warned you about.
     */
    @Nonnull
    public ThreadLocalRequestProvider getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Lazy initialization through static holder class.
     * https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
     */
    static final class Holder {

        public static final ThreadLocalRequestProvider INSTANCE = new ThreadLocalRequestProvider();

        private Holder() { }
    }

}
