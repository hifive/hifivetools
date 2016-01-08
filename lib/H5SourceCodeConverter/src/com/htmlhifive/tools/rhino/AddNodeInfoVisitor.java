/*
 * Copyright (C) 2012-2016 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.htmlhifive.tools.rhino;

import static com.htmlhifive.tools.rhino.Util.addChild;
import static com.htmlhifive.tools.rhino.Util.addChildren;
import static com.htmlhifive.tools.rhino.Util.addChildrenForVariables;
import static com.htmlhifive.tools.rhino.Util.isVSDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.ArrayComprehension;
import org.mozilla.javascript.ast.ArrayComprehensionLoop;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.CatchClause;
import org.mozilla.javascript.ast.Comment;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.EmptyExpression;
import org.mozilla.javascript.ast.EmptyStatement;
import org.mozilla.javascript.ast.ErrorNode;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Jump;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Label;
import org.mozilla.javascript.ast.LabeledStatement;
import org.mozilla.javascript.ast.LetNode;
import org.mozilla.javascript.ast.Loop;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.RegExpLiteral;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.ThrowStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;
import org.mozilla.javascript.ast.Word;
import org.mozilla.javascript.ast.XmlDotQuery;
import org.mozilla.javascript.ast.XmlElemRef;
import org.mozilla.javascript.ast.XmlExpression;
import org.mozilla.javascript.ast.XmlFragment;
import org.mozilla.javascript.ast.XmlLiteral;
import org.mozilla.javascript.ast.XmlMemberGet;
import org.mozilla.javascript.ast.XmlPropRef;
import org.mozilla.javascript.ast.XmlString;
import org.mozilla.javascript.ast.Yield;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ツリー構造をとる様に、ノードを追加する.
 */
public class AddNodeInfoVisitor implements NodeVisitor {

	private static Logger logger = LoggerFactory.getLogger(AddNodeInfoVisitor.class);

	/** 深さ. */
	protected int depth = 0;

	/** インデント. */
	protected int indent = 0;

	/** コンストラクタ. */
	public AddNodeInfoVisitor() {

	}

