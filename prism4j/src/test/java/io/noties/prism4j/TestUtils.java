package io.noties.prism4j;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import ix.Ix;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

public abstract class TestUtils {

    private static final String DELIMITER = "-{52}";

    private static final Gson GSON = new Gson();

    private TestUtils() {
    }

    @NotNull
    public static Collection<Object> testFiles(@NotNull String lang) {

        final String folder = "languages/" + lang + "/";

        try (InputStream in = TestUtils.class.getClassLoader().getResourceAsStream(folder)) {
            //noinspection unchecked
            return (Collection) Ix.from(IOUtils.readLines(in, StandardCharsets.UTF_8))
                    .filter(s -> s.endsWith(".test"))
                    .map(s -> folder + s)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Case readCase(@NotNull String file) {

        final String raw;
        try {
            raw = IOUtils.resourceToString(file, StandardCharsets.UTF_8, TestUtils.class.getClassLoader());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        if (raw == null
                || raw.length() == 0) {
            throw new RuntimeException("Test file has no contents, file: " + file);
        }

        final String[] split = raw.split(DELIMITER);
        if (split.length < 2) {
            throw new RuntimeException("Test file seems to have wrong delimiter, file: " + file);
        }

        final String input = split[0].trim();
        final JsonArray simplifiedOutput = GSON.fromJson(split[1].trim(), JsonArray.class);
        final String description = split[2].trim();

        return new Case(input, simplifiedOutput, description);
    }

    public static void assertCase(@NotNull Case c, @NotNull List<? extends Prism4j.Node> nodes) {

        final String expected = c.simplifiedOutput.toString();
        final String actual = simplify(nodes).toString();

        try {
            assertJsonEquals(expected, actual);
        } catch (AssertionError e) {
            final String newMessage = c.description + "\n" +
                    "" + e.getMessage() + "\n" +
                    "expected: " + expected + "\n" +
                    "actual  : " + actual + "\n\n";
            throw new AssertionError(newMessage, e);
        }
    }

    @NotNull
    private static JsonArray simplify(@NotNull List<? extends Prism4j.Node> nodes) {
        // root array
        final JsonArray array = new JsonArray();
        for (Prism4j.Node node : nodes) {
            if (node instanceof Prism4j.Text) {
                final String literal = ((Prism4j.Text) node).literal();
                if (literal.trim().length() != 0) {
                    array.add(literal);
                }
            } else {
                final Prism4j.Syntax syntax = (Prism4j.Syntax) node;
                final JsonArray inner = new JsonArray();
                inner.add(syntax.type());
                if (syntax.tokenized()) {
                    inner.add(simplify(syntax.children()));
                } else {
                    inner.addAll(simplify(syntax.children()));
                }
                array.add(inner);
            }
        }
        return array;
    }

    public static class Case {

        public final String input;
        public final JsonArray simplifiedOutput;
        public final String description;

        Case(@NotNull String input, @NotNull JsonArray simplifiedOutput, @NotNull String description) {
            this.input = input;
            this.simplifiedOutput = simplifiedOutput;
            this.description = description;
        }
    }
}