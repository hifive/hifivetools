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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import jp.co.nssol.h5.tool.jslint.Activator;
import jp.co.nssol.h5.tool.jslint.JSLintPluginConstant;
import jp.co.nssol.h5.tool.jslint.configure.ConfigBean;
import jp.co.nssol.h5.tool.jslint.configure.FilterBean;
import jp.co.nssol.h5.tool.jslint.configure.JSLintConfigManager;
import jp.co.nssol.h5.tool.jslint.configure.FilterBean.FilterRevel;
import jp.co.nssol.h5.tool.jslint.engine.JSChecker;
import jp.co.nssol.h5.tool.jslint.engine.JSCheckerErrorBean;
import jp.co.nssol.h5.tool.jslint.engine.JSCheckerFactory;
import jp.co.nssol.h5.tool.jslint.engine.JSCheckerResult;
import jp.co.nssol.h5.tool.jslint.engine.option.CheckOption;
import jp.co.nssol.h5.tool.jslint.engine.option.CheckOptionFileWrapper;
import jp.co.nssol.h5.tool.jslint.engine.option.CheckOptionFileWrapperFactory;
import jp.co.nssol.h5.tool.jslint.engine.option.Engine;
import jp.co.nssol.h5.tool.jslint.engine.option.JSHintDefaultOptions;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLoggerFactory;
import jp.co.nssol.h5.tool.jslint.messages.Messages;
import jp.co.nssol.h5.tool.jslint.util.ConfigBeanUtil;
import jp.co.nssol.h5.tool.jslint.util.PluginResourceUtils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * JSlintを使用し、選択されたオブジェクトをパースするクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JsParser implements Parser {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(JsParser.class);
	/**
	 * 全てのタスク量.
	 */
	private static final int TASK_ALL = 1000;

	/**
	 * JSファイルを取得するタスク量.
	 */
	private static final int TASK_SERCH_JS = 50;

	/**
	 * JSLint.jsおよびオプションファイルを読み込むタスク量.
	 */
	private static final int TASK_LOAD_JSLINT = 50;

	/**
	 * 実行中のパーサ.
	 */
	private IProgressMonitor monitor = null;

	/**
	 * 解析されるリソース(選択されたリソース).
	 */
	private IResource resource;

	/**
	 * 解析に利用されるコンフィグビーン.
	 */
	private ConfigBean bean;

	/**
	 * オプションで設定した最大エラー数.
	 */
	private int maxerr = 50;

	/**
	 * コンストラクタ.
	 * 
	 * @param resource 解析されるリソース.
	 * @param monitor
	 */
	public JsParser(IResource resource) {

		this.resource = resource;
		IProject project = resource.getProject();
		ConfigBean configBean = JSLintConfigManager.getConfigBean(project);

		if (configBean.isUseOtherProject()) {
			bean = JSLintConfigManager.getConfigBean((IProject) PluginResourceUtils.pathToContainer(configBean
					.getOtherProjectPath()));
		} else {
			bean = configBean;
		}
		logger.debug("use project path : " + bean.getOtherProjectPath());
	}

	@Override
	public synchronized void parse(IProgressMonitor monitor) throws CoreException, InterruptedException {

		try {
			ParserManager.replaceCurrentParser(this);
			this.monitor = monitor;
			// 入力チェック
			String[] errorMessages = ConfigBeanUtil.checkProperty(bean);
			if (errorMessages.length > 0) {
				throwCoreException(IStatus.WARNING, errorMessages);
			}
			checkCancel();
			monitor.beginTask(Messages.T0000.getText(), TASK_ALL);
			monitor.subTask(Messages.T0001.getText());

			monitor.worked(TASK_SERCH_JS);
			monitor.subTask(Messages.T0002.getText());
			logger.debug("prop file is " + bean.getOptionFilePath());
			checkCancel();

			// プロパティファイルの取得
			IFile propFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(bean.getOptionFilePath());
			CheckOptionFileWrapper option = CheckOptionFileWrapperFactory.createCheckOptionFileWrapper(propFile);
			logger.debug("jslint file is " + bean.getJsLintPath());
			checkCancel();

			// エンジンファイルの取得,エンジンオブジェクト生成.
			IFile jslintFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(bean.getJsLintPath());
			long parseStart = System.currentTimeMillis();
			checkCancel();

			// チェッカエンジンの指定.
			Engine engine = getEngine(jslintFile);
			// 最大エラー数を取得.
			String maxerrStr = option.getOption("maxerr", engine.getKey()).getValue();
			if (maxerrStr != null) {
				maxerr = Integer.parseInt(maxerrStr);
			}
			CheckOption[] options = option.getEnableOptions(engine);
			CheckOption[] newOptions = handleMaxerr(options);
			checkCancel();

			JSChecker jsLint = JSCheckerFactory.createJSChecker(jslintFile, newOptions);
			logger.debug("create checker time" + String.valueOf(System.currentTimeMillis() - parseStart));
			monitor.worked(TASK_LOAD_JSLINT);
			monitor.subTask(Messages.T0003.getText());
			long getLibStart = System.currentTimeMillis();
			// ライブラリの取得.
			JsFileInfo libStr = getLibrary();
			logger.debug("get lib time " + String.valueOf(System.currentTimeMillis() - getLibStart));
			checkCancel();

			// ソースファイルの取得.
			IFile[] jsFiles = getJsFile();
			beforeCheck();
			// ソースファイルのチェック.
			for (IFile iFile : jsFiles) {
				checkCancel();
				logger.debug("targetFile : " + iFile.getName());
				JsFileInfo target = null;
				JsFileInfo fileInfo = new JsFileInfo(iFile);
				JSCheckerResult result = null;
				if (libStr != null) {
					target = libStr.clone();
					target.append(fileInfo);
				} else {
					target = fileInfo.clone();
				}
				beforeCheckAtFile(iFile);
				checkCancel();
				long parseAtFileStart = System.currentTimeMillis();
				result = jsLint.lint(target.getSourceStr());
				logger.debug("parse at file time" + String.valueOf(System.currentTimeMillis() - parseAtFileStart));
				long markStart = System.currentTimeMillis();
				markJsFile(iFile, result.getErrors(), libStr != null ? libStr.getLineCount() : 0);
				logger.debug("mark time " + String.valueOf(System.currentTimeMillis() - markStart));
				// １ファイルあたりのタスク量を進める
				monitor.worked((TASK_ALL - TASK_LOAD_JSLINT - TASK_SERCH_JS) / jsFiles.length);
			}
			logger.debug("parse time : " + String.valueOf(System.currentTimeMillis() - parseStart));
		} catch (IOException e) {
			logger.put(Messages.EM0100, e);
			throwCoreException(IStatus.ERROR, e);
		} catch (ScriptException e) {
			throwCoreException(IStatus.WARNING, Messages.EM0003.format(e.getFileName()));
		}
		// catch (RuntimeException e) {
		// // TODO RuntimeExceptionの扱い
		// // throwCoreException(IStatus.WARNING, Messages.EM0004.getText());
		// throwCoreException(IStatus.ERROR, e);
		// }
		monitor.subTask(Messages.T0004.getText());
		monitor.done();
		ParserManager.clearCurrentParser();
	}

	private void checkCancel() throws InterruptedException {

		if (monitor.isCanceled()) {
			ParserManager.clearCurrentParser();
			throw new InterruptedException();
		}

	}

	/**
	 * 引数のoptionsのmaxerrを差し替える.<br>
	 * オプションの設定値をそのままJSLintに適用してしまうと,<br>
	 * ライブラリと合わせると数がずれてしまうため、maxerrのオプションの値を変える必要がある。
	 * 
	 * @param options オプション.
	 * @return 変換後のオプション.
	 */
	private CheckOption[] handleMaxerr(CheckOption[] options) {

		List<CheckOption> newOptionList = new ArrayList<CheckOption>();
		boolean containMaxerr = false;
		for (CheckOption checkOption : options) {
			logger.debug("option : " + checkOption.toString());

			if ("maxerr".equals(checkOption.getKey())) {
				checkOption.setValue(String.valueOf(Integer.MAX_VALUE));
				containMaxerr = true;
			}
			newOptionList.add(checkOption);
		}
		if (containMaxerr) {
			return (CheckOption[]) newOptionList.toArray(new CheckOption[newOptionList.size()]);
		}
		// なかった場合はmaxerrのオプションを加える.
		CheckOption maxerrOption = JSHintDefaultOptions.MAXERR.convertToOption();
		newOptionList.add(maxerrOption);
		return (CheckOption[]) newOptionList.toArray(new CheckOption[newOptionList.size()]);
	}

	/**
	 * エンジンファイルからEngineオブジェクトを取得する.
	 * 
	 * @param jslintFile エンジンファイル.
	 * @return Engineオブジェクト.
	 */
	private Engine getEngine(IFile jslintFile) {

		if (JSLintPluginConstant.JS_LINT_NAME.equals(jslintFile.getName())) {
			return Engine.JSLINT;
		} else {
			return Engine.JSHINT;
		}
	}

	/**
	 * parse前の初期化処理を行う.
	 * 
	 * @throws CoreException 解析例外.
	 */
	void beforeCheck() throws CoreException {

	}

	/**
	 * 対象ファイルの解析前の処理.
	 * 
	 * @param file 解析ファイル.
	 * @throws CoreException 解析例外.
	 */
	void beforeCheckAtFile(IFile file) throws CoreException {

		file.deleteMarkers(JSLintPluginConstant.JS_TYPE_MARKER, true, IResource.DEPTH_INFINITE);
	}

	/**
	 * ライブラリの設定を取得する.
	 * 
	 * @return ライブラリ情報.
	 * @throws CoreException 解析例外.
	 */
	protected JsFileInfo getLibrary() throws CoreException {

		return null;
	}

	/**
	 * 検査対象のJsファイルを取得する.
	 * 
	 * @param jsFileList
	 * @return 検査対象のJsファイル
	 * @throws CoreException 解析例外.
	 */
	protected IFile[] getJsFile() throws CoreException {

		final List<IFile> jsFileList = new ArrayList<IFile>();
		if (resource instanceof IContainer) {
			IContainer container = (IContainer) resource;
			// コンテナ内のjsファイルを取得.
			container.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) throws CoreException {

					if (JSLintPluginConstant.EXTENTION_JS.equals(resource.getFileExtension())) {
						jsFileList.add((IFile) resource);
					}
					return true;
				}
			});
		} else if (JSLintPluginConstant.EXTENTION_JS.equals(resource.getFileExtension())) {
			jsFileList.add((IFile) resource);
		} else {
			// 恐らく来ない
			throw new AssertionError();
		}
		return (IFile[]) jsFileList.toArray(new IFile[jsFileList.size()]);
	}

	/**
	 * CoreExceptinをスローする.
	 * 
	 * @param severity エラーレベル.
	 * @param e 例外.
	 * @throws CoreException CoreException例外
	 */
	private void throwCoreException(int severity, Exception e) throws CoreException {

		throw new CoreException(new Status(severity, Activator.PLUGIN_ID, null, e));

	}

	/**
	 * CoreExceptinをスローする.
	 * 
	 * @param severity エラーレベル.
	 * @param messages エラーメッセージ.
	 * @throws CoreException CoreException例外
	 */
	private void throwCoreException(int severity, String... messages) throws CoreException {

		MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, Messages.EM0001.getText(), null);
		for (String string : messages) {
			IStatus iStatus = new Status(severity, Activator.PLUGIN_ID, string, null);
			multiStatus.add(iStatus);
		}
		throw new CoreException(multiStatus);

	}

	/**
	 * JSLintによるエラー発生箇所をiFile(jsファイル)にマークする.
	 * 
	 * @param iFile jsファイル
	 * @param errors 検査エラー.
	 * @param startPosition 開始行数.
	 * @throws CoreException 検査例外
	 */
	private void markJsFile(IFile iFile, JSCheckerErrorBean[] errors, int startPosition) throws CoreException {

		Map<String, Object> attributeMap = new HashMap<String, Object>();
		int i = 0;
		for (JSCheckerErrorBean jsLintError : errors) {
			if (jsLintError.getLine() > startPosition) {
				// フィルタレベル判定用.
				FilterRevel revel = matchExcludeFilter(jsLintError.getReason());
				if (i < maxerr && !FilterRevel.IGNORE.equals(revel)) {
					IMarker marker = iFile.createMarker(JSLintPluginConstant.JS_TYPE_MARKER);
					attributeMap.put(IMarker.MESSAGE, jsLintError.getReason());
					if (FilterRevel.ERROR.equals(revel)) {
						attributeMap.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					} else {
						attributeMap.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					}
					attributeMap.put(IMarker.LOCATION,
							Messages.VM0000.format(jsLintError.getLine().intValue() - startPosition));
					attributeMap.put(IMarker.LINE_NUMBER, jsLintError.getLine().intValue() - startPosition);
					marker.setAttributes(attributeMap);
					i++;
				}
			}
		}

	}

	/**
	 * エラー理由がフィルターににマッチするかどうかを判定する.
	 * 
	 * @param reason エラー理由.
	 * @return フィルターにマッチすれば該当するフィルターレベル,しなければnull.
	 */
	private FilterRevel matchExcludeFilter(String reason) {

		FilterBean[] filterBeans = bean.getFilterBeans();
		FilterRevel revel = null;
		for (FilterBean filterBean : filterBeans) {
			if (!filterBean.isState()) {
				continue;
			}
			Pattern pattern = Pattern.compile(filterBean.getRegex());
			Matcher matcher = pattern.matcher(reason);
			// エラーと無視が被った場合無視が優先
			if (matcher.matches()) {
				revel = filterBean.getRevel();
				if (FilterRevel.IGNORE.equals(revel)) {
					// レベルが無視だったら返す.
					return revel;
				}
			}

		}
		return revel;
	}

	/**
	 * 解析されるリソース(選択されたリソース)を取得する.
	 * 
	 * @return 解析されるリソース(選択されたリソース)
	 */
	protected IResource getResource() {

		return resource;
	}

	@Override
	public void cansel() {

		monitor.setCanceled(true);

	}
}