	@Override
	public boolean visit(AstNode node) {

		depth = Util.getPropValue(node, Constants.DEPTH);
		indent = Util.getPropValue(node, Constants.INDENT);
		// ---------------------------------------------------------------------
		if (node instanceof ArrayLiteral) {
			visit((ArrayLiteral) node);
		} else if (node instanceof Block) {
			visit((Block) node);
		} else if (node instanceof CatchClause) {
			visit((CatchClause) node);
		} else if (node instanceof Comment) {
			visit((Comment) node);
		} else if (node instanceof ConditionalExpression) {
			visit((ConditionalExpression) node);
		} else if (node instanceof ElementGet) {
			visit((ElementGet) node);
		} else if (node instanceof EmptyStatement){
			visit((EmptyStatement) node);
		} else if (node instanceof EmptyExpression) {
			visit((EmptyExpression) node);
		} else if (node instanceof ErrorNode) {
			visit((ErrorNode) node);
		} else if (node instanceof ExpressionStatement) {
			visit((ExpressionStatement) node);
		} else if (node instanceof NewExpression) {
			visit((NewExpression) node);
		} else if (node instanceof FunctionCall) {
			visit((FunctionCall) node);
		} else if (node instanceof IfStatement) {
			visit((IfStatement) node);
		}
		// ---------------------------------------------------------------------
		// else if (node instanceof Assignment) {
		// InfixExpressionと同様
		else if (node instanceof ObjectProperty) {
			visit((ObjectProperty) node);
		} else if (node instanceof PropertyGet) {
			visit((PropertyGet) node);
		} else if (node instanceof XmlDotQuery) {
			visit((XmlDotQuery) node);
		} else if (node instanceof XmlMemberGet) {
			visit((XmlMemberGet) node);
		} else if (node instanceof InfixExpression) {
			visit((InfixExpression) node);
		}
		// ---------------------------------------------------------------------
		else if (node instanceof BreakStatement) {
			visit((BreakStatement) node);
		} else if (node instanceof ContinueStatement) {
			visit((ContinueStatement) node);
		} else if (node instanceof Label) {
			visit((Label) node);
		} else if (node instanceof ArrayComprehension) {
			visit((ArrayComprehension) node);
		} else if (node instanceof LetNode) {
			visit((LetNode) node);
		} else if (node instanceof DoLoop) {
			visit((DoLoop) node);
		} else if (node instanceof ArrayComprehensionLoop) {
			visit((ArrayComprehensionLoop) node);
		} else if (node instanceof ForInLoop) {
			visit((ForInLoop) node);
		} else if (node instanceof ForLoop) {
			visit((ForLoop) node);
		} else if (node instanceof WhileLoop) {
			visit((WhileLoop) node);
		} else if (node instanceof AstRoot) {
			//
		} else if (node instanceof FunctionNode) {
			visit((FunctionNode) node);
		} else if (node instanceof SwitchStatement) {
			visit((SwitchStatement) node);
		} else if (node instanceof ScriptNode) {
			//
		} else if (node instanceof Scope) {
			visit((Scope) node);
		} else if (node instanceof Jump) {
			//
		} else if (node instanceof KeywordLiteral) {
			visit((KeywordLiteral) node);
		} else if (node instanceof LabeledStatement) {
			visit((LabeledStatement) node);
		} else if (node instanceof Name) {
			visit((Name) node);
		} else if (node instanceof NumberLiteral) {
			visit((NumberLiteral) node);
		} else if (node instanceof ObjectLiteral) {
			visit((ObjectLiteral) node);
		} else if (node instanceof ParenthesizedExpression) {
			visit((ParenthesizedExpression) node);
		} else if (node instanceof RegExpLiteral) {
			visit((RegExpLiteral) node);
		} else if (node instanceof ReturnStatement) {
			visit((ReturnStatement) node);
		} else if (node instanceof StringLiteral) {
			visit((StringLiteral) node);
		} else if (node instanceof SwitchCase) {
			visit((SwitchCase) node);
		} else if (node instanceof ThrowStatement) {
			visit((ThrowStatement) node);
		} else if (node instanceof TryStatement) {
			visit((TryStatement) node);
		} else if (node instanceof UnaryExpression) {
			visit((UnaryExpression) node);
		} else if (node instanceof VariableDeclaration) {
			visit((VariableDeclaration) node);
		} else if (node instanceof VariableInitializer) {
			visit((VariableInitializer) node);
		} else if (node instanceof WithStatement) {
			visit((WithStatement) node);
		} else if (node instanceof XmlExpression) {
			visit((XmlExpression) node);
		} else if (node instanceof XmlString) {
			visit((XmlString) node);
		} else if (node instanceof XmlLiteral) {
			visit((XmlLiteral) node);
		} else if (node instanceof XmlElemRef) {
			visit((XmlElemRef) node);
		} else if (node instanceof XmlPropRef) {
			visit((XmlPropRef) node);
		} else if (node instanceof Yield) {
			visit((Yield) node);
		} else if (node instanceof Word) {
			//
		} else {
			System.out.println(node.toSource());
			throw new IllegalArgumentException();

		}
		return true;
	}

