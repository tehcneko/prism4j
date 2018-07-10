package ru.noties.prism4j;

import android.support.annotation.NonNull;

import java.util.List;

public class TokenImpl implements Prism4j.Token {

    private final String name;
    private final List<Prism4j.Pattern> patterns;

    public TokenImpl(@NonNull String name, @NonNull List<Prism4j.Pattern> patterns) {
        this.name = name;
        this.patterns = patterns;
    }

    @NonNull
    @Override
    public String name() {
        return name;
    }

    @NonNull
    @Override
    public List<Prism4j.Pattern> patterns() {
        return patterns;
    }

    @Override
    public String toString() {
        return "TokenImpl{" +
                "name='" + name + '\'' +
                ", patterns=" + patterns +
                '}';
    }
}
