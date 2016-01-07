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
package com.htmlhifive.tools.codeassist.core.proposal.collector;

import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFunctionDeclaration;
import org.eclipse.wst.jsdt.core.ast.IFunctionExpression;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteralField;
import org.eclipse.wst.jsdt.core.ast.ISingleNameReference;

/**
 * イベントハンドラを収集するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class InitializationHandlerCollector extends EventHandlerCollector {
	private static final String[] INITIAL_FIELD_NAMES = { "__construct", "__init", "__ready" };

	InitializationHandlerCollector(Pattern controllerPattern) {

		super(controllerPattern);
	}

	@Override
	protected boolean supportField(IObjectLiteralField field) {

		// 判定内容
		// 1.ISingleNameReferenceインスタンスかどうか
		// 2.名前が次のいずれかか("__construct", "__init", "__ready")
		// 3.フィールドのイニシャライザが関数かどうか
		// 4.関数宣言がnullでないかどうか
		// 5.関数の引数があるかどうか
		IExpression fieldName = field.getFieldName();
		if (!(fieldName instanceof ISingleNameReference)) {
			return false;
		}
		if (!ArrayUtils.contains(INITIAL_FIELD_NAMES, String.valueOf(((ISingleNameReference) fieldName).getToken()))) {
			return false;
		}
		if (!(field.getInitializer() instanceof IFunctionExpression)) {
			return false;
		}
		IFunctionDeclaration functionDec = ((IFunctionExpression) field.getInitializer()).getMethodDeclaration();
		return functionDec != null && functionDec.getArguments() != null && functionDec.getArguments().length > 0;
	}
}