	// -------------------------------------------------------------------------
	protected void visit(ArrayLiteral an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "[");
		pos = addChildren(an, pos, an.getElements(), depth);
		pos = addChild(an, an.getLength() - 1, "]");
	}

	protected void visit(Block an) {

		List<Node> children = new ArrayList<Node>();
		for (Node kid : an) {
			children.add(kid);
		}
		an.removeChildren();
		//
		int pos = addChild(an, 0, Util.makeIndent(indent)); // TODO
		pos = addChild(an, pos, "{" + Constants.LINE_SEPARATOR);
		int newDepth = depth + 1;
		for (Node child : children) {
			pos = addChild(an, (AstNode) child, newDepth, newDepth);
		}

		String indent = Util.makeIndent(depth);
		if (null != indent) {
			pos = addChild(an, an.getLength() - 2 - indent.length(), indent);
		}
		pos = addChild(an, an.getLength() - 2, "}" + Constants.LINE_SEPARATOR);
	}

	protected void visit(CatchClause an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "catch (");
		pos = addChild(an, an.getVarName(), depth);
		if (null != an.getCatchCondition()) {
			pos = addChild(an, pos, " if ");
			pos = addChild(an, an.getCatchCondition(), depth);
		}
		pos = addChild(an, an.getBody().getPosition() - 1, ") ");
		pos = addChild(an, an.getBody(), depth);
	}

	protected void visit(Comment an) {

		logger.debug("Comment Visit Start--------------------------------");
		Token.CommentType type = an.getCommentType();
		logger.debug("Comment type :" + type.toString());
		AstNode parent = an.getParent();
		logger.debug("parent node " + parent.shortName());
		int parentDepth = Util.getPropValue(parent, Constants.DEPTH);
		int pos = 0;
		if (type == Token.CommentType.JSDOC) {
			// 2012/4/18 修正
			if (an.getProp(Constants.VSDOC_FLAG) == null) {
				pos = addChild(an, pos, Constants.LINE_SEPARATOR);
			}
			// pos = addChild(an, pos, Util.makeIndent(parentDepth + 1));
		} else if (type == Token.CommentType.LINE) {

			// TODO かなり制御が煩雑.
			// prev -> コメントノードのひとつ前のノードの一番最後の子供
			AstNode prev = Util.getLast(Util.getPrev(an));
			if ((null == prev) || (prev.getLineno() != an.getLineno())) {
				// 直前ノードと、行コメントが別の行であれば、インデントを1足して出力.
				// pos = addChild(an, pos, Util.makeIndent(parentDepth + 1));
			} else {
				if (prev instanceof Word) {
					Word word = (Word) prev;
					String wordValue = word.getValue();
					logger.debug("Word value : " + wordValue);
					if (wordValue.trim().length() == 0) {
						if (wordValue.equals(Constants.LINE_SEPARATOR)) {
							while (true) {
								if (!(prev instanceof Word)
										|| (Constants.LINE_SEPARATOR.equals(((Word) prev).getValue()))) {
									break;
								}
								prev = Util.getLast(Util.getPrev(prev));
							}
							if ((prev.getLineno() == an.getLineno())) {
								// 元ファイル中、行コメントと、直前ノードが同じ行であれば、間の改行を取り除く.
								word.setValue(wordValue.substring(0, wordValue.length() - 1));
							}
						}
						// pos = addChild(an, pos, Util.makeIndent(1));
					} else if (wordValue.endsWith(Constants.LINE_SEPARATOR)) {
						word.setValue(wordValue.substring(0, wordValue.length() - 1));
					}
				}
			}
		}
		// 2012/4/18 修正
		// String document = StringUtils.isEmpty(an.getJsDoc()) ? an.getValue() : an.getJsDoc();
		// pos = addChild(an, pos, document);
		// pos = addChild(an, an.getLength() - 1, "\n");

		// JsDoc/VSDoc生成時切り替え機能未リリースのため一旦処理を簡素化 2013/05/31 by
		pos = addChild(an,  0,  an.getValue());
		Token.CommentType commentType = an.getCommentType();
		if (commentType == Token.CommentType.LINE || commentType == Token.CommentType.JSDOC) {
			addChild(an, pos, Constants.LINE_SEPARATOR);
		}

		/*
		String document = StringUtils.isEmpty(an.getJsDoc()) ? an.getValue() : an.getJsDoc();
		// 一行ずつ取得
		StringTokenizer st = new StringTokenizer(document, Constants.LINE_SEPARATOR);
		boolean first = true;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			logger.debug(token);
			if (!(parent instanceof AstRoot)) {
				// 親がAstRootだった場合は深さは0
				logger.debug("parentDepth" + parentDepth);
				pos = addChild(an, pos, Util.makeIndent(parentDepth + 1));
			}
			if (!first && Token.CommentType.JSDOC == an.getCommentType() && !isVSDoc(token)) {
				// 一行目以外は空白を入れる
				pos = addChild(an, pos, " ");
			}
			first = false;
			pos = addChild(an, pos, token);
			pos = addChild(an, pos, Constants.LINE_SEPARATOR);
		}

		*/
		logger.debug("Comment Visit end--------------------------------");
	}

	protected void visit(ConditionalExpression an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, an.getTestExpression(), depth);
		pos = addChild(an, pos, " ? ");
		pos = addChild(an, an.getTrueExpression(), depth);
		pos = addChild(an, pos, " : ");
		pos = addChild(an, an.getFalseExpression(), depth);
	}

	protected void visit(ElementGet an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, an.getTarget(), depth);
		pos = addChild(an, pos, "[");
		pos = addChild(an, an.getElement(), depth);
		pos = addChild(an, an.getLength() - 1, "]");
	}

	protected void visit(EmptyExpression an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
	}

	protected void visit(EmptyStatement an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
	}

	protected void visit(ErrorNode an) {

		int pos = addChild(an, 0, an.getMessage());
	}

	protected void visit(ExpressionStatement an) {

		int pos = addChild(an, an.getExpression(), depth, indent);
		pos = addChild(an, an.getLength() - 2, ";" + Constants.LINE_SEPARATOR);
	}

	protected void visit(NewExpression an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "new ");
		pos = addChild(an, an.getTarget(), depth);
		pos = addChild(an, pos, "(");
		pos = addChildren(an, pos, an.getArguments(), depth);
		pos = addChild(an, pos, ")");
		if (null != an.getInitializer()) {
			pos = addChild(an, pos, " ");
			pos = addChild(an, an.getInitializer(), depth);
		}
	}

	protected void visit(FunctionCall an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, an.getTarget(), depth);
		pos = addChild(an, pos, "(");
		pos = addChildren(an, pos, an.getArguments(), depth);
		pos = addChild(an, an.getLength() - 1, ")");
	}

	protected void visit(IfStatement an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "if (");
		pos = addChild(an, an.getCondition(), depth);
		pos = addChild(an, pos, ")");

		AstNode thenPart = an.getThenPart();
		if (!(thenPart instanceof Block)) {
			pos = addChild(an, pos, " ");
		}
		pos = addChild(an, thenPart, depth, 0, true);
		AstNode elsePart = an.getElsePart();
		if (elsePart instanceof IfStatement) { // TODO elseパートの扱い要確認
			pos = addChild(an, pos, " else ");
			pos = addChild(an, elsePart, depth, 0, true);
		} else if (null != elsePart) {
			pos = addChild(an, pos, " else ");
			pos = addChild(an, elsePart, depth, 0, true);
		}
		pos = addChild(an, an.getLength() - 1, Constants.LINE_SEPARATOR);
	}

	protected void visit(Assignment an) {

		// InfixExpressionと同様
	}

	protected void visit(ObjectProperty an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		if (an.isGetter()) {
			pos = addChild(an, pos, "get ");
		} else if (an.isSetter()) {
			pos = addChild(an, pos, "set ");
		}
		pos = addChild(an, an.getLeft(), depth);
		if (an.getType() == Token.COLON) {
			pos = addChild(an, pos, ": ");
		}
		pos = addChild(an, an.getRight(), depth);
	}

	protected void visit(PropertyGet an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, an.getLeft(), depth);
		pos = addChild(an, pos, ".");
		pos = addChild(an, an.getRight(), depth);
	}

	protected void visit(XmlDotQuery an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, an.getLeft(), depth);
		pos = addChild(an, pos, ".(");
		pos = addChild(an, an.getRight(), depth);
		pos = addChild(an, an.getLength() - 1, ")");
	}

	protected void visit(XmlMemberGet an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, an.getLeft(), depth);
		pos = addChild(an, pos, AstNode.operatorToString(an.getType()));
		pos = addChild(an, an.getRight(), depth);
	}

	protected void visit(InfixExpression an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, an.getLeft(), depth);
		pos = addChild(an, pos, " ");
		pos = addChild(an, pos, AstNode.operatorToString(an.getType()));
		pos = addChild(an, pos, " ");
		pos = addChild(an, an.getRight(), depth);
	}

	protected void visit(BreakStatement an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "break");
		Name breakLabel = an.getBreakLabel();
		if (null != breakLabel) {
			pos = addChild(an, pos, " ");
			pos = addChild(an, breakLabel, depth);
		}
		pos = addChild(an, an.getLength() - 2, ";" + Constants.LINE_SEPARATOR);
	}

	protected void visit(ContinueStatement an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "continue");
		Name label = an.getLabel();
		if (null != label) {
			pos = addChild(an, pos, " ");
			pos = addChild(an, label, depth);
		}
		pos = addChild(an, an.getLength() - 2, ";" + Constants.LINE_SEPARATOR);
	}

	protected void visit(Label an) {

		int pos = addChild(an, 0, Util.makeIndent(depth));
		pos = addChild(an, pos, an.getName());
		pos = addChild(an, an.getLength() - 2, ":" + Constants.LINE_SEPARATOR);
	}

	protected void visit(ArrayComprehension an) {

		int pos = addChild(an, 0, "[");
		pos = addChild(an, an.getResult(), depth);
		for (ArrayComprehensionLoop loop : an.getLoops()) {
			pos = addChild(an, loop, depth);
		}
		if (null != an.getFilter()) {
			pos = addChild(an, pos, " if (");
			pos = addChild(an, an.getFilter(), depth);
			pos = addChild(an, pos, ")");
		}
		pos = addChild(an, an.getLength() - 1, "]");
	}

	protected void visit(LetNode an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "let (");
		pos = addChild(an, an.getVariables(), depth);
		pos = addChild(an, pos, ")");
		if (null != an.getBody()) {
			pos = addChild(an, an.getBody(), depth);
		}
	}

	protected void visit(DoLoop an) {

		int pos = addChild(an, 0, "do ");
		pos = addChild(an, an.getBody(), depth, 0, true);
		pos = addChild(an, pos, " while (");
		pos = addChild(an, an.getCondition(), depth);
		pos = addChild(an, an.getLength() - 3, ");" + Constants.LINE_SEPARATOR);
	}

	protected void visit(ArrayComprehensionLoop an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, " for (");
		pos = addChild(an, an.getIterator(), depth);
		pos = addChild(an, pos, " in ");
		pos = addChild(an, an.getIteratedObject(), depth);
		pos = addChild(an, an.getLength() - 1, ")");
	}

	protected void visit(ForInLoop an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "for ");
		if (an.isForEach()) {
			pos = addChild(an, pos, "each ");
		}
		pos = addChild(an, pos, "(");
		pos = addChild(an, an.getIterator(), depth);
		pos = addChild(an, pos, " in ");
		pos = addChild(an, an.getIteratedObject(), depth);
		pos = addChild(an, pos, ") ");
		if (an.getBody() instanceof Block) {
			pos = addChild(an, an.getBody(), depth, 0, true);
			pos = addChild(an, an.getLength() - 1, Constants.LINE_SEPARATOR);
		} else {
			pos = addChild(an, an.getBody(), depth);// TODO 調整中(depth + 1)
		}
	}

	protected void visit(ForLoop an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "for (");

		AstNode initializer = an.getInitializer();
		initializer.putProp(Constants.IN_FOR_LOOP, true);
		pos = addChild(an, initializer, depth);
		pos = addChild(an, pos, "; ");
		pos = addChild(an, an.getCondition(), depth);
		pos = addChild(an, pos, "; ");
		pos = addChild(an, an.getIncrement(), depth);
		pos = addChild(an, pos, ") ");
		if (an.getBody() instanceof Block) { // TODO インデントが1つ多い?
			pos = addChild(an, an.getBody(), depth, 0, true);
			pos = addChild(an, an.getLength() - 1, Constants.LINE_SEPARATOR);
		} else {
			pos = addChild(an, an.getBody(), depth); // TODO 調整中(depth + 1)
		}
	}

	protected void visit(WhileLoop an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "while (");
		pos = addChild(an, an.getCondition(), depth);
		pos = addChild(an, pos, ") ");
		if (an.getBody() instanceof Block) {
			pos = addChild(an, an.getBody(), depth, 0, true);
			pos = addChild(an, an.getLength() - 1, Constants.LINE_SEPARATOR);
		} else {
			pos = addChild(an, pos, Constants.LINE_SEPARATOR);
			pos = addChild(an, pos, Util.makeIndent(depth));
			pos = addChild(an, an.getBody(), depth + 1);
		}
	}

	protected void visit(AstRoot an) {

		//
	}

	protected void visit(FunctionNode an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "function");
		if (an.getFunctionName() != null) {
			pos = addChild(an, pos, " ");
			pos = addChild(an, an.getFunctionName(), depth);
		}
		if (an.getParams() == null) {
			pos = addChild(an, pos, "() ");
		} else {
			pos = addChild(an, pos, "(");
			pos = addChildren(an, pos, an.getParams(), depth);
			pos = addChild(an, pos, ") ");
		}
		if (an.isExpressionClosure()) {
			pos = addChild(an, pos, " ");
			pos = addChild(an, an.getBody(), depth);
		} else {
			pos = addChild(an, an.getBody(), depth, 0, true);// TODO インデント調整中
		}
		if (an.getFunctionType() == FunctionNode.FUNCTION_STATEMENT) {
			pos = addChild(an, an.getLength() - 1, Constants.LINE_SEPARATOR);
		}
	}

	protected void visit(SwitchStatement an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "switch (");
		pos = addChild(an, an.getExpression(), depth);
		pos = addChild(an, pos, ") {" + Constants.LINE_SEPARATOR);
		int newDepth = depth + 1;
		for (SwitchCase sc : an.getCases()) {
			pos = addChild(an, sc, newDepth, newDepth);
		}
		String indent = Util.makeIndent(depth);
		if (null != indent) {
			pos = addChild(an, an.getLength() - 2 - indent.length(), indent);
		}
		pos = addChild(an, an.getLength() - 2, "}" + Constants.LINE_SEPARATOR);
	}

	protected void visit(ScriptNode an) {

		//
	}

	protected void visit(Scope an) {

		List<Node> chidlren = new ArrayList<Node>();
		for (Node kid : an) {
			chidlren.add(kid);
		}
		an.removeChildren();

		int pos = addChild(an, 0, Util.makeIndent(indent));// TODO
		pos = addChild(an, pos, "{" + Constants.LINE_SEPARATOR);
		int newDepth = depth + 1;
		for (Node child : chidlren) {
			pos = addChild(an, (AstNode) child, newDepth, newDepth);
		}

		String indent = Util.makeIndent(depth);
		if (null != indent) {
			pos = addChild(an, an.getLength() - 2 - indent.length(), indent);
		}
		pos = addChild(an, an.getLength() - 2, "}" + Constants.LINE_SEPARATOR);
	}

	protected void visit(Jump an) {

		//
	}

	protected void visit(KeywordLiteral an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		switch (an.getType()) {
			case Token.THIS:
				pos = addChild(an, pos, "this");
				break;
			case Token.NULL:
				pos = addChild(an, pos, "null");
				break;
			case Token.TRUE:
				pos = addChild(an, pos, "true");
				break;
			case Token.FALSE:
				pos = addChild(an, pos, "false");
				break;
			case Token.DEBUGGER:
				pos = addChild(an, pos, "debugger");
				break;
			default:
				throw new IllegalStateException("Invalid keyword literal type: " + an.getType());
		}
	}

	protected void visit(LabeledStatement an) {

		int pos = 0;
		for (Label label : an.getLabels()) {
			pos = addChild(an, label, depth);
		}
		pos = addChild(an, an.getStatement(), depth + 1);
	}

	protected void visit(Name an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		if (an.getIdentifier() == null) {
			pos = addChild(an, pos, "<null>");
		} else {
			pos = addChild(an, pos, an.getIdentifier());
		}
	}

	protected void visit(NumberLiteral an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		if (an.getValue() == null) {
			pos = addChild(an, pos, "<null>");
		} else {
			pos = addChild(an, pos, an.getValue());
		}
	}

	protected void visit(ObjectLiteral an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "{");
		if (an.getElements() != null) {
			pos = addChildren(an, pos, an.getElements(), depth, true);
		}
		pos = addChild(an, an.getLength() - 1, "}");
	}

	protected void visit(ParenthesizedExpression an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "(");
		pos = addChild(an, an.getExpression(), depth);
		pos = addChild(an, an.getLength() - 1, ")");
	}

	protected void visit(RegExpLiteral an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "/");
		pos = addChild(an, pos, an.getValue());
		pos = addChild(an, pos, "/");
		if (an.getFlags() != null) {
			pos = addChild(an, pos, an.getFlags());
		}
	}

	protected void visit(ReturnStatement an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "return");
		if (an.getReturnValue() != null) {
			// pos = addChild(an, pos, " ");
			pos = addChild(an, 6, " ");
			pos = addChild(an, an.getReturnValue(), depth);
		}
		pos = addChild(an, an.getLength() - 2, ";" + Constants.LINE_SEPARATOR);
	}

	protected void visit(StringLiteral an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		char quoteCharacter = an.getQuoteCharacter();
		pos = addChild(an, pos, String.valueOf(quoteCharacter));
		String value = an.getValue();
		value = value.replaceAll("\\\\", "\\\\\\\\");
		value = value.replaceAll("\\n", "\\\\n");
		value = value.replaceAll("\\r", "\\\\r");
		// value = value.replaceAll("\\\"", "\\\\\"");
		// value = value.replaceAll("\\\'", "\\\\\'");
		// value = value.replaceAll("\\/", "\\\\/");
		if ('\'' == quoteCharacter) {
			value = value.replaceAll("\\'", "\\\\'");
		} else if ('"' == quoteCharacter) {
			value = value.replaceAll("\\\"", "\\\\\"");
		}

		pos = addChild(an, pos, value);
		pos = addChild(an, an.getLength() - 1, String.valueOf(quoteCharacter));
	}

	protected void visit(SwitchCase an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		if (an.getExpression() == null) {
			pos = addChild(an, pos, "default:" + Constants.LINE_SEPARATOR);
		} else {
			pos = addChild(an, pos, "case ");
			pos = addChild(an, an.getExpression(), depth);
			pos = addChild(an, pos, ":" + Constants.LINE_SEPARATOR);
		}
		if (an.getStatements() != null) {
			int newDepth = depth + 1;
			for (AstNode s : an.getStatements()) {
				pos = addChild(an, s, newDepth, newDepth);
			}
		}
	}

	protected void visit(ThrowStatement an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "throw");
		pos = addChild(an, pos, " ");
		pos = addChild(an, an.getExpression(), depth);
		pos = addChild(an, an.getLength() - 2, ";" + Constants.LINE_SEPARATOR);
	}

	protected void visit(TryStatement an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "try ");
		pos = addChild(an, an.getTryBlock(), depth, 0);
		for (CatchClause cc : an.getCatchClauses()) {
			pos = addChild(an, cc, depth, depth);
		}
		if (an.getFinallyBlock() != null) {
			pos = addChild(an, pos, " finally ");
			pos = addChild(an, an.getFinallyBlock(), depth);
		}
	}

	protected void visit(UnaryExpression an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		int type = an.getType();
		String operator = AstNode.operatorToString(type);
		if (!an.isPostfix()) {
			pos = addChild(an, pos, operator);
			if (type == Token.TYPEOF || type == Token.DELPROP) {
				pos = addChild(an, pos, " ");
			}
		}
		pos = addChild(an, an.getOperand(), depth);
		if (an.isPostfix()) {
			pos = addChild(an, pos, operator);
		}
	}

	protected void visit(VariableDeclaration an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, Token.typeToName(an.getType()).toLowerCase());
		pos = addChild(an, pos, " ");

		boolean lnFlag = true;
		Object flagObj = an.getProp(Constants.IN_FOR_LOOP);
		if ((null != flagObj) && (Boolean.valueOf(flagObj.toString()))) {
			lnFlag = false;
		}
		pos = addChildrenForVariables(an, pos, an.getVariables(), depth, lnFlag);
		if (!(an.getParent() instanceof Loop)) {
			pos = addChild(an, an.getLength() - 2, ";" + Constants.LINE_SEPARATOR);
		}
	}

	protected void visit(VariableInitializer an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, an.getTarget(), depth);
		if (null != an.getInitializer()) {
			pos = addChild(an, pos, " = ");
			pos = addChild(an, an.getInitializer(), depth);
		}
	}

	protected void visit(WithStatement an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "with (");
		pos = addChild(an, an.getExpression(), depth);
		pos = addChild(an, pos, ") ");
		pos = addChild(an, an.getStatement(), depth + 1);
		if (!(an.getStatement() instanceof Block)) {
			pos = addChild(an, an.getLength() - 2, ";" + Constants.LINE_SEPARATOR);
		}
	}

	protected void visit(XmlExpression an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, "{");
		pos = addChild(an, an.getExpression(), depth);
		pos = addChild(an, an.getLength() - 1, "}");
	}

	protected void visit(XmlString an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		pos = addChild(an, pos, an.getXml());
	}

	protected void visit(XmlLiteral an) {

		int pos = 0;
		for (XmlFragment frag : an.getFragments()) {
			pos = addChild(an, frag, depth);
		}
	}

	protected void visit(XmlElemRef an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		if (an.isAttributeAccess()) {
			pos = addChild(an, pos, "@");
		}
		if (an.getNamespace() != null) {
			pos = addChild(an, an.getNamespace(), depth);
			pos = addChild(an, pos, "::");
		}
		pos = addChild(an, pos, "[");
		pos = addChild(an, an.getExpression(), depth);
		pos = addChild(an, an.getLength() - 1, "]");
	}

	protected void visit(XmlPropRef an) {

		int pos = addChild(an, 0, Util.makeIndent(indent));
		if (an.isAttributeAccess()) {
			pos = addChild(an, pos, "@");
		}
		if (an.getNamespace() != null) {
			pos = addChild(an, an.getNamespace(), depth);
			pos = addChild(an, pos, "::");
		}
		pos = addChild(an, an.getPropName(), depth);
	}

	protected void visit(Yield an) {

		int pos = addChild(an, 0, "yield");
		if (an.getValue() != null) {
			pos = addChild(an, an.getValue(), depth);
		}
	}

	protected void visit(Word an) {

		//
	}

}
