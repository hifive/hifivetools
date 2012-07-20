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
package jp.co.nssol.h5.tool.jslint.library;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.wst.jsdt.core.IIncludePathEntry;

/**
 * IncludePathEntryWrapperのファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class IncludePathEntryWrapperFactory {

	/**
	 * コンストラクタ.
	 */
	private IncludePathEntryWrapperFactory() {

	}

	/**
	 * IncludePathEntryWrapperを取得する.
	 * 
	 * @param iIncludePathEntry インクルードパスエントリ.
	 * @return インクルードパスエントリ.
	 */
	public static IncludePathEntryWrapper getEntryWrapper(IIncludePathEntry iIncludePathEntry) {

		try {
			return IncludePathEntryWrapper.class.getConstructor(IIncludePathEntry.class).newInstance(iIncludePathEntry);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * IncludePathEntryWrapperを取得する.
	 * 
	 * @param iIncludePathEntries エントリ群.
	 * @return エントリ配列.
	 */
	public static IncludePathEntryWrapper[] getEntryWrappers(IIncludePathEntry[] iIncludePathEntries) {

		IncludePathEntryWrapper[] result = new IncludePathEntryWrapper[iIncludePathEntries.length];

		for (int i = 0; i < result.length; i++) {
			result[i] = getEntryWrapper(iIncludePathEntries[i]);
		}

		return result;
	}

}
