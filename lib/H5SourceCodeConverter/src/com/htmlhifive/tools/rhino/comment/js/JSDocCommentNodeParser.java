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

package com.htmlhifive.tools.rhino.comment.js;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.htmlhifive.tools.rhino.Constants;
import com.htmlhifive.tools.rhino.comment.RelationNodeType;
import com.htmlhifive.tools.rhino.comment.TagType;
import com.htmlhifive.tools.rhino.comment.Token;
import com.htmlhifive.tools.rhino.comment.TokenType;
import com.htmlhifive.tools.rhino.comment.TokenUtil;

public class JSDocCommentNodeParser {

	private RelationNodeType docType;

	private List<Token> tokenList;

	public JSDocCommentNodeParser(RelationNodeType docType) {

		this.docType = docType;
		tokenList = new ArrayList<Token>();
	}

	public JSDocRoot parse(String comment) {

		StringTokenizer lineSeparator = new StringTokenizer(comment, "\n\r");
		int currentLineNum = 0;
		int currentIndexNum = 0;
		tokenList = new ArrayList<Token>();
		while (lineSeparator.hasMoreTokens()) {
			// コメント一行分取得
			String lineStr = lineSeparator.nextToken();
			// 一行分をトークンに分割
			StringTokenizer tokenSeparator = new StringTokenizer(lineStr);

			while (tokenSeparator.hasMoreTokens()) {
				// tokenを取得
				String tokenStr = tokenSeparator.nextToken();
				Token token = new Token(TokenUtil.resolveType(tokenStr), tokenStr);
				// tokenの行番号を設定
				token.setLineNum(currentLineNum);
				// tokenの該当行からの文字位置を設定
				token.setIndexNum(currentIndexNum);
				tokenList.add(token);
				currentIndexNum = currentIndexNum + tokenStr.length() + 1;
			}
			currentIndexNum = 0;
			currentLineNum++;
		}

		return resolveJsDoc();
	}

	private JSDocRoot resolveJsDoc() {

		// 解析に関係しているタグ(@~~)
		JSTag currentTag = JSTag.ROOT;
		// 前のTokenのタイプ
		TokenType previousTokenType = TokenType.START;
		JSDocRoot root = new JSDocRoot(docType);
		// 一時保管ノード.このノードがツリーに追加される.
		JSTagNode tempNode = root;
		int currentLineNum = 0;
		// 現在の説明構築用StringBuilder
		StringBuilder currentDesc = new StringBuilder();
		// JsDocコメントの説明部分.
		String desc = null;
		for (Token token : tokenList) {
			if (currentDesc.length() != 0 && !TokenUtil.isSymbolType(token.getType())) {
				if (token.getLineNum() != currentLineNum) {
					// 行が変わっていたら改行コードを追加
					currentDesc.append(Constants.LINE_SEPARATOR);
					currentLineNum = token.getLineNum();
				} else {
					// 改行されていないToken間は空白を追加.
					currentDesc.append(" ");
				}
			}
			switch (token.getType()) {
				case STRING_LITERAL:
					resolveStringLiteral(token, currentTag, previousTokenType, tempNode, currentDesc);
					break;
				case ANNOTATION:
					if (tempNode instanceof JSDocRoot) {
						// 一時ノードがルートだった場合はdescはJSDocの説明部分なのでdescに格納
						desc = currentDesc.toString();
					} else if (tempNode instanceof JSSinglePartTagNode) {
						// パートが一つ以上あるタグには値を追加.
						((JSSinglePartTagNode) tempNode).setValue(StringUtils.chomp(currentDesc.toString()));
					}
					if (tempNode != root) {
						root.addTagNode(tempNode);
					}
					currentDesc = new StringBuilder();
					// タグの書き換え.
					JSTag temp = TokenUtil.resolveTagType(token);
					if (temp != null) {
						currentTag = temp;
					}
					// 一時ノードの書き換え.
					tempNode = resolveTagNode(currentTag);
					break;
				case TYPE:
					if (currentTag == null) {
						// タグの下ではなかったら説明に追加
						currentDesc.append(token.getValue());
					}
					if (tempNode instanceof JSTypePartNode) {
						// Typeを保持するノードの場合はタイプを一時nodeにセット
						((JSTypePartNode) tempNode).setType(StringUtils.strip(token.getValue(), "{}"));
					}
					break;
				case END:
					if (tempNode instanceof JSSinglePartTagNode) {
						// パートが一つ以上あるタグには値を追加.
						((JSSinglePartTagNode) tempNode).setValue(currentDesc.toString());
					}
					root.addTagNode(tempNode);
					break;
				case START:
				case SYMBOL:
				default:
					break;
			}
			previousTokenType = token.getType();
		}
		// ルートに説明をセット.
		root.setDescription(desc);
		return root;
	}

	/**
	 * token解析中の処理.現在のトークンが文字列だった場合の分岐処理.
	 *
	 * @param token 現在のトークン.
	 * @param currentTag 現在の係っているタグ
	 * @param previousTokenType ひとつ前のトークンタイプ.
	 * @param tempNode 一時ノード.
	 * @param desc 説明.
	 */
	private void resolveStringLiteral(Token token, JSTag currentTag, TokenType previousTokenType, JSTagNode tempNode,
			StringBuilder desc) {

		switch (currentTag) {
			case PARAM:
			case PROPERTY:
				// 現在のかかわりのタグがParamかプロパティの場合はtempNodeはTypeNamePartNode
				switch (previousTokenType) {
					case TYPE:
						// 前のトークンが型だった場合はその次のトークンは変数名.
						if (tempNode instanceof JSTypeNamePartNode) {
							((JSTypeNamePartNode) tempNode).setName(token.getValue());
						}
						return;
					default:
						break;
				}
			default:
				// タグの場合はタグデスクプションに追加.
				desc.append(token.getValue());
				break;
		}

	}

	private JSTagNode resolveTagNode(JSTag currentTag) {

		if (ArrayUtils.contains(TagType.NO_PART_TAG.getJsTag(), currentTag)) {
			return new JSNoPartTagNode(currentTag);
		} else if (ArrayUtils.contains(TagType.SINGLE_PART_TAG.getJsTag(), currentTag)) {
			return new JSSinglePartTagNode(currentTag);
		} else if (ArrayUtils.contains(TagType.OTHER_PARAM_TAG.getJsTag(), currentTag)) {
			switch (currentTag) {
				case PARAM:
				case PROPERTY:
					return new JSTypeNamePartNode(currentTag);

				case THROWS:
				case RETURNS:
					return new JSTypePartNode(currentTag);
				default:
					break;
			}
			// 上記以外のタグはSinglePartTagを返す
			return new JSSinglePartTagNode(currentTag);
		}
		return null;
	}
}
