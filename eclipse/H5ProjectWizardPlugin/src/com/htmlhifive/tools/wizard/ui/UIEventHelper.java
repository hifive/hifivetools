package com.htmlhifive.tools.wizard.ui;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

/**
 * <H3>イベント用ヘルパークラス.</H3>
 * 
 * @author fkubo
 */
public abstract class UIEventHelper {

	/** EVENT_SET_MESSAGE. */
	public static final int SET_MESSAGE = 10001;

	/** EVENT_SET_PAGE_COMPLETE. */
	public static final int SET_PAGE_COMPLETE = 10002;

	// 以下、イベント識別用

	/** EVENT_PROJECT_CHANGE. */
	public static final int PROJECT_CHANGE = 10003;

	/** EVENT_TABLE_SELECTION_CHANGE. */
	public static final int TABLE_SELECTION_CHANGE = 10004;

	/** LIST_RELOAD. */
	public static final int LIST_RELOAD = 10005;

	/**
	 * 変更があった時.
	 * 
	 * @param widget ウィジェット
	 * @param eventCode イベントコード
	 */
	public static void notifyListeners(Widget widget, int eventCode) {

		widget.notifyListeners(eventCode, new Event());
	}

	/**
	 * 変更があった時.
	 * 
	 * @param widget ウィジェット
	 * @param eventCode イベントコード
	 * @param eventItem イベントウィジェット
	 */
	public static void notifyListeners(Widget widget, int eventCode, Widget eventItem) {

		Event event = new Event();
		event.item = eventItem;
		widget.notifyListeners(eventCode, event);
	}

	/**
	 * メッセージを上のページに設定する.
	 * 
	 * @param widget ウィジェット
	 * @param message メッセージ
	 */
	public static void setErrorMessage(Widget widget, String message) {

		Event event = new Event();
		event.text = message;
		widget.notifyListeners(UIEventHelper.SET_MESSAGE, event);
	}

	/**
	 * ページコンプリートを通知する.
	 * 
	 * @param widget
	 * @param doit フラグ
	 */
	public static void setPageComplete(Widget widget, boolean doit) {

		Event event = new Event();
		event.doit = doit;
		widget.notifyListeners(UIEventHelper.SET_PAGE_COMPLETE, event);
	}
}
