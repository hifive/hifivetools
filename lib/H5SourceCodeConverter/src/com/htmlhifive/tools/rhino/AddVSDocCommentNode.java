/*
 * Copyright (C) 2012 NS Solutions Corporation
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mozilla.javascript.Token.CommentType;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.Comment;
import org.mozilla.javascript.ast.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ツリー上、適切な位置に、コメントノードを挿入する.
 */
public class AddVSDocCommentNode implements AddCommentNode {

	private static Logger logger = LoggerFactory.getLogger(AddVSDocCommentNode.class);

	/** コメントノード. */
	private final List<Comment> comments = new ArrayList<Comment>();

	/**
	 * コンストラクタ.
	 *
	 * @param comments
	 */
	public AddVSDocCommentNode(Collection<Comment> comments) {

		if (comments != null) {
			this.comments.addAll(comments);
		}
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see com.htmlhifive.tools.rhino.AddCommentNode#setup(org.mozilla.javascript.ast.AstNode)
	 */
	@Override
	public void setup(AstNode root) {

		if (comments.size() == 0) {
			return;
		}
		NodeVisitor visitor = new NodeVisitor(root);
		AstNode node = null;
		boolean continueFlg = false;
		for (Iterator<Comment> iterator = comments.iterator(); iterator.hasNext();) {
			Comment comment = iterator.next();
			logger.debug("--------------------------------------");
			// コメントノードが二つ以上つづいてる状態を表すフラグ.
			while (continueFlg || (node = visitor.next()) != null) {
				int absolutePosition = node.getAbsolutePosition();
				int commentAbsolutePosition = comment.getAbsolutePosition();
				if (commentAbsolutePosition >= absolutePosition || node instanceof Comment) {
					continueFlg = false;
					continue;
				} else if (comment.getCommentType() != CommentType.JSDOC) {
					logger.debug("comment value : " + comment.getValue());
					logger.debug("parent before :	" + comment.getParent().shortName());
					Util.addBefore(comment, node);
					if (commentAbsolutePosition == absolutePosition) {
						if ((node instanceof Word) && (Constants.LINE_SEPARATOR.equals(((Word) node).getValue()))) {
							node.getParent().removeChild(node);
						}
					}
					logger.debug("parent after :	" + comment.getParent().shortName());
					continueFlg = true;
					break;
				} else {
					FunctionBodyVisitor bodyVisitor = new FunctionBodyVisitor(node);
					Word startWord = bodyVisitor.getStartWord();
					Block block = bodyVisitor.getFunctionBody();
					if (block == null) {
						// TODO いずれはオブジェクトのVSDocコメントも。。。
						break;
					}
					comment.setParent(node);
					block.addChildAfter(comment, startWord);
					break;
				}
			}
		}

	}
}
