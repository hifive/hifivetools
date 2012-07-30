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

import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.Word;

public class FunctionBodyVisitor implements NodeVisitor {

	private Block functionBody;

	private FunctionNode functionNode;

	private Word startWord;

	public FunctionBodyVisitor(AstNode node) {

		node.visit(this);
	}

	@Override
	public boolean visit(AstNode node) {

		if (node instanceof FunctionNode) {
			this.functionBody = (Block) ((FunctionNode) node).getBody();
			this.functionNode = (FunctionNode) node;
			functionBody.visit(new NodeVisitor() {

				@Override
				public boolean visit(AstNode node) {

					if (node instanceof Word && StringUtils.equals("{", StringUtils.strip(((Word) node).getValue()))) {
						if(startWord == null){
							startWord = (Word) node;
						}
						return false;
					}
					return true;
				}
			});
			return false;
		}
		return true;
	}

	public Block getFunctionBody() {

		return functionBody;
	}

	public FunctionNode getFunctionNode() {

		return functionNode;
	}

	public Word getStartWord() {

		return startWord;
	}

}
