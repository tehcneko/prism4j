package io.noties.prism4j;


import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

abstract class ToString {

    private ToString() {
    }

    @NotNull
    static String toString(@NotNull Grammar grammar) {
        final StringBuilder builder = new StringBuilder();
        toString(builder, new Cache(), grammar);
        return builder.toString();
    }

    @NotNull
    static String toString(@NotNull Token token) {
        final StringBuilder builder = new StringBuilder();
        toString(builder, new Cache(), token);
        return builder.toString();
    }

    @NotNull
    static String toString(@NotNull Pattern pattern) {
        final StringBuilder builder = new StringBuilder();
        toString(builder, new Cache(), pattern);
        return builder.toString();
    }

    private static void toString(@NotNull StringBuilder builder, @NotNull Cache cache, @NotNull Grammar grammar) {

        builder
                .append("Grammar{id=0x")
                .append(Integer.toHexString(System.identityHashCode(grammar)))
                .append(",name=\"")
                .append(grammar.name())
                .append('\"');

        if (cache.visited(grammar)) {
            builder.append(",[...]");
        } else {
            cache.markVisited(grammar);
            builder.append(",tokens=[");
            boolean first = true;
            for (Token token : grammar.tokens()) {
                if (first) {
                    first = false;
                } else {
                    builder.append(',');
                }
                toString(builder, cache, token);
            }
            builder.append(']');
        }

        builder.append('}');
    }

    private static void toString(@NotNull StringBuilder builder, @NotNull Cache cache, @NotNull Token token) {

        builder
                .append("Token{id=0x")
                .append(Integer.toHexString(System.identityHashCode(token)))
                .append(",name=\"")
                .append(token.name())
                .append('\"');

        if (cache.visited(token)) {
            builder.append(",[...]");
        } else {
            cache.markVisited(token);
            builder.append(",patterns=[");
            boolean first = true;
            for (Pattern pattern : token.patterns()) {
                if (first) {
                    first = false;
                } else {
                    builder.append(',');
                }
                toString(builder, cache, pattern);
            }
            builder.append(']');
        }
        builder.append('}');
    }

    private static void toString(@NotNull StringBuilder builder, @NotNull Cache cache, @NotNull Pattern pattern) {

        builder
                .append("Pattern{id=0x")
                .append(Integer.toHexString(System.identityHashCode(pattern)));

        if (cache.visited(pattern)) {
            builder.append(",[...]");
        } else {

            cache.markVisited(pattern);

            builder.append(",regex=\"").append(pattern.regex()).append('\"');

            if (pattern.lookbehind()) {
                builder.append(",lookbehind=true");
            }

            if (pattern.greedy()) {
                builder.append(",greedy=true");
            }

            if (pattern.alias() != null) {
                builder.append(",alias=\"").append(pattern.alias()).append('\"');
            }

            final Grammar inside = pattern.inside();
            if (inside != null) {
                builder.append(",inside=");
                toString(builder, cache, inside);
            }
        }

        builder.append('}');
    }

    private static class Cache {

        private final Set<Integer> cache = new HashSet<>(3);

        @NotNull
        private static Integer key(@NotNull Object o) {
            return System.identityHashCode(o);
        }

        public boolean visited(@NotNull Object o) {
            return cache.contains(key(o));
        }

        public void markVisited(@NotNull Object o) {
            cache.add(key(o));
        }
    }
}
