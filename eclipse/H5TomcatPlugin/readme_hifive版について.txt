TomcatPluginhifive版について

このプラグインはTomcatプラグインに改良を加えた物です。
TomcatLauncherPlugin V3.3をベースにしています。

・改良点
  ・Tomcatベース、Tomcatホーム、コンテキストルート/ファイルのパス指定に相対パスを指定できるようにした
  ・TomcatLauncherPlugin.javaの以下のメソッドに相対パスか絶対パスかをチェックするコード追加
      ・183行目  getTomcatDir()
      ・198行目  getTomcatBase()
      ・213行目  getConfigFile()
      ・233行目  getContextsDir()
  ・開発環境を任意の場所に移動してEclipseを起動した後に、TOMCAT_HOMEクラスパスを自動で修正するコード追加
  ・startup拡張ポイント新規設定
  ・TomcatPluginStartupクラス追加
      ・earlyStartup()をオーバーライドし、元々あるinitTomcatClasspathVariable()を呼んで、クラスパスを修正