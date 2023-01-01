package org.zalando.guild.api.json.fields.jackson;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * ContextProvider implementation using ThreadLocals. Should work for most cases, as asynchronous requests will
 * (hopefully) still be rendered synchronously.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
public final class ThreadLocalContextProvider implements ContextProvider {

    private ThreadLocalContextProvider() { }

    public static ThreadLocalContextProvider getInstance() {
        return Holder.INSTANCE;
    }

    static final class Holder {

        public static final ThreadLocalContextProvider INSTANCE = new ThreadLocalContextProvider();

        private Holder() { }
    }

    private static final ThreadLocal<LinkedList<String>> STORE = new ThreadLocal<LinkedList<String>>() {
        @Override
        protected LinkedList<String> initialValue() {
            return new LinkedList<>();
        }
    };

    @Override
    @Nonnull
    public List<String> getContext() {
        return Collections.unmodifiableList(STORE.get());
    }

    @Override
    public void pushContext(@Nonnull final String context) {
        STORE.get().addLast(context);
    }

    @Override
    public void popContext() {
        STORE.get().removeLast();
    }

    @Override
    public void clear() {
        STORE.get().clear();
    }
}
