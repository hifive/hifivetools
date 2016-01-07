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
package com.htmlhifive.tools.codeassist.core.proposal.build;

import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.VarReferenceBean;


/**
 * 
 * フィールド情報、メソッド情報をからコード(クラス構造)を生成するビルダー抽象クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public abstract class AbstractContextCodeBuilder implements CodeBuilder {

	/**
	 * クラス名.
	 */
	private final String className;

	/**
	 * メソッドリスト.
	 */
	private List<FunctionBean> functionList;

	/**
	 * フィールドリスト.
	 */
	private List<VarReferenceBean> fieldList;

	/**
	 * コンストラクタ. <br>
	 * 
	 * @param className クラス名.
	 */
	public AbstractContextCodeBuilder(String className) {

		this.className = className;
		functionList = new ArrayList<FunctionBean>();
		fieldList = new ArrayList<VarReferenceBean>();
	}

	@Override
	public String build(StringBuilder sb, int insertPosition) {

		StringBuilder part = new StringBuilder();
		buildStart(part);
		buildConstructor(part);
		buildVarRef(part);
		buildMethod(part);
		buildEnd(sb, part, insertPosition);
		return sb.toString();
	}

	@Override
	public String build(StringBuilder sb) {

		return this.build(sb, sb.length());
	}

	@Override
	public String build() {

		StringBuilder sb = new StringBuilder();
		return this.build(sb);
	}

	/**
	 * コードビルドの後処理. <br>
	 * 必要があれば上書き.
	 * 
	 * @param sb 生成したコードを追加する.
	 * @param part 生成したビルドパーツ.
	 * @param insertPosition 挿入位置.
	 * 
	 */
	protected void buildEnd(StringBuilder sb, StringBuilder part, int insertPosition) {

		sb.insert(insertPosition, part.toString());
	}

	/**
	 * コードビルドの前処理.<br>
	 * 必要があれば上書き.
	 * 
	 * @param sb 生成したコードを追加する.
	 * @param insertPosition
	 */
	protected void buildStart(StringBuilder sb) {

	}

	/**
	 * メソッドを定義するコードを生成する.<br>
	 * buildメソッドでコードを生成する順番は<br>
	 * buildConstructor() → buildField() → buildMethod() → buildObject();
	 * 
	 * @param sb 生成したコードを追加する.
	 */
	protected abstract void buildMethod(StringBuilder sb);

	/**
	 * フィールドを定義するコードを生成する<br>
	 * buildメソッドでコードを生成する順番は<br>
	 * buildConstructor() → buildField() → buildMethod().
	 * 
	 * @param sb 生成したコードを追加する.
	 */
	protected abstract void buildVarRef(StringBuilder sb);

	/**
	 * コンストラクタを定義するコードを生成する.<br>
	 * buildメソッドでコードを生成する順番は<br>
	 * buildConstructor() → buildField() → buildMethod().
	 * 
	 * @param sb 生成したコードを追加する.
	 */
	protected abstract void buildConstructor(StringBuilder sb);

	/**
	 * クラス名を取得する.
	 * 
	 * @return クラス名
	 */
	protected String getClassName() {

		return className;
	}

	@Override
	public void addField(VarReferenceBean field) {

		fieldList.add(field);
	}

	@Override
	public void addFunction(FunctionBean function) {

		functionList.add(function);

	}

	/**
	 * 追加したメソッドを取得する.
	 * 
	 * @return 追加したメソッド.
	 */
	FunctionBean[] getFunctions() {

		return (FunctionBean[]) functionList.toArray(new FunctionBean[functionList.size()]);
	}

	/**
	 * 追加したフィールドを取得する.
	 * 
	 * @return 追加したフィールド.
	 */
	VarReferenceBean[] getFields() {

		return (VarReferenceBean[]) fieldList.toArray(new VarReferenceBean[fieldList.size()]);
	}

}
