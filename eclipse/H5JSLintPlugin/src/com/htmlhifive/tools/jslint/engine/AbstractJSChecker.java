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
package com.htmlhifive.tools.jslint.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.core.runtime.CoreException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.htmlhifive.tools.jslint.engine.option.CheckOption;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.util.CheckJavaScriptUtils;

/**
 * JSLintかJSHintを使用してjsファイルをチェックするチェッカクラスの抽象クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public abstract class AbstractJSChecker implements JSChecker {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(AbstractJSChecker.class);

	/**
	 * Rhinoの最適化レベル.(-1~9).
	 */
	private static final int OPTIMAZATION_LEVEL = 0;

	/**
	 * コンテキストファクトリ.
	 */
	private ContextFactory factory;

	/**
	 * スコープ.
	 */
	private ScriptableObject scope;

	/**
	 * 追加されたオプション.
	 */
	private CheckOption[] options;

	/**
	 * JSLint/JSHintの結果オブジェクト
	 */
	protected Object result;

	/**
	 * コンストラクタ.
	 * 
	 * @param jslint JSLINTのjsファイルパス
	 * @param options オプション
	 * @throws CoreException 解析例外
	 */
	public AbstractJSChecker(Reader jslint, CheckOption[] options) throws CoreException {
		if (options != null) {
			this.options = options.clone();
		}
		try {
			factory = new ContextFactory();
			Context context = factory.enterContext();
			context.setOptimizationLevel(OPTIMAZATION_LEVEL);
			logger.debug("optimizationLevel is " + String.valueOf(context.getOptimizationLevel()));
			scope = context.initStandardObjects();
			context.evaluateReader(scope, jslint, "test", 1, null);
		} catch (FileNotFoundException e) {
			throw new CoreException(null);
		} catch (IOException e) {
			throw new CoreException(null);
		}
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param jslint JSLINTのjsファイルパス
	 * @throws CoreException 解析例外
	 */
	public AbstractJSChecker(Reader jslint) throws CoreException {
		this(jslint, null);
	}

	@Override
	public JSCheckerResult lint(final String source) {

		long lintStart = System.currentTimeMillis();
		factory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {

				String src = source == null ? "" : source;
				logger.debug("target source : " + src.toCharArray().length);
				Object[] args = new Object[] { src, optionsAsJavaScriptObject() };
				Function lintFunc = (Function) scope.get(getCheckerMethodName(), scope);
				long lintFuncstart = System.currentTimeMillis();
				result = lintFunc.call(cx, scope, scope, args);
				logger.debug("lint func time " + String.valueOf(System.currentTimeMillis() - lintFuncstart));
				return null;
			}
		});
		logger.debug("lint time " + String.valueOf(System.currentTimeMillis() - lintStart));
		return builtResults();
	}

	/**
	 * JSLINTの実行結果を取得する.
	 * 
	 * @return 実行エラー結果.
	 */
	private JSCheckerResult builtResults() {

		long createResultStart = System.currentTimeMillis();
		JSCheckerResult result = (JSCheckerResult) factory.call(new ContextAction() {

			@Override
			public Object run(Context cx) {

				JSCheckerResult result = new JSCheckerResult();
				NativeArray errors = getErrors();
				logger.debug("error count : " + errors.getLength());
				for (int i = 0; i < errors.getLength(); i++) {
					Scriptable err = (Scriptable) errors.get(i, errors);
					if (err != null) {
						addError(result, err);
					}
				}
				return result;
			}

		});
		logger.debug("result build time " + String.valueOf(System.currentTimeMillis() - createResultStart));
		return result;

	}

	/**
	 * 使用するチェッカメソッドを取得する.<br>
	 * JSLINT or JSHINT
	 * 
	 * @return 使用するチェッカメソッド
	 */
	abstract String getCheckerMethodName();

	/**
	 * エラー情報を取得する
	 * 
	 * @return エラー情報オブジェクトの配列
	 */
	abstract protected NativeArray getErrors();

	/**
	 * JavaScriptオブジェクトのエラー情報をBeanに変換する
	 * 
	 * @param err
	 * @return
	 */
	abstract protected JSCheckerErrorBean convertToErrorBean(Scriptable err);

	/**
	 * JSLintのエラーをリストに追加する.
	 * 
	 * @param result 追加するリスト
	 * @param err エラーオブジェクト
	 */
	private void addError(JSCheckerResult result, Scriptable err) {

		JSCheckerErrorBean e = convertToErrorBean(err);
		result.addErroList(e);

	}

	/**
	 * 設定ファイルのオプション設定をJavaScriptのオブジェクトとして返す.
	 * 
	 * @return オプションオブジェクト
	 */
	Scriptable optionsAsJavaScriptObject() {

		return (Scriptable) factory.call(new ContextAction() {
			public Object run(Context cx) {

				Scriptable opts = cx.newObject(scope);
				if (options != null && options.length != 0) {
					for (CheckOption option : options) {
						putOpts(opts, option);
					}
				}
				return opts;
			}

		});
	}

	/**
	 * Scriptableオブジェクトにプロパティをセットする.
	 * 
	 * @param opts セットするプロパティ.
	 * @param option オプション
	 */
	private void putOpts(Scriptable opts, CheckOption option) {

		if (!option.isEnable()) {
			return;
		}
		Object val = null;
		Class<?> clazz = option.getClazz();
		if (clazz == Boolean.class) {
			val = Boolean.valueOf(option.isEnable());
		} else if (clazz == Integer.class) {
			val = NumberUtils.isNumber(option.getValue()) ? Integer.valueOf(option.getValue()) : null;
		}
		opts.put(option.getKey(), opts, val);

	}

	@Override
	public JSCheckerResult lint(Reader reader) throws IOException {

		return lint(CheckJavaScriptUtils.readerToString(reader));
	}

	/**
	 * コンテキストファクトリを取得する.
	 * 
	 * @return コンテキストファクトリ
	 */
	ContextFactory getFactory() {

		return factory;
	}

	/**
	 * スコープを取得する.
	 * 
	 * @return スコープ
	 */
	ScriptableObject getScope() {

		return scope;
	}
}
