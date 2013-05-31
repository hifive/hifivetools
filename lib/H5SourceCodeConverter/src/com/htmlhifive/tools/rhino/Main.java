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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.SortedSet;
import java.util.StringTokenizer;


import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token.CommentType;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.htmlhifive.tools.rhino.comment.JSDocVSDocConverter;
import com.htmlhifive.tools.rhino.comment.RelationNodeType;
import com.htmlhifive.tools.rhino.comment.js.JSDocCommentNodeParser;
import com.htmlhifive.tools.rhino.comment.js.JSDocRoot;
import com.htmlhifive.tools.rhino.comment.vs.VSCommentBuilder;
import com.htmlhifive.tools.rhino.comment.vs.VSDocRoot;

/**
 *
 */
public class Main {

	private Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		boolean printTree = false;
		String[] inPaths = new String[] {//
				//
				"js/h5.tmp.js",//
				"js/h5.tmp2.js",//
				"js/h5.js",//
				"js/h5.dev.js",//
				"js/h5.async.js",//
				"js/jquery-codeAssist.js",
				"js/jquery-1.7.js",//
				"js/jquery-1.6.2-vsdoc.js",//
				"js/jquery.mobile-1.0.js",//
				"js/test.js"
				};

		Main sample = new Main();

		for (String path : inPaths) {
			try {
				String dstPath = getOutPath(path);
				sample.execute(path, dstPath, printTree);
				sample.execute(path, getOutPath(dstPath), printTree, DocType.VSDOC);
				// sample.execute(dstPath, getOutPath(dstPath), false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void execute(String path, String dstPath, boolean printTree) throws IOException {

		this.execute(path, dstPath, printTree, DocType.JSDOC);

	}

	/**
	 *
	 * @param srcPath
	 * @param dstPath
	 * @param printTree
	 * @throws IOException
	 */
	public void execute(String srcPath, String dstPath, boolean printTree, DocType docType) throws IOException {

		Reader sourceReader = new FileReader(new File(srcPath));
		String sourceName = srcPath;

		File out = new File(dstPath);
		AstRoot astRoot = parse(sourceReader, sourceName, docType);
		// System.out.println(astRoot.toSource());
		logger.info("[" + sourceName + "]-------------------------");
		// System.out.println("[" + sourceName + "]-------------------------");
		// String source = SourceMaker.toSource(astRoot);
		// System.out.println(source);
		Util.dumpToFile(astRoot, out);
		// System.out.println(astRoot.toSource());

		if (printTree) {
			logger.info("-------------------------");
			logger.debug(Util.printTree(astRoot));
		}

	}

	/**
	 * *
	 *
	 * @param src
	 * @param srcName
	 * @return
	 * @throws IOException
	 */
	public AstRoot parse(String src, String srcName) throws IOException {

		return parse(src, null, srcName, DocType.JSDOC);
	}

	/**
	 * *
	 *
	 * @param src
	 * @param srcName
	 * @return
	 * @throws IOException
	 */
	public AstRoot parse(String src, String srcName, DocType docType) throws IOException {

		return parse(src, null, srcName, docType);
	}

	/* *
	 *
	 * @param srcReader
	 *
	 * @param srcName
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	public AstRoot parse(Reader srcReader, String srcName, DocType docType) throws IOException {

		return parse(null, srcReader, srcName, docType);
	}

	// /**
	// * @param content
	// * @param srcReader
	// * @param srcName
	// * @param docType
	// * @return
	// * @throws IOException
	// */
	// public AstRoot parse(String src, Reader srcReader, String srcName,
	// DocType doctype) throws IOException {
	// return parse(src, srcReader, srcName, doctype, System.out);
	// }

	/**
	 * @param content
	 * @param srcReader
	 * @param srcName
	 * @param docType
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public AstRoot parse(String src, Reader srcReader, String srcName, DocType doctype) throws IOException {

		Context context = Context.enter();
		try {
			context.setOptimizationLevel(-1);

			CompilerEnvirons compilerEnv = new CompilerEnvirons();
			compilerEnv.setRecordingComments(true);
			compilerEnv.setRecordingLocalJsDocComments(true);
			compilerEnv.initFromContext(context);

			Parser p = new Parser(compilerEnv);

			// Rhino通常のパース.
			int lineno = 1;
			AstRoot astRoot = null;
			if (null != src) {
				astRoot = p.parse(src, srcName, lineno);
			} else {
				astRoot = p.parse(srcReader, srcName, lineno);
			}
			logger.debug(astRoot.toSource());
			// 木構造の生成.
			AddNodeInfoVisitor nodeVisitor = new AddNodeInfoVisitor();
			astRoot.visit(nodeVisitor);

			// 行番号の追加.
			AddLineno.setup(astRoot);

			// コメントノードの追加.
			SortedSet<Comment> comments = astRoot.getComments();
			AddCommentNode node = null;
			switch (doctype) {
				case JSDOC:
					cleanComment(comments);
					node = new AddJSDocCommentNode(comments);
					break;
				case VSDOC:
					convertVsDoc(comments);
					node = new AddVSDocCommentNode(comments);
					break;
				default:
					throw new IllegalArgumentException();
			}
			node.setup(astRoot);
			if (null != comments) {
				for (Comment comment : comments) {
					nodeVisitor.visit(comment);
				}
			}

			// ログ出力コードの削除.
			// きっとどんな変換を行うか、設定可能とした方が良いんだろうな.
			SuppressLoggerVisitor logVisitor;
			// if (out instanceof PrintStream) {
			// logVisitor = new SuppressLoggerVisitor((PrintStream) out);
			// } else {
			logVisitor = new SuppressLoggerVisitor();
			// }
			astRoot.visit(logVisitor);
			return astRoot;
		} finally {
			Context.exit();
		}
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public static String getOutPath(String path) {

		int index = path.lastIndexOf('/');
		if (0 > index) {
			return path;
		} else {
			String outPath = path.substring(0, index) + "/out" + path.substring(index);
			return outPath;
		}
	}

	/**
	 * コメントを一行ずつ両端の空白を除去する.
	 *
	 * @param comments コメントノードのセット
	 */
	private void cleanComment(SortedSet<Comment> comments) {

		for (Comment comment : comments) {
			if (comment.getCommentType() == CommentType.JSDOC) {
				if (comment.getCommentType() == CommentType.JSDOC) {
					StringBuilder sb = new StringBuilder();
					StringTokenizer st = new StringTokenizer(comment.getValue(), Constants.LINE_SEPARATOR);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						sb.append(StringUtils.strip(token));
						sb.append("\n");
					}
//					comment.setJsDoc(sb.toString());
					comment.setJsDocNode(new Comment(0, 0, CommentType.JSDOC, sb.toString()));
				}
			}
		}
	}

	private void convertVsDoc(SortedSet<Comment> comments) {

		for (Comment comment : comments) {
			if (comment.getCommentType() == CommentType.JSDOC) {
				JSDocCommentNodeParser nodeParser = new JSDocCommentNodeParser(RelationNodeType.FUNCTION);
				JSDocRoot jsdoc = nodeParser.parse(comment.getValue());
				JSDocVSDocConverter converter = new JSDocVSDocConverter();
				VSDocRoot vsDocRoot = converter.convert(jsdoc);
				VSCommentBuilder builder = new VSCommentBuilder(vsDocRoot);
				comment.putProp(Constants.VSDOC_FLAG, Boolean.valueOf(true));
//				comment.setJsDoc(builder.build());
				comment.setJsDocNode(new Comment(0, 0, CommentType.JSDOC, builder.build()));
			}
		}
	}

	public enum DocType {
		JSDOC, VSDOC
	}
}
