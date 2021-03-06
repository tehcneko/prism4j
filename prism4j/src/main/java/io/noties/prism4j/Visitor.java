package io.noties.prism4j;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Visitor {

    public void visit(@NotNull List<? extends Node> nodes) {
        for (Node node : nodes) {
            if (node.isSyntax()) {
                visitSyntax((Syntax) node);
            } else {
                visitText((Text) node);
            }
        }
    }

    protected abstract void visitText(@NotNull Text text);

    // do not forget to call visit(syntax.children()) inside
    protected abstract void visitSyntax(@NotNull Syntax syntax);
}
