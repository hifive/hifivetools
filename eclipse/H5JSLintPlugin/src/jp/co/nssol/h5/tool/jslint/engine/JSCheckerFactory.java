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
 package jp.co.nssol.h5.tool.jslint.engine;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import jp.co.nssol.h5.tool.jslint.JSLintPluginConstant;
import jp.co.nssol.h5.tool.jslint.engine.option.CheckOption;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLoggerFactory;
import jp.co.nssol.h5.tool.jslint.messages.Messages;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * jsファイルチェッカーのファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class JSCheckerFactory {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(JSCheckerFactory.class);

	/**
	 * コンストラクタ.
	 */
	private JSCheckerFactory() {

		// nocreate
	}

	/**
	 * Checkerを生成する.
	 * 
	 * @param checker チェッカーエンジンファイル.
	 * @param options オプション
	 * @return チェッカ
	 * @throws CoreException 生成例外.
	 */
	public static JSChecker createJSChecker(IResource checker, CheckOption[] options) throws CoreException {

		IFile chekerFile = (IFile) checker;
		String charset = chekerFile.getCharset(true);

		try {
			if (StringUtils.equals(checker.getName(), JSLintPluginConstant.JS_LINT_NAME)) {
				return new JSLint(new InputStreamReader(((IFile) checker).getContents(), charset), options);
			} else if (StringUtils.equals(checker.getName(), JSLintPluginConstant.JS_HINT_NAME)) {
				return new JSHint(new InputStreamReader(((IFile) checker).getContents(), charset), options);
			} else {
				logger.put(Messages.EM0004);
				throw new IllegalArgumentException(Messages.EM0004.getText());
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
