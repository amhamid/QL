package org.uva.sea.ql.ast.expression.impl;

import org.uva.sea.ql.ast.expression.ArithmeticOperation;
import org.uva.sea.ql.ast.expression.ExprNode;
import org.uva.sea.ql.value.Value;
import org.uva.sea.ql.visitor.ExpressionVisitor;

import java.util.Map;

public class EqualNode extends ArithmeticOperation
{

    public EqualNode(final ExprNode lhs, final ExprNode rhs)
    {
        super(lhs, rhs);
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> expressionVisitor)
    {
        return expressionVisitor.visit(this);
    }

    @Override
    public Value evaluate(final Map<IdentifierNode, Value> variables)
    {
        final Value value1 = this.lhs.evaluate(variables);
        final Value value2 = this.rhs.evaluate(variables);
        return value1.equal(value2);
    }

    @Override
    protected String getOperator()
    {
        return "==";
    }
}
