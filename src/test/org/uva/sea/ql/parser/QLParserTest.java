package org.uva.sea.ql.parser;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.uva.sea.ql.Message;
import org.uva.sea.ql.ast.expression.impl.IdentifierNode;
import org.uva.sea.ql.ast.statement.impl.IfNode;
import org.uva.sea.ql.parser.exception.ParserException;
import org.uva.sea.ql.parser.impl.ANTLRParser;
import org.uva.sea.ql.value.Value;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QLParserTest
{
    private final IParser parser;

    public QLParserTest()
    {
        this.parser = new ANTLRParser();
    }

    @After
    public void tearDown()
    {
        System.out.println("OK");
    }

    @Test
    public void simpleValidFormTest() throws ParseException, RecognitionException
    {
        final String validSrc = "" +
                "form test " +
                "{ " +
                "	\"Did you sell a house in 2010?\" hasSoldHouse: boolean " +
                "	\"Did you sell a car in 2010?\" hasSoldCar: integer " +
                "	if (1+1==2) " +
                "	{ " +
                "		\"nothing?\" hasNothing: boolean " +
                "		\"really nothing?\" hasNothing2: boolean " +
                "	}" +
                "	else" +
                "	{" +
                "		\"nothing?\" hasNothing: boolean " +
                "	}" +
                "}";

        QLParser qlParser = this.parser.createQLParser("1+2*1");
        String actualExpression = qlParser.expression().tree.toStringTree();
        System.out.println(actualExpression);
        final String expectedExpression = "(+ 1 (* 2 1))";
        Assert.assertEquals("Result should be the same", expectedExpression, actualExpression);

        QLParser qlParser2 = this.parser.createQLParser("if(1+2) { \"Really?\" hasSoldIt:  boolean }");
        String actualIfBlock = qlParser2.ifStatement().tree.toStringTree();
        System.out.println(actualIfBlock);
        final String expectedIfBlock = "(IF (EXPRESSION (+ 1 2) (BLOCK (ASSIGNMENT Really? hasSoldIt boolean))))";
        Assert.assertEquals("Result should be the same", expectedIfBlock, actualIfBlock);

        QLParser qlParser3 = this.parser.createQLParser(validSrc);
        String actualForm = qlParser3.form().tree.toStringTree();
        System.out.println(actualForm);
        final String expectedForm = "(FORM test (BLOCK (ASSIGNMENT Did you sell a house in 2010? hasSoldHouse boolean) (ASSIGNMENT Did you sell a car in 2010? hasSoldCar integer) (IF (EXPRESSION (== (+ 1 1) 2) (BLOCK (ASSIGNMENT nothing? hasNothing boolean) (ASSIGNMENT really nothing? hasNothing2 boolean))) (EXPRESSION (BLOCK (ASSIGNMENT nothing? hasNothing boolean))))))";
        Assert.assertEquals("Result should be the same", expectedForm, actualForm);

        final QLParser qlParser4 = this.parser.createQLParser(validSrc);
        final CommonTree commonTree = (CommonTree) qlParser4.form().getTree();
        final CommonTreeNodeStream commonTreeNodeStream = new CommonTreeNodeStream(commonTree);
        final QLTreeWalker qlTreeWalker = new QLTreeWalker(commonTreeNodeStream);
        qlTreeWalker.walk();

        final QLParser qlParser5 = this.parser.createQLParser("(1+15)+20");
        final CommonTree commonTree1 = (CommonTree) qlParser5.expression().getTree();
        final CommonTreeNodeStream commonTreeNodeStream1 = new CommonTreeNodeStream(commonTree1);
        final QLTreeWalker qlTreeWalker1 = new QLTreeWalker(commonTreeNodeStream1);
        QLTreeWalker.expression_return expression = qlTreeWalker1.expression();
        System.out.println("walk = " + expression.node.evaluate(new HashMap<IdentifierNode, Value>()));
    }

    @Test
    public void invalidIfStatementTest() throws RecognitionException
    {
        final String invalidSrc =
                "	if (1 + true == 3) " +
                        "	{ " +
                        "		\"nothing?\" hasNothing: boolean " +
                        "	}";
        testIfStatement(invalidSrc, false, 1);
    }

    @Test
    public void validIfStatementTest() throws RecognitionException
    {
        final String validSrc =
                "	if (1 + 2 == 3) " +
                        "	{ " +
                        "		\"nothing?\" hasNothing: boolean " +
                        "	}";
        testIfStatement(validSrc, true, 0);
    }

    private void testIfStatement(final String source, final boolean expectedValidationResult, final int errorMessageSize) throws RecognitionException
    {
        final QLParser qlParser = this.parser.createQLParser(source);
        final CommonTree commonTree = (CommonTree) qlParser.ifStatement().getTree();
        final CommonTreeNodeStream commonTreeNodeStream = new CommonTreeNodeStream(commonTree);
        final QLTreeWalker qlTreeWalker = new QLTreeWalker(commonTreeNodeStream);
        IfNode ifNode = qlTreeWalker.ifStatement().node;
        List<IfNode.Branch> branches = ifNode.getBranches();
        final List<Message> messages = new ArrayList<>();
        for(IfNode.Branch branch : branches)
        {
            final boolean validate = branch.getExprNode().validate(messages);
            Assert.assertEquals("The result should be the same", expectedValidationResult, validate);
        }

        Assert.assertEquals("The result should be the same", errorMessageSize, messages.size());
    }

    @Test(expected = ParserException.class)
    public void simpleInvalidFormTest()
    {
        // missing the keyword 'form'
        this.parser.parseForm("" +
                "test " +
                "{ " +
                "	hasSoldHouse: \"Did you sell a house in 2010?\" boolean " +
                "	hasSoldCar: \"Did you sell a car in 2010?\" integer " +
                "	if (1+1==2) " +
                "	{ " +
                "		hasNothing: \"nothing?\" boolean " +
                "	}" +
                "	else" +
                "	{" +
                "		hasNothing: \"nothing?\" boolean " +
                "	}" +
                "}");
    }
}
