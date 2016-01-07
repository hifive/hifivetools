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
import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Comment;
import org.mozilla.javascript.ast.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ツリー上、適切な位置に、コメントノードを挿入する.
 */
public class AddJSDocCommentNode implements AddCommentNode {

	private static Logger logger = LoggerFactory.getLogger(AddJSDocCommentNode.class);

	/** コメントノード. */
	private final List<Comment> comments = new ArrayList<Comment>();

	/**
	 * コンストラクタ.
	 *
	 * @param comments
	 */
	public AddJSDocCommentNode(Collection<Comment> comments) {

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
		int index = 0;
		AstNode node;
		Comment comment = comments.get(index);
		while ((node = visitor.next()) != null) {

			// System.out.print(Util.printNode(node));
			int absolutePosition = node.getAbsolutePosition();
			while (true) {
				int commentAbsolutePosition = comment.getAbsolutePosition();
				if (commentAbsolutePosition <= absolutePosition) {

					// System.out.println(comment.getAbsolutePosition());
					// System.out.println(node.getAbsolutePosition());
					// System.out.println(((AstNode)node.getNext()).getAbsolutePosition());

					logger.debug("-------------------------------------");
					logger.debug("comment value : " + comment.getValue());
					logger.debug("parent before :	" + comment.getParent().shortName());
					Util.addBefore(comment, node);
					if (commentAbsolutePosition == absolutePosition) {
						if ((node instanceof Word) && (Constants.LINE_SEPARATOR.equals(((Word) node).getValue()))) {
							node.getParent().removeChild(node);
						}
					}
					logger.debug("parent after :	" + comment.getParent().shortName());
					if (++index == comments.size()) {
						return;
					}
					comment = comments.get(index);
				} else {
					break;
				}
			}
		}
	}

}
