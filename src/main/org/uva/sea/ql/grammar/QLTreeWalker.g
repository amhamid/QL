tree grammar QLTreeWalker;

options
{
	tokenVocab = QL;
	output = AST;
    ASTLabelType = CommonTree;
}
  
@header
{
	package org.uva.sea.ql.parser;
	import org.uva.sea.ql.ast.Node;
	import org.uva.sea.ql.ast.FormNode;
	import org.uva.sea.ql.ast.statement.Statement;
	import org.uva.sea.ql.ast.statement.impl.AssignmentNode;
	import org.uva.sea.ql.ast.statement.impl.ComputedNode;
	import org.uva.sea.ql.ast.statement.impl.IfNode;
	import org.uva.sea.ql.ast.statement.BlockNode;
	import org.uva.sea.ql.ast.expression.ExprNode;
	import org.uva.sea.ql.ast.expression.impl.ValueNode;
	import org.uva.sea.ql.ast.expression.impl.AddNode;
	import org.uva.sea.ql.ast.expression.impl.AndNode;
	import org.uva.sea.ql.ast.expression.impl.OrNode;
	import org.uva.sea.ql.ast.expression.impl.EqualNode;
	import org.uva.sea.ql.ast.expression.impl.NotEqualNode;
	import org.uva.sea.ql.ast.expression.impl.LessThanNode;
	import org.uva.sea.ql.ast.expression.impl.LessEqualNode;
	import org.uva.sea.ql.ast.expression.impl.GreaterThanNode;
	import org.uva.sea.ql.ast.expression.impl.GreaterEqualNode;
	import org.uva.sea.ql.ast.expression.impl.MultiplyNode;
	import org.uva.sea.ql.ast.expression.impl.DivideNode;
	import org.uva.sea.ql.ast.expression.impl.SubtractNode;
	import org.uva.sea.ql.ast.expression.impl.NotNode;
	import org.uva.sea.ql.ast.expression.impl.NegateNode;
	import org.uva.sea.ql.ast.expression.impl.IdentifierNode;
	import org.uva.sea.ql.ast.expression.impl.UndefinedIdentifierNode;
	import org.uva.sea.ql.value.Value;
	import org.uva.sea.ql.value.impl.IntegerValue;
	import org.uva.sea.ql.value.impl.BooleanValue;
	import org.uva.sea.ql.value.impl.MoneyValue;
	import org.uva.sea.ql.value.impl.StringValue;
	import org.uva.sea.ql.value.impl.UndefinedValue;
	import org.uva.sea.ql.type.Type;
	import org.uva.sea.ql.type.impl.BooleanType;
	import org.uva.sea.ql.type.impl.IntegerType;
	import org.uva.sea.ql.type.impl.StringType;
	import org.uva.sea.ql.type.impl.MoneyType;
	import java.util.Map;
	import java.util.HashMap;
}

@members
{
    private final Map<IdentifierNode, Value> variables = new HashMap<>();
}

walk returns [FormNode node]
    :   form { $node = $form.node; }
    ;   
   
form returns [FormNode node]
	:	^(FORM Identifier ^(BLOCK block)) { $node = new FormNode($Identifier.text, $block.node, variables); }
	;

block returns [BlockNode node]
@init
{
    final BlockNode blockNode = new BlockNode();
    $node = blockNode;
}
    :   (statement { blockNode.addStatement($statement.node); })*
    ;

statement returns [Statement node]
	:	ifStatement { $node = $ifStatement.node; }
		| assignmentStatement { $node = $assignmentStatement.node; }
		| computedStatement { $node = $computedStatement.node; }
	;

ifStatement returns [IfNode node]
@init
{
    final IfNode ifNode = new IfNode();
    $node = ifNode;
}
	:   ^(IF
	        (^(EXPRESSION expression ^(BLOCK b1=block)) { ifNode.addBranch($expression.node, $b1.node); })+
	        (^(EXPRESSION ^(BLOCK b2=block)) { ifNode.addBranch(new ValueNode(new BooleanValue("true")), $b2.node); })?
	     )
	;

assignmentStatement returns [AssignmentNode node]
	:	^(ASSIGNMENT StringLiteral Identifier type)
	    {
	        final Value defaultValue = $type.defaultValue;
	        final IdentifierNode identifierNode = new IdentifierNode($Identifier.text, defaultValue);
	        variables.put(identifierNode, defaultValue);
	        $node = new AssignmentNode($StringLiteral.text, identifierNode);
	    }
	;

computedStatement returns [ComputedNode node]
    :   ^(COMPUTED StringLiteral Identifier type expression)
        {
            final Value defaultValue = $type.defaultValue;
            final IdentifierNode identifierNode = new IdentifierNode($Identifier.text, defaultValue);
            variables.put(identifierNode, defaultValue);
            $node = new ComputedNode($StringLiteral.text, identifierNode, $expression.node);
        }
    ;

type returns [Value defaultValue]
	:	'boolean'   { $defaultValue = new BooleanValue("false"); }
		| 'integer' { $defaultValue = new IntegerValue("0"); }
		| 'string'  { $defaultValue = new StringValue(""); }
		| 'money'   { $defaultValue = new MoneyValue("0.0"); }
	;

expression returns [ExprNode node]
    :   ^('&&' lhs=expression rhs=expression) { $node = new AndNode($lhs.node, $rhs.node); }
    |   ^('||' lhs=expression rhs=expression) { $node = new OrNode($lhs.node, $rhs.node); }
    |   ^('==' lhs=expression rhs=expression) { $node = new EqualNode($lhs.node, $rhs.node); }
    |   ^('!=' lhs=expression rhs=expression) { $node = new NotEqualNode($lhs.node, $rhs.node); }
    |   ^('<' lhs=expression rhs=expression) { $node = new LessThanNode($lhs.node, $rhs.node); }
    |   ^('<=' lhs=expression rhs=expression) { $node = new LessEqualNode($lhs.node, $rhs.node); }
    |   ^('>=' lhs=expression rhs=expression) { $node = new GreaterEqualNode($lhs.node, $rhs.node); }
    |   ^('>' lhs=expression rhs=expression) { $node = new GreaterThanNode($lhs.node, $rhs.node); }
    |   ^('+' lhs=expression rhs=expression) { $node = new AddNode($lhs.node, $rhs.node); }
    |   ^('-' lhs=expression rhs=expression) { $node = new SubtractNode($lhs.node, $rhs.node); }
    |   ^('*' lhs=expression rhs=expression) { $node = new MultiplyNode($lhs.node, $rhs.node); }
    |   ^('/' lhs=expression rhs=expression) { $node = new DivideNode($lhs.node, $rhs.node); }
    |   ^(NOT op=expression) { $node = new NotNode($op.node); }
    |   ^(NEGATION op=expression) { $node = new NegateNode($op.node); }
    |   Boolean  { $node = new ValueNode(new BooleanValue($Boolean.text)); }
    |   Integer { $node = new ValueNode(new IntegerValue($Integer.text)); }
    |   Money { $node = new ValueNode(new MoneyValue($Money.text)); }
    |   StringLiteral { $node = new ValueNode(new StringValue($StringLiteral.text)); }
    |   Identifier
        {
            final Value defaultValue = variables.get(new IdentifierNode($Identifier.text, null));

            if(defaultValue == null)
            {
                // variable is undefined
                $node = new UndefinedIdentifierNode($Identifier.text);
            }
            else
            {
                $node = new IdentifierNode($Identifier.text, defaultValue);
            }
        }
    ;
    