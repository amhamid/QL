package org.uva.sea.ql.ast.statement;

import org.uva.sea.ql.ast.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BlockNode implements Node
{
    private final Collection<Statement> statements;

    public BlockNode()
    {
        this.statements = new ArrayList<>();
    }

    public void addStatement(final Statement statement)
    {
        this.statements.add(statement);
    }

    public Collection<Statement> getStatements()
    {
        return Collections.unmodifiableCollection(statements);
    }

}
