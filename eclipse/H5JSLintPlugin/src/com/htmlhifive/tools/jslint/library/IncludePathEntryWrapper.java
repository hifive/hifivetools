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
package com.htmlhifive.tools.jslint.library;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.jsdt.core.IIncludePathEntry;

/**
 * クラスパスエントリのラッパークラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class IncludePathEntryWrapper {

	/**
	 * ライブラリを表す定数.
	 */
	public static final int CPE_LIBRARY = 1;

	/**
	 * プロジェクトを表す定数.
	 */
	public static final int CPE_PROJECT = 2;

	/**
	 * ソースを表す定数.
	 */
	public static final int CPE_SOURCE = 3;

	/**
	 * コンテナを表す定数.
	 */
	public static final int CPE_CONTAINER = 5;

	/**
	 * インクルードパスエントリの実体.
	 */
	private IIncludePathEntry entry;

	/**
	 * インクルードパスのリスト.
	 */
	private List<IPath> includePathList;

	/**
	 * 除外パスのリスト.
	 */
	private List<IPath> excludePathList;

	/**
	 * コンストラクタ.
	 * 
	 * @param entry インクルードパスエントリの実体.
	 */
	public IncludePathEntryWrapper(IIncludePathEntry entry) {

		this.entry = entry;
	}

	/**
	 * 実体を返す.
	 * 
	 * @return エントリの実体.
	 */
	public IIncludePathEntry getEntry() {

		return entry;
	}

	/**
	 * エントリのパスを取得する.<br>
	 * ソースフォルダの場合は
	 * 
	 * @return パス.
	 */
	public IPath getPath() {

		return entry.getPath();
	}

	/**
	 * エントリの種類を取得する.
	 * 
	 * @return エントリの種類.
	 */
	public int getEntryKind() {

		return entry.getEntryKind();
	}

	/**
	 * エントリがソースフォルダの場合、除外フィルタパターンのワークスペースをルートとした場合のパスを取得.
	 * 
	 * @return 除外フィルタパターンパス.
	 */
	public IPath[] getFullExclusionPatterns() {

		if (!(CPE_SOURCE == entry.getEntryKind())) {
			return null;
		}
		if (excludePathList == null) {
			IPath[] excludePaths = getExclusionPatterns();
			excludePathList = new ArrayList<IPath>();
			for (IPath exclude : excludePaths) {
				excludePathList.add(getPath().append(exclude));
			}
		}
		return (IPath[]) excludePathList.toArray(new IPath[excludePathList.size()]);
	}

	/**
	 * エントリの除外パターンを取得する.
	 * 
	 * @return エントリの除外パターン.
	 */
	public IPath[] getExclusionPatterns() {

		return entry.getExclusionPatterns();
	}

	/**
	 * エントリのインクルードパターンを取得する.
	 * 
	 * @return エントリのインクルードパターン.
	 */
	public IPath[] getInclusionPatterns() {

		return entry.getInclusionPatterns();
	}

	/**
	 * エントリがソースフォルダの場合、インクルードフィルタパターンのワークスペースをルートとした場合のパスを取得.
	 * 
	 * @return 除外フィルタパターンパス.
	 */
	public IPath[] getFullInclusionPatterns() {

		if (!(CPE_SOURCE == entry.getEntryKind())) {
			return null;
		}
		if (includePathList == null) {
			IPath[] inclusions = getInclusionPatterns();
			includePathList = new ArrayList<IPath>();
			for (IPath inclusion : inclusions) {
				includePathList.add(getPath().append(inclusion));
			}
		}
		return (IPath[]) includePathList.toArray(new IPath[includePathList.size()]);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return entry.toString();
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof IncludePathEntryWrapper)) {
			return false;
		}
		IncludePathEntryWrapper target = (IncludePathEntryWrapper) obj;

		return entry.equals(target.getEntry());
	}

	@Override
	public int hashCode() {

		return entry.hashCode();
	}

}
