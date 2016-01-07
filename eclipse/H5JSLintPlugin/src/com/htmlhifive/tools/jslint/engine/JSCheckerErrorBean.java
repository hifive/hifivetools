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

import org.eclipse.core.resources.IFile;

/**
 * JSLintの結果を保持するビーン.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSCheckerErrorBean {

	/**
	 * 検査対象ファイル.
	 */
	private IFile jsFile;

	/**
	 * エラー発生行番号.
	 */
	private Double line;

	/**
	 * エラー発生文字番号.
	 */
	private Double character;

	/**
	 * 
	 * エラー理由.
	 */
	private String reason;

	/**
	 * エラー箇所.
	 */
	private String evidence;

	/**
	 * 検査対象ファイル.を取得します.
	 * 
	 * @return 検査対象ファイル.
	 */
	public IFile getJsFile() {

		return jsFile;
	}

	/**
	 * 検査対象ファイル.を設定します.
	 * 
	 * @param jsFile 検査対象ファイル.
	 */
	public void setJsFile(IFile jsFile) {

		this.jsFile = jsFile;
	}

	/**
	 * エラー発生行番号.を取得します.
	 * 
	 * @return エラー発生行番号.
	 */
	public Double getLine() {

		return line;
	}

	/**
	 * エラー発生行番号.を設定します.
	 * 
	 * @param line エラー発生行番号.
	 */
	public void setLine(Double line) {

		this.line = line;
	}

	/**
	 * エラー発生文字番号.を取得します.
	 * 
	 * @return エラー発生文字番号.
	 */
	public Double getCharacter() {

		return character;
	}

	/**
	 * エラー発生文字番号.を設定します.
	 * 
	 * @param character エラー発生文字番号.
	 */
	public void setCharacter(Double character) {

		this.character = character;
	}

	/**
	 * エラー理由.を取得します.
	 * 
	 * @return エラー理由.
	 */
	public String getReason() {

		return reason;
	}

	/**
	 * エラー理由.を設定します.
	 * 
	 * @param reason エラー理由.
	 */
	public void setReason(String reason) {

		this.reason = reason;
	}

	/**
	 * エラー箇所.を取得します.
	 * 
	 * @return エラー箇所.
	 */
	public String getEvidence() {

		return evidence;
	}

	/**
	 * エラー箇所.を設定します.
	 * 
	 * @param evidence エラー箇所.
	 */
	public void setEvidence(String evidence) {

		this.evidence = evidence;
	}

}
