package org.zalando.guild.api.json.fields.jackson

import java.util.*

/**
 * ContextProvider implementation using ThreadLocals. Should work for most cases, as asynchronous requests will
 * (hopefully) still be rendered synchronously.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
class ThreadLocalContextProvider private constructor() : ContextProvider {

    override val context: List<String>
        get() = Collections.unmodifiableList(STORE.get())

    override fun pushContext(context: String?) {
        STORE.get().addLast(context)
    }

    override fun popContext() {
        STORE.get().removeLast()
    }

    override fun clear() {
        STORE.get().clear()
    }

    companion object {
        private val STORE: ThreadLocal<LinkedList<String>> = object : ThreadLocal<LinkedList<String>>() {
            override fun initialValue(): LinkedList<String> {
                return LinkedList()
            }
        }
        val instance = ThreadLocalContextProvider()
    }
}