package org.uva.sea.ql.visitor.render;

import org.uva.sea.ql.ast.expression.impl.IdentifierNode;
import org.uva.sea.ql.ast.statement.BlockNode;
import org.uva.sea.ql.ast.statement.Statement;
import org.uva.sea.ql.ast.statement.impl.AssignmentNode;
import org.uva.sea.ql.ast.statement.impl.ComputedNode;
import org.uva.sea.ql.ast.statement.impl.IfNode;
import org.uva.sea.ql.type.Type;
import org.uva.sea.ql.value.Value;
import org.uva.sea.ql.variable.VariableState;
import org.uva.sea.ql.visitor.StatementVisitor;
import org.uva.sea.ql.observer.ComputedObserver;
import org.uva.sea.ql.observer.ConditionObserver;
import org.uva.sea.ql.widget.CustomWidget;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatementWidgetVisitor implements StatementVisitor
{
    private final JFrame frame;
    private final JPanel panel;
    private final VariableState variableState;

    private StatementWidgetVisitor(final JFrame frame, final JPanel panel, final VariableState variableState)
    {
        this.frame = frame;
        this.panel = panel;
        this.variableState = variableState;
    }

    public static JPanel render(final JFrame frame, final JPanel panel, final Statement statement, final VariableState variableState)
    {
        final StatementWidgetVisitor statementWidgetVisitor = new StatementWidgetVisitor(frame, panel, variableState);
        statement.accept(statementWidgetVisitor);
        return statementWidgetVisitor.getPanel();
    }

    @Override
    public void visit(final AssignmentNode assignmentNode)
    {
        final String question = assignmentNode.getQuestion();
        final JPanel questionPanel = new JPanel();
        questionPanel.add(new JLabel(question));

        final JPanel typePanel = new JPanel();
        final Type type = assignmentNode.getType();
        type.accept(new TypeWidgetVisitor(typePanel, assignmentNode.getIdentifierNode(), this.variableState));

        addQuestionPanel(questionPanel);
        addTypePanel(typePanel);
    }

    @Override
    public void visit(ComputedNode computedNode)
    {
        final String question = computedNode.getQuestion();
        final JPanel questionPanel = new JPanel();
        questionPanel.add(new JLabel(question));
        addQuestionPanel(questionPanel);

        final JPanel typePanel = new JPanel();
        final Type type = computedNode.getType();
        final TypeWidgetVisitor typeWidgetVisitor = new TypeWidgetVisitor(typePanel, computedNode.getIdentifierNode(), this.variableState, false);
        final CustomWidget customWidget = type.accept(typeWidgetVisitor);
        addTypePanel(typePanel);

        registerComputedObserver(computedNode, customWidget);
    }

    @Override
    public void visit(final IfNode ifNode)
    {
        final List<ConditionObserver.BranchComponent> branchComponents = new ArrayList<>();
        for(final IfNode.Branch branch : ifNode.getBranches())
        {
            final BlockNode blockNode = branch.getBlock();
            final List<Component> components = new ArrayList<>();
            for(final Statement statement : blockNode.getStatements())
            {
                final JPanel jPanel = render(this.frame, new JPanel(), statement, this.variableState);

                final Component question = jPanel.getComponent(0);
                final Component type = jPanel.getComponent(1);

                components.add(question);
                components.add(type);

                addQuestionPanel(question);
                addTypePanel(type);
            }

            branchComponents.add(new ConditionObserver.BranchComponent(branch, components));
        }

        registerConditionObserver(ifNode, branchComponents, this.variableState.getVariables());
    }

    private ConditionObserver registerConditionObserver(final IfNode ifNode, final List<ConditionObserver.BranchComponent> branchComponents, final Map<IdentifierNode, Value> variables)
    {
        ConditionObserver conditionObserver = new ConditionObserver(this.frame, branchComponents, variables);
        ifNode.addObserver(conditionObserver);

        // trigger if there is an 'else' statement to be initialize
        conditionObserver.update(null, null);
        return conditionObserver;
    }

    private void registerComputedObserver(ComputedNode computedNode, CustomWidget customWidget)
    {
        final ComputedObserver computedObserver = new ComputedObserver(computedNode.getExprNode(), customWidget, this.variableState.getVariables());
        computedNode.addObserver(computedObserver);
    }

    public JPanel getPanel()
    {
        return panel;
    }

    private void addQuestionPanel(final Component question)
    {
        this.panel.add(question, "left, gapright 10");
    }

    private void addTypePanel(final Component type)
    {
        this.panel.add(type, "left, span");
    }

}
