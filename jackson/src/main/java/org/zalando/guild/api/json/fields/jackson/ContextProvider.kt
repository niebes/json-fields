package org.zalando.guild.api.json.fields.jackson

/**
 * This abstraction holds the context for the fields hierarchy required by the [JsonFieldsFilterProvider].
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
interface ContextProvider {

    val context: List<String>

    /**
     * Add the supplied field name to the end of the context.
     */
    fun pushContext(context: String)

    /**
     * Remove the last element from the field context.
     */
    fun popContext()

    /**
     * Clear the context.
     */
    fun clear()
}