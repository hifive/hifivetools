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

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IFieldReference;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;

/**
 * コード補完のビジター.
 * 
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
public class MemberAccessVisitor extends ASTVisitor {
	/**
	 * コード補完ノード.
	 */
	private CompletionOnMemberAccess memberAccess;

	@Override
	public boolean visit(IFieldReference fieldReference) {

		if (fieldReference instanceof CompletionOnMemberAccess) {
			this.memberAccess = (CompletionOnMemberAccess) fieldReference;
			return false;
		}
		return super.visit(fieldReference);
	}

	/**
	 * コード補完ノードを取得する.
	 * 
	 * @return コード補完ノード
	 */
	public CompletionOnMemberAccess getMemberAccess() {

		return memberAccess;
	}

}
