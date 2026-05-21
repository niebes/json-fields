package net.niebes.jsonfields.grammar;

import java.io.File;

/**
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   26.08.2015
 */
public final class Constants {
    public static final File GRAMMAR_FILE = new File("src/main/antlr4/JsonFields.g4");
    public static final String TOP_LEVEL_RULE = "json_fields";

    private Constants() { }
}
