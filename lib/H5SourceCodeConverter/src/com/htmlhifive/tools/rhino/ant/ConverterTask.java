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

package com.htmlhifive.tools.rhino.ant;

import java.io.IOException;


import org.apache.tools.ant.Task;

import com.htmlhifive.tools.rhino.Main;

public class ConverterTask extends Task {

	private Main main = null;

	private String srcPath = null;

	private String dstPath = null;

	private boolean printTree = false;

	public void init() {
		main = new Main();
	}

	public void execute() {
		try {
			main.execute(srcPath, dstPath, printTree);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

	public String getDstPath() {
		return dstPath;
	}

	public void setDstPath(String dstPath) {
		this.dstPath = dstPath;
	}

	public boolean isPrintTree() {
		return printTree;
	}

	public void setPrintTree(boolean printTree) {
		this.printTree = printTree;
	}

}
