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
package jp.co.nssol.h5.tools.codeassist.core.proposal.collector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IFunctionCall;
import org.eclipse.wst.jsdt.core.ast.ISingleNameReference;
import org.eclipse.wst.jsdt.core.ast.IThisReference;

/**
 * コード補完を呼び出したときのファンクション名を走査するビジター.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class FunctionNameVisitor extends ASTVisitor {
	/**
	 * 呼び出された関数名のリスト.
	 */
	private List<String> functionNameList = new ArrayList<String>();

	@Override
	public boolean visit(IFunctionCall functionCall) {

		functionNameList.add(String.valueOf(functionCall.getSelector()));
		return super.visit(functionCall);
	}

	@Override
	public boolean visit(IThisReference thisReference) {

		return false;
	}

	@Override
	public boolean visit(ISingleNameReference singleNameReference) {

		return false;
	}

	/**
	 * 呼び出された関数名を取得する.
	 * 
	 * @return 呼び出された関数名
	 */
	public List<String> getLastFunctionNames() {

		return functionNameList;
	}
}
