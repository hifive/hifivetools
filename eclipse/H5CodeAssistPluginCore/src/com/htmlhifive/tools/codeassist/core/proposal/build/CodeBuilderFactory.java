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
package com.htmlhifive.tools.codeassist.core.proposal.build;

import com.htmlhifive.tools.codeassist.core.messages.Messages;
import com.htmlhifive.tools.codeassist.core.proposal.checker.DelegateDummyCodeInfo;
import com.htmlhifive.tools.codeassist.core.proposal.checker.DummyCodeInfo;

/**
 * コードビルダーのファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class CodeBuilderFactory {

	/**
	 * コンストラクタ.
	 */
	private CodeBuilderFactory() {

		// nocreate
	}

	/**
	 * コード補完のコンテキストから適切はコードビルダーを生成する.<br>
	 * 適切なビルダーがない場合はnullを返す.
	 * 
	 * @param info ダミーコード生成情報.
	 * @return コードビルダー.
	 */
	public static CodeBuilder createCodeBuilder(DummyCodeInfo info) {

		CodeBuilderType type = info.getBuilderType();
		if (type == null) {
			return null;
		}
		CodeBuilder builder = null;
		switch (type) {
			case OBJ_LITERAL:
				builder = new ObjectLiteralCodeBuilder();
				break;
			case REFERENCE_OBJ:
				if (info instanceof DelegateDummyCodeInfo) {
					builder = new ReferenceCodeBuilder(((DelegateDummyCodeInfo) info).getAddedObjName());
				} else {
					throw new IllegalArgumentException(Messages.EM0007.format(ReferenceCodeBuilder.class
							.getSimpleName()));
				}
				break;
			case CREATE_OBJ:
				if (info instanceof DelegateDummyCodeInfo) {
					builder = new CreateObjectBuilder(((DelegateDummyCodeInfo) info).getAddedObjName());
				} else {
					throw new IllegalArgumentException(Messages.EM0007.format(ReferenceCodeBuilder.class
							.getSimpleName()));
				}
				break;
			case OTHER:
				if (info instanceof DelegateDummyCodeInfo) {
					builder = new JsContextCodeBuilder(((DelegateDummyCodeInfo) info).getAddedObjName());
				} else {
					throw new IllegalArgumentException(Messages.EM0007.format(JsContextCodeBuilder.class
							.getSimpleName()));
				}
				break;
			default:
				break;
		}
		return builder;
	}
}
