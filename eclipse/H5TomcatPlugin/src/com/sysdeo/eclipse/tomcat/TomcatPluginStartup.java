/*
 * (c) Copyright Sysdeo SA 2001, 2002.
 * All Rights Reserved.
 */
package com.sysdeo.eclipse.tomcat;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TomcatPluginStartup extends AbstractUIPlugin implements IStartup {

    public void earlyStartup() {
        // 開発環境(EclipseとTomcat)の場所が変わった場合を考慮して
        // Tomcatのクラスパスを初期化する.
        TomcatLauncherPlugin.getDefault().initTomcatClasspathVariable();
    }

}
