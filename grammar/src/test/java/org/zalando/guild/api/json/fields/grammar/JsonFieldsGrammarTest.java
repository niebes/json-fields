package org.zalando.guild.api.json.fields.grammar;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;

import java.util.Arrays;

import org.antlr.v4.Tool;
import org.antlr.v4.tool.ANTLRMessage;
import org.antlr.v4.tool.ANTLRToolListener;

import org.junit.jupiter.api.Test;

/**
 * This test only makes sure the grammar compiles.
 *
 * @author  Sean Patrick Floyd (sean.floyd@zalando.de)
 * @since   26.08.2015
 */
public class JsonFieldsGrammarTest {

    @Test
    public void verifyGrammarCompiles() throws IOException {

        final File tempDir = Files.createTempDirectory("antlroutput").toFile();

        final String[] args = {                      //
            "-no-listener",                          //
            "-o",                                    //
            tempDir.getAbsolutePath(),               //
            Constants.GRAMMAR_FILE.getAbsolutePath() //
        };                                           //

        final Tool antlr = new Tool(args);

        antlr.addListener(new ANTLRToolListener() {
                @Override
                public void info(final String msg) { }

                @Override
                public void error(final ANTLRMessage msg) {
                    fail(format(msg));
                }

                private String format(final ANTLRMessage msg) {
                    return String.format("Message{errorType=%s, args=%s, fileName='%s', line=%d, charPosition=%d}",
                            msg.getErrorType(), Arrays.asList(msg.getArgs()), msg.fileName, msg.line, msg.charPosition);
                }

                @Override
                public void warning(final ANTLRMessage msg) {
                    fail(format(msg));
                }
            });
        antlr.processGrammarsOnCommandLine();

    }
}
