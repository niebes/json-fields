package org.zalando.guild.api.json.fields.jackson;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * This abstraction holds the context for the fields hierarchy required by the {@link JsonFieldsFilterProvider}.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   23.09.2015
 */
public interface ContextProvider {

    /**
     * Return the current list of fields. This will usually be an unmodifiable list.
     */
    @Nonnull
    List<String> getContext();

    /**
     * Add the supplied field name to the end of the context.
     */
    void pushContext(@Nonnull String context);

    /**
     * Remove the last element from the field context.
     */
    void popContext();

    /**
     * Clear the context.
     */
    void clear();
}
