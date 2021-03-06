package io.noties.prism4j.languages;

import io.noties.prism4j.Grammar;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static java.util.regex.Pattern.*;

@SuppressWarnings("unused")
public class Prism_c {

    @NotNull
    public static Grammar create(@NotNull Prism4j prism4j) {

        final Token commentToken = GrammarUtils.token("comment", GrammarUtils.pattern(compile("//(?:[^\\r\\n\\\\]|\\\\(?:\\r\\n?|\\n|(?![\\r\\n])))*|/\\*[\\s\\S]*?(?:\\*/|$)"), false, true));
        final Grammar c = prism4j.requireGrammar("clike").extend(
                "c",
                token -> {
                    final String name = token.name();
                    return !"boolean".equals(name);
                },
                commentToken,
                GrammarUtils.token("string", GrammarUtils.pattern(compile("\"(?:\\\\(?:\\r\\n|[\\s\\S])|[^\"\\\\\\r\\n])*\""), false, true)),
                GrammarUtils.token("class-name", GrammarUtils.pattern(compile("(\\b(?:enum|struct)\\s+(?:__attribute__\\s*\\(\\([\\s\\S]*?\\)\\)\\s*)?)\\w+|\\b[a-z]\\w*_t\\b"), true)),
                GrammarUtils.token("keyword", GrammarUtils.pattern(compile("\\b(?:__attribute__|_Alignas|_Alignof|_Atomic|_Bool|_Complex|_Generic|_Imaginary|_Noreturn|_Static_assert|_Thread_local|asm|typeof|inline|auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|int|long|register|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while)\\b"))),
                GrammarUtils.token("function", GrammarUtils.pattern(compile("\\b[a-z_]\\w*(?=\\s*\\()", CASE_INSENSITIVE))),
                GrammarUtils.token("number", GrammarUtils.pattern(compile("(?:\\b0x(?:[\\da-f]+(?:\\.[\\da-f]*)?|\\.[\\da-f]+)(?:p[+-]?\\d+)?|(?:\\b\\d+(?:\\.\\d*)?|\\B\\.\\d+)(?:e[+-]?\\d+)?)[ful]{0,4}", CASE_INSENSITIVE))),
                GrammarUtils.token("operator", GrammarUtils.pattern(compile(">>=?|<<=?|->|([-+&|:])\\1|[?:~]|[-+*/%&|^!=<>]=?")))
        );

        c.insertBeforeToken("string",
                GrammarUtils.token("char", GrammarUtils.pattern(compile("'(?:\\\\(?:\\r\\n|[\\s\\S])|[^'\\\\\\r\\n]){0,32}'"))),
                GrammarUtils.token("macro", GrammarUtils.pattern(
                        compile("(^[\\t ]*)#\\s*[a-z](?:[^\\r\\n\\\\/]|/(?!\\*)|/\\*(?:[^*]|\\*(?!/))*\\*/|\\\\(?:\\r\\n|[\\s\\S]))*", CASE_INSENSITIVE | MULTILINE),
                        true,
                        true,
                        "property",
                        GrammarUtils.grammar("inside",
                                GrammarUtils.token("string",
                                        GrammarUtils.pattern(
                                                compile("^(#\\s*include\\s*)<[^>]+>"),
                                                true),
                                        Objects.requireNonNull(prism4j.requireGrammar("clike").findToken("string")).patterns().get(0)),
                                commentToken,
                                GrammarUtils.token("macro-name",
                                        GrammarUtils.pattern(compile("(^#\\s*define\\s+)\\w+\\b(?!\\()", CASE_INSENSITIVE), true),
                                        GrammarUtils.pattern(compile("(^#\\s*define\\s+)\\w+\\b(?=\\()", CASE_INSENSITIVE), true, false, "function")),
                                GrammarUtils.token("directive", GrammarUtils.pattern(
                                        compile("^(#\\s*)[a-z]+"),
                                        true,
                                        false,
                                        "keyword"
                                )),
                                GrammarUtils.token("directive-hash", GrammarUtils.pattern(compile("^#"))),
                                GrammarUtils.token("punctuation", GrammarUtils.pattern(compile("##|\\\\(?=[\\r\\n])"))),
                                GrammarUtils.token("expression", GrammarUtils.pattern(compile("\\S[\\s\\S]*"), false, false, null, c))
                        )
                )),
                GrammarUtils.token("constant", GrammarUtils.pattern(compile("\\b(?:__FILE__|__LINE__|__DATE__|__TIME__|__TIMESTAMP__|__func__|EOF|NULL|SEEK_CUR|SEEK_END|SEEK_SET|stdin|stdout|stderr)\\b")))
        );

        return c;
    }
}
