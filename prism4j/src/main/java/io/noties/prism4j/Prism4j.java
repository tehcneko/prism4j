package io.noties.prism4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class Prism4j {

    private final GrammarLocator grammarLocator;

    public Prism4j() {
        this(new DefaultGrammarLocator());
    }

    public Prism4j(@NotNull GrammarLocator grammarLocator) {
        this.grammarLocator = grammarLocator;
    }

    private static boolean isSyntaxNode(@NotNull Node node) {
        return node.isSyntax();
    }

    private static boolean isGreedyNode(@NotNull Node node) {
        return node.isSyntax() && ((Syntax) node).greedy();
    }

    @NotNull
    public List<Node> tokenize(@NotNull String text, @NotNull Grammar grammar) {
        final List<Node> entries = new ArrayList<>(3);
        entries.add(new Text(text));
        if (text.length() > 0) {
            matchGrammar(text, entries, grammar, 0, 0, false, null);
        }
        return entries;
    }

    @NotNull
    public List<Node> tokenize(@NotNull String text, @NotNull String languageName) {
        return tokenize(text, requireGrammar(languageName));
    }

    public void visit(@NotNull Visitor visitor, @NotNull String text, @NotNull Grammar grammar) {
        visitor.visit(tokenize(text, grammar));
    }

    public void visit(@NotNull Visitor visitor, @NotNull String text, @NotNull String languageName) {
        visit(visitor, text, requireGrammar(languageName));
    }

    @Nullable
    public Grammar grammar(@NotNull String name) {
        return grammarLocator.grammar(this, name);
    }

    @NotNull
    public Grammar requireGrammar(@NotNull String name) {
        final Grammar grammar = grammar(name);
        if (grammar == null) {
            throw new NullPointerException("Requested language is not found: " + name);
        }
        return grammar;
    }

    private void matchGrammar(
            @NotNull String text,
            @NotNull List<Node> entries,
            @NotNull Grammar grammar,
            int index,
            int startPosition,
            boolean oneShot,
            @Nullable Token target
    ) {

        final int textLength = text.length();

        for (Token token : grammar.tokens()) {

            if (token == target) {
                return;
            }

            for (Pattern pattern : token.patterns()) {

                final boolean lookbehind = pattern.lookbehind();
                final boolean greedy = pattern.greedy();
                int lookbehindLength = 0;

                final java.util.regex.Pattern regex = pattern.regex();

                // Don't cache textLength as it changes during the loop
                for (int i = index, position = startPosition; i < entries.size(); position += entries.get(i).textLength(), ++i) {

                    if (entries.size() > textLength) {
                        throw new RuntimeException("Prism4j internal error. Number of entry nodes " +
                                "is greater that the text length.\n" +
                                "Nodes: " + entries + "\n" +
                                "Text: " + text);
                    }

                    final Node node = entries.get(i);
                    if (isSyntaxNode(node)) {
                        continue;
                    }

                    String str = ((Text) node).literal();

                    final Matcher matcher;
                    final int deleteCount;
                    final boolean greedyMatch;
                    int greedyAdd = 0;

                    if (greedy && i != entries.size() - 1) {

                        matcher = regex.matcher(text);
                        // limit search to the position (?)
                        matcher.region(position, textLength);

                        if (!matcher.find()) {
                            break;
                        }

                        int from = matcher.start();

                        if (lookbehind) {
                            from += matcher.group(1).length();
                        }
                        final int to = matcher.start() + matcher.group(0).length();

                        int k = i;
                        int p = position;

                        for (int len = entries.size(); k < len && (p < to || (!isSyntaxNode(entries.get(k)) && !isGreedyNode(entries.get(k - 1)))); ++k) {
                            p += entries.get(k).textLength();
                            // Move the index i to the element in strarr that is closest to from
                            if (from >= p) {
                                i += 1;
                                position = p;
                            }
                        }

                        if (isSyntaxNode(entries.get(i))) {
                            continue;
                        }

                        deleteCount = k - i;
                        str = text.substring(position, p);
                        greedyMatch = true;
                        greedyAdd = -position;

                    } else {
                        matcher = regex.matcher(str);
                        deleteCount = 1;
                        greedyMatch = false;
                    }

                    if (!greedyMatch && !matcher.find()) {
                        if (oneShot) {
                            break;
                        }
                        continue;
                    }

                    if (lookbehind) {
                        final String group = matcher.group(1);
                        lookbehindLength = group != null ? group.length() : 0;
                    }

                    final int from = matcher.start() + greedyAdd + lookbehindLength;
                    final String match;
                    if (lookbehindLength > 0) {
                        match = matcher.group().substring(lookbehindLength);
                    } else {
                        match = matcher.group();
                    }
                    final int to = from + match.length();

                    for (int d = 0; d < deleteCount; d++) {
                        entries.remove(i);
                    }

                    int i2 = i;

                    if (from != 0) {
                        final String before = str.substring(0, from);
                        i += 1;
                        position += before.length();
                        entries.add(i2++, new Text(before));
                    }

                    final List<? extends Node> tokenEntries;
                    final Grammar inside = pattern.inside();
                    final boolean hasInside = inside != null;
                    if (hasInside) {
                        tokenEntries = tokenize(match, inside);
                    } else {
                        tokenEntries = Collections.singletonList(new Text(match));
                    }

                    entries.add(i2++, new Syntax(
                            token.name(),
                            tokenEntries,
                            pattern.alias(),
                            match,
                            greedy,
                            hasInside
                    ));

                    // important thing here (famous off-by one error) to check against full length (not `length - 1`)
                    if (to < str.length()) {
                        final String after = str.substring(to);
                        entries.add(i2, new Text(after));
                    }

                    if (deleteCount != 1) {
                        matchGrammar(text, entries, grammar, i, position, true, token);
                    }

                    if (oneShot) {
                        break;
                    }
                }
            }
        }
    }

}
