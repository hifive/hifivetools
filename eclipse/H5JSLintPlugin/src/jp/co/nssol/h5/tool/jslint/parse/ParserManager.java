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
 package jp.co.nssol.h5.tool.jslint.parse;

/**
 * 
 * パーサを制御するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class ParserManager {
	/**
	 * 現在実行中のパーサ.
	 */
	private static Parser currentParser;

	/**
	 * コンストラクタ.
	 */
	private ParserManager() {

	}

	/**
	 * 実行中のパーサを取得する.
	 * 
	 * @return 実行中のパーサ.
	 */
	public static Parser getCurrentParser() {

		return currentParser;
	}

	/**
	 * 実行中のパーサをキャンセルし、新しいパーサに置き換える.
	 * 
	 * @param newParser 新しいパーサ.
	 */
	public static void replaceCurrentParser(Parser newParser) {

		if (currentParser == null) {
			currentParser = newParser;
			return;
		}
		cancelCurrentParser();
		currentParser = newParser;

	}

	/**
	 * 実行中のパーサをキャンセルする.
	 */
	public static void cancelCurrentParser() {

		if (currentParser != null) {
			currentParser.cansel();
		}

	}

	/**
	 * 実行中のパーサをクリアする.
	 */
	public static void clearCurrentParser() {

		currentParser = null;
	}
}
