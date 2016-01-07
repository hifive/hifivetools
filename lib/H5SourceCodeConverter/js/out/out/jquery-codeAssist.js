function jQuery() {
}
jQuery.prototype = new Object();
jQuery.prototype.ajax = function() {
  /// <summary>
  ///  HTTP通信でページを読み込みます。
  ///  この関数はjQueryにおけるAJAX通信の基本部分で、実際には$.getや$.postといった関数を使った方が、容易に実装できます。
  ///  但し、これらの抽象化された関数は実装の容易さと引き換えに、エラー時のコールバックなどの複雑な機能を失っています。そのような処理を実装したい場合は、やはり基幹であるこの関数を用いる必要があります。
  ///  $.ajax関数は、戻り値として XMLHttpRequestオブジェクトを返します。殆どの場合、このオブジェクトを直接操作することは無いと思われますが、例えば投げてしまったリクエストを中断する場合など、必要であれば利用して下さい。
  ///  この関数は引数をひとつだけとりますが、実際にはハッシュで、キーと値の組み合わせにより多くのオプションを受け取ります。
  ///  以下にその一覧を載せますので、参考にして下さい。
  ///  async boolean非同期通信フラグ。初期値ではtrue（非同期通信）で、リクエストが投げられてから応答があるまで、ユーザエージェントは非同期に処理を続行します。falseに設定（同期通信）した場合、通信に応答があるまでブラウザはロックされ、操作を受け付けなくなることに注意してください。beforeSend functionAJAXによりリクエストが送信される前に呼ばれるAjax Eventです。戻り値にfalseを設定すれば、AJAX送信をキャンセルすることができます。function(XMLHttpRequest){
  ///  this; // AJAX送信に設定したオプションを示すオブジェクト
  ///  }cache booleanjQuery 1.2より。初期値は通常はtrueですが、dataTypeがscriptの場合にはfalseになります。通信結果をキャッシュしたくない場合には、falseを設定してください。complete functionAJAX通信完了時に呼ばれる関数です。successやerrorが呼ばれた後に呼び出されるAjax Eventです。function(XMLHttpRequest, textStatus){
  ///  this; // AJAX送信に設定したオプションを示すオブジェクト
  ///  }contentType stringサーバにデータを送信する際に用いるcontent-typeヘッダの値です。初期値は&quot;application/x-www-form-urlencoded&quot;で、殆どの場合はこの設定のままで問題ないはずです。data object, stringサーバに送信する値。オブジェクトが指定された場合、クエリー文字列に変換されてGETリクエストとして付加されます。この変換処理については、後述するprocessDataを参照して下さい。オブジェクトはキーと値の組み合わせである必要がありますが、もし値が配列だった場合、jQueryは同じキーを持つ複数の値にシリアライズします。例えば {foo: [&quot;bar1&quot;, &quot;bar2&quot;]} のように指定された場合、 &amp;foo=bar1&amp;foo=bar2 のように組み立てられます。dataFilter function基本レベルでのXMLHttpRequestによる戻りデータをフィルタリングします。サーバからの戻り値をサニタイズする場合などに有用です。関数は第1引数に生データを、第2引数にdataTypeの値を受け取ります。フィルタをかけた値を戻り値として返して下さい。function(data, type){
  ///  // フィルタ処理
  ///  // 最後に、サニタイズ後のデータを返す
  ///  return data;
  ///  }dataType stringサーバから返されるデータの型を指定します。省略した場合は、jQueryがMIMEタイプなどを見ながら自動的に判別します。指定可能な値は、次のようなものです。&quot;xml&quot;: XMLドキュメント
  ///  &quot;html&quot;: HTMLをテキストデータとして。ここにscriptタグが含まれた場合、処理は実行されます。
  ///  &quot;script&quot;: JavaScriptコードをテキストデータとして。cacheオプションに特に指定が無ければ、キャッシュは自動的に無効になります。リモートドメインに対するリクエストの場合、POSTはGETに変換されます。
  ///  &quot;json&quot;: JSON形式のデータとして評価し、JavaScriptのオブジェクトに変換します。
  ///  &quot;jsonp&quot;: JSONPとしてリクエストを呼び、callbackパラメータで指定した関数に呼び戻された値をJSONデータとして処理します。(jQuery 1.2より追加)
  ///  &quot;text&quot;: 通常の文字列。
  ///  dataTypeを指定する際は、幾つかの注すべき点があります。後述の注意1,2も参照して下さい。error function通信に失敗した際に呼び出されるAjax Eventです。引数は3つで、順にXMLHttpRequestオブジェクト、エラー内容、補足的な例外オブジェクトを受け取ります。第2引数には &quot;timeout&quot;, &quot;error&quot;, &quot;notmodified&quot;, &quot;parsererror&quot;などが返ります。function(XMLHttpRequest, textStatus, errorThrown){
  ///  // 通常はここでtextStatusやerrorThrownの値を見て処理を切り分けるか、
  ///  // 単純に通信に失敗した際の処理を記述します。
  ///  this; // thisは他のコールバック関数同様にAJAX通信時のオプションを示します。
  ///  }global booleanAjax EventsのGlobal Eventsを実行するかどうかを指定します。通常はtrueですが、特別な通信でfalseにすることも出来ます。詳しくはAjax Eventsを参照して下さい。ifModified booleanサーバからの応答にあるLast-Modifiedヘッダを見て、前回の通信から変更がある場合のみ成功ステータスを返します。jsonp stringjsonpリクエストを行う際に、callbackではないパラメータであれば指定します。例えば {jsonp: &apos;onJsonPLoad&apos;} と指定すれば、実際のリクエストには onJsonPLoad=[関数名] が付与されます。password string認証が必要なHTTP通信時に、パスワードを指定します。processData booleandataに指定したオブジェクトをクエリ文字列に変換するかどうかを設定します。初期値はtrueで、自動的に &quot;application/x-www-form-urlencoded&quot; 形式に変換します。DOMDocumentそのものなど、他の形式でデータを送るために自動変換を行いたくない場合はfalseを指定します。scriptCharset stringスクリプトを読み込む際のキャラセットを指定します。dataTypeが&quot;jsonp&quot;もしくは&quot;script&quot;で、実行されるページと呼び出しているサーバ側のキャラセットが異なる場合のみ指定する必要があります。success functionAJAX通信が成功した場合に呼び出されるAjax Eventです。戻ってきたデータとdataTypeに指定した値の2つの引数を受け取ります。function(data, dataType){
  ///  // dataの値を用いて、通信成功時の処理を記述します。
  ///  this; // thisはAJAX送信時に設定したオプションです
  ///  }timeout numberタイムアウト時間をミリ秒で設定します。$.ajaxSetupで指定した値を、通信に応じて個別に上書きすることができます。type string&quot;POST&quot;か&quot;GET&quot;を指定して、HTTP通信の種類を設定します。初期値は&quot;GET&quot;です。RESTfulに&quot;PUT&quot;や&quot;DELETE&quot;を指定することもできますが、全てのブラウザが対応しているわけではないので注意が必要です。url stringリクエストを送信する先のURLです。省略時は呼び出し元のページに送信します。username string認証が必要なHTTP通信時に、ユーザ名を指定します。xhr functionXMLHttpRequestオブジェクトが作成された際に呼び出されるコールバック関数です。この関数は、例えばIEではXMLHttpRequestではなくActiveXObjectが作られた際に呼ばれます。もし、サイト特有のXMLHttpRequestオブジェクトの拡張やインスタンス管理のファクトリーを持っている場合には、この関数で生成物を上書きすることが出来ます。jQuery1.2.6以前では利用できません。
  ///  ※注意1
  ///  dataTypeオプションを用いる場合、サーバからの応答が正しいMIMEタイプを返すことを確認して下さい。
  ///  もしMIMEタイプと指定されたdataTypeに不整合がある場合、予期しない問題を引き起こす場合があります。
  ///  詳しくはSpecifying the Data Type for AJAX Requests (英語)を参照して下さい。
  ///  ※注意2
  ///  dataTypeに&quot;script&quot;を指定して他のドメインへの送信を行う場合、POSTを指定してもリクエストはGETに自動的に変換されます。
  ///  jQuery 1.2からは、異なったドメインからもJSONPを用いてJSONデータを取得できるオプションが付きました。JSONPを提供するサーバが &quot;url?callback=function&quot; のような形でリクエストを受け付ける場合には、jQueryは自動的にfunctionを指定してJSONデータを受け取ります。
  ///  また、パラメータが callback ではない場合、jsonpオプションにパラメータ名を指定することで同様に処理できます。
  ///  function ajax()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.get = function(url, data, callback) {
  /// <summary>
  ///  HTTP(GET)通信でページを読み込みます。
  ///  シンプルなGETリクエストを送る簡単な方法で、複雑な$.ajax関数を使わずにサーバと通信ができます。通信の完了時に実行される関数を引数で指定することも可能ですが、これは成功時のみ実行されるので、失敗時と成功時の両方をカバーするには、$.ajaxを使う必要があります。
  ///  function get()
  ///  More
  /// </summary>
  /// <param  name = "url" type = "String" >
  /// </param>
  /// <param  name = "data" type = "Map" >
  /// </param>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "XMLHttpRequest" >
  /// </returns>
};
jQuery.prototype.getJSON = function(url, data, callback) {
  /// <summary>
  ///  HTTP(GET)通信でJSON形式のデータを読み込む。
  ///  jQuery1.2では、JSONPのコールバック関数を指定すれば、別のドメインにあるJSON形式のデータを読み込む事が可能になった。(書式：&quot;myurl?callback=?&quot;)jQueryは？を呼び出したい関数名に置換し、それを実行する。
  ///  【注意】この関数以下のコードは、コールバック関数が呼ばれる前に実行される。
  ///  function getJSON()
  ///  More
  /// </summary>
  /// <param  name = "url" type = "String" >
  /// </param>
  /// <param  name = "data" type = "Map" >
  /// </param>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "XMLHttpRequest" >
  /// </returns>
};
jQuery.prototype.getScript = function(url, callback) {
  /// <summary>
  ///  HTTP(GET)HTTP通信(GET)で、ローカルのJavaScriptファイルを読み込み、実行する。
  ///  jQuery1.2以前では、getScriptは同ドメイン内のスクリプトを読み込むだけだったが、jQuery1.2では別のドメインのJavaScriptを読み込む事もできるようになった。。
  ///  【注意】Safari2とそれ以前のバージョンでは、global context 内でスクリプトを同期で評価することはできないので、後で呼び出すこと。
  ///  function getScript()
  ///  More
  /// </summary>
  /// <param  name = "url" type = "String" >
  /// </param>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "XMLHttpRequest" >
  /// </returns>
};
jQuery.prototype.load = function(url, data, callback) {
  /// <summary>
  ///  HTMLを読み込み、DOMに挿入します。
  ///  デフォルトはGET通信ですが、追加のパラメータを設定すればPOSTでも可。
  ///  jQuery1.2ではURLの中でjQueryセレクタを使用可能で、これによって結果の中からセレクタにマッチする要素のみ取り出して挿入することが可能です。書式は、例えば&quot;url #id &gt; セレクタ&quot;のようになります。詳しくは下記のサンプルを参照のこと。
  ///  function load()
  ///  More
  /// </summary>
  /// <param  name = "url" type = "String" >
  /// </param>
  /// <param  name = "data" type = "Map" >
  /// </param>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.serialize = function() {
  /// <summary>
  ///  入力された全てのElementを文字列のデータにシリアライズする。
  ///  jQuery1.2ではserializeメソッドはformをシリアライズするが、それ以前のバージョンだとForm Plugin(Form用プラグイン)のfieldSerializeメソッドでシリアライズする必要がある。
  ///  function serialize()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.serializeArray = function() {
  /// <summary>
  ///  serializeメソッドのようにFormやElementをシリアライズするが、JSON形式のデータ構造で戻り値を返す。
  ///  function serializeArray()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.addClass = function(class) {
  /// <summary>
  ///  指定した要素に、CSSクラスを追加する。
  ///  function addClass()
  ///  More
  /// </summary>
  /// <param  name = "class" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.attr = function(key, fn) {
  /// <summary>
  ///  値の変わりにコールバック関数を設定し、全ての要素に属性を設定する。
  ///  上記の用途で値として関数の戻り値を入れるのではなく、関数ポインタを渡してやることで属性を設定しながら関数がコールバックされる形になる。
  ///  コールバック関数のスコープ内でのthisポインタは処理中の要素そのものになる。また、要素のインデックスを取りたい場合は第一引数で渡される配列の[0]を取得すること。
  ///  function attr()
  ///  More
  /// </summary>
  /// <param  name = "key" type = "String" >
  /// </param>
  /// <param  name = "fn" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.attr = function(key, value) {
  /// <summary>
  ///  キーと値を渡して、全ての要素に属性を設定する。
  ///  function attr()
  ///  More
  /// </summary>
  /// <param  name = "key" type = "String" >
  /// </param>
  /// <param  name = "value" type = "Object" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.attr = function(name) {
  /// <summary>
  ///  最初の要素が持つ指定属性の値を返す。
  ///  要素が指定属性を持っていない場合、関数はundefinedを返す。
  ///  function attr()
  ///  More
  /// </summary>
  /// <param  name = "name" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.attr = function(properties) {
  /// <summary>
  ///  キーと値の組み合わせからなるハッシュオブジェクトを引数に渡し、全ての要素に複数の属性を同時に設定する。
  ///  これは大量の属性を設定したい場合に適した方法である。
  ///  ※もしclass属性を設定したい場合は、キーの名前は&apos;className&apos;である必要がある。これは、Internet Explorerでclassが予約語扱いになっているためである。もしくは、.addClass(class)/.removeClass(class)メソッドを用いること。
  ///  function attr()
  ///  More
  /// </summary>
  /// <param  name = "properties" type = "Map" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.html = function(val) {
  /// <summary>
  ///  指定した要素のHTMLに指定値をセットする。
  ///  この関数はXMLでは動作しないが、XHTMLでは有効。
  ///  function html()
  ///  More
  /// </summary>
  /// <param  name = "val" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.html = function() {
  /// <summary>
  ///  最初の要素をHTML文字列で返す。組み込みであるinnerHTMLの値と同じ。
  ///  この関数はXMLでは動作しないが、XHTMLでは有効である。
  ///  function html()
  ///  More
  /// </summary>
  /// <returns  type = "String" >
  /// </returns>
};
jQuery.prototype.removeAttr = function(name) {
  /// <summary>
  ///  指定属性を持つ要素から、属性を削除する。
  ///  function removeAttr()
  ///  More
  /// </summary>
  /// <param  name = "name" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.removeClass = function(class) {
  /// <summary>
  ///  指定した要素から、CSSクラスを削除する。
  ///  function removeClass()
  ///  More
  /// </summary>
  /// <param  name = "class" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.text = function(val) {
  /// <summary>
  ///  条件に一致する全ての要素にテキストを設定する。
  ///  html(val)に似ているが、これはあくまでテキストなので、”“などはエスケープされてHTMLエンティティとして追加される。
  ///  function text()
  ///  More
  /// </summary>
  /// <param  name = "val" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.text = function() {
  /// <summary>
  ///  指定した要素が持つテキストノードを結合したものを返す。
  ///  返される文字列は、条件に一致する全ての要素が子孫にいたるまで持っているテキストを結合したものになる。
  ///  この関数は、HTMLでもXMLでも動作する。
  ///  function text()
  ///  More
  /// </summary>
  /// <returns  type = "String" >
  /// </returns>
};
jQuery.prototype.toggleClass = function(class, switch2) {
  /// <summary>
  ///  指定したCSSクラスを要素に、switchがtrueであれば追加し、falseであれば削除する。
  ///  function toggleClass()
  ///  More
  /// </summary>
  /// <param  name = "class" type = "String" >
  /// </param>
  /// <param  name = "switch2" type = "Boolean" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.toggleClass = function(class) {
  /// <summary>
  ///  指定したCSSクラスが要素に無ければ追加し、あれば削除する。
  ///  function toggleClass()
  ///  More
  /// </summary>
  /// <param  name = "class" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.val = function(val) {
  /// <summary>
  ///  全ての要素のvalue属性を返す。
  ///  jQuery1.2では、selectボックスにも値をセットできるようになった。
  ///  function val()
  ///  More
  /// </summary>
  /// <param  name = "val" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.val = function() {
  /// <summary>
  ///  全ての要素のvalue属性を返す。
  ///  jQuery1.2では、最初の要素だけではなく全てのvalue属性を返すようになった。複数選択可能なselectボックスなどでは、値は配列として返す。旧バージョンではFormプラグインのfieldValue関数として実装されていた動作。
  ///  function val()
  ///  More
  /// </summary>
  /// <returns  type = "Array" >
  /// </returns>
};
jQuery.prototype.context = "";
jQuery.prototype.each = function(callback) {
  /// <summary>
  ///  合致した全てのエレメントに対して関数を実行する。
  ///  これは、合致するエレメントが見つかる度に1度ずつ、毎回関数が実行されることを意味する。
  ///  その際に、関数内でthisポインタは各エレメントを指す。
  ///  そしてコールバック関数は第一引数に、エレメントセットの中での、ゼロから始まるインデックスを受け取る。
  ///  関数内で戻り値にfalseを設定した場合、ループはそこで終了。これは通常のloop構文におけるbreakのような役割である。
  ///  戻り値にtrueを返した場合は、次のループに処理が移る。こちらはloop文のcontinueのような働きである。
  ///  function each()
  ///  More
  /// </summary>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.eq = function(position) {
  /// <summary>
  ///  エレメントの集合から、指定したポジションのエレメントだけを取り出す。
  ///  ゼロからlength-1までのうちから、合致する位置にあるエレメントだけが戻される。
  ///  function eq()
  ///  More
  /// </summary>
  /// <param  name = "position" type = "Number" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.get = function(index) {
  /// <summary>
  ///  DOMエレメントの集合からインデックスを指定して、ひとつのエレメントを参照する。
  ///  これによって、特にjQueryオブジェクトである必要のないケースで特定のDOM Elementそのものを操作することが可能。
  ///  例えば$(this).get(0)は、配列オペレータである$(this)[0]と同等の意味になる。
  ///  function get()
  ///  More
  /// </summary>
  /// <param  name = "index" type = "Number" >
  /// </param>
  /// <returns  type = "Element" >
  /// </returns>
};
jQuery.prototype.get = function() {
  /// <summary>
  ///  DOMエレメントの配列にアクセスする。
  ///  jQueryオブジェクトが持つエレメント全てを配列の形で返す。
  ///  このライブラリで用意されたjQueryオブジェクトではなく、標準スタイルの配列でエレメントを操作したい場合に有用。
  ///  jQuery以外のライブラリで作られた関数などに配列を渡す際にも役に立つ。
  ///  function get()
  ///  More
  /// </summary>
  /// <returns  >
  ///  {Array Element}
  /// </returns>
};
jQuery.prototype.index = function(subject) {
  /// <summary>
  ///  jQueryオブジェクト内で、引数で指定されたエレメントのインデックス番号を返す。インデックスは、ゼロから始まる連番。
  ///  もし渡されたエレメントがjQueryオブジェクト内に存在しない場合、戻り値には-1が返る。
  ///  function index()
  ///  More
  /// </summary>
  /// <param  name = "subject" type = "Element" >
  /// </param>
  /// <returns  type = "Number" >
  /// </returns>
};
jQuery.prototype.jQuery = function(callback) {
  /// <summary>
  ///  $(document).ready()の短縮形。
  ///  DOM Documentのロードが終わった際に、バインドしておいた関数が実行されるようになる。
  ///  この関数は$(document).ready()と全く同様に動作する。
  ///  この関数は技術的には他の$()関数と同様に連鎖可能であるが、使い道は無い。
  ///  function jQuery()
  ///  More
  /// </summary>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.jQuery = function(elements) {
  /// <summary>
  ///  単数もしくは複数のDOM
  ///  Elementを、jQueryオブジェクトに変換する。
  ///  この関数の引数は、XML DocumentsやWindowオブジェクトのようなDOM Elementではないものに関しても受け付けることができる。
  ///  function jQuery()
  ///  More
  /// </summary>
  /// <param  >
  ///  {Element,Arrya Element} elements
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.jQuery = function(expression, context) {
  /// <summary>
  ///  この関数は、エレメントとマッチさせるCSSセレクターを含む文字列を受け取る。
  ///  jQueryの核になる関数である。jQueryの全てはこの関数を基本にしているか、もしくは何がしかの形で使っている。
  ///  この機能の最も基本的な利用方法は、合致するエレメントを抽出するためのexpression（大抵はCSSを含む）を受け取ることである。
  ///  もしcontextが何も指定されなければ、$()関数は現在のHTMLのDOMエレメントを検索する。
  ///  逆にDOMエレメントやjQueryオブジェクトなどのcontextが指定されれば、expressionはそのcontextに対して合致するものを捜します。
  ///  expressionの文法については、Selectorsのページを参照。
  ///  function jQuery()
  ///  More
  /// </summary>
  /// <param  name = "expression" type = "String" >
  /// </param>
  /// <param  name = "context" type = "Element,jQuery" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.extend = function(object) {
  /// <summary>
  ///  jQueryオブジェクトそのものを拡張する。
  ///  jQuery.fn.extendがjQueryオブジェクトのプロトタイプを拡張するのに対して、このメソッドはjQuery名前空間に新たなメソッドを追加する。
  ///  function extend()
  ///  More
  /// </summary>
  /// <param  name = "object" type = "Object" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.fn.extend = function(object) {
  /// <summary>
  ///  jQueryエレメントに独自の新しいメソッドを追加する。（典型的なjQueryプラグインの作成方法）
  ///  function fn.extend()
  ///  More
  /// </summary>
  /// <param  name = "object" type = "Object" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.jQuery = function(html) {
  /// <summary>
  ///  生のHTML文字列からDOMエレメントを作成します。
  ///  ベタ書きであれ、何がしかのテンプレートエンジンやプラグイン、AJAXでのロードであれ、文字列として書かれたHTMLを受け取ります。
  ///  注意点として、inputタグの作成時に若干の制限があります。これはサンプル2を参照してください。
  ///  また、スラッシュを含むような文字列（imgタグのパスなど）を渡す場合は、これをエスケープしてやる必要があります。
  ///  XHTMLフォーマットでの記述時に空要素を記述する場合は、$(&quot;&quot;)のように書きます。jQuery1.3からは、$(document.createElement(&quot;span&quot;))のように記述することもできます。
  ///  function jQuery()
  ///  More
  /// </summary>
  /// <param  name = "html" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.noConflict = function(extreme) {
  /// <summary>
  ///  $関数のみならず、jQueryオブジェクトも含めて完全にグローバルの名前空間から除去する。運用は慎重に行うこと。
  ///  これは、上記のnoConflict()を更に極端にして$関数だけでなくjQueryオブジェクトも、先に定義された動作に戻してしまうものである。
  ///  これを使わなければいけないケースは極めて稀だと考えられるが、例えば複数のバージョンのjQueryを混在して使わなければならないような場合だとか。あるいは、jQueryオブジェクトへの拡張がConflictしてしまった場合などに必要かもしれない。
  ///  function noConflict()
  ///  More
  /// </summary>
  /// <param  name = "extreme" type = "Boolean" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.noConflict = function() {
  /// <summary>
  ///  この関数を実行すると、$関数の動作が先に定義されている動作に戻る。
  ///  $関数はprototype.jsなどをはじめ、多くのライブラリがそれぞれ拡張している関数である。
  ///  jQueryでも、核となるjQueryオブジェクトのショートカットして極めて頻繁に利用される。
  ///  このコマンドは、そのような$関数を定義する複数のライブラリを用いた際に衝突することを防ぐものである。
  ///  noConflictを使った場合、jQueryオブジェクトの呼び出しには明確に&apos;jQuery&apos;と書く必要がある。
  ///  例えば$(“div p”)と書いていたものも、jQuery(“div p”)と書かなければならない。
  ///  function noConflict()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.length = "";
jQuery.prototype.selector = "";
jQuery.prototype.size = function() {
  /// <summary>
  ///  jQueryオブジェクトのエレメント数を返す。
  ///  返される値はjQueryオブジェクトのlengthプロパティと同じである。
  ///  function size()
  ///  More
  /// </summary>
  /// <returns  type = "Number" >
  /// </returns>
};
jQuery.prototype.css = function() {
  /// <summary>
  ///  キーと値を引数に渡して、全ての要素のstyle属性を設定します。
  ///  valueに数値が入った場合、自動的に単位はピクセルとみなされます。
  ///  function css()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.css = function() {
  /// <summary>
  ///  最初の要素が持つstyle属性から指定スタイルの値を返します。
  ///  function css()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.css = function() {
  /// <summary>
  ///  キーと値がセットになったハッシュを渡すことで、全ての要素のstyle属性を設定します。
  ///  全ての要素に同じスタイルをセットしたい場合に便利です。
  ///  function css()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.height = function() {
  /// <summary>
  ///  全ての要素の高さを指定します。
  ///  引数の値が数値のみで、”em”や”%“のような単位が指定されない場合は全て”px”として判断します。
  ///  function height()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.height = function() {
  /// <summary>
  ///  最初の要素の高さをピクセル単位で取得します。
  ///  jQuery1.2からは、このメソッドでwindowやdocumentの高さも取得できるようになりました。
  ///  function height()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.innerHeight = function() {
  /// <summary>
  ///  最初の要素の内部高さ(borderは除き、paddingは含む)を取得します。
  ///  この関数は、要素の表示/非表示状態にかかわらず機能します。
  ///  function innerHeight()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.innerWidth = function() {
  /// <summary>
  ///  最初の要素の内部横幅(borderは除き、paddingは含む)を取得します。
  ///  この関数は、要素の表示/非表示状態にかかわらず機能します。
  ///  function innerWidth()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.offset = function() {
  /// <summary>
  ///  最初の要素の、ドキュメント上での表示位置を返します。
  ///  戻り値のオブジェクトはtopとleftの2つの数値を持ちます。この関数は、可視状態にある要素に対してのみ有効です。
  ///  function offset()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.outerHeight = function() {
  /// <summary>
  ///  最初の要素の外部高さ(border、paddingを含む)を取得します。
  ///  オプションにmarginを指定してやることで、高さにmerginを含めることもできます。
  ///  この関数は、要素の表示/非表示状態にかかわらず機能します。
  ///  function outerHeight()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.outerWidth = function() {
  /// <summary>
  ///  最初の要素の外部横幅(border、paddingを含む)を取得します。
  ///  オプションにmarginを指定してやることで、横幅にmerginを含めることもできます。
  ///  この関数は、要素の表示/非表示状態にかかわらず機能します。
  ///  function outerWidth()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.position = function() {
  /// <summary>
  ///  最初の要素の、親要素からの相対的な表示位置を返します。
  ///  戻り値はtop、leftを持つオブジェクトで、各値はpixel単位の数値になります。
  ///  この値は、marginやborder、paddingなどを含めて正確に計算された値です。
  ///  この関数は、表示されている要素にのみ有効です。
  ///  function position()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.scrollLeft = function() {
  /// <summary>
  ///  合致する全ての要素のスクロール左位置を指定します
  ///  この関数は、要素の表示/非表示状態にかかわらず機能します。
  ///  function scrollLeft()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.scrollLeft = function() {
  /// <summary>
  ///  最初の要素の現在のスクロール上の左位置を取得します。
  ///  この関数は、要素の表示/非表示状態にかかわらず機能します。
  ///  function scrollLeft()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.scrollTop = function() {
  /// <summary>
  ///  合致する全ての要素のスクロール上位置を指定します
  ///  この関数は、要素の表示/非表示状態にかかわらず機能します。
  ///  function scrollTop()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.scrollTop = function() {
  /// <summary>
  ///  最初の要素の現在のスクロール上の上位置を取得します。
  ///  この関数は、要素の表示/非表示状態にかかわらず機能します。
  ///  function scrollTop()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.width = function() {
  /// <summary>
  ///  全ての要素の横幅を指定します。
  ///  引数の値が数値のみで、”em”や”%“のような単位が指定されない場合は全て”px”として判断します。
  ///  function width()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.width = function() {
  /// <summary>
  ///  最初の要素の横幅をピクセル単位で取得します。
  ///  jQuery1.2からは、このメソッドでwindowやdocumentの横幅も取得できるようになりました。
  ///  function width()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.animate = function() {
  /// <summary>
  ///  自分で独自のアニメーション効果を作成するための関数です。
  ///  この関数でポイントになるのは、style属性の変化です。例えば”height”,”top”,”opacity”のようなstyleを、どのような値で完了させたいかを渡してやることで現在の値から変化させていきます。
  ///  ※ これらの値は、キャメルケースで表記されなければなりません。例えばmargin-leftは、marginLeftのように記述します。
  ///  例えば現在のheightが10pxで、animate関数に{height: “100px”}と渡した場合、高さが10pxから100pxに徐々に変化していく効果が得られます。これは数値のみに適用されますが、それ以外にも” hide”,”show”,”toggle”などの文字列が指定された場合にも、対応した効果を作成してくれます。
  ///  そもそも数値型の値をとらない属性（backgroundColorなど）には、animate関数は対応していません。
  ///  第二引数にはアニメーションの動作期間を指定します。”slow”、”normal”、”fast”、もしくは完了までの時間をミリ秒単位で指定します。例えば”1500”であれば、1.5秒かけてアニメーションが行われます。
  ///  第三引数には、値の変化量を調節するeasing名を渡します。ここに独自の関数を指定することで「徐々に速くなる」「最後にゆっくりになる」「上下しつつ進む」などの変化に富んだ効果を得ることが出来ます。
  ///  第四引数には、アニメーション終了時に実行する関数のポインタを渡すことができます。
  ///  jQuery1.2からは、”px”だけでなく”em”や”%“にも対応するようになりました。
  ///  更に、バージョン1.2からは相対的な値の指定が可能になっています。値の前に”+=“、”-=“を付けることで、現在の値からの増減を表すことができます。
  ///  jQuery1.3からは、durationに 0 を指定した場合の処理タイミングが若干変更になりました。
  ///  以前はステータスが完了状態になる前に、非同期にアニメーションが終了（dutrationがゼロなので、実際には最終形に変化するだけ）していましたが、1.3からはこのタイミングを完全に合わせています。
  ///  function animate()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.animate = function() {
  /// <summary>
  ///  前述の指定では第二引数以降で指定していたものを、ハッシュにして第二引数で選択的に渡すことが可能になりました。
  ///  第三引数以降を用いたものでは、例えば終了時点の関数のみを指定したくても、全ての引数にデフォルトの値を明示的に指定してやらないといけません。そういった煩わしさを無くすため、連想配列で指定できるようになっています。
  ///  指定できるオプションは、以下の通りです。
  ///  durationアニメーションの動作期間を指定します。”slow”、”normal”、”fast”、もしくは完了までの時間をミリ秒単位で指定します。例えば”1500”であれば、1.5秒かけてアニメーションが行われます。
  ///  初期値は”normal”です。
  ///  easing値の変化量を調節するカスタム関数の名前を渡します(参考)。ここに独自の関数を指定することで「徐々に速くなる」「最後にゆっくりになる」「上下しつつ進む」などの変化に富んだ効果を得ることが出来ます。
  ///  独自に作成したり、プラグインを入れなくても使える値は”linear”と”swing”だけです。
  ///  初期値は”swing”です。
  ///  complete各要素のアニメーションが終わった際に実行される関数を指定します。
  ///  stepアニメーション実行中のフレーム毎に呼び出される関数を指定します。
  ///  現在の値から着地点になる値が増えている場合は0から1、減っていく場合は1から0の値が第一引数に渡されてきます。例えば引数に0.5が来れば、全体のちょうど半分が実行されたタイミングであることを示します。
  ///  関数が何回、どのようなタイミングで呼ばれるかはCPUによりますので、常に不定です。
  ///  queueここにfalseを指定すると、アニメーションはキューに保存されずに、ただちに実行されます。
  ///  初期値はtrueです。
  ///  jQuery1.2から追加されました。
  ///  function animate()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.dequeue = function() {
  /// <summary>
  ///  待ち行列の先頭から処理を取り出し、実行します。
  ///  function dequeue()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.fadeIn = function() {
  /// <summary>
  ///  各要素の透明度を操作して、非表示の要素をフェードイン表示させます。
  ///  アニメーション効果は指定したスピードで実行されます。
  ///  速度は、”slow”、”normal”、”fast”、もしくは完了までの時間をミリ秒単位で指定します。例えば”1500”であれば、1.5秒かけてアニメーションが行われます。
  ///  省略された場合は、”normal”が用いられます。
  ///  また、効果が完了した際に呼び出される関数を第二引数に指定することも出来ます。
  ///  function fadeIn()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.fadeOut = function() {
  /// <summary>
  ///  各要素の透明度を操作して、表示されている要素をフェードアウトさせます。
  ///  アニメーション効果は指定したスピードで実行されます。
  ///  速度は、”slow”、”normal”、”fast”、もしくは完了までの時間をミリ秒単位で指定します。例えば”1500”であれば、1.5秒かけてアニメーションが行われます。
  ///  省略された場合は、”normal”が用いられます。
  ///  また、効果が完了した際に呼び出される関数を第二引数に指定することも出来ます。
  ///  function fadeOut()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.fadeTo = function() {
  /// <summary>
  ///  各要素の透明度を、指定した値まで徐々に変化させる効果を与えます。
  ///  アニメーション効果は指定したスピードで実行されます。
  ///  速度は、”slow”、”normal”、”fast”、もしくは完了までの時間をミリ秒単位で指定します。例えば”1500”であれば、1.5秒かけてアニメーションが行われます。
  ///  到達する透明度は、1を100%の濃度（透過しない状態）、0を完全に透明な状態として指定します。例えば0.33であれば、これは33%の見え方になります。
  ///  また、効果が完了した際に呼び出される関数を第三引数に指定することも出来ます。
  ///  function fadeTo()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.hide = function() {
  /// <summary>
  ///  各要素のうち、表示状態にあるものをアニメーション効果付きで非表示にします。
  ///  同時に、非表示状態になった時（アニメーション効果が完了した時）に実行されるコールバック関数を登録することも可能です。
  ///  アニメーションは指定された速度で、横幅、高さ、透明度が変化しながら消えていきます。速度は”slow”、”normal”、”fast”のいずれかか、もしくはアニメーションが完了するまでの時間をミリ秒で指定します。例えば” 1500”が指定されれば、1.5秒かかって非表示になる効果が与えられることになります。
  ///  function hide()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.hide = function() {
  /// <summary>
  ///  各要素のうち、表示状態にあるものを非表示にします。
  ///  hide(speed, [callback])と同じ動作ですが、アニメーションが無く即座に非表示になります。要素が既に非表示になっている場合は、何も起こりません。
  ///  function hide()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.fx.off = "";
jQuery.prototype.queue = function() {
  /// <summary>
  ///  全ての要素集合のqueueの末尾に、新しいエフェクトを追加します。
  ///  function queue()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.queue = function() {
  /// <summary>
  ///  全ての要素集合の持つqueueを、引数で渡したものに差し替えます。
  ///  渡すのは新たなqueueとなる関数の配列になります。
  ///  function queue()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.queue = function() {
  /// <summary>
  ///  最初の要素が持つqueueを、関数配列として返します。
  ///  function queue()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.show = function() {
  /// <summary>
  ///  各要素のうち、非表示状態にあるものをアニメーション効果付きに表示します。
  ///  同時に、表示状態になった時（アニメーション効果が完了した時）に実行されるコールバック関数を登録することも可能です。
  ///  アニメーションは指定された速度で、横幅、高さ、透明度が変化しながら表示されていきます。速度は”slow”、”normal”、”fast”のいずれかか、もしくはアニメーションが完了するまでの時間をミリ秒で指定します。例えば” 1500”が指定されれば、1.5秒かかって表示される効果が与えられることになります。
  ///  function show()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.show = function() {
  /// <summary>
  ///  各要素のうち、非表示状態にあるものを表示します。
  ///  show(speed, [callback])と同じ動作ですが、アニメーションが無く即座に表示されます。要素が既に表示されている場合は、何も起こりません。要素の「非表示状態」は、hide()メソッドを使ったものであれ、スタイル属性で display:none を用いたものであれ、同様に表示状態にします。
  ///  function show()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.slideDown = function() {
  /// <summary>
  ///  各要素の高さを操作して、上から下にスライドして降りて来るイメージの効果で表示させます。
  ///  アニメーション効果は指定したスピードで実行されます。
  ///  速度は、”slow”、”normal”、”fast”、もしくは完了までの時間をミリ秒単位で指定します。例えば”1500”であれば、1.5秒かけてアニメーションが行われます。
  ///  省略した場合は”normal”が用いられます。
  ///  また、効果が完了した際に呼び出される関数を第二引数に指定することも出来ます。
  ///  function slideDown()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.slideToggle = function() {
  /// <summary>
  ///  各要素の高さを操作して、slideDown/slideUpの動作を交互に行います。
  ///  アニメーション効果は指定したスピードで実行されます。
  ///  速度は、”slow”、”normal”、”fast”、もしくは完了までの時間をミリ秒単位で指定します。例えば”1500”であれば、1.5秒かけてアニメーションが行われます。
  ///  省略された場合は、”normal”が用いられます。
  ///  また、効果が完了した際に呼び出される関数を第二引数に指定することも出来ます。
  ///  function slideToggle()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.slideUp = function() {
  /// <summary>
  ///  各要素の高さを操作して、下から上に消えていくイメージの効果で非表示にします。
  ///  アニメーション効果は指定したスピードで実行されます。
  ///  速度は、”slow”、”normal”、”fast”、もしくは完了までの時間をミリ秒単位で指定します。例えば”1500”であれば、1.5秒かけてアニメーションが行われます。
  ///  省略された場合は、”normal”が用いられます。
  ///  また、効果が完了した際に呼び出される関数を第二引数に指定することも出来ます。
  ///  function slideUp()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.stop = function() {
  /// <summary>
  ///  指定した要素集合から、現在動作中のアニメーション処理を全て中止します。
  ///  他のアニメーションがqueueに入ってる場合、次のアニメーションが直ちに実行されることになります。
  ///  function stop()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.toggle = function() {
  /// <summary>
  ///  要素の表示/非表示を、関数が呼び出される度にアニメーション付きで切り替えます。
  ///  同時に、アニメーション終了時に呼び出されるコールバック関数を指定することもできます。
  ///  アニメーションは、高さ、横幅、透明度が指定された速度で徐々に消えていく形になります。
  ///  jQuery1.3からは、各要素の padding や margin の値も同時に変化するようになりました。
  ///  function toggle()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.toggle = function() {
  /// <summary>
  ///  各要素を、引数がtrueであれば表示、falseであれば非表示に切り替えます。
  ///  function toggle()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.toggle = function() {
  /// <summary>
  ///  各要素のうち、表示状態にあるものを非表示にし、非表示状態にあるものは表示状態にします。
  ///  function toggle()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.bind = function() {
  /// <summary>
  ///  要素が持つ、例えば”click”などのイベントに対してコールバック関数を紐付けます。カスタムイベントに対してもbind可能です。
  ///  イベントハンドラは、コールバック関数に対してイベントオブジェクトを渡します。”click”や”submit”などの元々の動作をキャンセルするには、戻り値にfalseを返してください。これにより、イベントのbubblingも止まりますので、親要素が持つイベントの発生もキャンセルされてしまうことに注意してください。ほとんどの場合は、イベントには無名関数を渡すことが出来ます。そうしたケースであれば同じクロージャの中で変数を利用できますが、それが難しいような場合は第二引数を用いてデータを引き渡すことも可能です。（その場合はコールバック関数は三番目の引数になります）
  ///  function bind()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.blur = function() {
  /// <summary>
  ///  各要素のblurイベントに関数をbindします。
  ///  blurイベントは通常、要素がマウスなどのポインティング・デバイスやタブキーなどでフォーカスを失ったタイミングで発生します。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function blur()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.change = function() {
  /// <summary>
  ///  各要素のchangeイベントに関数をbindします。
  ///  changeイベントは通常、フォーカスを失った状態のinput要素がフォーカスを得て、値の変更を完了した時に実行されます。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function change()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.click = function() {
  /// <summary>
  ///  各要素のclickイベントに関数をbindします。
  ///  clickイベントは通常、要素がマウスなどのポインティングデバイスでクリックされた場合に呼び出されます。
  ///  クリックは、mousedownとmouseupの組み合わせで定義されます。これらのイベントの実行順は、以下のようになります。
  ///  mousedown
  ///  mouseup
  ///  click
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function click()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.dblclick = function() {
  /// <summary>
  ///  各要素のdblclickイベントに関数をbindします。
  ///  dblclickイベントは通常、要素がマウスなどのポインティングデバイスでダブルクリックされた場合に呼び出されます。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function dblclick()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.die = function() {
  /// <summary>
  ///  jQuery
  ///  1.3より実装。
  ///  live関数と対になり、登録されたイベントを削除します。
  ///  引数がまったく省略された場合、全てのliveイベントを削除します。
  ///  live関数で登録した、カスタムイベントも削除されます。
  ///  typeのみを指定した場合、そのtypeのイベントが全ての要素から削除されます。
  ///  第二引数にliveで登録した関数を指定した場合、その関数だけが削除されます。
  ///  function die()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.error = function() {
  /// <summary>
  ///  各要素のerrorイベントに関数をbindします。
  ///  errorイベントは標準実装では存在しません。しかし多くのブラウザでは、JavaScriptがページ内で何らかのエラーを検知した際にこのイベントを発生させます。例えばimg要素でsrc属性に存在しない画像のパスや壊れた画像を指定した場合などに、errorイベントが発生します。
  ///  ブラウザのwindowオブジェクトからエラーが投げられた場合、イベントハンドラは関数に3つの引数を渡します。
  ///  発生したエラーを説明する文字列(“varName is not defined”、”missing operator in expression”など)
  ///  エラーが発生したページのURL
  ///  エラーを検出した行番号
  ///  コールバック関数がtrueを返す場合、それはエラーが関数内で処理された合図となり、ブラウザはエラーとして処理しません。
  ///  各ブラウザのエラー処理に関する更に詳細な動作については、以下を参照してください。
  ///  msdn onerror Event
  ///  Gecko DOM Reference onerror Event
  ///  Gecko DOM Reference Event object
  ///  Wikipedia: DOM Events
  ///  function error()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.focus = function() {
  /// <summary>
  ///  各要素のfocusイベントに関数をbindします。
  ///  focusイベントは通常、マウスなどのポインティングデバイスやタブキーで要素がフォーカスを受け取った際に呼び出されます。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function focus()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.hover = function() {
  /// <summary>
  ///  マウスホバーの動きをシミュレートします。
  ///  マウスカーソルが要素の上に乗った時に、第一引数に渡した関数を実行します。マウスが要素から外れた時には第二引数が実行されます。要素内にある他の要素上にマウスカーソルが入った場合にも、マウスは”out”にならず、”over”のままです。例えばAというdiv内にBというimgがある場合、B上にカーソルが入ってもAのoutは発生しません。これはdivのmouseoutイベントを用いた場合とは違う動作になるので注意してください。
  ///  function hover()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.keydown = function() {
  /// <summary>
  ///  各要素のkeydownイベントに関数をbindします。
  ///  keydownイベントは通常、キーボードの何かのキーが押し込まれた際に呼び出されます。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function keydown()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.keypress = function() {
  /// <summary>
  ///  各要素のkeypressイベントに関数をbindします。
  ///  keypressイベントは通常、キーボードのキーが押された際に呼び出されます。
  ///  keydownとkeyupの組み合わせがkeypressになります。キーが叩かれた際の各イベントは、次の順番で呼ばれます。
  ///  keydown
  ///  keyup
  ///  keypress
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function keypress()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.keyup = function() {
  /// <summary>
  ///  各要素のkeyupイベントに関数をbindします。
  ///  keyupイベントは通常、キーボードのキーが押され、上がった際に呼び出されます。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function keyup()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.live = function() {
  /// <summary>
  ///  jQuery
  ///  1.3より実装。
  ///  イベントに対してハンドラを登録します。
  ///  登録されたイベントは、現在および将来的にも、セレクタにマッチする全ての要素に適用されます。
  ///  カスタムイベントに対してbindすることも可能です。
  ///  この関数で指定できるイベントは、次の通りです:
  ///  click, dblclick, mousedown, mouseup, mousemove, mouseover, mouseout, keydown, keypress, keyup
  ///  現時点ではサポートしていないイベントは、次の通りです:
  ///  blur, focus, mouseenter, mouseleave, change, submit
  ///  bindとほぼ同様の関数ですが、ハンドラ登録時にマッチする要素だけでなく、永続的にイベント発生時点でマッチする要素に反応する点が異なります。
  ///  例えばli要素に対してclickイベントを登録した場合、bindであれば、その時点でページ上に存在するli要素に対してイベントが登録されるだけでした。しかしliveでは、その後で動的にli要素が追加された場合も、そのli要素でのクリックに対してハンドラが実行されます。
  ///  この関数は、既にプラグインとして広く使われているliveQueryと似た動きをしますが、いくつかの大きな違いがあります。
  ///  live関数がサポートするのは、イベントのうちの一部のみ（上記サポート/非サポートリスト参照）
  ///  liveQueryがしているような、イベントスタイルでないコールバック関数のサポートはしていない
  ///  live関数には、setupやcleanupといった手順が必要ない
  ///  liveで設定したイベントを削除するには、die関数を用います。
  ///  function live()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.load = function() {
  /// <summary>
  ///  各要素のloadイベントに関数をbindします。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function load()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.mousemove = function() {
  /// <summary>
  ///  各要素のmousemoveイベントに関数をbindします。
  ///  mousemoveイベントは通常、マウスなどのポインティングデバイスが要素上で動いた際に呼び出されます。
  ///  イベントハンドラはコールバック関数にイベントオブジェクトを渡します。イベントオブジェクトはclientX/clientYというプロパティを持ち、マウスカーソルの位置を取得できます。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function mousemove()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.one = function() {
  /// <summary>
  ///  各要素のイベントに、1度だけ呼び出される関数をbindします。
  ///  登録したコールバック関数は、各要素で1度だけ呼び出されます。それ以外の動作については、bind関数と同じです。
  ///  イベントハンドラにはeventオブジェクトが渡され、イベントやバブリングをキャンセルすることができます。あるいは、戻り値にfalseを返すことで両方をキャンセルすることが可能です。
  ///  多くの場合、コールバック関数は無名関数で作成できるでしょう。それが不可能な場合は関数ポインタを渡すことになりますが、その場合は第二引数を用いて追加データを受け渡すことが可能です。（その場合、関数ポインタは第三引数になります）
  ///  function one()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.ready = function() {
  /// <summary>
  ///  DOMがロードされて操作・解析が可能になったタイミングで関数を実行します。
  ///  これはおそらく、最も重要なイベントになります。殆ど全てのJavaScriptはDOMの準備が出来たタイミングで処理を実行したいと思いますが、 window.onloadでは画像などのロードが済む時点にタイミングを合わせるブラウザもあります。readyイベントを用いることで、アプリケーションの体感処理速度を大きく向上させることができます。
  ///  ready関数にコールバック関数を渡してやります。コールバック関数の引数に$エイリアスが来るので、これを用いることでグローバル名前空間での衝突を避けた安全なコードを書くことが出来ます。
  ///  この関数を使う場合、bodyのonloadイベントには何も書かないようにしてください。readyイベントが実行されなくなってしまいます。
  ///  $(document).readyを用いてもかまいません。複数の関数を登録した場合、登録した順に実行されます。
  ///  function ready()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.scroll = function() {
  /// <summary>
  ///  各要素のscrollイベントに関数をbindします。
  ///  scrollイベントは、文書がスクロールした際に呼び出されます。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function scroll()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.select = function() {
  /// <summary>
  ///  各要素のselectイベントに関数をbindします。
  ///  selectイベントは通常、テキストエリアの文字列を選択状態にしたり、選択範囲を変更した際に呼び出されます。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function select()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.submit = function() {
  /// <summary>
  ///  各要素のsubmitイベントに関数をbindします。
  ///  submitイベントは通常、フォームがsubmitされた際に呼び出されます。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function submit()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.toggle = function() {
  /// <summary>
  ///  要素がクリックされる毎に、引数で渡した関数を順番に呼び出します。
  ///  最初に要素をクリックすると、第一引数に渡した関数が実行されます。もう1度クリックすると、第二引数に渡した関数が実行されます。以降、クリックされる度に関数が順に実行され、最後まで行くと最初の関数が実行されます。
  ///  jQuery1.2.6から、この関数は複数の引数を取れるようになりました。
  ///  それ以前のバージョンでは、引数は最初の2つだけが有効です。
  ///  また、この関数を設定した後で削除するには、unbind(&quot;click&quot;)である必要がありました。
  ///  これも1.2.6以降では、直接unbind(&quot;toggle&quot;)とすることが可能になっています。
  ///  function toggle()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.trigger = function() {
  /// <summary>
  ///  各要素の指定されたイベントを実行します。
  ///  この関数を実行すると、指定されたイベントそのものの動作と、登録されたイベントハンドラの呼び出しを共に行います。例えば”submit”を呼び出した場合、そのformのsubmit処理が実際に実行されます。この動作は例で言えばsubmitボタンが押された場合と全く同じで、コールバック関数内の戻り値にfalseを返すなどの処理でキャンセルすることも可能です。
  ///  デフォルトで存在するイベントだけでなく、bindで登録したカスタムイベントなども呼び出すことが出来ます。
  ///  イベントハンドラは、標準化されたイベントオブジェクトを受け取ることができます。ただし、これはブラウザによる独自のプロパティ（keyCode、pageX、pageYなど）は保持していません。
  ///  また、jQueryはNamespaced Events (名前空間付きイベント)を実装しています。
  ///  これにより、triggerやunbindをまとまった単位で処理することができるようになりました。
  ///  イベント名の末尾に「!」を付けると、名前空間を持っていないハンドラだけを指定することになります。
  ///  jQuery 1.3からは、イベントがDOMツリーをbubble upするようになりました。
  ///  例えばドキュメント内に次のような階層があった場合、
  ///  
  ///  title
  ///  
  ///  abcxyz
  ///  
  ///  xyzをクリックすれば、まずイベントは当然ながらspan要素で発生します。その後、ツリーを親の方に辿ってp、divとクリックイベントを発生させていきます。（h1は親子関係が無いので、発生しません）
  ///  abcをクリックした場合も同様ですが、spanタグにあるxyzは子要素なので、バブリングは伝播しません。
  ///  この処理を止めるには、イベントハンドラ内でjQuery.Eventオブジェクトを使い、stopPropagation()関数でバブリングを中断してやります。
  ///  jQueryのイベントオブジェクトは公開され、開発者が独自のオブジェクトを作ることが出来るようになりました。
  ///  イベントオブジェクトの詳細については、jQuery.Eventを参照して下さい。
  ///  function trigger()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.triggerHandler = function() {
  /// <summary>
  ///  各要素の指定されたイベントにひもづけられた、コールバック関数のみを実行します。
  ///  trigger関数との違いは、ブラウザのデフォルトの動作を行わない点だけです。
  ///  function triggerHandler()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.unbind = function() {
  /// <summary>
  ///  bind関数とは反対に、各要素のイベントに関連付けられた関数を削除します。
  ///  引数を全て省略した場合、全ての要素から全イベントが削除されます。
  ///  第一引数にイベント名が指定された場合、そのイベントに関連付けられた関数だけが削除されます。
  ///  第二引数に関数ポインタを渡した場合、指定イベントに結び付けられた、指定の関数のみが削除されます。
  ///  bindしたカスタムイベントを削除することも可能です。
  ///  function unbind()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.unload = function() {
  /// <summary>
  ///  各要素のunloadイベントに関数をbindします。
  ///  jQueryのイベントは、コールバック関数の最初の引数でjQuery.Eventオブジェクトを受け取ることができます。このオブジェクトを使って、規定のイベント動作のキャンセルや、バブリングの抑制などを行います。
  ///  function unload()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.data = function() {
  /// <summary>
  ///  要素に紐づいたデータを設定し、新たに設定された値を返します。
  ///  このAPIは、要素ごとに付随した情報を持たせたい場合などに非常に有用です。
  ///  例えば地図上にマーカーを配置するような場合、マーカーとして用いるimg要素にデータを付随させることで、特別な拡張が無くても様々な情報を保持させることができます。
  ///  ここで設定する値は文字列には限らず、数値や配列など、どんな型であっても受け入れられます。
  ///  プラグインで要素に固有の値を持たせる場合、コンフリクトしないようにプラグイン名を用いて、オブジェクトでデータを保持するのが良いでしょう。
  ///  var obj = jQuery.data($(&quot;#target&quot;).get(0), &quot;your_plugin_name&quot;, ... });
  ///  function data()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.data = function() {
  /// <summary>
  ///  要素に関連付けられた、指定された名前の値を返します。
  ///  ここで指定する要素は、あくまでDOMの要素であることに注意して下さい。
  ///  操作中の要素がjQueryオブジェクトである場合、get()などを用いてDOM要素を抽出する必要があります。
  ///  function data()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.data = function() {
  /// <summary>
  ///  要素ごとに一意にふられたIDを返します。
  ///  この関数は、主に内部的な処理に用いられます。
  ///  一連のdata APIの処理中で必要に応じて呼ばれ、その際に自動的にIDを割り振ります。
  ///  このAPIが返すIDは、いわゆる(X)HTMLが持つID属性とは全く別のものです。
  ///  指定するものではなく、あくまで内部的に要素をユニークに指定するためのものであることに注意して下さい。
  ///  function data()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.param = function() {
  /// <summary>
  ///  form要素やオブジェクトの値をシリアライズします。
  ///  この関数は.serialize()のコアにあたるものですが、単体で個別に使っても有用な場面があるでしょう。
  ///  function param()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.removeData = function() {
  /// <summary>
  ///  ある要素に関連付けられた、指定された値を削除します。
  ///  function removeData()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.removeData = function() {
  /// <summary>
  ///  ある要素に関連付けられたデータを全て削除します。
  ///  function removeData()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.after = function(content) {
  /// <summary>
  ///  各要素の後ろにコンテンツを挿入する。
  ///  function after()
  ///  More
  /// </summary>
  /// <param  name = "content" type = "String,Element,jQuery" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.append = function(contents) {
  /// <summary>
  ///  各要素に引数で指定したコンテンツを追加する。
  ///  これは、全ての要素に対して appendChild を行うことに近く、操作後はDOMに要素が追加された状態になる。
  ///  function append()
  ///  More
  /// </summary>
  /// <param  name = "contents" type = "String,Element,jQuery" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.appendTo = function(contents) {
  /// <summary>
  ///  要素の中身を他の要素に追加する。
  ///  例えば $(A).append(B) とした場合にAにBが追加されるのに対して、$(A).appendTo(B) ではBにAが追加される。
  ///  両方のサンプルを見て、違いに注意すること。
  ///  function appendTo()
  ///  More
  /// </summary>
  /// <param  name = "contents" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.before = function(content) {
  /// <summary>
  ///  各要素の前にコンテンツを挿入する。
  ///  function before()
  ///  More
  /// </summary>
  /// <param  name = "content" type = "String,Element,jQuery" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.clone = function(t) {
  /// <summary>
  ///  要素のクローンを作成し、そのクローンを選択状態にする。
  ///  この関数は、ある要素のコピーを作成してDOMの他の場所に配置する際に便利である。
  ///  function clone()
  ///  More
  /// </summary>
  /// <param  name = "t" type = "Boolean" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.clone = function() {
  /// <summary>
  ///  要素のクローンを作成し、そのクローンを選択状態にする。
  ///  この関数は、ある要素のコピーを作成してDOMの他の場所に配置する際に便利である。
  ///  function clone()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.empty = function() {
  /// <summary>
  ///  各要素の子要素を全て削除し、空にする。
  ///  同時にイベントハンドラや内部でキャッシュしているデータも削除する。
  ///  function empty()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.insertAfter = function(content) {
  /// <summary>
  ///  要素を指定した他の要素の後に挿入する。
  ///  例えば $(A).after(B) とした場合にAの後にBが挿入されるのに対して、$(A).insertAfter(B) ではBの後にAが挿入される。
  ///  両方のサンプルを見て、違いに注意すること。
  ///  function insertAfter()
  ///  More
  /// </summary>
  /// <param  name = "content" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.insertBefore = function(content) {
  /// <summary>
  ///  要素を指定した他の要素の前に挿入する。
  ///  例えば $(A).before(B) とした場合にAの前にBが挿入されるのに対して、$(A).insertBefore(B) ではBの前にAが挿入される。
  ///  両方のサンプルを見て、違いに注意すること。
  ///  function insertBefore()
  ///  More
  /// </summary>
  /// <param  name = "content" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.prepend = function(contents) {
  /// <summary>
  ///  引数で指定したコンテンツを各要素の先頭に挿入する。
  ///  function prepend()
  ///  More
  /// </summary>
  /// <param  name = "contents" type = "String,Element,jQuery" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.prependTo = function(content) {
  /// <summary>
  ///  要素の中身を他の要素の先頭に挿入する。
  ///  例えば $(A).prepend(B) とした場合にAにBが挿入されるのに対して、$(A).prependTo(B) ではBにAが挿入される。
  ///  両方のサンプルを見て、違いに注意すること。
  ///  function prependTo()
  ///  More
  /// </summary>
  /// <param  name = "content" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.remove = function(expr) {
  /// <summary>
  ///  全ての要素をドキュメントから削除する。
  ///  この関数は、jQueryオブジェクトからは要素を削除しません。jQueryオブジェクト上では、引き続き要素の操作をすることが可能です。
  ///  ver1.2.2から、この関数はイベントハンドラや内部キャッシュデータも削除するようになります。ですので、
  ///  $(&quot;#foo&quot;).remove().appendTo(&quot;#bar&quot;);
  ///  というコードは、もし$(”#foo”)が持つイベントを失いたくないのであれば
  ///  $(&quot;#foo&quot;).appendTo(&quot;#bar&quot;);
  ///  のように記述しなければなりません。
  ///  引数に選択条件式を指定することで、削除する要素を絞り込むことが可能です。
  ///  function remove()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.replaceAll = function(selecter) {
  /// <summary>
  ///  セレクターで選択された要素を全て置き換える。
  ///  この関数はreplaceWithと引数の関係が逆になっているだけで、同じ動作をする。
  ///  function replaceAll()
  ///  More
  /// </summary>
  /// <param  name = "selecter" type = "Selector" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.replaceWith = function(content) {
  /// <summary>
  ///  全ての要素を、指定されたHTMLやDOM
  ///  Elementで置き換える。
  ///  function replaceWith()
  ///  More
  /// </summary>
  /// <param  >
  ///  {String, Element, jQuery} content
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.wrap = function(elem) {
  /// <summary>
  ///  指定要素を、実行要素で囲む。
  ///  例えば $(A).wrap(B) であれば、A要素をB要素で囲む。
  ///  function wrap()
  ///  More
  /// </summary>
  /// <param  name = "elem" type = "Element" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.wrap = function(html) {
  /// <summary>
  ///  各要素を構造的に指定HTMLで囲む。
  ///  ドキュメントに追加構造を差し込む際に、その論理的な構成を崩さずに操作を行うことが出来る。
  ///  この関数は、渡されたHTMLをその場で解析し、最初の要素から最も深い階層を捜して、そこへ指定要素を挟み込む。
  ///  引数に指定するHTMLがテキストを含んでいる場合、この関数はうまく動作しない。その場合はwrap関数実行後にテキスト追加を行うこと。
  ///  また、指定HTMLが兄弟構造を持っていたり、逆にwrapされる要素が入れ子関係にあると上手く動作しない。
  ///  function wrap()
  ///  More
  /// </summary>
  /// <param  name = "html" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.wrapAll = function(elem) {
  /// <summary>
  ///  wrapAll(html)と同様だが、HTML文字列ではなくDOM
  ///  Elementなどを指定する。
  ///  wrap(elem)関数との違いも、上記 wrapAll(html) を参照のこと。
  ///  function wrapAll()
  ///  More
  /// </summary>
  /// <param  name = "elem" type = "Element" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.wrapAll = function(html) {
  /// <summary>
  ///  要素集合をまとめて、指定HTML内に1つにまとめて挟み込む。
  ///  wrapAllとwrapの違いは、各要素に対してそれぞれwrap処理を行うか、全てをひとつにまとめるかの違いである。
  ///  ドキュメントに追加構造を差し込む際に、その論理的な構成を崩さずに操作を行う最良の方法である。
  ///  この関数は、渡されたHTMLをその場で解析し、最初の要素から最も深い階層を捜して、そこへ指定要素を挟み込む。
  ///  function wrapAll()
  ///  More
  /// </summary>
  /// <param  name = "html" type = "Element" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.wrapInner = function(elem) {
  /// <summary>
  ///  各要素の子要素を、引数で渡された要素で囲む。
  ///  function wrapInner()
  ///  More
  /// </summary>
  /// <param  name = "elem" type = "Element" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.wrapInner = function(html) {
  /// <summary>
  ///  各要素の子要素を、HTMLで作成した要素で囲む。
  ///  function wrapInner()
  ///  More
  /// </summary>
  /// <param  name = "html" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.class = "";
jQuery.prototype.element = "";
jQuery.prototype.add = function(expr) {
  /// <summary>
  ///  合致させるHTMLの文字列,Elementなど
  ///  function add()
  ///  More
  /// </summary>
  /// <param  >
  ///  {String,DOM Element,Array} expr
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.andSelf = function() {
  /// <summary>
  ///  現在の要素に加えて、ひとつ前の状態の要素集合を選択。
  ///  一つ前の選択状態に加え、更に幾つかの要素を絞り込んで同時に処理することが出来る。
  ///  function andSelf()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.children = function(expr) {
  /// <summary>
  ///  要素内の全ての子要素を選択する。選択される要素は直下にある子要素のみで、孫要素以下は対象外となる。
  ///  この関数は、条件式を渡して選択される子要素を更に絞り込むことも可能。
  ///  parents()関数が先祖まで辿って行くのに対し、children()関数は直下の子要素のみ選択する。
  ///  function children()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.closest = function() {
  /// <summary>
  ///  jQuery
  ///  1.3より。
  ///  開始要素から最も近い親要素を選択します。引数にセレクター書式を指定した場合、マッチする最も近い親要素を返します。
  ///  フィルタにマッチすれば、開始要素そのものが返る場合もあります。
  ///  ルートドキュメントまで辿ってもマッチする要素が無い場合、戻り値はnoneになります。
  ///  closestは、特にイベント操作で便利です。
  ///  function closest()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.contents = function() {
  /// <summary>
  ///  要素のテキストノードも含めた全子要素を取得します。対象要素がiframeであれば、呼び出されるコンテンツのDocumentを選択する。
  ///  function contents()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.end = function() {
  /// <summary>
  ///  jQueryオブジェクトを連鎖的に呼び出していった際に、現在の選択状態を破棄して1つ前の状態に戻します。
  ///  1つ前の状態が無い場合、空の選択状態が返ってきます。
  ///  状態を戻せるのは全てのjQueryオブジェクトを返すTarversing関数で、以下のようなものが挙げられます。
  ///  add
  ///  andSelf
  ///  children
  ///  filter
  ///  find
  ///  map
  ///  next
  ///  nextAll
  ///  not
  ///  parent
  ///  parents
  ///  prev
  ///  prevAll
  ///  siblings
  ///  slice
  ///  これらに加えて、Manipulation関数であるclone関数などにも用いることができます。
  ///  また、次の関数も対象となっています。
  ///  clone
  ///  appendTo
  ///  prependTo
  ///  insertBefore
  ///  insertAfter
  ///  replaceAll
  ///  この関数は、連鎖関数をブロック要素的に利用するためのものだと捉えると判り易いでしょう。
  ///  function end()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.eq = function(index) {
  /// <summary>
  ///  要素集合から引数にインデックスを指定し、ひとつだけの要素を選択する。
  ///  インデックスは0から全要素数-1までの連番。
  ///  function eq()
  ///  More
  /// </summary>
  /// <param  name = "index" type = "Number" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.filter = function(index) {
  /// <summary>
  ///  要素集合から、引数で渡す条件式に合致しない全ての要素を削除したものを返す。
  ///  この関数は、抽出結果を更に絞り込むために用いられる。
  ///  条件式には、カンマ区切りで指定することで複数のフィルタを同時にかけることが可能。
  ///  function filter()
  ///  More
  /// </summary>
  /// <param  name = "index" type = "Number" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.filter = function(fn) {
  /// <summary>
  ///  要素集合から、引数で渡したコールバック関数で合致と判定しなかった要素を全て削除したものを返す。
  ///  この関数は、全ての要素に対して $.each のように順に実行されます。この時にfalseを返せば、その要素は集合から外される。
  ///  false以外の値を返せば、その要素は残る。
  ///  function filter()
  ///  More
  /// </summary>
  /// <param  name = "fn" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.find = function(expr) {
  /// <summary>
  ///  指定要素が持つ全子孫要素から、指定条件式に合致するものを選択する。
  ///  この関数は、処理中の要素から更に絞込みをかけるのに便利である。
  ///  条件式はjQueryのセレクター書式で記述されますが、CSS1-3のセレクター書式でも可能。
  ///  function find()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.hasClass = function(class) {
  /// <summary>
  ///  要素集合全てのうちから、引数に指定したクラスを持つ要素がひとつでもあればtrueを返す。
  ///  これは、is(”.”+class) と同じ動作である。
  ///  function hasClass()
  ///  More
  /// </summary>
  /// <param  name = "class" type = "String" >
  /// </param>
  /// <returns  type = "Boolean" >
  /// </returns>
};
jQuery.prototype.is = function() {
  /// <summary>
  ///  要素集合のうち、1つでも条件式に合致する要素があればtrueを返します。
  ///  もし何の要素も一致しないか、条件式が不正であればfalseが返されます。
  ///  jQuery 1.3からは、引数に全てのセレクター書式が指定できるようになりました。例えば&quot;+&quot;や&quot;~&quot;、&quot;&gt;&quot;のような以前は常にtrueを返していた階層構造を示す書式も、きちんと評価されます。
  ///  内部的にfilterを使っているので、条件式のルールは同じになります。
  ///  function is()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.map = function(callback) {
  /// <summary>
  ///  jQueryオブジェクトが持つ要素集合を、elementなどの他の値の配列に変換する。
  ///  この機能を使って、valueや属性、cssなど様々な値の配列を作ることが出来る。
  ///  この関数は、$.map()の形で呼び出すことも可能。
  ///  function map()
  ///  More
  /// </summary>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.next = function(expr) {
  /// <summary>
  ///  要素集合の各要素の「次」にあたる兄弟要素を、全て抽出する。
  ///  このnext関数はあくまで各要素のすぐ隣の要素のみを抽出し、次以降を選択するのではない。その場合はnextAllを用いること。
  ///  引数には条件式を指定し、結果セットから更に絞込みを行うことも可能。
  ///  function next()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.nextAll = function(expr) {
  /// <summary>
  ///  現在の要素の次以降にある兄弟要素を全て返す。
  ///  条件式を渡して要素を絞り込むことも可能。
  ///  function nextAll()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.not = function(expr) {
  /// <summary>
  ///  要素集合から指定した条件式に合致する要素を削除する。
  ///  function not()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.parent = function(expr) {
  /// <summary>
  ///  各要素の親要素を全て返す。
  ///  引数に選択条件式を指定することで、更に絞り込むことも可能。
  ///  function parent()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.parents = function(expr) {
  /// <summary>
  ///  各要素の先祖要素を全て返す。
  ///  parent()関数が親のみを返すのに対し、parents()はルートを除く先祖要素を全て返す。
  ///  引数に選択条件式を指定することで、更に絞り込むことも可能。
  ///  function parents()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.prev = function(expr) {
  /// <summary>
  ///  要素集合の各要素の「前」にあたる兄弟要素を、全て抽出する。
  ///  このprev関数はあくまで各要素のすぐ隣の要素のみを抽出し、前以前を全て選択するわけではない。その場合はprevAll()を用いること。
  ///  引数には条件式を指定し、結果セットから更に絞込みを行うことも可能。
  ///  function prev()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.prevAll = function(expr) {
  /// <summary>
  ///  現在の要素の前以前にある兄弟要素を全て返す。
  ///  条件式を渡して要素を絞り込むことも可能。
  ///  function prevAll()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.siblings = function(expr) {
  /// <summary>
  ///  各要素の兄弟要素を全て返す。
  ///  条件式を渡して要素を絞り込むことも可能。
  ///  function siblings()
  ///  More
  /// </summary>
  /// <param  name = "expr" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.slice = function(start, end) {
  /// <summary>
  ///  要素集合から指定範囲のものを返す。
  ///  Javascriptに標準で組み込まれている、配列に対するslice関数と同じ動作である。
  ///  function slice()
  ///  More
  /// </summary>
  /// <param  name = "start" type = "Ineger" >
  /// </param>
  /// <param  name = "end" type = "Ineger" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.boxModel = "";
jQuery.prototype.browser = "";
jQuery.prototype.browser.version = "";
jQuery.prototype.each = function() {
  /// <summary>
  ///  配列/オブジェクトを問わずに汎用的に用いることができる、繰り返し処理用の関数です。
  ///  この関数は、jQueryオブジェクトのプロトタイプに実装されている $().each() とは異なります。こちらは、引数で渡した単なる配列やjQueryでないオブジェクトも繰り返し操作することができます。
  ///  コールバック関数は2つの引数を持ちます。
  ///  1番目はオブジェクトであればハッシュKEY、配列であればインデックスを受け取ります。
  ///  2番目には、値が受け渡されます。
  ///  繰り返し処理中にループを抜けたい場合（一般的なループ処理で言うところのbreak）、コールバック関数でfalseを返すことで実装できます。それ以外の値を返した場合は、無視されます。
  ///  function each()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.grep = function() {
  /// <summary>
  ///  配列中から、フィルタ関数を指定して特定の値だけを残した配列を返します。
  ///  コールバックされるフィルタ関数は、2つの引数を受け取ります。
  ///  1番目に渡されるのは、配列中の値そのものです。
  ///  2番目に渡されるのは、配列のインデックスです。
  ///  関数は受け取った値を配列中に残したければtrueを、除去したければfalseを返す必要があります。
  ///  但し、grep関数の第三引数のinvertにtrueを指定するとこの動作は逆になり、trueが除去、falseが残す処理になります。
  ///  function grep()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.inArray = function() {
  /// <summary>
  ///  第一引数に渡した値が配列中にあれば、そのインデックスを返します。
  ///  例え該当する値が複数あっても、最初に見つかった時点でその値を戻します。
  ///  値が配列中に見つからない場合は、-1を返します。
  ///  function inArray()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.isArray = function() {
  /// <summary>
  ///  jQuery
  ///  1.3より追加。
  ///  引数で渡された値が配列であるかどうかを判別します。
  ///  function isArray()
  ///  More
  /// </summary>
  /// <returns  type = "boolean" >
  /// </returns>
};
jQuery.prototype.isFunction = function() {
  /// <summary>
  ///  渡された値が関数かどうかを判別します。
  ///  function isFunction()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.makeArray = function() {
  /// <summary>
  ///  オブジェクトを、配列に変換します。
  ///  対象になるオブジェクトはlengthプロパティを持ち、保持しているプロパティが0からlength-1であるものです。典型的なものとしてはDOMのHTMLElementsなどが挙げられますが、通常の方法でjQueryを用いていれば特に使う必要は無いはずです。
  ///  function makeArray()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.map = function() {
  /// <summary>
  ///  配列の各値を関数で処理して、新たな配列を作成します。
  ///  引数に渡した変換用の関数は、配列の要素数分呼び出されます。
  ///  引数として値そのものとインデックスを受け取り、変換後の値を戻り値として返します。
  ///  コールバック関数が&quot;null&quot;を返すと、配列には何も追加されません。
  ///  配列を返した場合、それらは2次配列ではなく配列に1次的に並べられます。
  ///  これらのことから、map処理後の配列は必ずしも元の配列と同じ要素数にはならないことになります。
  ///  function map()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.support = "";
jQuery.prototype.trim = function() {
  /// <summary>
  ///  文字列の先頭と末尾から、空白を除去します。
  ///  渡された文字列から、正規表現で空白と見做されるものを除去します。
  ///  そのため、改行コードや全角のブランクであっても、空白として処理されます。
  ///  function trim()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.unique = function() {
  /// <summary>
  ///  配列中から重複している値を除去し、ユニークになったものを返します。
  ///  function unique()
  ///  More
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.ajaxComplete = function(callback) {
  /// <summary>
  ///  Attach
  ///  a function to be executed whenever an AJAX request completes. This is an Ajax Event.The XMLHttpRequest and settings used for that request are passed as arguments to the callback.
  ///  function ajaxComplete()
  ///  More
  /// </summary>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.ajaxError = function(callback) {
  /// <summary>
  ///  Attach
  ///  a function to be executed whenever an AJAX request fails. This is an Ajax Event.The XMLHttpRequest and settings used for that request are passed as arguments to the callback. A third argument, an exception object, is passed if an exception occured while processing the request.
  ///  function ajaxError()
  ///  More
  /// </summary>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.ajaxSend = function(callback) {
  /// <summary>
  ///  Attach
  ///  a function to be executed before an AJAX request is sent. This is an Ajax Event.The XMLHttpRequest and settings used for that request are passed as arguments to the callback.
  ///  function ajaxSend()
  ///  More
  /// </summary>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.ajaxStart = function(callback) {
  /// <summary>
  ///  Attach
  ///  a function to be executed whenever an AJAX request begins and there is none already active. This is an Ajax Event.
  ///  function ajaxStart()
  ///  More
  /// </summary>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.ajaxStop = function(callback) {
  /// <summary>
  ///  Attach
  ///  a function to be executed whenever all AJAX requests have ended. This is an Ajax Event.
  ///  function ajaxStop()
  ///  More
  /// </summary>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.ajaxSuccess = function(callback) {
  /// <summary>
  ///  Attach
  ///  a function to be executed whenever an AJAX request completes successfully. This is an Ajax Event.The event object, XMLHttpRequest, and settings used for that request are passed as arguments to the callback.
  ///  function ajaxSuccess()
  ///  More
  /// </summary>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.ajaxSetup = function(options) {
  /// <summary>
  ///  Setup
  ///  global settings for AJAX requests.See $.ajaxfor a description of all available options. Note: Do not set handlers for complete, error, or success functions with this method; use the global ajax events instead. 
  ///  function ajaxSetup()
  ///  More
  /// </summary>
  /// <param  name = "options" type = "Options" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.post = function(url, data, callback, type) {
  /// <summary>
  ///  Load
  ///  a remote page using an HTTP POST request.This is an easy way to send a simple POST request to a server without having to use the more complex $.ajax function. It allows a single callback function to be specified that will be executed when the request is complete (and only if the response has a successful response code). The returned data format can be specified by the fourth parameter.
  ///  If you need to have both error and success callbacks, you may want to use $.ajax. $.post is a (simplified) wrapper function for $.ajax. $.post() returns the XMLHttpRequest that it creates. In most cases you won&apos;t need that object to manipulate directly, but it is available if you need to abort the request manually. 
  ///  function post()
  ///  More
  /// </summary>
  /// <param  name = "url" type = "String" >
  /// </param>
  /// <param  >
  ///  {Map,
  ///  String} data
  /// </param>
  /// <param  name = "callback" type = "Function" >
  /// </param>
  /// <param  name = "type" type = "String" >
  /// </param>
  /// <returns  type = "XMLHttpRequest" >
  /// </returns>
};
jQuery.prototype.data = "";
jQuery.prototype.data = "";
jQuery.prototype.removeData = function(name) {
  /// <summary>
  ///  Removes
  ///  named data store from an element.This is the complement function to $(...).data(name, value).
  ///  function removeData()
  ///  More
  /// </summary>
  /// <param  name = "name" type = "String" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.mousedown = function(fn) {
  /// <summary>
  ///  Binds
  ///  a function to the mousedown event of each matched element.The mousedown event fires when the pointing device button is pressed over an element.
  ///  function mousedown()
  ///  More
  /// </summary>
  /// <param  name = "fn" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.mouseenter = function(fn) {
  /// <summary>
  ///  Bind
  ///  a function to the mouseenter event of each matched element.The mouseenter event fires once when the pointing device is moved into an element. This convenience method was added in jQuery 1.3. Previously the mouseenter event was available via bind.
  ///  function mouseenter()
  ///  More
  /// </summary>
  /// <param  name = "fn" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.mouseleave = function(fn) {
  /// <summary>
  ///  Bind
  ///  a function to the mouseleave event of each matched element.The mouseleave event fires once when the pointing device is moved away from an element. This convenience method was added in jQuery 1.3. Previously the mouseleave event was available via bind.
  ///  function mouseleave()
  ///  More
  /// </summary>
  /// <param  name = "fn" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.mouseout = function(fn) {
  /// <summary>
  ///  Bind
  ///  a function to the mouseout event of each matched element.The mouseout event fires when the pointing device is moved away from an element.
  ///  function mouseout()
  ///  More
  /// </summary>
  /// <param  name = "fn" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.mouseover = function(fn) {
  /// <summary>
  ///  Bind
  ///  a function to the mouseover event of each matched element.The mouseover event fires when the pointing device is moved onto an element.
  ///  function mouseover()
  ///  More
  /// </summary>
  /// <param  name = "fn" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.mouseup = function(fn) {
  /// <summary>
  ///  Bind
  ///  a function to the mouseup event of each matched element.The mouseup event fires when the pointing device button is released over an element.
  ///  function mouseup()
  ///  More
  /// </summary>
  /// <param  name = "fn" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.resize = function(fn) {
  /// <summary>
  ///  Bind
  ///  a function to the resize event of each matched element.The resize event fires when a document view is resized
  ///  function resize()
  ///  More
  /// </summary>
  /// <param  name = "fn" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.offsetParent = "";
jQuery.prototype.merge = function(first, second) {
  /// <summary>
  ///  Merge
  ///  two arrays together.The result is the altered first argument with the elements from the second array added. The arguments should be true Javascript Array objects; use jQuery.makeArray if they are not. To remove duplicate elements from the resulting array, use $.unique().
  ///  function merge()
  ///  More
  /// </summary>
  /// <param  name = "first" type = "Array" >
  /// </param>
  /// <param  name = "second" type = "Array" >
  /// </param>
  /// <returns  type = "Array" >
  /// </returns>
};
jQuery.prototype.done = function(doneCallbacks) {
  /// <summary>
  ///  Add
  ///  handlers to be called when the Deferred object is resolved.
  ///  function done()
  /// </summary>
  /// <param  name = "doneCallbacks" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.fail = function(failCallbacks) {
  /// <summary>
  ///  Add
  ///  handlers to be called when the Deferred object is rejected.
  ///  function fail()
  /// </summary>
  /// <param  name = "failCallbacks" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.isRejected = function() {
  /// <summary>
  ///  Determine
  ///  whether a Deferred object has been rejected.
  ///  Returns true if the Deferred object is in the rejected state, meaning that either deferred.reject() or deferred.rejectWith() has been called for the object and the failCallbacks have been called (or are in the process of being called).
  ///  Note that a Deferred object can be in one of three states: unresolved, resolved, or rejected; use deferred.isResolved() to determine whether the Deferred object is in the resolved state. These methods are primarily useful for debugging, for example to determine whether a Deferred has already been resolved even though you are inside code that intended to reject it.
  ///  function isRejected()
  /// </summary>
  /// <returns  type = "Boolean" >
  /// </returns>
};
jQuery.prototype.isResolved = function() {
  /// <summary>
  ///  Determine
  ///  whether a Deferred object has been resolved.
  ///  Returns true if the Deferred object is in the resolved state, meaning that either deferred.resolve() or deferred.resolveWith() has been called for the object and the doneCallbacks have been called (or are in the process of being called).
  ///  Note that a Deferred object can be in one of three states: unresolved, resolved, or rejected; use deferred.isRejected() to determine whether the Deferred object is in the rejected state. These methods are primarily useful for debugging, for example to determine whether a Deferred has already been resolved even though you are inside code that intended to reject it.
  ///  function isResolved()
  /// </summary>
  /// <returns  type = "Boolean" >
  /// </returns>
};
jQuery.prototype.promise = function() {
  /// <summary>
  ///  Return
  ///  a Deferred&apos;s Promise object.
  ///  The deferred.promise() method allows an asynchronous function to prevent other code from interfering with the progress or status of its internal request. The Promise exposes only the Deferred methods needed to attach additional handlers or determine the state (then, done, fail, isResolved, and isRejected), but not ones that change the state (resolve, reject, resolveWith, and rejectWith).
  ///  If you are creating a Deferred, keep a reference to the Deferred so that it can be resolved or rejected at some point. Return only the Promise object via deferred.promise() so other code can register callbacks or inspect the current state.
  ///  function promise()
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.reject = function() {
  /// <summary>
  ///  Reject
  ///  a Deferred object and call any failCallbacks with the given args.
  ///  function reject()
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.rejectWith = function(context, args) {
  /// <summary>
  ///  Reject
  ///  a Deferred object and call any failCallbacks with the given context and args.
  ///  Normally, only the creator of a Deferred should call this method; you can prevent other code from changing the Deferred&apos;s state by returning a restricted Promise object through deferred.promise().
  ///  When the Deferred is rejected, any failCallbacks added by deferred.then or deferred.fail are called. Callbacks are executed in the order they were added. Each callback is passed the args from the deferred.reject() call. Any failCallbacks added after the Deferred enters the rejected state are executed immediately when they are added, using the arguments that were passed to the .reject() call.
  ///  function rejectWith()
  /// </summary>
  /// <param  name = "context" type = "Object" >
  /// </param>
  /// <param  name = "args" type = "Object" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.resolve = function(args) {
  /// <summary>
  ///  Resolve
  ///  a Deferred object and call any doneCallbacks with the given args.
  ///  When the Deferred is resolved, any doneCallbacks added by deferred.then or deferred.done are called. Callbacks are executed in the order they were added. Each callback is passed the args from the .resolve(). Any doneCallbacks added after the Deferred enters the resolved state are executed immediately when they are added, using the arguments that were passed to the .resolve() call.
  ///  function resolve()
  /// </summary>
  /// <param  name = "args" type = "Object" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.resolveWith = function(context, args) {
  /// <summary>
  ///  Resolve
  ///  a Deferred object and call any doneCallbacks with the given context and args.
  ///  function resolveWith()
  /// </summary>
  /// <param  name = "context" type = "Object" >
  /// </param>
  /// <param  name = "args" type = "Object" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.then = function(doneCallbacks, failCallbacks) {
  /// <summary>
  ///  Add
  ///  handlers to be called when the Deferred object is resolved or rejected.
  ///  function then()
  /// </summary>
  /// <param  name = "doneCallbacks" type = "Function" >
  /// </param>
  /// <param  name = "failCallbacks" type = "Function" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.hasData = function(element) {
  /// <summary>
  ///  The
  ///  jQuery.hasData() method provides a way to determine if an element currently has any values that were set using jQuery.data(). If no data is associated with an element (there is no data object at all or the data object is empty), the method returns false; otherwise it returns true.
  ///  The primary advantage of jQuery.hasData(element) is that it does not create and associate a data object with the element if none currently exists. In contrast, jQuery.data(element) always returns a data object to the caller, creating one if no data object previously existed.
  ///  function hasData()
  /// </summary>
  /// <param  name = "element" type = "DOMElement" >
  /// </param>
  /// <returns  type = "Boolean" >
  /// </returns>
};
jQuery.prototype.parseXML = function(data) {
  /// <summary>
  ///  jQuery.parseXML
  ///  uses the native parsing function of the browser to create a valid XML Document. This document can then be passed to jQuery to create a typical jQuery object that can be traversed and manipulated.
  ///  function parseXML()
  /// </summary>
  /// <param  name = "data" type = "String" >
  /// </param>
  /// <returns  type = "DOMElement" >
  /// </returns>
};
jQuery.prototype.sub = function() {
  /// <summary>
  ///  There
  ///  are two specific use cases for which jQuery.sub() was created. The first was for providing a painless way of overriding jQuery methods without completely destroying the original methods and another was for helping to do encapsulation and basic namespacing for jQuery plugins.
  ///  function sub()
  /// </summary>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.when = function(deferreds) {
  /// <summary>
  ///  Provides
  ///  a way to execute callback functions based on one or more objects, usually Deferred objects that represent asynchronous events.
  ///  function when()
  /// </summary>
  /// <param  name = "deferreds" type = "Object" >
  /// </param>
  /// <returns  type = "Object" >
  /// </returns>
};
jQuery.prototype.always = function(alwaysCallbacks) {
  /// <summary>
  ///  Add
  ///  handlers to be called when the Deferred object is either resolved or rejected.
  ///  function always()
  /// </summary>
  /// <param  name = "alwaysCallbacks" type = "Object" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.pipe = function(doneFilter, failFilter) {
  /// <summary>
  ///  The
  ///  deferred.pipe() method returns a new promise that filters the status and values of a deferred through a function. The doneFilter and failFilter functions filter the original deferred&apos;s resolved rejected status and values. These filter functions can return a new value to be passed along to the piped promise&apos;s done() or fail() callbacks, or they can return another observable object (Deferred, Promise, etc) which will pass its resolved rejected status and values to the piped promise&apos;s callbacks. If the filter function used is null, or not specified, the piped promise will be resolved or rejected with the same values as the original.
  ///  function pipe()
  /// </summary>
  /// <param  name = "doneFilter" type = "Object" >
  /// </param>
  /// <param  name = "failFilter" type = "Object" >
  /// </param>
};
jQuery.prototype.holdReady = function(hold) {
  /// <summary>
  ///  Holds
  ///  or releases the execution of jQuery&apos;s ready event.
  ///  function holdReady()
  /// </summary>
  /// <param  name = "hold" type = "Boolean" >
  /// </param>
};
jQuery.prototype.promise = function(type, target) {
  /// <summary>
  ///  Return
  ///  a Promise object to observe when all actions of a certain type bound to the collection, queued or not, have finished.
  ///  function promise()
  /// </summary>
  /// <param  name = "type" type = "String" >
  /// </param>
  /// <param  name = "target" type = "Object" >
  /// </param>
  /// <returns  type = "jQuery" >
  /// </returns>
};
jQuery.prototype.prop = function(propertyName) {
  /// <summary>
  ///  Get
  ///  the value of a property for the first element in the set of matched elements.
  ///  function prop()
  /// </summary>
  /// <param  name = "propertyName" type = "String" >
  /// </param>
  /// <returns  type = "String" >
  /// </returns>
};
jQuery.prototype.removeProp = function(propertyName, value) {
  /// <summary>
  ///  Remove
  ///  a property for the set of matched elements
  ///  function removeProp()
  /// </summary>
  /// <param  name = "propertyName" type = "String" >
  /// </param>
  /// <param  name = "value" type = "Object" >
  /// </param>
};
Window.$ = new jQuery();
Window.$ = new jQuery();
Window.prototype.$ = new jQuery();
Global.prototype.$ = function(s) {
  return new jQuery();
};
