/* hifive ver.0.5.7 (2012/02/24 20:44:22) */
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */

/**
 * hifiveビルドの中心
 *
 * @param {Object} jQuery
 */
(function(window, $) {
  // 二重読み込み防止
  if (window.h5 !== undefined) {
    return;
  }
  // ns関数で名前空間を作ったときに参照が変わらないように、先にwindowにh5を紐づけておく

  /**
   * h5オブジェクト
   *
   * @name h5
   * @namespace
   */
  window.h5 = {};
  // グローバルコンテキストを作成
  h5.pageContext = ns('h5.core.globalContext');

  /**
   * ユーティリティを格納するh5.uオブジェクト
   *
   * @name u
   * @memberOf h5
   * @namespace
   */
  ns('h5.u');

  /**
   * 設定を格納するh5.settingsオブジェクト
   *
   * @name settings
   * @memberOf h5
   * @namespace
   */
  ns('h5.settings');

  /**
   * hifiveで使用するUI部品を格納するオブジェクト
   *
   * @name ui
   * @memberOf h5
   * @namespace
   */
  ns('h5.ui');

  /**
   * hifive APIs
   *
   * @name api
   * @memberOf h5
   * @namespace
   */
  ns('h5.api');
  h5.settings = {

    /**
     * 開発モードで実行するか<br>
     * (実行する: true / 実行しない: false)
     *
     * @memberOf h5.settings
     * @type Boolean
     * @default false
     */
    allowDevMode: false,

    /**
     * failコールバックが設定されていない時にrejectされた場合に発動する共通ハンドラ.
     *
     * @memberOf h5.settings
     * @type Function
     */
    commonFailHandler: null,

    /**
     * コントローラ、ロジックへのアスペクト
     *
     * @memberOf h5.settings
     * @type Aspect|Aspect[]
     */
    aspects: null
  };

  /**
   * ドット区切りで名前空間オブジェクトを生成します。 （ns('com.htmlhifive')と呼ぶと、window.com.htmlhifiveとオブジェクトを生成します。）
   * すでにオブジェクトが存在した場合は、それをそのまま使用します。 引数にString以外が渡された場合はエラーとします。
   *
   * @param {String} namespace 名前空間
   * @memberOf h5.u.obj
   */
  function ns(namespace) {
    if (!namespace) {
      return;
    }
    if ($.type(namespace) !== 'string') {
      throw new Error('パラメータ namespaceにはStringを指定して下さい。');
    }
    var nsArray = namespace.split('.');
    var count = nsArray.length;
    var parentObj = window;
    for (var i = 0; i < count; i++) {
      if (parentObj[nsArray[i]] === undefined) {
        parentObj[nsArray[i]] = {};
      }
      parentObj = parentObj[nsArray[i]];
    }
    // ループが終了しているので、parentObjは一番末尾のオブジェクトを指している
    return parentObj;
  }

  /**
   * オブジェクトを指定された名前空間に登録し、グローバルに公開します。 引数namespaceの型がObjectでそのObjectがグローバルに紐付いていない場合は公開されません。
   *
   * @param {String|Object} namespace 名前空間
   * @param {Object} object 登録するオブジェクト
   * @memberOf h5.u.obj
   */
  function expose(namespace, object) {
    var nsObj = ns(namespace);
    for (var prop in object) {
      if (object.hasOwnProperty(prop)) {
        if (nsObj[prop]) {
          throw new Error(h5.u.str.format('名前空間"{0}"には、プロパティ"{1}"が既に存在します。', namespace, prop));
        }
        nsObj[prop] = object[prop];
      }
    }
  }
  // ns()、expose()をuに追加
  expose('h5.u.obj', {
    expose: expose,
    ns: ns
  });
  // TODO h5preinitでglobalAspectsの設定をしている関係上、別ファイルではなく、ここに置いている。

  /**
   * 実行時間の計測を行うインターセプタ。
   *
   * @param {Function} invocation 次に実行する関数
   * @returns {Any} invocationの戻り値
   * @memberOf h5.core.interceptor
   */
  var lapInterceptor = function(invocation) {
    var currentFuncName = invocation.funcName;
    var currentName = this.__name;
    var startTime = new Date();
    var that = this;
    var post = function() {
      var endTime = new Date();
      that.log.info(currentName + ' "' + currentFuncName + '": ' + (endTime - startTime) + "ms");
    };
    var ret = invocation.proceed();
    if (ret && h5.async.isPromise(ret)) {
      var dfd = this.deferred();
      ret.done(function() {
        post();
        dfd.resolve.apply(dfd, arguments);
      }).fail(function() {
        post();
        dfd.reject.apply(dfd, arguments);
      }).progress(function() {
        dfd.notify.apply(dfd, arguments);
      });
      return dfd.promise();
    }
    post();
    return ret;
  };

  /**
   * イベントコンテキストに格納されているものをコンソールに出力するインターセプタ。
   *
   * @param {Function} invocation 次に実行する関数
   * @returns {Any} invocationの戻り値
   * @memberOf h5.core.interceptor
   */
  var logInterceptor = function(invocation) {
    var currentFuncName = invocation.funcName;
    var currentName = this.__name;
    this.log.info(currentName + ' "' + currentFuncName + '"が開始されました。 ');
    this.log.info(invocation.args);
    var that = this;
    var post = function() {
      that.log.info(currentName + ' "' + currentFuncName + '"が終了しました。 ');
    };
    var ret = invocation.proceed();
    if (ret && h5.async.isPromise(ret)) {
      var dfd = this.deferred();
      ret.done(function() {
        post();
        dfd.resolve.apply(dfd, arguments);
      }).fail(function() {
        post();
        dfd.reject.apply(dfd, arguments);
      }).progress(function() {
        dfd.notify.apply(dfd, arguments);
      });
      return dfd.promise();
    }
    post();
    return ret;
  };

  /**
   * invocationからあがってきたエラーを受け取りcommonFailHandlerに処理を任せるインターセプタ。
   *
   * @param {Function} invocation 次に実行する関数
   * @returns {Any} invocationの戻り値
   * @memberOf h5.core.interceptor
   */
  var errorInterceptor = function(invocation) {
    var ret = null;
    try {
      ret = invocation.proceed();
    }
    catch (e) {
      if (h5.settings.commonFailHandler && $.isFunction(h5.settings.commonFailHandler)) {
        h5.settings.commonFailHandler.call(null, e);
      }
    }
    if (ret && h5.async.isPromise(ret)) {
      var dfd = this.deferred();
      ret.done(function() {
        dfd.resolve.apply(dfd, arguments);
      }).fail(function() {
        dfd.reject.apply(dfd, arguments);
      }).progress(function() {
        dfd.notify.apply(dfd, arguments);
      });
      return dfd.promise();
    }
    return ret;
  };
  // #include h5const.js
  // #include h5util.js
  // #include log.js
  // #include controller.js
  // #include view.js
  // #include h5api.js
  // lap、logをh5.core.interceptorに追加

  /**
   * @name interceptor
   * @memberOf h5.core
   * @namespace
   */
  expose('h5.core.interceptor', {
    lapInterceptor: lapInterceptor,
    logInterceptor: logInterceptor,
    errorInterceptor: errorInterceptor
  });
  // h5preinitイベントをトリガ.
  $(window.document).trigger('h5preinit');
  var escapeRegex = function(str) {
    return str.replace(/\W/g, '\\$&');
  };
  var getRegex = function(target) {
    if ($.type(target) === 'regexp') {
      return target;
    }
    var str = '';
    if (target.indexOf('*') !== -1) {
      var array = $.map(target.split('*'), function(n) {
        return escapeRegex(n);
      });
      str = array.join('.*');
    } else {
      str = target;
    }
    return new RegExp('^' + str + '$');
  };
  var compile = function(aspect) {
    if (aspect.target) {
      aspect.compiledTarget = getRegex(aspect.target);
    }
    if (aspect.pointCut) {
      aspect.compiledPointCut = getRegex(aspect.pointCut);
    }
    return aspect;
  };
  var compileAspects = function(aspects) {
    if ($.type(aspects) !== 'array') {
      return compile(aspects);
    }
    return $.map(aspects, function(n) {
      return compile(n);
    });
  };
  if (h5.settings.aspects) {
    h5.settings.aspects = compileAspects(h5.settings.aspects);
  }
})(window, jQuery);
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */

/**
 * 基本はこれ(オブジェクトが1つあればよいパターン)
 *
 * @param {Object} jQuery
 */
(function(window, $) {
  // #delete end
  var logLevel = {

    /**
     * ログレベル: ERROR
     *
     * @memberOf Log.LEVEL
     * @const {Object} ERROR
     * @type Number
     */
    ERROR: 50,

    /**
     * ログレベル: WARN
     *
     * @memberOf Log.LEVEL
     * @const {Object} WARN
     * @type Number
     */
    WARN: 40,

    /**
     * ログレベル: INFO
     *
     * @memberOf Log.LEVEL
     * @const {Object} INFO
     * @type Number
     */
    INFO: 30,

    /**
     * ログレベル: DEBUG
     *
     * @memberOf Log.LEVEL
     * @const {Object} DEBUG
     * @type Number
     */
    DEBUG: 20,

    /**
     * ログレベル: TRACE
     *
     * @memberOf Log.LEVEL
     * @const {Object} TRACE
     * @type Number
     */
    TRACE: 10,

    /**
     * ログレベル: ALL
     *
     * @memberOf Log.LEVEL
     * @const {Object} ALL
     * @type Number
     */
    ALL: 0
  };

  /**
   * コンソールにログを出力するログターゲット
   *
   * @name ConsoleLogTarget
   * @constructor
   */
  function ConsoleLogTarget() {
    //
  }
  ConsoleLogTarget.prototype = {

    /**
     * ログをコンソールに出力します。
     *
     * @memberOf ConsoleLogTarget
     * @function
     * @param {Object} logObj ログ情報を保持するオブジェクト
     */
    log: function(logObj) {
      if (!window.console) {
        return;
      }
      var args = logObj.args;
      if (typeof args[0] !== 'string') {
        this._logObj(logObj);
      } else {
        this._logMsg(logObj);
      }
    },

    /**
     * 指定された文字列をコンソールに出力します。
     *
     * @memberOf ConsoleLogTarget
     * @private
     * @function
     * @param {Object} logObj ログ情報を保持するオブジェクト
     */
    _logMsg: function(logObj) {
      var args = logObj.args;
      var msg = null;
      if (args.length === 1) {
        msg = args[0];
      } else {
        msg = h5.u.str.format.apply(h5.u.str, args);
      }
      var logMsg = this._getLogPrefix(logObj) + msg;
      if (logObj.logger.enableStackTrace) {
        logMsg += '  [' + logObj.funcTrace.traceShort + ']';
      }
      if (logObj.logger.enableStackTrace && console.groupCollapsed) {
        console.groupCollapsed(logMsg);
      } else {
        this._consoleOut(logObj.level, logMsg);
      }
      if (logObj.logger.enableStackTrace) {
        if (console.trace) {
          console.trace();
        } else {
          this._consoleOut(logObj.level, logObj.funcTrace.traceDetail);
        }
      }
      if (logObj.logger.enableStackTrace && console.groupEnd) {
        console.groupEnd();
      }
    },
    _consoleOut: function(level, str) {
      var logPrinted = false;
      // 専用メソッドがあればそれを使用して出力
      if ((level == logLevel.ERROR) && console.error) {
        console.error(str);
        logPrinted = true;
      } else if ((level == logLevel.WARN) && console.warn) {
        console.warn(str);
        logPrinted = true;
      } else if ((level == logLevel.INFO) && console.info) {
        console.info(str);
        logPrinted = true;
      } else if ((level == logLevel.DEBUG) && console.debug) {
        console.debug(str);
        logPrinted = true;
      }
      if (!logPrinted && console.log) {
        // this.trace()の場合、または固有メソッドがない場合はlogメソッドで出力
        console.log(str);
      }
    },

    /**
     * 出力するログのプレフィックスを作成します。
     *
     * @memberOf ConsoleLogTarget
     * @private
     * @function
     * @param {Object} logObj ログ情報を保持するオブジェクト
     * @return ログのプレフィックス
     */
    _getLogPrefix: function(logObj) {
      return '[' + logObj.levelString + ']' + logObj.date.getHours() + ':' + logObj.date.getMinutes() + ':' + logObj.date.getSeconds() + ',' + logObj.date.getMilliseconds() + ': ';
    },

    /**
     * 指定されたオブジェクトをコンソールに出力します。
     *
     * @memberOf ConsoleLogTarget
     * @private
     * @function
     * @param {Object} logObj ログ情報を保持するオブジェクト
     */
    _logObj: function(logObj) {
      // 専用メソッドがあればそれを使用して出力
      var args = logObj.args;
      var prefix = this._getLogPrefix(logObj);
      args.unshift(prefix);
      if ((logObj.level == logLevel.ERROR) && console.error) {
        this._output(console.error, args);
      } else if ((logObj.level == logLevel.WARN) && console.warn) {
        this._output(console.warn, args);
      } else if ((logObj.level == logLevel.INFO) && console.info) {
        this._output(console.info, args);
      } else if ((logObj.level == logLevel.DEBUG) && console.debug) {
        this._output(console.debug, args);
      } else {
        this._output(console.log, args);
      }
    },
    _output: function(func, args) {
      if (typeof func.apply !== 'undefined') {
        func.apply(console, args);
        return;
      }
      var msg = '';
      if (!$.isArray(args)) {
        msg += args.toString();
      } else {
        msg += args.join(' ');
      }
      func(msg);
    }
  };

  /**
   * リモートサーバにログ出力するログターゲット
   *
   * @name RemoteLogTarget
   * @constructor
   */
  function RemoteLogTarget() {

    /**
     * ログを受け取るサーバのURL
     *
     * @memberOf RemoteLogTarget
     * @name url
     * @type String
     */
    this.url = null;
  }
  RemoteLogTarget.prototype = {

    /**
     * ログを指定されたURLに送信します。
     *
     * @memberOf RemoteLogTarget
     * @function
     * @param {Object} logObj ログ情報を保持するオブジェクト
     */
    log: function(logObj) {
      if (!this.url) {
        return;
      }
      var args = logObj.args;
      var msg = '';
      if (typeof args[0] !== 'string') {
        var argsLen = args.length;
        for (var i = 0; i < argsLen; i++) {
          // TODO ここはあとでオブジェクトの中身を追ってtoStringする処理を追加する。
          msg += args[0].toString();
        }
      } else {
        if (args.length === 1) {
          msg = args[0];
        } else {
          msg = h5.u.str.format.apply(h5.u.str, args);
        }
      }
      var sendLogObj = {
        lv: logObj.levelString,
        msg: msg,
        date: logObj.date.getTime(),
        funcTrace: logObj.funcTrace
      };
      this._pendingLogs.push(sendLogObj);
      if (this._sendTimerId) {
        clearTimeout(this._sendTimerId);
      }
      this._sendTimerId = setTimeout(function(thisArg) {
        return function() {
          thisArg._sendLog();
        };
      }(this), 500);      // 500ms待って、新たなログ出力がなければ実際にサーバーに送信する
    },

    /**
     * 送信処理を実行したsetTimeout関数のタイマーID
     *
     * @memberOf RemoteLogTarget
     * @private
     * @type Number
     */
    _sendTimerId: null,

    /**
     * 送信対象のログを保持します。
     *
     * @memberOf RemoteLogTarget
     * @private
     * @type Array
     */
    _pendingLogs: [],

    /**
     * ログをサーバに送信します。
     *
     * @memberOf RemoteLogTarget
     * @private
     * @function
     */
    _sendLog: function() {
      this._sendTimerId = null;
      $.post(this.url, {
        logs: this._pendingLogs,
        count: this._pendingLogs.length
      }, 'json');
      this._pendingLogs = [];
    }
  };

  /**
   * ログを生成するクラス
   *
   * @class
   * @name Log
   */
  function Log(category) {

    /**
     * ログカテゴリ。<br>
     * 指定されない場合、nullをセットします。
     *
     * @memberOf Log
     * @type String
     */
    this.category = (category === undefined) ? null : category;

    /**
     * 現在のログレベル。<br>
     * 小さいほど詳細なレベルのログまで出力します。
     *
     * @private
     * @memberOf Log
     * @type Number
     */
    this._levelThreshold = 0;

    /**
     * コンソールログターゲット
     *
     * @name consoleLogTarget
     * @memberOf Log
     */
    this.consoleLogTarget = new ConsoleLogTarget();

    /**
     * リモートログターゲット
     *
     * @name remoteLogTarget
     * @memberOf Log
     */
    this.remoteLogTarget = new RemoteLogTarget();

    /**
     * 出力対象のログターゲット (デフォルトはコンソールにのみ出力する)
     *
     * @name logTarget
     * @memberOf Log
     * @type Array
     */
    this.logTarget = [this.consoleLogTarget];
  }
  Log.prototype = {

    /**
     * ログ出力時、スタックトレース(関数呼び出し関係)を表示するか設定します。<br>
     * (デフォルト: false[表示しない])
     *
     * @type Boolean
     * @memberOf Log
     */
    enableStackTrace: false,

    /**
     * ログに表示するトレースの最大数を設定します。<br>
     * (デフォルト:10)
     *
     * @type Number
     * @memberOf Log
     */
    maxStackSize: 10,

    /**
     * ログレベル
     *
     * @namespace
     * @name LEVEL
     * @memberOf Log
     */
    LEVEL: logLevel,

    /**
     * ログの出力レベルを設定します。
     * <p>
     * ログレベルの値は、以下の名前空間で保持しています。<br>
     * <ul>
     * <li>Log.LEVEL.ERROR</li>
     * <li>Log.LEVEL.WARN</li>
     * <li>Log.LEVEL.INFO</li>
     * <li>Log.LEVEL.DEBUG</li>
     * <li>Log.LEVEL.TRACE</li>
     * <li>Log.LEVEL.ALL</li>
     * </ul>
     *
     * @see Log.LEVEL
     * @memberOf Log
     * @function
     * @param {Number} level ログレベル
     */
    setLevel: function(level) {
      this._levelThreshold = level;
    },

    /**
     * LEVEL.ERROR レベルのログを出力します。
     * <p>
     * 引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
     * <p>
     * 書式については、h5.u.str.format関数のドキュメントを参照下さい。
     *
     * @see h5.u.str.format
     * @memberOf Log
     * @function
     * @param {Any} var_args
     */
    error: function(var_args) {
      this._log({
        level: this.LEVEL.ERROR,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.error) : ''
      });
    },

    /**
     * LEVEL.WARN レベルのログを出力します。
     * <p>
     * 引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
     * <p>
     * 書式については、h5.u.str.format関数のドキュメントを参照下さい。
     *
     * @see h5.u.str.format
     * @memberOf Log
     * @function
     * @param {Any} var_args
     */
    warn: function(var_args) {
      this._log({
        level: this.LEVEL.WARN,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.warn) : ''
      });
    },

    /**
     * LEVEL.INFO レベルのログを出力します。
     * <p>
     * 引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
     * <p>
     * 書式については、h5.u.str.format関数のドキュメントを参照下さい。
     *
     * @see h5.u.str.format
     * @memberOf Log
     * @function
     * @param {Any} var_args
     */
    info: function(var_args) {
      this._log({
        level: this.LEVEL.INFO,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.info) : ''
      });
    },

    /**
     * LEVEL.DEBUG レベルのログを出力します。
     * <p>
     * 引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
     * <p>
     * 書式については、h5.u.str.format関数のドキュメントを参照下さい。
     *
     * @see h5.u.str.format
     * @function
     * @memberOf Log
     * @param {Any} var_args
     */
    debug: function(var_args) {
      this._log({
        level: this.LEVEL.DEBUG,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.debug) : ''
      });
    },

    /**
     * LEVEL.TRACE レベルのログを出力します。
     * <p>
     * 引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
     * <p>
     * 書式については、h5.u.str.format関数のドキュメントを参照下さい。
     *
     * @see h5.u.str.format
     * @memberOf Log
     * @function
     * @param {Any} var_args
     */
    trace: function(var_args) {
      this._log({
        level: this.LEVEL.TRACE,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.trace) : ''
      });
    },

    /**
     * スタックトレース(関数呼び出し関係)を取得します。
     *
     * @private
     * @memberOf Log
     * @function
     * @param fn {Function} トレース対象の関数
     * @returns {String} スタックトレース
     */
    _traceFunctionName: function(fn) {
      var e = new Error();
      var errMsg = e.stack || e.stacktrace;
      var result = {
        traceShort: '',
        traceDetail: ''
      };
      var stShort = null;
      var traces = [];
      var index = 0;
      // Chrome, FireFox, Opera
      if (errMsg) {
        traces = errMsg.replace(/\r\n/, '\n').replace(/at\b|@|Error\b|\t|\[arguments not available\]/ig, '').replace(/(http|https|file):.+[0-9]/g, '').replace(/ +/g, ' ').split("\n");
        var ret = null;
        traces = $.map(traces, function(value) {
          if (value.length === 0) {
            ret = null;            // 不要なデータ(Chromeは配列の先頭, FireFoxは配列の末尾に存在する)
          } else if ($.trim(value) === '') {
            ret = '{anonymous}';            // ログとして出力されたが関数名が無い
          } else {
            ret = $.trim(value);
          }
          return ret;
        });
        // Log, LogTargetクラスのトレースはログに出力しないので削除
        traces = traces.slice(2);
        stShort = traces.slice(0, 3).join(' <- ');
        if (traces.length >= 3) {
          stShort += ' ...';
        }
        result.traceShort = stShort;
        result.traceDetail = traces.join('\n');
        // IE, Safari
      } else {
        var currentCaller = fn.caller;
        if (!currentCaller) {
          var str = '{unable to trace}';
          result.traceShort = str;
          result.traceDetail = str;
        } else {
          while (true) 
          {
              var argStr = this._parseArgs(currentCaller.arguments);
              var funcName = this._getFunctionName(currentCaller);
              if (funcName) {
                traces.push('{' + funcName + '}(' + argStr + ')');
              } else {
                if (!currentCaller.caller) {
                  traces.push('{root}(' + argStr + ')');
                } else {
                  traces.push('{anonymous}(' + argStr + ')');
                }
              }
              if (!currentCaller.caller || index >= this.maxStackSize) {
                stShort = traces.slice(0, 3).join(' <- ');
                if (traces.length >= 3) {
                  stShort += ' ...';
                }
                result.traceShort = stShort;
                result.traceDetail = traces.join('\n');
                break;
              }
              currentCaller = currentCaller.caller;
              index++;
            }
        }
      }
      return result;
    },

    /**
     * トレースした関数名を取得します。
     *
     * @private
     * @memberOf Log
     * @function
     * @param fn {Function} 名前を取得したい関数
     * @returns {String} 関数名
     */
    _getFunctionName: function(fn) {
      var ret = "";
      if (!fn.name || fn.name === "") {
        var regExp = /^\s*function\s*([\w\-\$]+)?\s*\(/i;
        regExp.test(fn.toString());
        ret = RegExp.$1;
      } else {
        ret = fn.name;
      }
      return ret;
    },

    /**
     * トレースした関数の引数の型を調べます。
     *
     * @private
     * @memberOf Log
     * @function
     * @param args トレースした関数のarguments
     * @returns {String} 文字列化された型情報
     */
    _parseArgs: function(args) {
      var argArray = h5.u.obj.argsToArray(args);
      var result = [];
      for (var i = 0; i < argArray.length; i++) {
        result.push($.type(argArray[i]));
      }
      return result.join(', ');
    },

    /**
     * ログ情報を保持するオブジェクトに以下の情報を付与し、コンソールまたはリモートサーバにログを出力しま す。
     * <ul>
     * <li>時刻
     * <li>ログの種別を表す文字列(ERROR, WARN, INFO, DEBUG, TRACE, OTHER)
     * </ul>
     *
     * @private
     * @memberOf Log
     * @function
     * @param {Object} logObj ログオブジェクト
     */
    _log: function(logObj) {
      if (logObj.level < this._levelThreshold) {
        return;
      }
      logObj.logger = this;
      logObj.date = new Date();
      logObj.levelString = this._levelToString(logObj.level);
      if (!this.logTarget) {
        return;
      }
      var count = this.logTarget.length;
      for (var i = 0; i < count; i++) {
        this.logTarget[i].log(logObj);
      }
    },

    /**
     * ログレベルを判定して、ログの種別を表す文字列を取得します。
     *
     * @private
     * @memberOf Log
     * @function
     * @param {Object} level
     */
    _levelToString: function(level) {
      if (level == this.LEVEL.ERROR) {
        return 'ERROR';
      } else if (level == this.LEVEL.WARN) {
        return 'WARN';
      } else if (level == this.LEVEL.INFO) {
        return 'INFO';
      } else if (level == this.LEVEL.DEBUG) {
        return 'DEBUG';
      } else if (level == this.LEVEL.TRACE) {
        return 'TRACE';
      } else {
        return 'OTHER';
      }
    }
  };

  /**
   * ロガーを作成します。
   *
   * @param {String} [category=null] カテゴリ.
   * @returns {Log} ロガー.
   * @name createLogger
   * @function
   * @memberOf h5.u
   * @see Log
   */
  var createLogger = function(category) {
    return new Log(category);
  };

  /**
   * 相対URLを絶対URLに変換します。
   *
   * @param {String} relativePath 相対URL
   * @private
   */
  var toAbsoluteUrl = function(relativePath) {
    var e = document.createElement('span');
    e.innerHTML = '<a href="' + relativePath + '" />';
    return e.firstChild.href;
  };
  var addedJS = [];

  /**
   * スクリプトをheadに追加します。 即時に実行させるため、$.getScriptを使用せず、headにSCRIPTタグを挿入している。
   *
   * @todo オプションで2重読み込みも許すようにする。今は2重読み込みは許可していない。
   * @param {String|String[]} src ソースパス
   * @name loadScript
   * @function
   * @memberOf h5.u
   */
  var loadScript = function(src) {
    var resource = $.isArray(src) ? src : [src];
    if (resource.length === 0) {
      return;
    }
    var srcLen = resource.length;
    var $head = $('head');
    for (var i = 0; i < srcLen; i++) {
      var s = resource[i];
      if ($.inArray(s, addedJS) === -1) {
        $head.append('<script type="text/javascript" src="' + s + '"></script>');
        addedJS.push(toAbsoluteUrl(s));
      }
    }
  };

  /**
   * 文字列のプレフィックスが指定したものかどうかを返します。
   *
   * @param {String} str 文字列
   * @param {String} prefix プレフィックス
   * @returns {Boolean} 文字列のプレフィックスが指定したものかどうか
   * @name startsWith
   * @function
   * @memberOf h5.u.str
   */
  var startsWith = function(str, prefix) {
    return str.lastIndexOf(prefix, 0) === 0;
  };

  /**
   * 文字列のサフィックスが指定したものかどうかを返します。
   *
   * @param {String} str 文字列
   * @param {String} suffix サフィックス
   * @returns {Boolean} 文字列のサフィックスが指定したものかどうか
   * @name endsWith
   * @function
   * @memberOf h5.u.str
   */
  var endsWith = function(str, suffix) {
    var sub = str.length - suffix.length;
    return (sub >= 0) && (str.lastIndexOf(suffix) === sub);
  };

  /**
   * 第一引数の文字列に含まれる{0}、{1}、{2}...{n} (nは数字)を、第2引数以降に指定されたパラメータに置換します。
   *
   * <pre>
   * 例：
   * 		var myValue = 10;
   * 		h5.u.str.format('{0} is {1}', 'myValue', myValue);
   * </pre>
   *
   * 実行結果: myValue is 10
   *
   * @param {String} str 文字列
   * @param {Any} var_args 可変長引数
   * @returns {String} フォーマット済み文字列
   * @name format
   * @function
   * @memberOf h5.u.str
   */
  var format = function(str, var_args) {
    var args = arguments;
    return str.replace(/\{(\d)\}/g, function(m, c) {
      return args[parseInt(c, 10) + 1];
    });
  };

  /**
   * 指定されたJavaScript文字列をエスケープします。
   *
   * @param {String} str 対象文字列
   * @returns {String} エスケープされた文字列
   * @name escapeJs
   * @function
   * @memberOf h5.u.str
   */
  var escapeJs = function(str) {
    if ($.type(str) !== 'string') {
      return str;
    }
    return str.replace(/\\/g, '\\\\').replace(/"/g, '"').replace(/'/g, "'").replace(/\//g, '/').replace(/</g, '&#x3c;').replace(/>/g, '&#x3e;').replace(/&#x0d/g, '\r').replace(/&#x0a/g, '\n');
  };

  /**
   * 指定されたHTML文字列をエスケープします。
   *
   * @param {String} str HTML文字列
   * @returns {String} エスケープ済HTML文字列
   * @name escapeHTML
   * @function
   * @memberOf h5.u.str
   */
  var escapeHtml = function(str) {
    if ($.type(str) !== 'string') {
      return str;
    }
    return str.replace(/&/g, "&amp;").replace(/'/g, '&apos;').replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  };

  /**
   * オブジェクトを文字列化します。
   *
   * @param {Object} value オブジェクト
   * @returns {String} 文字列化されたオブジェクト
   * @name serialize
   * @function
   * @memberOf h5.u.obj
   */
  var serialize = function(value, isDeep) {
    var deepFlg = isDeep === undefined ? true : isDeep;
    var func = function parse(val) {
      var ret = val;
      var type = '(' + $.type(val) + ')';
      try {
        switch (type) {
          case '(number)':
            if (($.isNaN && $.isNaN(val)) || ($.isNumeric && !$.isNumeric(val))) {
              if (val === Infinity) {
                ret = '(infinity)';
              } else if (val === -Infinity) {
                ret = '(-infinity)';
              } else {
                ret = '(nan)';
              }
            }
            break;
          case '(date)':
          case '(regexp)':
          case '(function)':
            ret = type + ret.toString();
            break;
          case '(array)':
          case '(object)':
            ret = type + JSON.stringify(val, function(retK, retV) {
              if (retV === val) {
                if ($.isArray(retV)) {
                  for (var i = 0, len = retV.length; i < len; i++) {
                    if (retV[i] === undefined) {
                      retV[i] = deepFlg ? '(undefined)' : 'undefined';
                    }
                  }
                }
                return retV;
              }
              if (deepFlg) {
                return func(val[retK]);
              }
              // 値がundefinedだとキーが消えてしまうため、文字列'undefined'を返す
              if ($.type(retV) === 'undefined') {
                return 'undefined';
              }
              // Function型の場合は文字列で返す
              if ($.isFunction(retV)) {
                return '' + retV;
              }
              return retV;
            });
            break;
          case '(null)':
          case '(undefined)':
            ret = type;
            break;
        }
        ret = JSON.stringify(ret);
      }
      catch (e) {
              //
}
      return ret;
    };
    return func(value);
  };

  /**
   * 文字列化されたオブジェクトを復元します。
   *
   * @param {String} value 文字列化されたオブジェクト
   * @returns {Object} 復元されたオブジェクト
   * @name deserialize
   * @function
   * @memberOf h5.u.obj
   */
  var deserialize = function(value, isDeep) {
    var deepFlg = isDeep === undefined ? true : isDeep;
    var ret = value;
    var func = function(val) {
      try {
        ret = JSON.parse(val);
        if ($.type(ret) === 'string') {
          var repPos = ret.indexOf(')');
          var clazz = ret.substring(ret.indexOf('(') + 1, repPos);
          var value = ret.substring(repPos + 1, ret.length);
          if (clazz !== undefined && clazz !== '') {
            switch (clazz) {
              case 'array':
              case 'object':
                var obj = JSON.parse(value);
                for (var key in obj) {
                  obj[key] = deepFlg ? func(obj[key]) : '' + obj[key];
                }
                ret = obj;
                break;
              case 'date':
                ret = new Date(value);
                break;
              case 'regexp':
                ret = new RegExp(value.replace(/\//g, ''));
                break;
              case 'null':
                ret = null;
                break;
              case 'undefined':
                ret = undefined;
                break;
              case 'nan':
                ret = NaN;
                break;
              case 'infinity':
                ret = Infinity;
                break;
              case '-infinity':
                ret = -Infinity;
                break;
              case 'function':
                var args = value.substring(value.indexOf('(') + 1, value.indexOf(')'));
                var body = value.substring(value.indexOf('{') + 1, value.lastIndexOf('}'));
                var argArray = [];
                if (args) {
                  argArray = args.split(',');
                }
                ret = new Function(argArray, body);
                break;
            }
          }
        }
      }
      catch (e) {
              //
}
      return ret;
    };
    return func(value);
  };

  /**
   * オブジェクトがjQueryオブジェクトかどうかを返します。
   *
   * @param {Object} obj オブジェクト
   * @returns {Boolean} jQueryオブジェクトかどうか
   * @name isJQueryObject
   * @function
   * @memberOf h5.u.obj
   */
  var isJQueryObject = function(obj) {
    if (!obj.jquery) {
      return false;
    }
    return (obj.jquery === $().jquery);
  };

  /**
   * argumentsを配列に変換します。
   *
   * @param {Arguments} args Arguments
   * @returns {Any[]} argumentsを変換した配列
   * @name argsToArray
   * @function
   * @memberOf h5.u.obj
   */
  var argsToArray = function(args) {
    return Array.prototype.slice.call(args);
  };

  /**
   * ユーザエージェントからiOSであるかどうかを返します。
   *
   * @returns {Boolean} 実行中の端末がiOSであるかどうか
   * @name isIOS
   * @function
   * @memberOf h5.u.ua
   */
  var isIOS = function() {
    var ua = navigator.userAgent;
    return !!ua.match(/iPhone/i) || ua.match(/iPad/i);
  };

  /**
   * ユーザエージェントからAndroidであるかどうかを返します。
   *
   * @returns {Boolean} 実行中の端末がAndroidであるかどうか
   * @name isAndroid
   * @function
   * @memberOf h5.u.ua
   */
  var isAndroid = function() {
    var ua = navigator.userAgent.toLowerCase();
    return !!ua.match(/android/i);
  };
  h5.u.obj.expose('h5.u', {
    createLogger: createLogger,
    loadScript: loadScript
  });

  /**
   * @namespace
   * @name str
   * @memberOf h5.u
   */
  h5.u.obj.expose('h5.u.str', {
    startsWith: startsWith,
    endsWith: endsWith,
    format: format,
    escapeJs: escapeJs,
    escapeHtml: escapeHtml
  });

  /**
   * @namespace
   * @name obj
   * @memberOf h5.u
   */
  h5.u.obj.expose('h5.u.obj', {
    serialize: serialize,
    deserialize: deserialize,
    isJQueryObject: isJQueryObject,
    argsToArray: argsToArray
  });

  /**
   * @namespace
   * @name ua
   * @memberOf h5.u
   */
  h5.u.obj.expose('h5.u.ua', {
    isIOS: isIOS,
    isAndroid: isAndroid
  });
  // #delete begin
})(window, jQuery);
// #delete end
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */

/**
 * h5.async名前空間
 *
 * @param {Object} window
 * @param {Object} jQuery
 */
(function(window, $) {
  var getCommonFailHandler = function() {
    return h5.settings.commonFailHandler;
  };
  var jqVersion = parseFloat($().jquery);

  /**
   * 登録された共通のエラー処理を実行できるDeferredオブジェクトを返します。<br>
   * 使用しているjQueryのバージョンが1.7以降の場合は、Deferredに notify() / notifyWith() / progress()を、
   * Deferred.Promiseにprogress()を追加したDeferredオブジェクトを返します。
   *
   * @returns {Deferred} Deferredオブジェクト
   * @name deferred
   * @function
   * @memberOf h5.async
   */
  var deferred = function() {
    var dfd = $.Deferred();
    // 1.6対応
    if (1.7 > jqVersion) {
      var notified = false;
      var lastNotifyContext = null;
      var lastNotifyParam = null;
      dfd.__h5__progressCallbacks = [];
      var progress = function(progressCallback) {
        if (notified) {
          progressCallback.apply(lastNotifyContext, lastNotifyParam);
        }
        dfd.__h5__progressCallbacks.push(progressCallback);
        return this;
      };
      dfd.progress = progress;
      var originalPromise = dfd.promise;
      dfd.promise = function(obj) {
        var promise = originalPromise.call(this, obj);
        promise.progress = progress;
        return promise;
      };
      dfd.notify = function(var_args) {
        notified = true;
        if (arguments.length !== -1) {
          lastNotifyContext = dfd;
          lastNotifyParam = h5.u.obj.argsToArray(arguments);
        }
        var callbacks = dfd.__h5__progressCallbacks;
        var callbackLen = callbacks.length;
        for (var i = 0; i < callbackLen; i++) {
          callbacks[i].apply(this, arguments);
        }
        return this;
      };
      dfd.notifyWith = function(context, args) {
        notified = true;
        lastNotifyContext = context;
        lastNotifyParam = args;
        var callbacks = dfd.__h5__progressCallbacks;
        var callbackLen = callbacks.length;
        for (var i = 0; i < callbackLen; i++) {
          callbacks[i].apply(context, args);
        }
        return this;
      };
    }
    var existFailHandler = false;
    var originalFail = dfd.fail;
    var fail = function(var_args) {
      if (arguments.length > 0) {
        existFailHandler = true;
      }
      return originalFail.apply(this, arguments);
    };
    dfd.fail = fail;
    var originalAlways = dfd.always;
    var always = function(var_args) {
      if (arguments.length > 0) {
        existFailHandler = true;
      }
      return originalAlways.apply(dfd, arguments);
    };
    dfd.always = always;
    var then = function(doneCallback, failCallback, progressCallback) {
      if (doneCallback) {
        this.done(doneCallback);
      }
      if (failCallback) {
        existFailHandler = true;
        this.fail(failCallback);
      }
      if (progressCallback) {
        this.progress(progressCallback);
      }
      return this;
    };
    dfd.then = then;
    var originalReject = dfd.reject;
    var reject = function(var_args) {
      var commonFailHandler = getCommonFailHandler();
      if (!existFailHandler && commonFailHandler) {
        originalFail.call(this, commonFailHandler);
      }
      return originalReject.apply(this, arguments);
    };
    dfd.reject = reject;
    var originalRejectWith = dfd.rejectWith;
    var rejectWith = function(var_args) {
      var commonFailHandler = getCommonFailHandler();
      if (!existFailHandler && commonFailHandler) {
        this.fail(commonFailHandler);
      }
      return originalRejectWith.apply(this, arguments);
    };
    dfd.rejectWith = rejectWith;
    var p = dfd.promise;
    dfd.promise = function(obj) {
      var promise = p.call(this, obj);
      promise.always = always;
      promise.then = then;
      promise.fail = fail;
      return promise;
    };
    return dfd;
  };

  /**
   * オブジェクトがプロミスであるかどうかを返します。
   *
   * @param {Object} object オブジェクト
   * @returns {Boolean} オブジェクトがプロミスであるかどうか
   * @name isPromise
   * @function
   * @memberOf h5.async
   */
  var isPromise = function(object) {
    return object != null && object['done'] && object['fail'] && !object['resolve'] && !object['reject'];
  };
  function OrderedFunction(thisArg, func, args, retry) {
    this.that = thisArg;
    this.func = func;
    this.args = args;
    this.retry = retry;
  }

  /**
   * Orderクラスのコンストラクタ
   *
   * @class
   * @name Order
   * @param {Object} thisArg 実行時にthisとしたいオブジェクト.
   */
  function Order(thisArg) {
    this.that = thisArg;
    this.resultObj = {
      data: []
    };
    this.orderFunctions = [];
  }
  var constructor = Order.prototype.constructor;
  Order.prototype = {
    constructor: constructor,

    /**
     * 関数をセットします。
     *
     * @memberOf Order
     * @param {Function} func 関数.
     * @param {Any|Any[]} [args] 関数に渡す引数.
     * @returns {Order} Orderオブジェクト
     */
    next: function(func, args) {
      var of;
      if (this._isOrder(func)) {
        of = this._createOrderedFunction(func, func.execute);
      } else {
        of = this._createOrderedFunction(null, func, args);
      }
      this.orderFunctions.push(of);
      return this;
    },

    /**
     * 関数をセットします。<br />
     * 関数が実行されるとresultObjの指定したキーに戻り値をセットします。
     *
     * @memberOf Order
     * @param {String} key キー.
     * @param {Function} func 関数.
     * @param {Any|Any[]} [args] 関数に渡す引数.
     * @returns {Order} Orderオブジェクト
     */
    nextAndSet: function(key, func, args) {
      var of;
      if (this._isOrder(func)) {
        of = this._createOrderedFunction(func, this._wrap(key, func.execute));
      } else {
        of = this._createOrderedFunction(null, this._wrap(key, func), args);
      }
      this.orderFunctions.push(of);
      return this;
    },

    /**
     * 関数と実行コンテキストをセットします。
     *
     * @memberOf Order
     * @param {Object} thisArg 実行時にthisとしたいオブジェクト.
     * @param {Function} func 関数.
     * @param {Any|Any[]} [args] 関数に渡す引数.
     * @returns {Order} Orderオブジェクト
     */
    nextWith: function(thisArg, func, args) {
      var of = this._createOrderedFunction(thisArg, func, args);
      this.orderFunctions.push(of);
      return this;
    },

    /**
     * 関数と実行コンテキストをセットします。<br />
     * 関数が実行されるとresultObjの指定したキーに戻り値をセットします。
     *
     * @memberOf Order
     * @param {String} key キー.
     * @param {Object} thisArg 実行時にthisとしたいオブジェクト.
     * @param {Function} func 関数.
     * @param {Any|Any[]} [args] 関数に渡す引数.
     * @returns {Order} Orderオブジェクト
     */
    nextAndSetWith: function(key, thisArg, func, args) {
      var of = this._createOrderedFunction(thisArg, this._wrap(key, func), args);
      this.orderFunctions.push(of);
      return this;
    },

    /**
     * パラレルに実行する関数をセットします。
     *
     * @memberOf Order
     * @param {Any[]} functions 実行したい関数セットの配列.<br />
     *            関数セットは配列で[Function, Any|Any[]]となる.
     * @returns {Order} Orderオブジェクト
     */
    parallel: function(functions) {
      var funcLen = functions.length;
      var array = [];
      for (var i = 0; i < funcLen; i++) {
        var func = functions[i];
        var of;
        if (this._isOrder(func)) {
          of = this._createOrderedFunction(func, func.execute);
        } else {
          var params;
          if ($.isArray(func)) {
            func.unshift(null);
            params = func;
          } else {
            params = [null, func];
          }
          of = this._createOrderedFunction.apply(this, params);
        }
        array.push(of);
      }
      this.orderFunctions.push(array);
      return this;
    },

    /**
     * パラレルに実行する関数をセットします。<br />
     * 関数が実行されるとresultObjの指定したキーに戻り値をセットします。
     *
     * @memberOf Order
     * @param {Any[]} functions 実行したい関数セットの配列.<br />
     *            関数セットは配列で[String, Function, Any|Any[]]となる.
     * @returns {Order} Orderオブジェクト
     */
    parallelAndSet: function(functions) {
      var funcLen = functions.length;
      var array = [];
      for (var i = 0; i < funcLen; i++) {
        var params = functions[i];
        var key = params[0];
        var func = params[1];
        var of;
        if (this._isOrder(func)) {
          of = this._createOrderedFunction(func, this._wrap(key, func.execute));
        } else {
          of = this._createOrderedFunction(null, this._wrap(key, func), params[2]);
        }
        array.push(of);
      }
      this.orderFunctions.push(array);
      return this;
    },

    /**
     * パラレルに実行する関数と実行コンテキストをセットします。
     *
     * @memberOf Order
     * @param {Any[]} functions 実行したい関数セットの配列.<br />
     *            関数セットは配列で[Object, Function, Any|Any[]]となる.
     * @returns {Order} Orderオブジェクト
     */
    parallelWith: function(functions) {
      var funcLen = functions.length;
      var array = [];
      for (var i = 0; i < funcLen; i++) {
        var of = this._createOrderedFunction.apply(this, functions[i]);
        array.push(of);
      }
      this.orderFunctions.push(array);
      return this;
    },

    /**
     * パラレルに実行する関数と実行コンテキストをセットします。<br />
     * 関数が実行されるとresultObjの指定したキーに戻り値をセットします。
     *
     * @memberOf Order
     * @param {Any[]} functions 実行したい関数セットの配列.<br />
     *            関数セットは配列で[String, Object, Function, Any|Any[]]となる.
     * @returns {Order} Orderオブジェクト
     */
    parallelAndSetWith: function(functions) {
      var funcLen = functions.length;
      var array = [];
      for (var i = 0; i < funcLen; i++) {
        var params = functions[i];
        var key = params[0];
        var of = this._createOrderedFunction(params[1], this._wrap(key, params[2]), params[3]);
        array.push(of);
      }
      this.orderFunctions.push(array);
      return this;
    },

    /**
     * 指定した回数分、リトライを行う関数をセットします。
     *
     * @memberOf Order
     * @param {Function} func 関数.
     * @param {Any|Any[]} args 関数に渡す引数.
     * @param {Number} retry リトライ回数.
     * @returns {Order} Orderオブジェクト
     */
    retry: function(func, args, retry) {
      var of;
      if (this._isOrder(func)) {
        of = this._createOrderedFunction(func, func.execute, null, retry);
      } else {
        of = this._createOrderedFunction(null, func, args, retry);
      }
      this.orderFunctions.push(of);
      return this;
    },

    /**
     * 指定した回数分、リトライを行う関数をセットします。<br />
     * 関数が実行されるとresultObjの指定したキーに戻り値をセットします。
     *
     * @memberOf Order
     * @param {String} key キー.
     * @param {Function} func 関数.
     * @param {Any|Any[]} args 関数に渡す引数.
     * @param {Number} retry リトライ回数.
     * @returns {Order} Orderオブジェクト
     */
    retryAndSet: function(key, func, args, retry) {
      var of;
      if (this._isOrder(func)) {
        of = this._createOrderedFunction(func, this._wrap(key, func), null, retry);
      } else {
        of = this._createOrderedFunction(null, this._wrap(key, func), args, retry);
      }
      this.orderFunctions.push(of);
      return this;
    },

    /**
     * 指定した回数分、リトライを行う関数と実行コンテキストをセットします。
     *
     * @memberOf Order
     * @param {Object} thisArg 実行時にthisとしたいオブジェクト.
     * @param {Function} func 関数.
     * @param {Any|Any[]} args 関数に渡す引数.
     * @param {Number} retry リトライ回数.
     * @returns {Order} Orderオブジェクト
     */
    retryWith: function(thisArg, func, args, retry) {
      var of = this._createOrderedFunction(thisArg, func, args, retry);
      this.orderFunctions.push(of);
      return this;
    },

    /**
     * 指定した回数分、リトライを行う関数と実行コンテキストをセットします。<br />
     * 関数が実行されるとresultObjの指定したキーに戻り値をセットします。
     *
     * @memberOf Order
     * @param {String} key キー.
     * @param {Object} thisArg 実行時にthisとしたいオブジェクト.
     * @param {Function} func 関数.
     * @param {Any|Any[]} args 関数に渡す引数.
     * @param {Number} retry リトライ回数.
     * @returns {Order} Orderオブジェクト
     */
    retryAndSetWith: function(key, thisArg, func, args, retry) {
      var of = this._createOrderedFunction(thisArg, this._wrap(key, func), args, retry);
      this.orderFunctions.push(of);
      return this;
    },
    _createOrderedFunction: function(thisArg, func, args, retry) {
      var param;
      if (args == null) {
        param = this.resultObj;
      } else if ($.isArray(args)) {
        args.unshift(this.resultObj);
        param = args;
      } else {
        param = [this.resultObj, args];
      }
      var context = thisArg ? thisArg : this.that;
      return new OrderedFunction(context, func, param, retry);
    },

    /**
     * セットされた関数を実行する.
     *
     * @memberOf Order
     * @returns {Promise} Promiseオブジェクト.
     */
    execute: function() {
      var ofLen = this.orderFunctions.length;
      if (ofLen === 0) {
        return;
      }
      var rootDfd = deferred();
      var that = this;
      var execute = function(index) {
        var of = that.orderFunctions[index];
        var asyncPromise;
        if (of.retry && of.retry > 1) {
          asyncPromise = that._async(that, that._executeRetry, of);
        } else if ($.isArray(of)) {
          asyncPromise = that._async(that, that._executePara, of);
        } else {
          asyncPromise = that._async(that, that._execute, of);
        }
        asyncPromise.always(function(ret) {
          if (!isPromise(ret)) {
            that.resultObj.data[index] = ret;
            if (index === (ofLen - 1)) {
              rootDfd.resolve(that.resultObj);
            } else {
              execute(index + 1);
            }
          } else {
            ret.done(function(var_args) {
              that.resultObj.data[index] = that._getArgs(arguments);
              if (index === (ofLen - 1)) {
                rootDfd.resolve(that.resultObj);
              } else {
                execute(index + 1);
              }
            }).fail(function(var_args) {
              that.resultObj.data[index] = that._getArgs(arguments);
              rootDfd.reject(that.resultObj);
            });
          }
        });
      };
      execute(0);
      return rootDfd.promise();
    },
    _async: function(thisArg, func, args) {
      var dfd = h5.async.deferred();
      setTimeout(function() {
        dfd.resolve(func.call(thisArg, args));
      }, 0);
      return dfd.promise();
    },
    _execute: function(of) {
      return $.isArray(of.args) ? of.func.apply(of.that, of.args) : of.func.call(of.that, of.args);
    },
    _executePara: function(ofs) {
      var rootDfd = $.Deferred();
      var array = [];
      var argsLen = ofs.length;
      for (var i = 0; i < argsLen; i++) {
        var of = ofs[i];
        array.push(this._execute(of));
      }
      $.when.apply($, array).then(function(var_args) {
        return rootDfd.resolveWith(this, arguments);
      }, function(var_args) {
        return rootDfd.rejectWith(this, arguments);
      });
      return rootDfd.promise();
    },
    _executeRetry: function(of) {
      var dfd = deferred();
      var that = this;
      var retry = of.retry;
      var execute = function(current, obj) {
        var ret = that._execute(obj);
        if (!isPromise(ret)) {
          return dfd.resolve(ret);
        }
        ret.done(function(var_args) {
          return dfd.resolveWith(this, arguments);
        }).fail(function(var_args) {
          if (current === retry) {
            return dfd.rejectWith(this, arguments);
          }
          execute(current + 1, obj);
        });
      };
      execute(1, of);
      return dfd.promise();
    },
    _getArgs: function(args) {
      if (args == null || args.length === 0) {
        return;
      }
      if (args.length === 1) {
        return args[0];
      }
      return h5.u.obj.argsToArray(args);
    },
    _wrap: function(key, func) {
      var that = this;
      return function(resultObj, var_args) {
        var dfd = deferred();
        var array = h5.u.obj.argsToArray(arguments);
        var ret;
        if (array.length > 1) {
          ret = func.apply(this, array.slice(1));
        } else {
          ret = func.apply(this);
        }
        if (!isPromise(ret)) {
          resultObj[key] = ret;
          dfd.resolve(ret);
        } else {
          ret.done(function(var_arg) {
            resultObj[key] = that._getArgs(arguments);
            dfd.resolve(resultObj[key]);
          }).fail(function(var_arg) {
            resultObj[key] = that._getArgs(arguments);
            dfd.reject(resultObj[key]);
          });
        }
        return dfd.promise();
      };
    },
    _isOrder: function(obj) {
      return Order.prototype.constructor === obj.constructor;
    }
  };

  /**
   * Orderオブジェクトを作成します。
   *
   * @param {Object} [thisArg=null] thisとして実行したいオブジェクト.
   * @returns {Order} Orderオブジェクト
   * @name order
   * @function
   * @memberOf h5.async
   */
  var order = function(thisArg) {
    return new Order(thisArg);
  };

  /**
   * @namespace
   * @name async
   * @memberOf h5
   */
  h5.u.obj.expose('h5.async', {
    deferred: deferred,
    isPromise: isPromise,
    order: order
  });
  // TODO ここからOrder2の定義
  // Order2にはOrder2クラスのネスト、リトライ機能は実装していない。

  /**
   * Order2クラスのコンストラクタ
   */
  function Order2(thisArg) {
    this.that = thisArg;
    this.resultObj = {
      data: []
    };
    this.orderFunctions = [];
  }
  var constructor = Order2.prototype.constructor;
  Order2.prototype = {
    constructor: constructor,

    /**
     * 関数をセットします。
     */
    next: function(param) {
      var of;
      if ($.isFunction(param)) {
        of = this._createOrderedFunction(null, param);
      } else {
        var useResult = param.useResult !== false;
        var resultKey = $.trim(param.resultKey);
        var func = param.func;
        if (resultKey && resultKey.length > 0) {
          func = this._wrap(resultKey, func, useResult);
        }
        of = this._createOrderedFunction(param.thisArg, func, param.args, useResult);
      }
      this.orderFunctions.push(of);
      return this;
    },

    /**
     * パラレルに実行する関数をセットします。
     */
    parallel: function(functions) {
      var funcLen = functions.length;
      var array = [];
      for (var i = 0; i < funcLen; i++) {
        var param = functions[i];
        var of;
        if ($.isFunction(param)) {
          of = this._createOrderedFunction(null, param);
        } else {
          var useResult = param.useResult !== false;
          var resultKey = $.trim(param.resultKey);
          var func = param.func;
          if (resultKey && resultKey.length > 0) {
            func = this._wrap(resultKey, func, useResult);
          }
          of = this._createOrderedFunction(param.thisArg, func, param.args, useResult);
        }
        array.push(of);
      }
      this.orderFunctions.push(array);
      return this;
    },
    _createOrderedFunction: function(thisArg, func, args, useResult, retry) {
      var param = null;
      if (args == null) {
        if (useResult) {
          param = this.resultObj;
        }
      } else if ($.isArray(args)) {
        if (useResult) {
          args.unshift(this.resultObj);
        }
        param = args;
      } else {
        param = [];
        if (useResult) {
          param.push(this.resultObj);
        }
        param.push(args);
      }
      var context = thisArg ? thisArg : this.that;
      return new OrderedFunction(context, func, param, retry);
    },

    /**
     * セットされた関数を実行する.
     *
     * @returns {Promise} Promiseオブジェクト.
     */
    execute: function() {
      var ofLen = this.orderFunctions.length;
      if (ofLen === 0) {
        return;
      }
      var rootDfd = deferred();
      var that = this;
      var execute = function(index) {
        var of = that.orderFunctions[index];
        var asyncPromise;
        if (of.retry && of.retry > 1) {
          asyncPromise = that._async(that, that._executeRetry, of);
        } else if ($.isArray(of)) {
          asyncPromise = that._async(that, that._executePara, of);
        } else {
          asyncPromise = that._async(that, that._execute, of);
        }
        asyncPromise.always(function(ret) {
          if (!isPromise(ret)) {
            that.resultObj.data[index] = ret;
            if (index === (ofLen - 1)) {
              rootDfd.resolve(that.resultObj);
            } else {
              execute(index + 1);
            }
          } else {
            ret.done(function(var_args) {
              that.resultObj.data[index] = that._getArgs(arguments);
              if (index === (ofLen - 1)) {
                rootDfd.resolve(that.resultObj);
              } else {
                execute(index + 1);
              }
            }).fail(function(var_args) {
              that.resultObj.data[index] = that._getArgs(arguments);
              rootDfd.reject(that.resultObj);
            });
          }
        });
      };
      execute(0);
      return rootDfd.promise();
    },
    _async: function(thisArg, func, args) {
      var dfd = h5.async.deferred();
      setTimeout(function() {
        dfd.resolve(func.call(thisArg, args));
      }, 0);
      return dfd.promise();
    },
    _execute: function(of) {
      return $.isArray(of.args) ? of.func.apply(of.that, of.args) : of.func.call(of.that, of.args);
    },
    _executePara: function(ofs) {
      var rootDfd = $.Deferred();
      var array = [];
      var argsLen = ofs.length;
      for (var i = 0; i < argsLen; i++) {
        var of = ofs[i];
        array.push(this._execute(of));
      }
      $.when.apply($, array).then(function(var_args) {
        return rootDfd.resolveWith(this, arguments);
      }, function(var_args) {
        return rootDfd.rejectWith(this, arguments);
      });
      return rootDfd.promise();
    },
    _executeRetry: function(of) {
      var dfd = deferred();
      var that = this;
      var retry = of.retry;
      var execute = function(current, obj) {
        var ret = that._execute(obj);
        if (!isPromise(ret)) {
          return dfd.resolve(ret);
        }
        ret.done(function(var_args) {
          return dfd.resolveWith(this, arguments);
        }).fail(function(var_args) {
          if (current === retry) {
            return dfd.rejectWith(this, arguments);
          }
          execute(current + 1, obj);
        });
      };
      execute(1, of);
      return dfd.promise();
    },
    _getArgs: function(args) {
      if (args == null || args.length === 0) {
        return;
      }
      if (args.length === 1) {
        return args[0];
      }
      return h5.u.obj.argsToArray(args);
    },
    _wrap: function(key, func, useResult) {
      var that = this;
      var result = this.resultObj;
      return function(resultObj, var_args) {
        var dfd = deferred();
        var array = h5.u.obj.argsToArray(arguments);
        var ret = func.apply(this, array);
        if (!isPromise(ret)) {
          result[key] = ret;
          dfd.resolve(ret);
        } else {
          ret.done(function(var_arg) {
            result[key] = that._getArgs(arguments);
            dfd.resolve(result[key]);
          }).fail(function(var_arg) {
            result[key] = that._getArgs(arguments);
            dfd.reject(result[key]);
          });
        }
        return dfd.promise();
      };
    },
    _isOrder: function(obj) {
      return Order.prototype.constructor === obj.constructor;
    }
  };

  /**
   * Orderオブジェクトを作成します。
   */
  var order2 = function(thisArg) {
    return new Order2(thisArg);
  };

  /**
   * @namespace
   * @name async
   * @memberOf h5
   */
  h5.u.obj.expose('h5.async', {
    order2: order2
  });
})(window, jQuery);
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */

/**
 * 基本はこれ(オブジェクトが1つあればよいパターン)
 *
 * @param {Object} jQuery
 */
(function(window, $) {
  // #delete end
  // CSSファイルから読み込んだCanvas用のスタイル定義をキャッシュする変数
  var throbberStyle = {};
  var canvasCSSClassNamePrefix = 'throbber-';
  var readCSSPropertyMap = {
    base: ['width', 'height', 'textAlign'],
    line: ['width', 'color'],
    percent: ['textAlign', 'font', 'color']
  };
  var throbberLineCoords = null;
  function readCSSCanvasStyle(cssClass) {
    if (throbberStyle[cssClass]) {
      return throbberStyle[cssClass];
    }
    var readStyles = {};
    for (var prop in readCSSPropertyMap) {
      var elem = $('<div></div>').addClass(cssClass).addClass(canvasCSSClassNamePrefix + prop).appendTo('body');
      readStyles[prop] = {};
      $.map(readCSSPropertyMap[prop], function(item, idx) {
        if (item === 'width' || item === 'height') {
          readStyles[prop][item] = parseInt(elem.css(item).replace(/\D/g, ''), 10);
        } else {
          readStyles[prop][item] = elem.css(item);
        }
      });
      elem.remove();
    }
    throbberStyle[cssClass] = readStyles;
    return readStyles;
  }
  var createVMLElement = function(tagName, option) {
    var elem = window.document.createElement('v:' + tagName);
    for (var prop in option) {
      elem.style[prop] = option[prop];
    }
    return elem;
  };
  var isCanvasSupported = function() {
    return !!document.createElement("canvas").getContext;
  };
  var isVMLSupported = function() {
    var tmp = document.body.appendChild(document.createElement('div'));
    tmp.innerHTML = '<v:shape adj="1" />';
    var tmpChild = tmp.firstChild;
    tmpChild.style.behavior = "url(#default#VML)";
    tmp.parentNode.removeChild(tmp);
    return tmpChild ? typeof tmpChild.adj == "object" : true;
  };
  var calculateLineCoords = function(size, line) {
    if (throbberLineCoords) {
      return throbberLineCoords;
    }
    var positions = [];
    var centerPos = size / 2;
    var radius = size * 0.8 / 2;
    var eachRadian = 360 / line * Math.PI / 180;
    for (var j = 1, n = line; j <= n; j++) {
      var rad = eachRadian * j;
      var cosRad = Math.cos(rad),
        sinRad = Math.sin(rad);
      positions.push({
        from: {
          x: centerPos + radius / 2 * cosRad,
          y: centerPos + radius / 2 * sinRad
        },
        to: {
          x: centerPos + radius * cosRad,
          y: centerPos + radius * sinRad
        }
      });
    }
    throbberLineCoords = positions;
    return positions;
  };
  $(function() {
    if (!isCanvasSupported() && isVMLSupported()) {
      document.namespaces.add('v', 'urn:schemas-microsoft-com:vml');
      document.createStyleSheet().cssText = ['v\\:stroke', 'v\\:line', 'v\\:textbox'].join(',') + '{behavior: url(#default#VML);}';
    }
  });

  /**
   * VML版スロバー (IE 6,7,8)用
   */
  var ThrobberVML = function(option) {
    this.style = $.extend(true, {}, option);
    var w = this.style.base.width;
    var h = this.style.base.height;
    this.group = createVMLElement('group', {
      width: w + 'px',
      height: h + 'px',
      display: 'inline-block',
      verticalAlign: 'middle',
      textAlign: 'left'      // IEのバグ回避
    });
    this.group.coordsize = w + ',' + h;
    this.group.coordorigin = '0,0';
    this.positions = calculateLineCoords(w, this.style.base.lines);
    var positions = this.positions;
    var lineColor = this.style.line.color;
    var lineWidth = this.style.line.width;
    for (var i = 0, n = positions.length; i < n; i++) {
      var position = positions[i];
      var from = position.from;
      var to = position.to;
      var e = createVMLElement('line');
      e.strokeweight = lineWidth;
      e.strokecolor = lineColor;
      e.fillcolor = lineColor;
      e.from = from.x + ',' + from.y;
      e.to = to.x + ',' + to.y;
      var ce = createVMLElement('stroke');
      ce.opacity = 1.0;
      e.appendChild(ce);
      this.group.appendChild(e);
    }
    this._createPercentArea();
  };
  ThrobberVML.prototype = {
    show: function(parent) {
      if (!parent) {
        return;
      }
      this.hide();
      this.highlightPos = 1;
      this.parent = parent;
      this.parent.appendChild(this.group);
      this._run();
    },
    hide: function() {
      if (!this.parent) {
        return;
      }
      this.parent.innerHTML = "";
      if (this._runId) {
        clearTimeout(this._runId);
        this._runId = null;
      }
    },
    _run: function() {
      var lineCount = this.style.base.lines;
      var roundTime = this.style.base.roundTime;
      var highlightPos = this.highlightPos;
      var lines = this.group.childNodes;
      for (var i = 0, n = lines.length; i < n; i++) {
        var child = lines[i];
        if (child.nodeName === 'textbox') {
          continue;
        }
        var lineNum = i + 1;
        var line = child.firstChild;
        if (lineNum == highlightPos) {
          line.opacity = "1.0";
        } else if (lineNum == highlightPos + 1 || lineNum == highlightPos - 1) {
          line.opacity = "0.75";
        } else {
          line.opacity = "0.4";
        }
      }
      if (highlightPos == lineCount) {
        highlightPos = 0;
      } else {
        highlightPos++;
      }
      this.highlightPos = highlightPos;
      var perMills = Math.floor(roundTime / lineCount);
      if (perMills < 50) {
        perMills = 50;
      }
      var that = this;
      this._runId = setTimeout(function() {
        that._run.call(that);
      }, perMills);
    },
    _createPercentArea: function() {
      var textPath = createVMLElement('textbox', this.style.percent);
      var table = $('<table><tr><td></td></tr></table>');
      table.find('td').addClass('throbber');
      textPath.appendChild(table[0]);
      this.group.appendChild(textPath);
    },
    setPercent: function(percent) {
      if ($.type(percent) !== 'number') {
        return;
      }
      $(this.group).find('.throbber').html(percent);
    }
  };

  /**
   * Canvas版スロバー
   */
  var ThrobberCanvas = function(option) {
    this.style = $.extend(true, {}, option);
    this.canvas = document.createElement('canvas');
    this.pcanvas = document.createElement('canvas');
    this.baseDiv = document.createElement('div');
    // CSSファイルから読み取ったスタイルを適用する
    this.canvas.width = this.pcanvas.width = this.style.base.width;
    this.canvas.height = this.pcanvas.height = this.style.base.height;
    this.canvas.style.display = this.pcanvas.style.display = 'block';
    this.baseDiv.style.width = this.style.base.width + 'px';
    this.baseDiv.style.height = this.style.base.height + 'px';
    this.positions = calculateLineCoords(this.canvas.width, this.style.base.lines);
  };
  ThrobberCanvas.prototype = {
    show: function(parent) {
      if (!parent) {
        return;
      }
      this.parent = parent;
      this.hide();
      this.canvas.style.position = "absolute";
      this.pcanvas.style.position = "absolute";
      this.baseDiv.appendChild(this.canvas);
      this.baseDiv.appendChild(this.pcanvas);
      parent.appendChild(this.baseDiv);
      this.highlightPos = 1;
      this._run();
    },
    hide: function() {
      if (!this.parent) {
        return;
      }
      this.parent.innerHTML = "";
      if (this._runId) {
        clearTimeout(this._runId);
        this._runId = null;
      }
    },
    _run: function() {
      var canvas = this.canvas;
      var ctx = canvas.getContext('2d');
      var highlightPos = this.highlightPos;
      var positions = this.positions;
      var lineColor = this.style.line.color;
      var lineWidth = this.style.line.width;
      var lineCount = this.style.base.lines;
      var roundTime = this.style.base.roundTime;
      canvas.width = canvas.width;
      for (var i = 0, n = positions.length; i < n; i++) {
        ctx.beginPath();
        ctx.strokeStyle = lineColor;
        ctx.lineWidth = lineWidth;
        var lineNum = i + 1;
        if (lineNum == highlightPos) {
          ctx.globalAlpha = 1;
        } else if (lineNum == highlightPos + 1 || lineNum == highlightPos - 1) {
          ctx.globalAlpha = 0.75;
        } else {
          ctx.globalAlpha = 0.4;
        }
        var position = positions[i];
        var from = position.from;
        var to = position.to;
        ctx.moveTo(from.x, from.y);
        ctx.lineTo(to.x, to.y);
        ctx.stroke();
      }
      if (highlightPos == lineCount) {
        highlightPos = 0;
      } else {
        highlightPos++;
      }
      this.highlightPos = highlightPos;
      var perMills = Math.floor(roundTime / lineCount);
      if (perMills < 50) {
        perMills = 50;
      }
      var that = this;
      this._runId = setTimeout(function() {
        that._run.call(that);
      }, perMills);
    },
    setPercent: function(percent) {
      if ($.type(percent) !== 'number') {
        return;
      }
      this.pcanvas.width = this.pcanvas.width;
      var style = this.style.percent;
      var ctx = this.pcanvas.getContext('2d');
      var centerPosX = this.pcanvas.width / 2;
      var centerPosY = this.pcanvas.height / 2;
      var diameter = this.pcanvas.width * 0.8;
      var radius = diameter / 2;
      ctx.fillStyle = style.color;
      for (var prop in style) {
        if (ctx[prop]) {
          ctx[prop] = style[prop];
        }
      }
      ctx.fillText(percent, centerPosX, centerPosY + (radius / 5), diameter);
    }
  };

  /**
   * インジケータ(メッセージ・画面ブロック・進捗表示)の表示や非表示を行うクラス。
   *
   * @class
   * @name Indicator
   * @param {String|Object} target インジケータを表示する対象のDOMオブジェクトまたはセレクタ
   * @param {Object} [option] オプション
   * @param {String} [option.message] メッセージ
   * @param {Number} [option.percent] 進捗を0～100の値で指定する。
   * @param {Boolean} [option.block] 操作できないよう画面をブロックするか (true:する/false:しない)
   * @param {Promise|Promise[]} [option.promises] Promiseオブジェクト (Promiseの状態と合わせてインジケータの表示・非表示する)
   * @param {String} [options.cssClass] インジケータの基点となるクラス名 (CSSでテーマごとにスタイルをする場合に使用する)
   */
  function Indicator(target, option) {
    $.blockUI.defaults.css = {};
    $.blockUI.defaults.overlayCSS = {};
    var opts = $.extend(true, {}, {
      message: '',
      percent: -1,
      block: true,
      promises: null
    }, option);
    // BlockUIのスタイル
    var blockUISetting = {
      message: '<span class="indicator-throbber" style="display:inline-block;">' + '</span><span class="indicator-message" style="' + (opts.message === "" ? 'display: none;' : 'display: inline-block;') + '">' + opts.message + '</span>',
      css: {},
      overlayCSS: {},
      blockMsgClass: opts.cssClass || 'a',
      showOverlay: opts.block
    };
    // スロバーのスタイル (基本的にはCSSで記述する。ただし固定値はここで設定している)
    var throbberSetting = {
      base: {
        roundTime: 1000,
        lines: 12
      },
      line: {},
      percent: {}
    };
    var promise = opts.promises;
    var promiseCallback = null;
    if ($.type(promise) === 'array') {
      $.map(promise, function(item, idx) {
        return h5.async.isPromise(item) ? item : null;
      });
      if (promise.length > 0) {
        promiseCallback = $.proxy(function() {
          this.hide();
        }, this);
        $.when.apply(null, promise).pipe(promiseCallback, promiseCallback);
      }
    } else if (h5.async.isPromise(promise)) {
      promiseCallback = $.proxy(function() {
        this.hide();
      }, this);
      promise.pipe(promiseCallback, promiseCallback);
    }
    var canvasStyles = readCSSCanvasStyle(blockUISetting.blockMsgClass);
    throbberSetting = $.extend(true, throbberSetting, canvasStyles);
    this.target = h5.u.obj.isJQueryObject(target) ? target.get(0) : target;
    this._style = $.extend(true, {}, blockUISetting, throbberSetting);
    if (isCanvasSupported()) {
      this.throbber = new ThrobberCanvas(this._style);
    } else if (isVMLSupported()) {
      this.throbber = new ThrobberVML(this._style);
    }
    if (this.throbber && opts.percent > -1) {
      this.throbber.setPercent(opts.percent);
    }
  }
  Indicator.prototype = {

    /**
     * 画面上にインジケータ(メッセージ・画面ブロック・進捗表示)を表示します。
     *
     * @memberOf Indicator
     * @function
     * @returns {Indicator} インジケータオブジェクト
     */
    show: function() {
      var setting = this._style;
      var $blockElement = null;
      if (this._isGlobalBlockTarget(this.target)) {
        $.blockUI(setting);
        $blockElement = $('body').children('.blockUI.' + setting.blockMsgClass + '.blockPage');
      } else {
        var $target = $(this.target);
        $target.block(setting);
        $blockElement = $(this.target).children('.blockUI.' + setting.blockMsgClass + '.blockElement');
      }
      this.throbber.show($blockElement.children('.indicator-throbber')[0]);
      this._setPosition();
      return this;
    },

    /**
     * インジケータの表示位置を中央に設定します。
     *
     * @memberOf Indicator
     * @function
     * @private
     */
    _setPosition: function() {
      var setting = this._style;
      var $blockParent = null;
      var $blockElement = null;
      var width = 0;
      if (this._isGlobalBlockTarget()) {
        $blockParent = $('body');
        $blockElement = $blockParent.children('.blockUI.' + setting.blockMsgClass + '.blockPage');
      } else {
        $blockParent = $(this.target);
        $blockElement = $blockParent.children('.blockUI.' + setting.blockMsgClass + '.blockElement');
      }
      var blockElementPadding = $blockElement.innerWidth() - $blockElement.width();
      $blockElement.children().each(function() {
        width += $(this).outerWidth(true);
      });
      $blockElement.width(width + blockElementPadding);
      $blockElement.css('left', (($blockParent.width() - $blockElement.outerWidth()) / 2) + 'px');
    },

    /**
     * 指定された要素がウィンドウ領域全体をブロックすべき要素か判定します。
     *
     * @memberOf Indicator
     * @function
     * @private
     * @returns {Boolean} 領域全体に対してブロックする要素か (true:対象要素 / false: 非対象要素)
     */
    _isGlobalBlockTarget: function() {
      return this.target === document || this.target === window || this.target === document.body;
    },

    /**
     * 画面上に表示されているインジケータ(メッセージ・画面ブロック・進捗表示)を除去します。
     *
     * @memberOf Indicator
     * @function
     * @returns {Indicator} インジケータオブジェクト
     */
    hide: function() {
      if (this._isGlobalBlockTarget()) {
        $.unblockUI();
      } else {
        $(this.target).unblock();
      }
      return this;
    },

    /**
     * 進捗のパーセント値を指定された値に更新します。
     *
     * @memberOf Indicator
     * @function
     * @param {Number} param 進捗率(0～100%)
     * @returns {Indicator} インジケータオブジェクト
     */
    percent: function(param) {
      if ($.type(param) !== 'number') {
        return;
      }
      this.throbber.setPercent(param);
      return this;
    },

    /**
     * メッセージを指定された値に更新します。
     *
     * @memberOf Indicator
     * @function
     * @param {String} param メッセージ
     * @returns {Indicator} インジケータオブジェクト
     */
    message: function(param) {
      if ($.type(param) !== 'string') {
        return;
      }
      var setting = this._style;
      var $blockElement = null;
      if (this._isGlobalBlockTarget()) {
        $blockElement = $('body').children('.blockUI.' + setting.blockMsgClass + '.blockPage');
      } else {
        $blockElement = $(this.target).children('.blockUI.' + setting.blockMsgClass + '.blockElement');
      }
      $blockElement.children('.indicator-message').css('display', 'inline-block').text(param);
      this._setPosition();
      return this;
    }
  };

  /**
   * 指定された要素に対して、インジケータ(メッセージ・画面ブロック・進捗)の表示や非表示を行うためのオブジェクトを取得します。
   * <h4>使用例</h4>
   * <b>画面全体をブロックする場合</b><br>
   * ・画面全体をブロックする場合、targetオプションに<b>document</b>、<b>window</b>または<b>body</b>を指定する。<br>
   *
   * <pre>
   * var indicator = h5.ui.indicator({
   * 	target: document,
   * }).show();
   * </pre>
   *
   * <b>li要素にスロバー(くるくる回るアイコン)を表示してブロックを表示しないる場合</b><br>
   *
   * <pre>
   * var indicator = h5.ui.indicator('li', {
   * 	block: false
   * }).show();
   * </pre>
   *
   * <b>パラメータにPromiseオブジェクトを指定して、done()/fail()の実行と同時にインジケータを除去する</b><br>
   * resolve() または resolve() が実行されると、画面からインジケータを除去します。
   *
   * <pre>
   * var df = $.Deferred();
   * var indicator = h5.ui.indicator(document, {
   * 	promises: df.promise()
   * }).show();
   * setTimeout(function() {
   * 	df.resolve() // ここでイジケータが除去される
   * }, 2000);
   * </pre>
   *
   * <b>パラメータに複数のPromiseオブジェクトを指定して、done()/fail()の実行と同時にインジケータを除去する</b><br>
   * Promiseオブジェクトを複数指定すると、全てのPromiseオブジェクトでresolve()が実行されるか、またはいずれかのPromiseオブジェクトでfail()が実行されるタイミングでインジケータを画面から除去します。
   *
   * <pre>
   * var df = $.Deferred();
   * var df2 = $.Deferred();
   * var indicator = h5.ui.indicator(document, {
   * 	promises: [df.promise(), df2.promise()]
   * }).show();
   * setTimeout(function() {
   * 	df.resolve()
   * }, 2000);
   * setTimeout(function() {
   * 	df.resolve() // ここでイジケータが除去される
   * }, 4000);
   * </pre>
   *
   * @memberOf h5.ui
   * @name indicator
   * @function
   * @param {String|Object} target インジケータを表示する対象のDOMオブジェクトまたはセレクタ
   * @param {String} [option.message] メッセージ
   * @param {Number} [option.percent] 進捗を0～100の値で指定する。
   * @param {Boolean} [option.block] 操作できないよう画面をブロックするか (true:する/false:しない)
   * @param {Object} [option.style] スタイルオプション (詳細はIndicatorクラスのドキュメントを参照)
   * @param {Promise|Promise[]} [option.promises] Promiseオブジェクト (Promiseの状態と合わせてインジケータの表示・非表示する)
   * @param {String} [options.cssClass] インジケータの基点となるクラス名 (CSSでテーマごとにスタイルをする場合に使用する)
   * @see Indicator
   */
  var indicator = function(target, option) {
    return new Indicator(target, option);
  };

  /**
   * 要素が可視範囲内であるかどうかを返します。
   *
   * @param {String|Element|jQuery} element 要素
   * @param {Object} container コンテナ
   * @returns {Boolean} 要素が可視範囲内にあるかどうか
   * @name isInView
   * @function
   * @memberOf h5.ui
   */
  var isInView = function(element, container) {
    if (container === undefined || container === window) {
      var height = h5.u.ua.isIOS() ? window.innerHeight : $(window).height();
      var scrollTop = $(window).scrollTop();
      var offsetTop = $(element).offset().top;
      var bottom = offsetTop + $(element).height();
      var fold1 = offsetTop - scrollTop;
      var fold2 = bottom - scrollTop;
      return (0 <= fold1 && fold1 <= height) || (0 <= fold2 && fold2 <= height);
    }
    // TODO コンテナがwindowではない場合は修正の必要がある。
    var cHeight = $(container).height();
    var cScrollTop = $(container).scrollTop;
    var posTop = $(element).position().top;
    var fold = posTop - cScrollTop;
    return 0 <= fold && fold <= cHeight;
  };

  /**
   * ブラウザのトップにスクロールします。
   *
   * @param {Number} wait スクロールを開始するまでのディレイ時間
   * @name scrollToTop
   * @function
   * @memberOf h5.ui
   */
  var scrollToTop = function(wait) {
    var waitMillis = wait;
    if (!wait) {
      waitMillis = 500;
    }
    setTimeout(function() {
      window.scrollTo(0, 1);
    }, waitMillis);
  };
  h5.u.obj.expose('h5.ui', {
    indicator: indicator,
    isInView: isInView,
    scrollToTop: scrollToTop
  });
  // #delete begin
})(window, jQuery);
// #delete end
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */

/**
 * h5.ajax関数
 *
 * @param {Object} window
 * @param {Object} jQuery
 */
(function(window, $) {
  var jqVersion = parseFloat($().jquery);

  /**
   * jQuery.ajax()の戻り値、jqXHRオブジェクトにprogressメソッドを追加して返します。
   *
   * @param {Any} var_args jQuery.ajaxに渡す引数
   * @returns {jqXHR} jqXHRオブジェクト
   * @name ajax
   * @function
   * @memberOf h5
   */
  var ajax = function(var_args) {
    var option = typeof arguments[0] === 'string' ? arguments[1] : arguments[0];
    var hasFailCallback = option && (option.error || option.fail || option.complete || option.always);
    var jqXHR = $.ajax.apply($, arguments);
    var callFail = false;
    var commonFailHandler = h5.settings.commonFailHandler;
    if (!hasFailCallback && commonFailHandler) {
      jqXHR.fail(function(var_args1) {
        if (!callFail) {
          commonFailHandler.apply(null, arguments);
        }
      });
      var originalFail = jqXHR.fail;
      jqXHR.fail = function(var_args1) {
        callFail = true;
        originalFail.apply(jqXHR, arguments);
      };
      jqXHR.error = jqXHR.fail;
      var originalAlways = jqXHR.always;
      jqXHR.always = function(var_args1) {
        callFail = true;
        originalAlways.apply(jqXHR, arguments);
      };
      jqXHR.complete = jqXHR.always;
    }
    if (1.7 <= jqVersion) {
      return jqXHR;
    }
    jqXHR.progress = function() {
      // 何もしない.
    };
    return jqXHR;
  };
  h5.u.obj.expose('h5', {
    ajax: ajax
  });
})(window, jQuery);
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
  // #delete end
  var getDeferred = h5.async.deferred;

  /**
   * EJSテンプレート内で使用可能なヘルパー関数
   */
  var helperExtras = {

    /**
     * Javascript文字列をエスケープします。
     *
     * @param {String} str エスケープ対象文字列
     * @returns エスケープされた文字列
     */
    escapeJs: function(str) {
      return h5.u.str.escapeJs(str);
    },

    /**
     * HTML文字列をエスケープします。
     *
     * @param {String} str エスケープ対象文字列
     * @returns エスケープされた文字列
     */
    escapeHtml: function(str) {
      return h5.u.str.escapeHtml(str);
    }
  };

  /**
   * jQueryオブジェクトか判定します。
   *
   * @function
   * @param {Object} obj DOM要素
   */
  function isJQueryObject(obj) {
    return h5.u.obj.isJQueryObject(obj);
  }

  /**
   * ビュー操作全般の処理を行います。
   *
   * @name view
   * @memberOf h5.core
   * @namespace
   */
  var View = function() {

    /**
     * キャッシュしたテンプレートを保持するオブジェクト
     *
     * @type Object
     * @name cachedTemplate
     * @memberOf h5.core.view
     */
    this.cachedTemplate = {};
  };

  /**
   * 画面HTMLに記述されているテンプレートと、指定されたパスのテンプレートファイルを読み込みキャッシュします。
   * パラメータを指定しない場合は、画面HTMLに記述されているテンプレートのみ読み込みます。
   *
   * @memberOf h5.core.view
   * @name load
   * @function
   * @param {String|Array[String]} resourcePaths テンプレートファイル(.ejs)のパス (配列で複数指定可能)
   * @param {Boolean} sync テンプレートの読み込みを同期的に行うか。デフォルトは非同期(false)。 (true:同期 / false:非同期)
   * @returns {Promise} promiseオブジェクト
   */
  View.prototype.load = function(resourcePaths, sync) {
    var that = this;

    /**
     * テンプレート読み込み結果オブジェクト
     */
    var resultObj = {
      fault: [],
      success: []
    };

    /**
     * テンプレートをEJS用にコンパイルされたテンプレートに変換し、キャッシュします。
     *
     * @param {jQuery} $templateElements テンプレートが記述されている要素(<script type="text/ejs">...</script>)
     * @param {String} filePath ファイルパス
     */
    var cacheCompiledTemplates = function($templateElements, filePath) {
      if ($templateElements.length === 0) {
        return;
      }
      $templateElements.each(function() {
        var templateId = $.trim(this.id);
        var templateText = $.trim(this.innerHTML);
        if (templateText.length === 0 || !templateId) {
          return;
        }
        try {
          var compiledTemplate = new EJS.Compiler(templateText, '[');
          compiledTemplate.compile();
          that.cachedTemplate[templateId] = compiledTemplate.process;
          resultObj.success.push({
            url: filePath,
            id: templateId
          });
        }
        catch (e) {
          var lineNo = e.lineNumber;
          var msg = lineNo ? ' line:' + lineNo : '';
          resultObj.fault.push({
            url: filePath,
            id: templateId,
            reason: '構文エラー ' + msg + e.message
          });
        }
      });
    };

    /**
     * 指定されたテンプレートファイルからテンプレートを非同期で読み込みます。
     *
     * @param {Array[String]} paths テンプレートパス
     */
    var loadTemplate = function(paths) {
      var tasks = [];
      for (var i = 0; i < paths.length; i++) {
        var func = $.get(paths[i]);
        tasks.push(func);
      }
      var df = getDeferred();
      function load(task, count) {
        var step = count || 0;
        if (task.length == step) {
          df.resolve();
          return;
        }
        task[step].then(function(result, statusText, obj) {
          var templateText = obj.responseText;
          var $elements = $(templateText);
          var filePath = this.url;
          if ($elements.not('script').length > 0) {
            resultObj.fault.push({
              url: this.url,
              reason: '構文エラー テンプレート文字列はscriptタグでネストして下さい。'
            });
            return;
          }
          cacheCompiledTemplates($elements.filter('script'), filePath);
        }).fail(function(result, statusText, message) {
          resultObj.fault.push({
            url: this.url,
            reason: 'テンプレートファイルがありません。'
          });
        }).always(function() {
          load(task, ++step);
        });
        return df.promise();
      }
      var parentDf = getDeferred();
      $.when(load(tasks)).done(function() {
        if (resultObj.fault.length === 0) {
          delete resultObj.fault;
          parentDf.resolve(resultObj);
        } else {
          parentDf.reject(resultObj);
        }
      });
      return parentDf.promise();
    };

    /**
     * 指定されたテンプレートファイルからテンプレートを同期で読み込みます。
     *
     * @param {Array[String]} paths テンプレートパス
     */
    var loadTemplateSync = function(paths) {
      var param = {
        url: null,
        dataType: 'text',
        async: false,
        success: function(result, statusText, obj) {
          var templateText = obj.responseText;
          var $elements = $(templateText);
          var filePath = this.url;
          if ($elements.not('script').length > 0) {
            resultObj.fault.push({
              url: this.url,
              reason: '構文エラー テンプレート文字列はscriptタグでネストして下さい。'
            });
            return;
          }
          cacheCompiledTemplates($elements.filter('script'), filePath);
        },
        error: function(result, statusText, message) {
          resultObj.fault.push({
            url: this.url,
            reason: 'テンプレートファイルがありません。'
          });
        }
      };
      var df = getDeferred();
      for (var i = 0; i < paths.length; i++) {
        param.url = paths[i];
        h5.ajax(param);
      }
      if (resultObj.fault.length === 0) {
        delete resultObj.fault;
        df.resolve(resultObj);
      } else {
        df.reject(resultObj);
      }
      return df.promise();
    };
    var path = null;
    switch ($.type(resourcePaths)) {
      case 'string':
        path = [resourcePaths];
        break;
      case 'array':
        path = resourcePaths;
        break;
      default:
        path = [];
        break;
    }
    var isSync = sync || false;
    var url = location.href.replace(/.*\//, '');
    cacheCompiledTemplates($('script[type="text/ejs"]'), url);
    var ret = null;
    if (isSync) {
      ret = loadTemplateSync(path);
    } else {
      ret = loadTemplate(path);
    }
    return ret;
  };

  /**
   * パラメータで置換された、指定されたテンプレートIDのテンプレートを取得します。 <br>
   * 取得するテンプレート内に置換要素([%= %])が存在する場合、パラメータを全て指定してください。
   *
   * @memberOf h5.core.view
   * @name get
   * @function
   * @param {String} templateId テンプレートID
   * @param {Object} [param] パラメータ(オブジェクトリテラルで指定)
   * @returns {String} テンプレート文字列
   */
  View.prototype.get = function(templateId, param) {
    var cache = this.cachedTemplate;
    if ($.isEmptyObject(cache)) {
      return null;
    }
    var template = cache[templateId];
    if (!template) {
      var errorMsg = 'テンプレートID:' + templateId + ' テンプレートがありません。';
      throw errorMsg;
    }
    var p = param || {};
    var helper = new EJS.Helpers(p, helperExtras);
    var ret = null;
    try {
      ret = template.call(p, p, helper);
    }
    catch (e) {
      var erroMsg = e.toString() + ' テンプレートにパラメータが設定されていません。';
      throw erroMsg;
    }
    return ret;
  };

  /**
   * 要素を指定されたIDのテンプレートで書き換えます。
   *
   * @memberOf h5.core.view
   * @name update
   * @function
   * @param {String|Element|jQuery} element DOM要素(セレクタ文字列, DOM要素, jQueryオブジェクト)
   * @param {String} templateId テンプレートID
   * @param {Object} [param] パラメータ
   * @returns {Object} テンプレートが適用されたDOM要素 (jQueryオブジェクト)
   */
  View.prototype.update = function(element, templateId, param) {
    var elem = null;
    elem = element;
    if (!elem) {
      return;
    }
    if (!isJQueryObject(element)) {
      elem = $(element);
    }
    if (elem.length === 0) {
      return;
    }
    elem.html(this.get(templateId, param));
    return elem;
  };

  /**
   * 要素の末尾に指定されたIDのテンプレートを挿入します。
   *
   * @memberOf h5.core.view
   * @name append
   * @function
   * @param {String|Element|jQuery} element DOM要素(セレクタ文字列, DOM要素, jQueryオブジェクト)
   * @param {String} templateId テンプレートID
   * @param {Object} [param] パラメータ
   * @returns {Object} テンプレートが適用されたDOM要素 (jQueryオブジェクト)
   */
  View.prototype.append = function(element, templateId, param) {
    var elem = null;
    elem = element;
    if (!elem) {
      return;
    }
    if (!isJQueryObject(element)) {
      elem = $(element);
    }
    if (elem.length === 0) {
      return;
    }
    elem.append(this.get(templateId, param));
    return elem;
  };

  /**
   * 要素の先頭に指定されたIDのテンプレートを挿入します。
   *
   * @memberOf h5.core.view
   * @name prepend
   * @function
   * @param {String|Element|jQuery} element DOM要素(セレクタ文字列, DOM要素, jQueryオブジェクト)
   * @param {String} templateId テンプレートID
   * @param {Object} [param] パラメータ
   * @returns {Object} テンプレートが適用されたDOM要素 (jQueryオブジェクト)
   */
  View.prototype.prepend = function(element, templateId, param) {
    var elem = null;
    elem = element;
    if (!elem) {
      return;
    }
    if (!isJQueryObject(element)) {
      elem = $(element);
    }
    if (elem.length === 0) {
      return;
    }
    elem.prepend(this.get(templateId, param));
    return elem;
  };

  /**
   * 指定されたテンプレートIDのテンプレートが存在するか判定します。
   *
   * @memberOf h5.core.view
   * @name isAvailable
   * @function
   * @param {String} templateId テンプレートID
   * @returns {Boolean} 判定結果(存在する: true / 存在しない: false)
   */
  View.prototype.isAvailable = function(templateId) {
    return this.cachedTemplate[templateId] != null;
  };

  /**
   * キャッシュされている全てのテンプレートを削除します。
   *
   * @memberOf h5.core.view
   * @name clear
   * @function
   */
  View.prototype.clear = function() {
    this.cachedTemplate = {};
  };
  h5.u.obj.expose('h5.core', {
    view: new View()
  });
  // #delete begin
})(jQuery);
// #delete end
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
  // #delete end
  var getDeferred = h5.async.deferred;
  var startsWith = h5.u.str.startsWith;
  var endsWith = h5.u.str.endsWith;
  var format = h5.u.str.format;
  var getView = h5.core.view;
  var argsToArray = h5.u.obj.argsToArray;

  /**
   * コントローラの作成とバインドで使用する内部メソッド。
   */
  var internal = {

    /**
     * コントローラのexecuteListenersを見てリスナーを実行するかどうかを決定します。
     *
     * @param {Object} invocation インヴォケーション.
     */
    checkExecuteListeners: function(invocation) {
      if (!this.__controllerContext.executeListeners) {
        return;
      }
      return invocation.proceed();
    },

    /**
     * 指定されたオブジェクトの関数にアスペクトを織り込みます。
     *
     * @param {Object} obj オブジェクト.
     * @param {Object} aops AOP用関数配列.
     * @param {Boolean} eventHandler イベントハンドラかどうか.
     * @returns {Object} AOPに必要なメソッドを織り込んだオブジェクト.
     */
    weaveControllerAspect: function(obj, prop, eventHandler) {
      var interceptors = this.getInterceptors(obj.__name, prop);
      if (eventHandler) {
        interceptors.push(this.checkExecuteListeners);
      }
      return this.createWeavedFunction(obj[prop], prop, interceptors);
    },
    isLifecycleEvent: function(obj, prop) {
      return (prop === '__ready' || prop === '__construct' || prop === '__init') && $.isFunction(obj[prop]);
    },

    /**
     * 指定されたオブジェクトの関数にアスペクトを織り込みます。
     *
     * @param {Object} obj オブジェクト.
     * @returns {Object} AOPに必要なメソッドを織り込んだオブジェクト.
     */
    weaveLogicAspect: function(obj) {
      var weavedObject = obj;
      for (var prop in obj) {
        if ($.isFunction(obj[prop])) {
          var interceptors = this.getInterceptors(obj.__name, prop);
          weavedObject[prop] = this.createWeavedFunction(obj[prop], prop, interceptors);
        } else {
          weavedObject[prop] = obj[prop];
        }
      }
      return weavedObject;
    },

    /**
     * 基本となる関数にアスペクトを織り込んだ関数を返します。
     *
     * @param {Function} baseFunc 基本関数.
     * @param {String} funcName 基本関数名.
     * @param {Function} aspects AOP用関数配列.
     * @returns {Function} AOP用関数を織り込んだ関数.
     */
    createWeavedFunction: function(base, funcName, aspects) {
      var f = base;
      for (var i = 0, l = aspects.length; i < l; i++) {
        f = this.weave(f, funcName, aspects[i]);
      }
      return f;
    },

    /**
     * アスペクトを織り込んだ関数を返します。
     *
     * @param {Any} var_args 可変長引数
     */
    weave: function(base, funcName, aspect) {
      return function(var_args) {
        var that = this;
        var invocation = {
          target: that,
          func: base,
          funcName: funcName,
          args: arguments,
          proceed: function() {
            return base.apply(that, this.args);
          }
        };
        return aspect.apply(that, [invocation]);
      };
    },

    /**
     * 関数名とポイントカットを比べて、条件に合致すればインターセプタを返す.
     *
     * @param {String} バインドする必要のある関数名.
     * @param {Object} aops AOP用関数配列.
     * @returns {Array} AOP用関数配列.
     */
    getInterceptors: function(targetName, pcName) {
      var ret = [];
      var aspects = h5.settings.aspects;
      if (!aspects || aspects.length === 0) {
        return ret;
      }
      if ($.type(aspects) !== 'array') {
        aspects = [aspects];
      }
      for (var i = aspects.length - 1; -1 < i; i--) {
        var aspect = aspects[i];
        if (aspect.target && !aspect.compiledTarget.test(targetName)) {
          continue;
        }
        var interceptors = aspect.interceptors;
        if (aspect.pointCut && !aspect.compiledPointCut.test(pcName)) {
          continue;
        }
        if ($.type(interceptors) !== 'array') {
          ret.push(interceptors);
          continue;
        }
        for (var j = interceptors.length - 1; -1 < j; j--) {
          ret = ret.concat(interceptors[j]);
        }
      }
      return ret;
    },

    /**
     * セレクタがコントローラの外側の要素を指しているかどうかを返します。<br>
     * (外側の要素 = true)
     *
     * @param selector セレクタ
     * @returns コントローラの外側の要素を指しているかどうか
     */
    isOutOfController: function(selector) {
      return selector.match(/^\{.*\}$/);
    },

    /**
     * セレクタから{}を外した文字列を返します。
     *
     * @param selector セレクタ
     * @returns セレクタから{}を外した文字列
     */
    unwrapOutOfController: function(selector) {
      return $.trim(selector.substring(1, selector.length - 1));
    },

    /**
     * 指定されたセレクタがwindow, window., document, document., navidator, navigator. で
     * 始まっていればそのオブジェクトを、そうでなければそのまま文字列を返します。
     *
     * @param selector セレクタ
     * @param rootElement ルートエレメント
     * @returns オブジェクト、もしくはセレクタ
     */
    getOutOfControllerTarget: function(selector, rootElement) {
      var retSelector = selector;
      if (startsWith(selector, 'window')) {
        if (startsWith(selector, 'window.')) {
          retSelector = window[selector.substring(7, selector.length)];
        } else {
          retSelector = window;
        }
      } else if (startsWith(selector, 'document')) {
        if (startsWith(selector, 'document.')) {
          retSelector = document[selector.substring(9, selector.length)];
        } else {
          retSelector = document;
        }
      } else if (startsWith(selector, 'navigator')) {
        if (startsWith(selector, 'navigator.')) {
          retSelector = navigator[selector.substring(10, selector.length)];
        } else {
          retSelector = navigator;
        }
      }
      return retSelector;
    },

    /**
     * イベント名がjQuery.bindを使って要素にイベントをバインドするかどうかを返します。
     *
     * @param eventName イベント名
     * @returns jQuery.bindを使って要素にイベントをバインドするかどうか
     */
    useBind: function(eventName) {
      return eventName.match(/^\[.*\]$/);
    },

    /**
     * イベント名から[]を外した文字列を返す
     *
     * @param eventName イベント名
     * @returns イベント名から[]を外した文字列
     */
    unwrapBindEventName: function(eventName) {
      return $.trim(eventName.substring(1, eventName.length - 1));
    },

    /**
     * 指定されたプロパティがイベントコールバックかどうかを返します。
     *
     * @param obj オブジェクト
     * @param prop プロパティ名
     * @returns {Boolean} プロパティがイベントコールバックかどうか
     */
    isEventCallback: function(obj, prop) {
      return prop.indexOf(' ') != -1 && $.isFunction(obj[prop]);
    },

    /**
     * グローバルコントローラかどうかを返します。
     *
     * @param targetElement
     * @param controllerName
     */
    isGlobalController: function(targetElement, controllerName) {
      return targetElement === document && controllerName === 'GlobalController';
    },

    /**
     * xxxControllerの循環参照のチェックを行います。
     *
     * @param object
     * @param array
     * @returns {Boolean}
     */
    checkDelegateCircular: function(object, array) {
      if ($.inArray(object, array) !== -1) {
        return false;
      }
      if (!object) {
        return true;
      }
      array.push(object);
      for (var prop in object) {
        if (this.isChildController(prop, object) && !this.checkDelegateCircular(object[prop], array)) {
          return false;
        }
      }
      return true;
    },

    /**
     * xxxControllerの循環参照のチェックを行います。
     *
     * @param object
     * @param array
     * @returns {Boolean}
     */
    checkLogicCircular: function(object, array) {
      if ($.inArray(object, array) !== -1) {
        return false;
      }
      if (!object) {
        return true;
      }
      array.push(object);
      for (var prop in object) {
        if (endsWith(prop, 'Logic') && !this.checkLogicCircular(object[prop], array)) {
          return false;
        }
      }
      return true;
    },
    getInternalControllerPromises: function(obj, propertyName, controllerContext) {
      var promises = [];
      var targets = [];
      var that = this;
      var worker = function(object) {
        targets.push(object);
        for (var prop in object) {
          if (that.isChildController(prop, object)) {
            var c = object[prop];
            if (!c) {
              continue;
            }
            var promise = controllerContext ? c.__controllerContext[propertyName] : c[propertyName];
            if (promise) {
              promises.push(promise);
            }
            var execute = true;
            for (var i = 0, l = targets.length; i < l; i++) {
              if (c === targets[i]) {
                execute = false;
                break;
              }
            }
            if (execute) {
              worker(c);
            }
          }
        }
      };
      worker(obj);
      return promises;
    },
    bindUseHandlers: function(obj) {
      var targets = [];
      var that = this;
      var execute = function(controller) {
        if (controller.__isReady || $.inArray(controller, targets) !== -1) {
          return;
        }
        targets.push(controller);
        for (var prop in controller) {
          var c = controller[prop];
          if (!c || !that.isChildController(prop, controller) || c.__controllerContext.isRoot) {
            continue;
          }
          execute(c);
        }
        var meta = controller.__meta;
        for (var prop in meta) {
          if (!meta[prop].useHandlers) {
            continue;
          }
          that.bindByBindMap(controller[prop]);
        }
      };
      execute(obj);
    },
    bindByBindMap: function(controller) {
      var bindMap = controller.bindMap;
      var that = this;
      for (var s in bindMap) {
        for (var e in bindMap[s]) {
          (function(selector, eventName) {
            that.bindEventHandler(controller, selector, eventName);
          })(s, e);
        }
      }
    },
    addEventListener: function(bindObj) {
      var controller = bindObj.controller;
      var rootElement = controller.rootElement;
      var selector = bindObj.selector;
      var eventName = bindObj.eventName;
      var handler = bindObj.handler;
      var useBind = this.useBind(eventName);
      var event = useBind ? this.unwrapBindEventName(eventName) : eventName;
      if (this.isOutOfController(selector)) {
        var selectTarget = this.unwrapOutOfController(selector);
        var isSelf = false;
        if (selectTarget === 'rootElement') {
          selectTarget = rootElement;
          isSelf = true;
        } else {
          selectTarget = this.getOutOfControllerTarget(selectTarget, rootElement);
        }
        var needBind = selectTarget === document || selectTarget === window;
        if (isSelf || useBind || needBind) {
          $(selectTarget).bind(event, handler);
        } else {
          $(selectTarget).live(event, handler);
        }
      } else {
        if (useBind) {
          $(selector, rootElement).bind(event, handler);
        } else {
          $(rootElement).delegate(selector, event, handler);
        }
      }
    },
    unbindUseHandlers: function(obj) {
      var that = this;
      var targets = [];
      var execute = function(controller) {
        if ($.inArray(controller, targets) !== -1) {
          return;
        }
        targets.push(controller);
        for (var prop in controller) {
          var c = controller[prop];
          if (!c || !that.isChildController(prop, controller) || c.__controllerContext.isRoot) {
            continue;
          }
          execute(c);
        }
        var meta = controller.__meta;
        for (var prop in meta) {
          if (!meta[prop].useHandlers) {
            continue;
          }
          that.unbindByBindMap(controller[prop]);
        }
      };
      execute(obj);
    },
    unbindByBindMap: function(controller) {
      var rootElement = controller.rootElement;
      var unbindMap = controller.__controllerContext.unbindMap;
      for (var selector in unbindMap) {
        for (var eventName in unbindMap[selector]) {
          var handler = unbindMap[selector][eventName];
          var useBind = this.useBind(eventName);
          var event = useBind ? this.unwrapBindEventName(eventName) : eventName;
          if (this.isOutOfController(selector)) {
            var selectTarget = this.unwrapOutOfController(selector);
            var isSelf = false;
            if (selectTarget === 'rootElement') {
              selectTarget = rootElement;
              isSelf = true;
            } else {
              selectTarget = this.getOutOfControllerTarget(selectTarget, rootElement);
            }
            var needBind = selectTarget === document || selectTarget === window;
            if (isSelf || useBind || needBind) {
              $(selectTarget).unbind(event, handler);
            } else {
              $(selectTarget).die(event, handler);
            }
          } else {
            if (useBind) {
              $(selector, rootElement).unbind(event, handler);
            } else {
              $(rootElement).undelegate(selector, event, handler);
            }
          }
        }
      }
    },
    changeExecuteListeners: function(controller, flag) {
      controller.__controllerContext.executeListeners = flag;
      var targets = [];
      var that = this;
      var worker = function(object) {
        targets.push(object);
        for (var prop in object) {
          if (that.isChildController(prop, object)) {
            var c = object[prop];
            if (!c) {
              continue;
            }
            c.__controllerContext.executeListeners = flag;
            var execute = true;
            for (var i = 0, l = targets.length; i < l; i++) {
              if (c === targets[i]) {
                execute = false;
                break;
              }
            }
            if (execute) {
              worker(c);
            }
          }
        }
      };
      worker(controller);
    },
    changeEventEmulation: function(controller, flag, isIncludeChildren) {
      if (!controller.__controllerContext.useEventEmulation) {
        var errMsg = format('コントローラ"{0}"は、eventEmulationが無効になっています。', this.__name);
        errMsg += '有効にしたければフレームワークオプションのeventEmulationプロパティをtrueにしてh5.core.controller()を使用してください。';
        throw new Error(errMsg);
      }
      controller.__controllerContext.eventEmulation = flag;
      if (!isIncludeChildren) {
        return;
      }
      var targets = [];
      var that = this;
      var worker = function(object) {
        targets.push(object);
        for (var prop in object) {
          if (that.isChildController(prop, object)) {
            var c = object[prop];
            if (!c) {
              continue;
            }
            c.__controllerContext.eventEmulation = flag;
            var execute = true;
            for (var i = 0, l = targets.length; i < l; i++) {
              if (c === targets[i]) {
                execute = false;
                break;
              }
            }
            if (execute) {
              worker(c);
            }
          }
        }
      };
      worker(controller);
    },
    isChildController: function(prop, obj) {
      return endsWith(prop, 'Controller') && prop !== '__rootController' && prop !== '__parentController' && !$.isFunction(obj[prop]);
    },
    setupRootAndParentController: function(controller) {
      var targets = [];
      var that = this;
      var setup = function(object, root, parent) {
        object.__rootController = root;
        object.__parentController = parent;
        targets.push(object);
        for (var prop in object) {
          if (that.isChildController(prop, object)) {
            var c = object[prop];
            if (!c || c.__rootController || c.__parentController) {
              continue;
            }
            var execute = true;
            for (var i = 0, l = targets.length; i < l; i++) {
              if (c === targets[i]) {
                execute = false;
                break;
              }
            }
            if (execute) {
              setup(c, root, object);
            }
          }
        }
      };
      setup(controller, controller, null);
    },

    /**
     * __init, __readyイベントを実行する.
     *
     * @param ｛Object} controller コントローラ.
     * @param {Booelan} init __initイベントを実行するかどうか.
     */
    executeLifecycleEvent: function(controller, init) {
      var targets = [];
      var that = this;
      var execute = function(obj, isRoot) {
        for (var i = 0, l = targets.length; i < l; i++) {
          if (obj === targets[i]) {
            return;
          }
        }
        targets.push(obj);
        if ((init && obj.__isInit) || (!init && obj.__isReady)) {
          return;
        }
        var promises = init ? that.getPromisesForInit(obj) : that.getPromisesForReady(obj);
        for (var prop in obj) {
          if (obj[prop] && that.isChildController(prop, obj)) {
            if (obj[prop]) {
              execute(obj[prop], init);
            }
          }
        }
        var func = function() {
          if ((init && obj.__isInit) || (!init && obj.__isReady)) {
            return;
          }
          var ret = null;
          var lifecycleFunc = init ? obj.__init : obj.__ready;
          if (lifecycleFunc) {
            var initializedContext = that.setupInitializedContext(obj);
            ret = init ? obj.__init(initializedContext) : obj.__ready(initializedContext);
          }
          var callback = init ? that.createCallbackForInit(obj, isRoot) : that.createCallbackForReady(obj, isRoot);
          if (ret && h5.async.isPromise(ret)) {
            ret.done(function() {
              callback();
            });
          } else {
            callback();
          }
        };
        if (promises.length > 0) {
          $.when.apply($, promises).done(function() {
            func();
          });
        } else {
          func();
        }
      };
      execute(controller, true);
    },
    getPromisesForInit: function(controller) {
      var templatePromises = this.getInternalControllerPromises(controller, 'templatePromise', true);
      templatePromises.push(controller.__controllerContext.templatePromise);
      var initPromises = this.getInternalControllerPromises(controller, '__initPromise');
      return templatePromises.concat(initPromises);
    },
    getPromisesForReady: function(controller) {
      return this.getInternalControllerPromises(controller, '__readyPromise');
    },
    createCallbackForInit: function(controller, isBind) {
      var that = this;
      return function() {
        if (controller.__isInit) {
          return;
        }
        controller.__isInit = true;
        controller.__controllerContext.initDfd.resolve();
        delete controller.__controllerContext.templatePromise;
        delete controller.__controllerContext.initDfd;
        if (isBind && controller.__controllerContext.isRoot) {
          that.bind(controller);
        }
      };
    },
    createCallbackForReady: function(controller, controllerReady) {
      return function() {
        if (controller.__isReady) {
          return;
        }
        controller.__isReady = true;
        controller.__controllerContext.readyDfd.resolve();
        delete controller.__controllerContext.readyDfd;
        if (controllerReady) {
          $(controller.rootElement).trigger('controllerReady', [controller]);
        }
      };
    },
    isCorrectTemplatePrefix: function(selector) {
      if (startsWith('window')) {
        return false;
      }
      if (startsWith('window.')) {
        return false;
      }
      if (startsWith('navigator')) {
        return false;
      }
      if (startsWith('navigator.')) {
        return false;
      }
      return true;
    },
    getTarget: function(element, rootElement, isTemplate) {
      var targets;
      if (typeof element === 'string') {
        var selector = $.trim(element);
        if (this.isOutOfController(selector)) {
          var s = this.unwrapOutOfController(selector);
          if (isTemplate && this.isCorrectTemplatePrefix(s)) {
            throw new Error('update/append/prependView() の第1引数に"window", "window.", "navigator", "navigator."で始まるセレクタは指定できません。');
          }
          targets = $(this.getOutOfControllerTarget(s));
        } else {
          targets = $(rootElement).find(element);
        }
      } else {
        targets = $(element);
      }
      return targets;
    },
    bindEventHandler: function(controller, selector, eventName) {
      var func = controller.bindMap[selector][eventName];
      var event = eventName;
      var useBind = this.useBind(eventName);
      if (useBind) {
        event = this.unwrapBindEventName(eventName);
      }
      var bindObj = null;
      switch (event) {
        case 'mousewheel':
          bindObj = this.normalizeMouseWheel(controller, selector, event, func);
          break;
        case 'h5trackstart':
        case 'h5trackmove':
        case 'h5trackend':
          bindObj = this.getH5TrackBindObj(controller, selector, eventName, func);
          break;
        default:
          bindObj = this.getNormalBindObj(controller, selector, event, func);
          break;
      }
      if (!bindObj) {
        return;
      }
      if (!$.isArray(bindObj)) {
        this.useBindObj(bindObj, useBind);
        return;
      }
      for (var i = 0, l = bindObj.length; i < l; i++) {
        this.useBindObj(bindObj[i], useBind);
      }
    },
    useBindObj: function(bindObj, useBind) {
      if (useBind) {
        bindObj.eventName = '[' + bindObj.eventName + ']';
      }
      this.setupUnbindMap(bindObj.controller, bindObj.selector, bindObj.eventName, bindObj.handler);
      this.addEventListener(bindObj);
    },
    setupUnbindMap: function(controller, selector, eventName, handler) {
      if (!controller.__controllerContext.unbindMap) {
        controller.__controllerContext.unbindMap = {};
      }
      if (!controller.__controllerContext.unbindMap[selector]) {
        controller.__controllerContext.unbindMap[selector] = {};
      }
      controller.__controllerContext.unbindMap[selector][eventName] = handler;
    },
    getNormalBindObj: function(controller, selector, eventName, func) {
      var that = this;
      var handler = function(var_args) {
        var eventContext = that.setupContext(controller, arguments);
        func.call(controller, eventContext);
      };
      return {
        controller: controller,
        selector: selector,
        eventName: eventName,
        handler: handler
      };
    },
    normalizeMouseWheel: function(controller, selector, eventName, func) {
      var that = this;
      var handler = function(var_args) {
        var eventContext = that.setupContext(controller, arguments);
        var event = eventContext.event;
        // Firefox
        if (event.detail) {
          event.wheelDelta = -event.detail * 40;
        }
        func.call(controller, eventContext);
      };
      var en = eventName;
      if (typeof document.onmousewheel === 'undefined') {
        en = 'DOMMouseScroll';
      }
      return {
        controller: controller,
        selector: selector,
        eventName: en,
        handler: handler
      };
    },
    getH5TrackBindObj: function(controller, selector, eventName, func) {
      var hasTouchEvent = typeof document.ontouchstart != 'undefined';
      if (eventName !== 'h5trackstart') {
        if (hasTouchEvent) {
          return this.getNormalBindObj(controller, selector, eventName, func);
        }
        var wrapper = function(context) {
          var event = context.event;
          var offset = $(event.currentTarget).offset();
          event.offsetX = event.pageX - offset.left;
          event.offsetY = event.pageY - offset.top;
          func.apply(this, arguments);
        };
        return this.getNormalBindObj(controller, selector, eventName, wrapper);
      }
      var that = this;
      var getEventType = function(en) {
        switch (en) {
          case 'touchstart':
          case 'mousedown':
            return 'h5trackstart';
          case 'touchmove':
          case 'mousemove':
            return 'h5trackmove';
          case 'touchend':
          case 'mouseup':
            return 'h5trackend';
          default:
            return;
        }
      };
      var copyEventObject = function(src, dest) {
        for (var prop in src) {
          if (src.hasOwnProperty(prop) && !dest[prop] && prop !== 'target' && prop !== 'currentTarget' && prop !== 'originalEvent' && prop !== 'liveFired') {
            dest[prop] = src[prop];
          }
        }
        dest.h5DelegatingEvent = src;
      };
      var start = hasTouchEvent ? 'touchstart' : 'mousedown';
      var move = hasTouchEvent ? 'touchmove' : 'mousemove';
      var end = hasTouchEvent ? 'touchend' : 'mouseup';
      var $document = $(document);
      var getBindObjects = function() {
        var removeHandlers = null;
        var execute = false;
        var getHandler = function(en, eventTarget, setup) {
          return function(var_args) {
            var type = getEventType(en);
            var isStart = type === 'h5trackstart';
            if (isStart && execute) {
              return;
            }
            var eventContext = that.setupContext(controller, arguments);
            var event = eventContext.event;
            if (hasTouchEvent) {
              that.setupTouchEventObject(event, en);
            }
            var newEvent = new $.Event(type);
            copyEventObject(event, newEvent);
            newEvent.h5DelegatingEvent = event;
            var target = event.target;
            if (eventTarget) {
              target = eventTarget;
            }
            if (setup) {
              setup(newEvent);
            }
            if (!hasTouchEvent || (execute || isStart)) {
              $(target).trigger(newEvent, eventContext.evArg);
              execute = true;
            }
            if (isStart && execute) {
              if (!newEvent.isDefaultPrevented()) {
                newEvent.h5DelegatingEvent.preventDefault();
                var nt = newEvent.target;
                var ox = newEvent.clientX;
                var oy = newEvent.clientY;
                var setupDPos = function(ev) {
                  var cx = ev.clientX;
                  var cy = ev.clientY;
                  ev.dx = cx - ox;
                  ev.dy = cy - oy;
                  ox = cx;
                  oy = cy;
                };
                var moveHandler = getHandler(move, nt, setupDPos);
                var upHandler = getHandler(end, nt);
                var bindTarget = hasTouchEvent ? $(nt) : $document;
                removeHandlers = function() {
                  bindTarget.unbind(move, moveHandler);
                  bindTarget.unbind(end, upHandler);
                };
                bindTarget.bind(move, moveHandler);
                bindTarget.bind(end, upHandler);
              } else {
                execute = false;
              }
            }
            if (type === 'h5trackend') {
              removeHandlers();
              execute = false;
            }
          };
        };
        var createBindObj = function(en) {
          return {
            controller: controller,
            selector: selector,
            eventName: en,
            handler: getHandler(en)
          };
        };
        var bindObjects = [that.getNormalBindObj(controller, selector, eventName, func)];
        bindObjects.push(createBindObj(start));
        return bindObjects;
      };
      return getBindObjects();
    },
    setupTouchEventObject: function(event, eventName) {
      var originalEvent = event.originalEvent;
      var touches = eventName === 'touchend' || eventName === 'touchcancel' ? originalEvent.changedTouches[0] : originalEvent.touches[0];
      var pageX = touches.pageX;
      var pageY = touches.pageY;
      event.pageX = pageX;
      event.pageY = pageY;
      originalEvent.pageX = pageX;
      originalEvent.pageY = pageY;
      var scrX = touches.screenX;
      var scrY = touches.screenY;
      event.screenX = scrX;
      event.screenY = scrY;
      originalEvent.screenX = scrX;
      originalEvent.screenY = scrY;
      var clientX = touches.clientX;
      var clientY = touches.clientY;
      event.clientX = clientX;
      event.clientY = clientY;
      originalEvent.clientX = clientX;
      originalEvent.clientY = clientY;
      var target = event.target;
      if (target.ownerSVGElement) {
        target = ownerSVGElement;
      } else if (target === window || target === document) {
        target = document.body;
      }
      var offset = $(target).offset();
      if (offset) {
        var offsetX = pageX - offset.left;
        var offsetY = pageY - offset.top;
        event.offsetX = offsetX;
        event.offsetY = offsetY;
        originalEvent.offsetX = offsetX;
        originalEvent.offsetY = offsetY;
      }
    },

    /**
     * イベントオブジェクトを正規化します。
     *
     * @param {Object} event イベントオブジェクト
     */
    normalizeEventObjext: function(event) {
      if (event && event.offsetX == null && event.offsetY == null && event.pageX && event.pageY) {
        var target = event.target;
        if (target.ownerSVGElement) {
          target = target.farthestViewportElement;
        } else if (target === window || target === document) {
          target = document.body;
        }
        var offset = $(target).offset();
        if (offset) {
          event.offsetX = event.pageX - offset.left;
          event.offsetY = event.pageY - offset.top;
        }
      }
    },

    /**
     * イベントコンテキストをセットアップします。
     *
     * @param {Object} controller コントローラ
     * @param {Object} args 1番目にはjQuery.Eventオブジェクト、2番目はjQuery.triggerに渡した引数
     */
    setupContext: function(controller, args) {
      var event = null;
      var evArg = null;
      if (args) {
        event = args[0];
        evArg = args[1];
      }
      // イベントオブジェクトの正規化
      this.normalizeEventObjext(event);
      return {
        controller: controller,
        rootElement: controller.rootElement,
        event: event,
        evArg: evArg
      };
    },

    /**
     * イニシャライズドイベントコンテキストをセットアップします。
     *
     * @param {Object} rootController ルートコントローラ
     */
    setupInitializedContext: function(controller) {
      return {
        args: controller.__controllerContext.args
      };
    },
    disposeRootElement: function(obj) {
      for (var prop in obj) {
        var c = obj[prop];
        if (c && this.isChildController(prop, obj) && !c.__controllerContext.isRoot) {
          c.rootElement = null;
          this.disposeRootElement(c);
        }
      }
    },
    copyAndSetRootElement: function(obj) {
      var rootElement = obj.rootElement;
      var meta = obj.__meta;
      for (var prop in obj) {
        var c = obj[prop];
        if (c && this.isChildController(prop, obj) && !c.__controllerContext.isRoot) {
          if (meta && meta[prop] && meta[prop].rootElement) {
            c.rootElement = this.getBindTarget(meta[prop].rootElement, rootElement, c);
          } else {
            c.rootElement = rootElement;
          }
          this.copyAndSetRootElement(c);
        }
      }
    },
    getBindTarget: function(element, rootElement, controller) {
      if (!element) {
        throw new Error('バインド対象となる要素を指定して下さい。');
      } else if (!controller) {
        throw new Error('コントローラ化したオブジェクトを指定して下さい。');
      }
      var targets;
      if (rootElement) {
        targets = this.getTarget(element, rootElement);
      } else {
        targets = $(element);
      }
      if (targets.length === 0) {
        throw new Error(format('コントローラ"{0}"のバインド対象となる要素が存在しません。', controller.__name));
      }
      if (targets.length > 1) {
        throw new Error(format('コントローラ"{0}"のバインド対象となる要素が2つ以上存在します。バインド対象は1つのみにしてください。', controller.__name));
      }
      return targets.get(0);
    },
    bind: function(controller) {
      this.bindByBindMap(controller);
      this.bindUseHandlers(controller);
      // グローバルコントローラに通知する
      if (h5.globalController) {
        h5.globalController.addController(controller);
      }
      // controllerBoundイベントをトリガ.
      $(controller.rootElement).trigger('controllerBound', [controller]);
      // コントローラの__ready処理を実行
      var that = this;
      var initPromises = this.getInternalControllerPromises(controller, '__initPromise');
      initPromises.push(controller.__initPromise);
      $.when.apply($, initPromises).done(function() {
        that.executeLifecycleEvent(controller, false);
      });
    },
    setRootAndTriggerInit: function(controller) {
      if (controller.__rootController === null) {
        // __rootControllerと__parentControllerのセット
        this.setupRootAndParentController(controller);
      }
      this.copyAndSetRootElement(controller);
      // __initイベントの発火
      this.executeLifecycleEvent(controller, true);
    },
    setInternalProperty: function(controller, param) {
      var templateDfd = getDeferred();
      templateDfd.resolve();
      controller.__controllerContext.templatePromise = templateDfd.promise();
      controller.__controllerContext.initDfd = getDeferred();
      controller.__initPromise = controller.__controllerContext.initDfd.promise();
      controller.__controllerContext.readyDfd = getDeferred();
      controller.__readyPromise = controller.__controllerContext.readyDfd.promise();
      controller.__isInit = false;
      controller.__isReady = false;
      controller.__controllerContext.args = param;
      for (var prop in controller) {
        var c = controller[prop];
        if (c && this.isChildController(prop, controller) && !c.__controllerContext.isRoot) {
          this.setInternalProperty(c);
        }
      }
    }
  };

  /**
   * コントローラのファクトリ
   *
   * @param {String|Element|jQuery} targetElement バインド対象とする要素のセレクタ、DOMエレメント、もしくはjQueryオブジェクト.
   * @param {Object} baseObj コントローラの元となるオブジェクト
   * @param {Object} [param] 初期化パラメータ.
   */
  // fwOptは内部的に使用している.
  function createController(targetElement, baseObj, param, fwOpt) {
    // コントローラ名
    var controllerName = baseObj.__name;
    if (!controllerName || $.trim(controllerName).length === 0) {
      throw new Error(baseObj);
    }
    // 初期化パラメータがオブジェクトかどうかチェック
    if (param && !$.isPlainObject(param)) {
      throw new Error(baseObj);
    }
    if (baseObj.__controllerContext) {
      throw new Error('指定されたオブジェクトは既にコントローラ化されています。');
    }
    var eventEmulation = !!(fwOpt && fwOpt.eventEmulation);

    /**
     * コントローラのコンストラクタ
     *
     * @param {Element} rootElement コントローラをバインドした要素
     * @name Controller
     * @class
     */
    function Controller(rootElement) {

      /**
       * コントローラ名.
       *
       * @type String
       * @name __name
       * @memberOf Controller
       */
      this.__name = controllerName;

      /**
       * テンプレート.
       *
       * @type String|String[]
       * @name __templates
       * @memberOf Controller
       */
      this.___templates = null;

      /**
       * グローバルコントローラかどうか.
       *
       * @name isGlobal
       * @memberOf Controller
       * @private
       */
      this.isGlobal = targetElement === document && controllerName === 'GlobalController';

      /**
       * コントローラがバインドされた要素.
       *
       * @type Element
       * @name rootElement
       * @memberOf Controller
       */
      this.rootElement = rootElement;

      /**
       * コントローラコンテキスト.
       *
       * @private
       * @memberOf Controller
       * @name __controllerContext
       */
      this.__controllerContext = {
        executeListeners: true,
        isRoot: !fwOpt || !fwOpt.isInternal,

        /**
         * コントローラ化時にイベントエミュレーションを有効にしたかどうか
         */
        useEventEmulation: eventEmulation,

        /**
         * イベントエミュレーション機能が有効になっているかどうか
         */
        eventEmulation: eventEmulation
      };
      if (param) {
        this.__controllerContext.args = $.extend(true, {}, param);
      }
      if (this.isGlobal) {

        /**
         * 全てのコントローラの配列.
         */
        this.__controllerContext.controllers = [];

        /**
         * 全てのコントローラの構造.
         */
        this.__controllerContext.controllersStructure = {};
      }

      /**
       * コントローラのライフサイクルイベント__initが終了したかどうかを返します。
       *
       * @type Boolean
       * @memberOf Controller
       * @name __isInit
       */
      this.__isInit = false;

      /**
       * コントローラのライフサイクルイベント__readyが終了したかどうかを返します。
       *
       * @type Boolean
       * @memberOf Controller
       * @name __isReady
       */
      this.__isReady = false;

      /**
       * 親子関係を持つコントローラ群の一番祖先であるコントローラを返します。祖先がいない場合、自分自身を返します。
       *
       * @type Controller
       * @memberOf Controller
       * @name __rootController
       */
      this.__rootController = null;

      /**
       * 親子関係を持つコントローラの親コントローラを返します。親コントローラがいない場合、nullを返します。
       *
       * @type Controller
       * @memberOf Controller
       * @name __parentController
       */
      this.__parentController = null;

      /**
       * コントローラのライフサイクルイベント__initについてのPromiseオブジェクトを返します。
       *
       * @type Promise
       * @memberOf Controller
       * @name __initPromise
       */
      this.__initPromise = null;

      /**
       * コントローラのライフサイクルイベント__readyについてのPromiseオブジェクトを返します。
       *
       * @type Promise
       * @memberOf Controller
       * @name __readyPromise
       */
      this.__readyPromise = null;

      /**
       * コントローラのロガーを返します。
       *
       * @type Log
       * @memberOf Controller
       * @name log
       */
      this.log = h5.u.createLogger(baseObj.__name);
    }
    var constructor = Controller.prototype.constructor;
    Controller.prototype = {
      constructor: constructor,

      /**
       * コントローラがバインドされた要素内から要素を選択します。
       *
       * @param {String} selector セレクタ
       * @returns {jQuery} セレクタにマッチするjQueryオブジェクト
       * @memberOf Controller
       */
      $find: function(selector) {
        return $(this.rootElement).find(selector);
      },

      /**
       * 指定されたテンプレートを使ってHTML文字列を作成します。
       *
       * @param {String} templateId テンプレートID
       * @param {Object} [data] テンプレートで使用するデータ
       * @returns {String} HTML文字列
       * @memberOf Controller
       */
      getView: function(templateId, data) {
        return getView.get(templateId, data);
      },

      /**
       * 指定されたテンプレートを使ってHTML文字列を作成し、対象を更新します。
       *
       * @param {String} element 要素
       * @param {String} templateId テンプレートID
       * @param {Object} [data] テンプレートで使用するデータ
       * @memberOf Controller
       */
      updateView: function(element, templateId, data) {
        var target = internal.getTarget(element, this.rootElement, true);
        getView.update(target, templateId, data);
      },

      /**
       * 指定されたテンプレートを使ってHTML文字列を作成し、対象に要素を追加します。
       *
       * @param {String} element 要素
       * @param {String} templateId テンプレートID
       * @param {Object} [data] テンプレートで使用するデータ
       * @memberOf Controller
       */
      appendView: function(element, templateId, data) {
        var target = internal.getTarget(element, this.rootElement, true);
        getView.append(target, templateId, data);
      },

      /**
       * 指定されたテンプレートを使ってHTML文字列を作成し、対象の先頭に要素を追加します。
       *
       * @param {String} element 要素
       * @param {String} templateId テンプレートID
       * @param {Object} [data] テンプレートで使用するデータ
       * @memberOf Controller
       */
      prependView: function(element, templateId, data) {
        var target = internal.getTarget(element, this.rootElement, true);
        getView.prepend(target, templateId, data);
      },

      /**
       * Deferredオブジェクトを返します。
       *
       * @returns {Deferred} Deferredオブジェクト
       * @memberOf Controller
       */
      deferred: function() {
        return getDeferred();
      },

      /**
       * ルート要素を起点に指定されたイベントを実行します。
       *
       * @param {String} eventName イベント名
       * @param {Object} [parameter] パラメータ
       * @memberOf Controller
       */
      trigger: function(eventName, parameter) {
        $(this.rootElement).trigger(eventName, [parameter]);
      },

      /**
       * 指定された関数に対して、コンテキスト(this)をコントローラに変更して実行する関数を返します。
       *
       * @param {Function} func 関数
       * @return {Function} コンテキスト(this)をコントローラに変更した関数
       * @memberOf Controller
       */
      own: function(func) {
        var that = this;
        return function(var_args) {
          func.apply(that, arguments);
        };
      },

      /**
       * 指定された関数に対して、コンテキスト(this)をコントローラに変更し、元々のthisを第1引数に加えて実行する関数を返します。
       *
       * @param {Function} func 関数
       * @return {Function} コンテキスト(this)をコントローラに変更し、元々のthisを第1引数に加えた関数
       * @memberOf Controller
       */
      ownWithOrg: function(func) {
        var that = this;
        return function(var_args) {
          var args = h5.u.obj.argsToArray(arguments);
          args.unshift(this);
          func.apply(that, args);
        };
      },

      /**
       * コントローラのバインドを解除します。
       *
       * @memberOf Controller
       */
      unbind: function() {
        if ($.isFunction(this.__unbind)) {
          this.__unbind();
        }
        internal.unbindByBindMap(this);
        internal.unbindUseHandlers(this);
        this.__controllerContext.unbindMap = {};
        // グローバルコントローラの管理対象から外す.
        h5.globalController.removeController(this.rootElement);
        $(this.rootElement).trigger('controllerUnbound');
        // rootElemetnのアンバインド
        this.rootElement = null;
        internal.disposeRootElement(this);
      },

      /**
       * コントローラのインジケータイベントを実行します。
       *
       * @param {Object} option オプション(message, percent, block)
       * @param {String} ev イベント名
       * @memberOf Controller
       */
      triggerIndicator: function(opt, evName) {
        var ev = evName;
        if (!ev || ev.length === 0) {
          ev = 'triggerIndicator';
        }
        $(this.rootElement).trigger(ev, [opt]);
      },

      /**
       * 指定された要素に対して、インジケータ(メッセージ・画面ブロック・進捗)の表示や非表示を行うためのオブジェクトを取得します。
       * <p>
       * targetには、インジケータを表示するDOMオブジェクト、またはセレクタを指定して下さい。<br>
       * targetを指定しない場合、コントローラを割り当てた要素(rootElement)に対してインジケータを表示します。
       * <p>
       * <h4>注意:</h4>
       * targetにセレクタを指定した場合、以下の制約があります。
       * <ul>
       * <li>コントローラがバインドされた要素内に存在する要素が対象となります。
       * <li>マッチした要素が複数存在する場合、最初にマッチした要素が対象となります。
       * </ul>
       * コントローラがバインドされた要素よりも外にある要素にインジケータを表示したい場合は、セレクタではなく<b>DOMオブジェクト</b>を指定して下さい。
       * <h4>使用例</h4>
       * <b>画面全体をブロックする場合</b><br>
       * ・画面全体をブロックする場合、targetオプションに<b>document</b>、<b>window</b>または<b>body</b>を指定する。<br>
       *
       * <pre>
       * var indicator = this.indicator({
       * 	target: document
       * }).show();
       * </pre>
       *
       * <b>li要素にスロバー(くるくる回るアイコン)を表示してブロックを表示しないる場合</b><br>
       *
       * <pre>
       * var indicator = this.indicator({
       * 	target: 'li',
       * 	block: false
       * }).show();
       * </pre>
       *
       * <b>パラメータにPromiseオブジェクトを指定して、done()/fail()の実行と同時にインジケータを除去する</b><br>
       * resolve() または resolve() が実行されると、画面からインジケータを除去します。
       *
       * <pre>
       * var df = $.Deferred();
       * var indicator = this.indicator({
       * 	target: document,
       * 	promises: df.promise()
       * }).show();
       * setTimeout(function() {
       * 	df.resolve() // ここでイジケータが除去される
       * }, 2000);
       * </pre>
       *
       * <b>パラメータに複数のPromiseオブジェクトを指定して、done()/fail()の実行と同時にインジケータを除去する</b><br>
       * Promiseオブジェクトを配列で複数指定すると、全てのPromiseオブジェクトでresolve()が実行されるか、またはいずれかのPromiseオブジェクトでfail()が実行されるタイミングでインジケータを画面から除去します。
       *
       * <pre>
       * var df = $.Deferred();
       * var df2 = $.Deferred();
       * var indicator = this.indicator({
       * 	target: document,
       * 	promises: [df.promise(), df2.promise()]
       * }).show();
       * setTimeout(function() {
       * 	df.resolve()
       * }, 2000);
       * setTimeout(function() {
       * 	df.resolve() // ここでイジケータが除去される
       * }, 4000);
       * </pre>
       *
       * @param {Object} [option]
       * @param {String} [option.message] メッセージ
       * @param {Number} [option.percent] 進捗を0～100の値で指定する。
       * @param {Boolean} [option.block] 操作できないよう画面をブロックするか (true:する/false:しない)
       * @param {Promise|Promise[]} [option.promises] Promiseオブジェクト
       *            (Promiseの状態と合わせてインジケータの表示・非表示する)
       * @param {String} [options.cssClass] インジケータの基点となるクラス名 (CSSでテーマごとにスタイルをする場合に使用する)
       * @returns {Indicator} インジケータオブジェクト
       * @memberOf Controller
       * @see Indicator
       */
      indicator: function(option) {
        var target = null;
        var opt = option;
        if ($.isPlainObject(opt)) {
          target = opt.target;
        } else {
          opt = {};
        }
        if (target) {
          switch ($.type(target)) {
            case 'string':
              target = this.$find(target);
              break;
            case 'object':
              target = $(target);
              break;
            default:
              return;
          }
          if (target.length === 0) {
            return;
          }
        } else {
          target = this.rootElement;
        }
        return h5.ui.indicator.call(this, target, opt);
      },

      /**
       * コントローラに定義されているリスナーの実行を許可します。
       *
       * @memberOf Controller
       */
      enableListeners: function() {
        internal.changeExecuteListeners(this, true);
      },

      /**
       * コントローラに定義されているリスナーの実行を禁止します。
       *
       * @memberOf Controller
       */
      disableListeners: function() {
        internal.changeExecuteListeners(this, false);
      },

      /**
       * フォーマット済みメッセージを詰めたエラーをthrowします。
       *
       * @memberOf Controller
       * @param {String|Object} parameter 文字列の場合、第2引数以降をパラメータとしてフォーマットします。<br />
       *            オブジェクトの場合、そのままErrorクラスへ格納します。
       * @param {Any} [var_args] 第1引数が文字列の場合のパラメータ
       */
      throwError: function(parameter, var_args) {
        var error = null;
        if (parameter && typeof parameter === 'string') {
          error = new Error(format.apply(null, argsToArray(arguments)));
        } else {
          error = Error.apply(null, arguments);
        }
        error.customType = null;
        throw error;
      },

      /**
       * エラータイプとフォーマット済みメッセージを詰めたエラーをthrowします。
       *
       * @memberOf Controller
       * @param {String} customType エラータイプ
       * @param {String|Object} parameter 文字列の場合、第3引数以降をパラメータとしてフォーマットします。<br />
       *            オブジェクトの場合、そのままErrorクラスへ格納します。
       * @param {Any} [var_args] 第2引数が文字列の場合のパラメータ
       */
      throwCustomError: function(customType, parameter, var_args) {
        if (customType == null) {
          throw new Error('エラータイプを指定してください。');
        }
        var args = argsToArray(arguments);
        args.shift();
        if (parameter && typeof parameter === 'string') {
          error = new Error(format.apply(null, argsToArray(args)));
        } else {
          error = Error.apply(null, args);
        }
        error.customType = customType;
        throw error;
      },
      enableEventEmulation: function(isIncludeChildren) {
        internal.changeEventEmulation(controller, true, isIncludeChildren);
      },
      disableEventEmulation: function(isIncludeChildren) {
        internal.changeEventEmulation(controller, false, isIncludeChildren);
      }
    };
    // バインド対象となる要素のチェック
    if (targetElement) {
      var bindTargetElement = $(targetElement);
      if (bindTargetElement.length === 0) {
        throw new Error(format('コントローラ"{0}"のバインド対象となる要素が存在しません。', controllerName));
      }
      if (bindTargetElement.length > 1) {
        throw new Error(format('コントローラ"{0}"のバインド対象となる要素が2つ以上存在します。バインド対象は1つのみにしてください。', controllerName));
      }
    }
    // テンプレートがあればロード
    var templates = baseObj.__templates;
    var initDfd = null;
    var templatePromise = null;
    if (templates && templates.length > 0) {
      templatePromise = getView.load(templates);
      templatePromise.done(function(result) {
        if (templates && templates.length > 0) {
        }
      }).fail(function(result) {
        var faults = result.fault;
        for (var i = 0, len = faults.length; i < len; i++) {
        }
      });
    } else {
      initDfd = getDeferred();
      templatePromise = initDfd.promise();
      initDfd.resolve();
    }
    var isGlobalController = internal.isGlobalController(targetElement, controllerName);
    var GlobalController = null;
    var targetObj = $.extend(true, {}, baseObj);
    var propObj = {
      bindMap: {}
    };
    if (isGlobalController) {

      /**
       * グローバルコントローラ
       *
       * @name GlobalController
       * @class
       */
      GlobalController = Controller;

      /**
       * すべてのコントローラのインスタンスの配列を返します。
       *
       * @returns {Controller[]} コントローラ配列
       * @memberOf GlobalController
       */
      GlobalController.prototype.getAllControllers = function() {
        return this.__controllerContext.controllers;
      };

      /**
       * すべてのコントローラの構造マップを返します。
       *
       * @returns コントローラの構造マップ
       * @memberOf GlobalController
       * @private
       */
      GlobalController.prototype.getAllControllerStructure = function() {
        return this.__controllerContext.controllersStructure;
      };

      /**
       * 指定した要素にバインドされているコントローラを返します。
       *
       * @param {String|Element|jQuery} rootElement 要素
       * @returns {Controller} コントローラ
       * @memberOf GlobalController
       */
      GlobalController.prototype.getController = function(rootElement) {
        var target = typeof rootElement === 'string' ? $(rootElement).get(0) : rootElement.get ? rootElement.get(0) : rootElement;
        var controllers = this.__controllerContext.controllers;
        for (var i = 0, len = controllers.length; i < len; i++) {
          if (target === controllers[i].rootElement) {
            return controllers[i];
          }
        }
      };

      /**
       * グローバルコントローラで管理するコントローラを追加します。
       *
       * @param {Controller} obj コントローラ
       * @memberOf GlobalController
       */
      GlobalController.prototype.addController = function(obj) {
        var allControllers = this.__controllerContext.controllers;
        if ($.inArray(obj, allControllers) === -1) {
          allControllers.push(obj);
          this.__controllerContext.controllers = allControllers;
          var structure = this.__controllerContext.controllersStructure;
          var size = allControllers.length;
          var currentRoot = obj.rootElement;
          var newArray = [];
          structure[size - 1] = newArray;
          for (var i = 0; i < size; i++) {
            var targetRoot = allControllers[i].rootElement;
            var isSelf = targetRoot === currentRoot;
            if (!isSelf && $(targetRoot).has(currentRoot).length) {
              structure[i].push(size - 1);
            } else if (!isSelf && $(currentRoot).has(targetRoot).length) {
              newArray.push(i);
            }
          }
        }
      };

      /**
       * 指定された要素にバインドされているコントローラを管理対象から外します。
       *
       * @param {Element} rootElement 要素
       * @memberOf GlobalController
       */
      GlobalController.prototype.removeController = function(rootElement) {
        var allControllers = this.__controllerContext.controllers;
        var controllers = $.grep(allControllers, function(el) {
          return el.rootElement !== rootElement;
        });
        this.__controllerContext.controllers = controllers;
      };

      /**
       * triggerIndicatorイベントハンドラ
       *
       * @param {EventContext} context
       * @memberOf GlobalController
       * @private
       */
      GlobalController.prototype['* triggerIndicator'] = function(context) {
        var option = context.evArg;
        var event = context.event;
        this.indicator(this.rootElement, option).show();
        event.stopPropagation();
      };
    }
    for (var prop in targetObj) {
      if (internal.isLifecycleEvent(targetObj, prop)) {
        propObj[prop] = internal.weaveControllerAspect(targetObj, prop);
      } else if (internal.isEventCallback(targetObj, prop)) {
        var lastIndex = $.trim(prop).lastIndexOf(' ');
        var selector = $.trim(prop.substring(0, lastIndex));
        var eventName = $.trim(prop.substring(lastIndex + 1, prop.length));
        if (internal.useBind(eventName)) {
          eventName = '[' + $.trim(internal.unwrapBindEventName(eventName)) + ']';
        }
        if (internal.isOutOfController(selector)) {
          var selectTarget = internal.unwrapOutOfController(selector);
          if (selectTarget === 'this') {
            throw new Error(format('コントローラ"{0}"でセレクタ名にthisが指定されています。' + 'コントローラをバインドした要素自身を指定したい時はrootElementを指定してください。', targetObj.__name));
          }
        }
        if (!propObj.bindMap[selector]) {
          propObj.bindMap[selector] = {};
        }
        if (propObj.bindMap[selector][eventName]) {
          throw new Error(format('"コントローラ"{0}"で、{1} {2}"というイベントハンドラが重複して設定されています。', targetObj.__name, selector, eventName));
        }
        var weavedFunc = internal.weaveControllerAspect(targetObj, prop, true);
        propObj.bindMap[selector][eventName] = weavedFunc;
        propObj[prop] = weavedFunc;
      } else if (internal.isChildController(prop, targetObj)) {
        var controllerTarget = targetObj[prop];
        if (!controllerTarget) {
          propObj[prop] = controllerTarget;
          continue;
        }
        // 循環参照チェック
        if (!internal.checkDelegateCircular(controllerTarget, [targetObj])) {
          throw new Error(format('コントローラ"{0}"で、参照が循環しているため、コントローラを生成できません。', targetObj.__name));
        }
        var c = createController(null, $.extend(true, {}, targetObj[prop]), param, $.extend({
          isInternal: true,
          eventEmulation: eventEmulation
        }, fwOpt));
        propObj[prop] = c;
      } else if (endsWith(prop, 'Logic')) {
        var logicTarget = targetObj[prop];
        // 循環参照チェック
        if (!internal.checkLogicCircular(logicTarget, [])) {
          throw new Error(format('コントローラ"{0}"のロジック"{1}"で、参照が循環しているため、ロジックを生成できません。', targetObj.__name, logicTarget.__name));
        }
        var logic = createLogic(logicTarget);
        propObj[prop] = logic;
      } else if ($.isFunction(targetObj[prop])) {
        propObj[prop] = internal.weaveControllerAspect(targetObj, prop);
      } else {
        propObj[prop] = targetObj[prop];
      }
    }
    // useHandlers
    var meta = propObj.__meta;
    if (meta) {
      for (var prop in meta) {
        var metaInfo = meta[prop];
        if (!metaInfo.useHandlers) {
          continue;
        }
        var c = propObj[prop];
        if (c === undefined) {
          throw new Error(original);
        }
        if (c === null) {
          throw new Error(original);
        }
        if (Controller.prototype.constructor === c.constructor) {
          throw new Error(original);
        }
      }
    }
    var controller;
    if (targetElement) {
      var target = $(targetElement).get(0);
      controller = isGlobalController ? new GlobalController(target) : new Controller(target);
    } else {
      controller = isGlobalController ? new GlobalController() : new Controller();
    }
    controller = $.extend(true, controller, propObj);
    controller.__controllerContext.templatePromise = templatePromise;
    controller.__controllerContext.initDfd = getDeferred();
    controller.__initPromise = controller.__controllerContext.initDfd.promise();
    controller.__controllerContext.readyDfd = getDeferred();
    controller.__readyPromise = controller.__controllerContext.readyDfd.promise();
    if (controller.__construct) {
      controller.__construct(internal.setupInitializedContext(controller));
    }
    if (!controller.__controllerContext.isRoot) {
      return controller;
    }
    internal.setRootAndTriggerInit(controller);
    return controller;
  }

  /**
   * コントローラを要素にバインドします。
   *
   * @param {String|Element|jQuery} targetElement バインド対象とする要素のセレクタ、DOMエレメント、もしくはjQueryオブジェクト.<br />
   *            セレクタで指定したときにバインド対象となる要素が存在しない、もしくは2つ以上存在する場合、エラーとなります。
   * @param {Controller} controller コントローラ
   * @param {Object} [param] 初期化パラメータ.<br />
   *            初期化パラメータは __init, __readyの引数として渡されるオブジェクトの argsプロパティとして格納されます。
   * @returns {Controller} コントローラ.
   * @name bindController
   * @function
   * @memberOf h5.core
   */
  function bindController(element, controller, param) {
    var target = internal.getBindTarget(element, null, controller);
    controller.rootElement = target;
    var args = null;
    if (param) {
      args = $.extend(true, {}, param);
    }
    internal.setInternalProperty(controller, args);
    internal.setRootAndTriggerInit(controller);
    return controller;
  }
  // JsDocのための定義.実際には使用しない.

  /**
   * ロジッククラス
   *
   * @name Logic
   * @class
   */
  function Logic() {

    /**
     * コントローラのロガーを返します。
     *
     * @type Log
     * @memberOf Logic
     * @name log
     */
    this.log = h5.u.createLogger(baseObj.__name);
  }

  /**
   * Deferredオブジェクトを返します。
   *
   * @returns {Deferred} Deferredオブジェクト
   * @memberOf Logic
   */
  Logic.prototype.deferred = getDeferred;

  /**
   * オブジェクトのロジック化を行います。
   *
   * @param {Object} object ロジック定義オブジェクト
   * @returns {Logic}
   * @name logic
   * @function
   * @memberOf h5.core
   */
  function createLogic(object) {
    var logicName = object.__name;
    if (!logicName || $.trim(logicName.length) === 0) {
      throw new Error('ロジック名が定義されていません。__nameにロジック名を設定して下さい。');
    }
    if (object.__logicContext) {
      throw new Error('指定されたオブジェクトは既にロジック化されています。');
    }
    var logic = internal.weaveLogicAspect($.extend(true, {}, object));
    logic.deferred = getDeferred;
    logic.log = h5.u.createLogger(logicName);
    logic.__logicContext = {};
    for (var prop in logic) {
      if (logic.hasOwnProperty(prop) && endsWith(prop, 'Logic')) {
        var target = logic[prop];
        logic[prop] = createLogic(target);
      }
    }
    return logic;
  }

  /**
   * Core MVCの名前空間
   *
   * @name core
   * @memberOf h5
   * @namespace
   */
  h5.u.obj.expose('h5.core', {

    /**
     * オブジェクトのコントローラ化と、要素へのバインドを行います。
     *
     * @param {String|Element|jQuery} targetElement バインド対象とする要素のセレクタ、DOMエレメント、もしくはjQueryオブジェクト..<br />
     *            セレクタで指定したときにバインド対象となる要素が存在しない、もしくは2つ以上存在する場合、エラーとなります。
     * @param {Object} obj コントローラ定義オブジェクト
     * @param {Object} [param] 初期化パラメータ.<br />
     *            初期化パラメータは __init, __readyの引数として渡されるオブジェクトの argsプロパティとして格納されます。
     * @returns {Controller} コントローラ
     * @name controller
     * @function
     * @memberOf h5.core
     */
    controller: createController,
    bindController: bindController,
    logic: createLogic,

    /**
     * コントローラ、ロジックを__nameで公開します。<br />
     * 例：__nameが"com.htmlhifive.controller.TestController"の場合、window.com.htmlhifive.controller.TestController
     * で グローバルから辿れるようにします。
     *
     * @param {Controller|Logic} obj コントローラ、もしくはロジック
     * @name expose
     * @function
     * @memberOf h5.core
     */
    expose: function(obj) {
      var objName = obj.__name;
      if (!objName) {
        throw new Error('コントローラ、もしくはロジックの __name が設定されていません。');
      }
      var lastIndex = objName.lastIndexOf('.');
      if (lastIndex === -1) {
        window[objName] = obj;
      } else {
        var ns = objName.substr(0, lastIndex);
        var key = objName.substr(lastIndex + 1, objName.length);
        var nsObj = {};
        nsObj[key] = obj;
        h5.u.obj.expose(ns, nsObj);
      }
    }
  });
  // GlobalController
  var global = {
    __name: 'GlobalController'
  };
  var globalController = h5.core.controller(document, global);

  /**
   * グローバルコントローラ
   *
   * @name globalController
   * @type GlobalController
   * @memberOf h5
   */
  h5.globalController = globalController;
  // #delete begin
})(jQuery);
// #delete end
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All rights reserved.
 */
(function($) {
  // #delete end
  var H5ApiApplicationCache = function() {
    //
  };

  /**
   * オフラインキャッシュ
   *
   * @memberOf h5.api
   * @name appCache
   * @namespace
   */
  H5ApiApplicationCache.prototype = {

    /**
     * Application Cacheが利用可能であるかの判定結果。
     *
     * @memberOf h5.api.appCache
     * @name isSupported
     * @type Boolean
     */
    isSupported: !!window.applicationCache,

    /**
     * 初期化処理.
     *
     * @memberOf h5.api.appCache
     * @name init
     * @function
     */
    init: function() {
      if (!this.isSupported) {
        return;
      }
      try {
        applicationCache.addEventListener('downloading', function(ev) {
        }, true);
        applicationCache.addEventListener('updateready', function(ev) {
          applicationCache.swapCache();
          location.reload();
        }, true);
      }
      catch (e) {
      }
    },

    /**
     * キャッシュ更新イベントを実行する。
     *
     * @memberOf h5.api.appCache
     * @name update
     * @function
     */
    update: function() {
      if (!this.isSupported) {
        return;
      }
      try {
        applicationCache.update();
      }
      catch (e) {
      }
    },

    /**
     * ステータス番号からステータス名を取得する。
     *
     * @param {Number} num ステータス番号
     * @memberOf h5.api.appCache
     * @name getStatus
     * @function
     * @returns {String} ステータス名
     */
    getStatus: function(num) {
      switch (num) {
        case applicationCache.UNCACHED:          // UNCACHED == 0
          return 'UNCACHED';
        case applicationCache.IDLE:          // IDLE == 1
          return 'IDLE';
        case applicationCache.CHECKING:          // CHECKING == 2
          return 'CHECKING';
        case applicationCache.DOWNLOADING:          // DOWNLOADING == 3
          return 'DOWNLOADING';
        case applicationCache.UPDATEREADY:          // UPDATEREADY == 4
          return 'UPDATEREADY';
        case applicationCache.OBSOLETE:          // OBSOLETE == 5
          return 'OBSOLETE';
        default:
          return 'UKNOWN CACHE STATUS';
      }
    }
  };
  h5.u.obj.expose('h5.api', {
    appCache: new H5ApiApplicationCache()
  });
  // #delete begin
})(jQuery);
// #delete end
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All rights reserved.
 */
(function($) {
  // #delete end

  /**
   * watchPositionを呼ぶと、このオブジェクトをプロミス化して返します。unWatch()を呼んで位置の監視を止めます。
   *
   * @class
   * @name WatchPositionPromise
   * @param {int} watchId watchPositionが返すId
   */
  function WatchPositionPromise(watchId) {
    this.watchId = watchId;
  }
  WatchPositionPromise.prototype = {
    unWatch: function() {
      clearInterval(this.watchId);
      navigator.geolocation.clearWatch(this.watchId);
    }
  };
  var h5apiGeoInternal = {

    /**
     * ブラウザがIEか判定し、IEのバージョン番号を取得します。
     *
     * @returns {Number} IEのバージョン番号
     */
    getIEVersion: function() {
      var appVer = navigator.appVersion;
      if (appVer.indexOf('MSIE') === -1) {
        return -1;
      }
      return parseFloat(appVer.split('MSIE')[1]);
    },

    /**
     * ヒュベニの距離計算式(2点間の距離を求める計算)で使用する定数群
     *
     * @constant {Object} HUBENY
     */
    HUBENY: {

      /**
       * 世界測地系
       *
       * @constant {Object} GRS80
       */
      GRS80: {

        /**
         * 扁平率
         *
         * @constant {Number} OBLATENESS
         */
        OBLATENESS: 298.257222,

        /**
         * 長(赤道)半径
         *
         * @constant {Number} SEMIMAJOR_AXIS
         */
        SEMIMAJOR_AXIS: 6378137.0
      },

      /**
       * 日本測地系
       *
       * @constant {Object} BESSEL
       */
      BESSEL: {

        /**
         * 扁平率
         *
         * @constant {Number} OBLATENESS
         */
        OBLATENESS: 299.152813,        // 扁平率

        /**
         * 長(赤道)半径
         *
         * @constant {Number} SEMIMAJOR_AXIS
         */
        SEMIMAJOR_AXIS: 6377397.155
      }
    }
  };
  var H5ApiGeolocation = function() {
    //
  };

  /**
   * Geolocation API
   *
   * @memberOf h5.api
   * @name geo
   * @namespace
   */
  H5ApiGeolocation.prototype = {

    /**
     * Geolocation APIが使用可能であるかの判定結果<br>
     * TODO 機能ベースで判定する。
     *
     * @type Boolean
     * @memberOf h5.api.geo
     * @name isSupported
     */
    isSupported: h5apiGeoInternal.getIEVersion() >= 9 ? true : !!navigator.geolocation,

    /**
     * 現在地の緯度・経度を取得します。
     *
     * @memberOf h5.api.geo
     * @name getCurrentPosition
     * @function
     * @param {Object} [option] 設定情報
     * @param {Boolean} [option.enableHighAccuracy] 正確な位置を取得するか (ただし消費電力の増加や応答が遅延する)
     * @param {Number} [option.timeout] 位置情報を取得するまで待機する時間 (ミリ秒)
     * @param {Number} [option.maximumAge] キャッシュされた位置情報の有効期間を指定する (ミリ秒)
     * @returns Promise Promiseオブジェクト
     */
    getCurrentPosition: function(option) {
      var dfd = h5.async.deferred();
      if (!this.isSupported) {
        dfd.reject();
        return dfd.promise();
      }
      navigator.geolocation.getCurrentPosition(function(geoPosition) {
        dfd.resolve(geoPosition);
      }, function(error) {
        dfd.reject(error);
      }, option);
      return dfd.promise();
    },

    /**
     * 現在地の緯度・経度を定期的に送信します。
     *
     * @memberOf h5.api.geo
     * @name watchPosition
     * @function
     * @param {Object} [option] 設定情報
     * @param {Boolean} [option.enableHighAccuracy] 正確な位置を取得するか (ただし消費電力の増加や応答が遅延する)
     * @param {Number} [option.timeout] 位置情報を取得するまで待機する時間 (ミリ秒)
     * @param {Number} [option.maximumAge] キャッシュされた位置情報の有効期間を指定する (ミリ秒)
     * @returns WatchPositionPromise
     */
    watchPosition: function(option) {
      var dfd = h5.async.deferred();
      if (!this.isSupported) {
        setTimeout(function() {
          dfd.reject();
        }, 10);
        return dfd.promise(new WatchPositionPromise(id));
      }
      var id = navigator.geolocation.watchPosition(function(pos) {
        dfd.notify(pos);
      }, function(error) {
        dfd.reject(error);
      }, option);
      return dfd.promise(new WatchPositionPromise(id));
    },

    /**
     * ヒュベニの法則を使用して、2点間の緯度・経度から直線距離(m)を取得します。
     * <p>
     * 定数に使用している長半径・扁平率は国土地理院で紹介されている値を使用。
     * <p>
     * 注意:アルゴリズム上、長距離(100km以上)の地点を図る場合1m以上の誤差が出てしまいます。
     * <p>
     * TODO 長距離の場合も考えて、距離によって誤差が大きくならない『測地線航海算法』で計算するメソッドの追加も要検討
     *
     * @memberOf h5.api.geo
     * @name getDistance
     * @function
     * @param {Number} lat1 地点1の緯度
     * @param {Number} lng1 地点1の経度
     * @param {Number} lat2 地点2の緯度
     * @param {Number} lng2 地点2の経度
     * @param {Boolean} mode 計算モード(false: 世界測地系[GRS80](特に指定が無い場合このモードで計算する) / true: 日本測地系[ベッセル])
     * @returns {Number} 2点間の直線距離
     */
    getDistance: function(lat1, lng1, lat2, lng2, mode) {
      if (!isFinite(lat1) || !isFinite(lng1) || !isFinite(lat2) || !isFinite(lng2)) {
        return;
      }
      // ヒュベニ計算用定数
      var HUBENY_CONSTANTS = null;
      if (!mode || mode === false) {
        HUBENY_CONSTANTS = h5apiGeoInternal.HUBENY.GRS80;
      } else {
        HUBENY_CONSTANTS = h5apiGeoInternal.HUBENY.BESSEL;
      }
      // 長半径(赤道半径)
      var A = HUBENY_CONSTANTS.SEMIMAJOR_AXIS;
      // 扁平率
      var O = HUBENY_CONSTANTS.OBLATENESS;
      // 起点の緯度のラジアン
      var latRad1 = lat1 * Math.PI / 180.0;
      // 起点の経度のラジアン
      var lngRad1 = lng1 * Math.PI / 180.0;
      // 終点の緯度のラジアン
      var latRad2 = lat2 * Math.PI / 180.0;
      // 終点の経度のラジアン
      var lngRad2 = lng2 * Math.PI / 180.0;
      // 2点の平均緯度
      var avgLat = (latRad1 + latRad2) / 2.0;
      // 第一離心率
      var e = (Math.sqrt(2.0 * O - 1.0)) / O;
      var e2 = Math.pow(e, 2);
      var W = Math.sqrt(1.0 - e2 * Math.pow(Math.sin(avgLat), 2));
      // 短半径(極半径)
      var semiminorAxis = A * (1.0 - e2);
      // 子午線曲率半径
      var M = semiminorAxis / Math.pow(W, 3);
      // 卯酉船曲率半径
      var N = A / W;
      // 2点の緯度差
      var deltaLat = latRad1 - latRad2;
      // 2点の経度差
      var deltaLon = lngRad1 - lngRad2;
      return Math.sqrt(Math.pow(M * deltaLat, 2) + Math.pow(N * Math.cos(avgLat) * deltaLon, 2));
    }
  };
  h5.u.obj.expose('h5.api', {
    geo: new H5ApiGeolocation()
  });
  // #delete begin
})(jQuery);
// #delete end
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All rights reserved.
 */
(function($) {
  var getDeferred = h5.async.deferred;
  // #delete end

  /**
   * SQLError拡張クラス
   *
   * @class
   * @name H5SQLError
   * @param {SQLError} sqlError SQLエラーオブジェクト
   * @param {String} errorQuery エラーが発生したクエリ
   */
  var H5SQLError = function(sqlError, query) {
    var code = sqlError.code;
    this.message = sqlError.message;
    this.query = query;
    this.detail = null;
    for (var key in sqlError) {
      if (sqlError.hasOwnProperty(key)) {
        continue;
      }
      if (code === sqlError[key]) {
        this.detail = key;
        break;
      }
    }
  };

  /**
   * SQLTransaction拡張クラス
   *
   * @class
   * @name H5Transaction
   * @param {Object} tx SQLTransaction
   * @param {Object} _df Deferred
   */
  var H5Transaction = function(tx, df) {
    this._tx = tx;
    this._df = df;
    this.context = {};
  };

  /**
   * トランザクションの処理中に、指定した値をトランザクションの外に渡します。
   * <p>
   * 値は H5Transaction#onprogress(arg) 関数で受け取ることができます。
   * <p>
   * 使用例.
   *
   * <pre>
   * db.H5Transaction(function() {
   * 	// 成功したことを通知する例
   * 		this.insert('USER', {
   * 			'NAME': 'TANAKA',
   * 			'AGE': 25
   * 		}).done(function(rs) {
   * 			this.notify('insert1 成功');
   * 		});
   * 		// 失敗したことを通知する例
   * 		this.insert('USER', {
   * 			'NAME': 'SUZUKI',
   * 			'NOT_EXIST_COLUMN': 10
   * 		}).fail(function(error, rollback) {
   * 			this.notify('insert2 失敗');
   * 		});
   * 	}).progress(function(param) { // this.notifyで指定した値が、引数paramに設定されている。
   * 			alert(param); // アラートに『insert1 成功』と『insert2 失敗』が表示される。
   * 		}).done(function() {
   * // コミット済処理
   * 		}).fail(function() {
   * // ロールバック済処理
   * 		});
   * </pre>
   *
   * @memberOf H5Transaction
   * @param {Any} var_args onprogressに渡す値(可変長)
   */
  H5Transaction.prototype.notify = function(var_args) {
    var df = this._df;
    df.notify.apply(df, arguments);
  };

  /**
   * 指定されたクエリを実行します。
   * <p>
   * 関数の記述方法.<br>
   * <h4> insert(テーブル名, {登録対象のカラム名: 登録する値}) </h4>
   * 例)
   *
   * <pre>
   *     db.executeSql('SELECT * FROM TABLE1 WHERE NAME = ?', ['HOGE']).done(resultSet) {
   * 			// {Object} resultSet 問合せ結果
   *     }).fail(error, rollback) {
   * 			// {Object} error エラー内容
   * 			// {Function} rollback トランザクション全体の処理をロールバックしたい場合は、この関数を実行する。
   *     });
   * </pre>
   *
   * @memberOf H5Transaction
   * @param {String} query クエリ
   * @param {Array} param 置換パラメータ (配列で指定)
   * @returns {Promise} promiseオブジェクト
   */
  H5Transaction.prototype.executeSql = function(query, param) {
    var df = getDeferred();
    var p = param || [];
    var that = this;
    this._tx.executeSql(query, p, function(tx, rs) {
      df.resolveWith(that, [rs]);
    }, function(tx, error) {
      var sqlError = new H5SQLError(error, query);
      var rollback = function() {
        that._df.reject(sqlError);
        $.error('[' + sqlError.detail + ']' + sqlError.message + ' クエリ:' + sqlError.query);
      };
      df.rejectWith(that, [sqlError, rollback]);
    });
    return df.promise();
  };

  /**
   * 指定されたテーブルに対して、登録処理(INSERT)を行います。
   * <p>
   * 関数の記述方法<br>
   * <h4> insert(テーブル名, { 登録対象のカラム名: 登録する値 }) </h4>
   * 例)
   *
   * <pre>
   *     db.insert('TABLE1', {'ID': 10, 'NAME =': 'TANAKA', 'ADDRESS': '東京都'}).done(resultSet) {
   * 			// {Object} resultSet 実行結果
   *     }).fail(error, rollback) {
   * 			// {Object} error エラー内容
   * 			// {Function} rollback トランザクション全体の処理をロールバックしたい場合この関数を実行する。
   *     });
   * </pre>
   *
   * @memberOf H5Transaction
   * @param {String} tableName テーブル名
   * @param {Object|Array} param パラメータ ({カラム名:値} または、全カラム対象の場合は配列を指定)
   * @returns {Promise} promiseオブジェクト
   */
  H5Transaction.prototype.insert = function(tableName, param) {
    if (tableName == null || param == null) {
      return;
    }
    var df = getDeferred();
    var query = 'INSERT INTO ' + tableName;
    var parameter = null;
    var values = [];
    var columns = [];
    if ($.isArray(param)) {
      parameter = param;
      for (var i = 0, len = param.length; i < len; i++) {
        values.push('?');
      }
      query += ' VALUES (' + values.join(', ') + ')';
    } else if ($.type(param) === 'object') {
      parameter = [];
      for (var key in param) {
        if (!param.hasOwnProperty(key)) {
          continue;
        }
        values.push('?');
        columns.push(key);
        parameter.push(param[key]);
      }
      query += ' (' + columns.join(', ') + ') VALUES (' + values.join(', ') + ')';
    }
    var that = this;
    this._tx.executeSql(query, parameter, function(tx, rs) {
      df.resolveWith(that, [rs]);
    }, function(tx, error) {
      var sqlError = new H5SQLError(error, query);
      var rollback = function() {
        that._df.reject(sqlError);
        $.error('[' + sqlError.detail + ']' + sqlError.message + ' クエリ:' + sqlError.query);
      };
      df.rejectWith(that, [sqlError, rollback]);
    });
    return df.promise();
  };

  /**
   * 指定されたテーブルに対して、更新処理(UPDATE)を行います。
   * <p>
   * 関数の記述方法<br>
   * <h4> update(テーブル名, {更新対象のカラム名:更新後の値}, {カラム名 オペレータ:値}) </h4>
   * 例. USERテーブル(ID, ADDRESS, NAME, REGISTERED_TIMESTAMP)の、IDが5から15までのレコードのNAMEを'YAMADA'に更新する
   *
   * <pre>
   * update('USER', {
   * 	'NAME': 'YAMADA'
   * }, {
   * 	'ID &gt;=': 5,
   * 	'ID &lt;=': 15
   * }).done(function(resultSet) {
   * // {Object} resultSet 実行結果
   * 		}).fail(function(error, rollback) {
   * // {Object} error エラー内容
   * 		// {Function} rollback トランザクション全体の処理をロールバックしたい場合この関数を実行する。
   * 		});
   * </pre>
   *
   * オペレータで使用可能な文字は以下の通りです。
   * <ul>
   * <li> <=
   * <li> <
   * <li> >=
   * <li> >
   * <li> =
   * <li> !=
   * <li> like (sqliteの仕様上大文字・小文字を区別しない)
   * </ul>
   * オペレータは省略可能です。省略した場合は等価(=)として処理されます。 なお、条件を複数指定した場合は、全てAND句で結合されます。
   * この関数が対応していないクエリを実行したい場合は、executeSql関数を使用して下さい。
   *
   * @memberOf H5Transaction
   * @param {String} tableName テーブル名
   * @param {Object} param パラメータ ({カラム名:値})
   * @param {Object} condition 条件(WHERE) ({カラム名 オペレータ:値})
   * @returns {Promise} promiseオブジェクト
   */
  H5Transaction.prototype.update = function(tableName, param, condition) {
    if (tableName == null || param == null || $.isEmptyObject(param)) {
      return;
    }
    var df = getDeferred();
    var query = null;
    var parameter = [];
    var columns = [];
    var key = null;
    for (key in param) {
      if (!param.hasOwnProperty(key)) {
        continue;
      }
      columns.push(key + ' = ?');
      parameter.push(param[key]);
    }
    query = 'UPDATE ' + tableName + ' SET ' + columns.join(', ');
    if (!$.isEmptyObject(condition)) {
      var conditionAr = [];
      for (key in condition) {
        var params = key.replace(/ +/g, ' ').split(' ');
        if (params.length === 0 || params[0] === "") {
          continue;
        }
        params.splice(2, 0, '?');
        conditionAr.push(params.join(' '));
        parameter.push(condition[key]);
      }
      query += ' WHERE ' + conditionAr.join(' AND ');
    }
    var that = this;
    this._tx.executeSql(query, parameter, function(tx, rs) {
      df.resolveWith(that, [rs]);
    }, function(tx, error) {
      var sqlError = new H5SQLError(error, query);
      var rollback = function() {
        that._df.reject(sqlError);
        $.error('[' + sqlError.detail + ']' + sqlError.message + ' クエリ:' + sqlError.query);
      };
      df.rejectWith(that, [sqlError, rollback]);
    });
    return df.promise();
  };

  /**
   * 指定されたテーブルに対して、削除処理(DELETE)を行います。
   * <p>
   * 関数の記述方法<br>
   * <h4> del(テーブル名, {カラム名 オペレータ:値}) </h4>
   * 例. USERテーブル(ID, ADDRESS, NAME, REGISTERED_TIMESTAMP)の、IDが5から15までのレコードを削除する
   *
   * <pre>
   * del('USER', {
   * 	'ID &gt;=': 5,
   * 	'ID &lt;=': 15
   * }).done(function(resultSet) {
   * // {Object} resultSet 実行結果
   * 		}).fail(function(error, rollback) {
   * // {Object} error エラー内容
   * 		// {Function} rollback トランザクション全体の処理をロールバックしたい場合この関数を実行する。
   * 		});
   * </pre>
   *
   * オペレータで使用可能な文字は以下の通りです。
   * <ul>
   * <li> <=
   * <li> <
   * <li> >=
   * <li> >
   * <li> =
   * <li> !=
   * <li> like (sqliteの仕様上大文字・小文字を区別しない)
   * </ul>
   * 条件を複数指定した場合は、全てAND句で結合されます。 この関数が対応していないクエリを実行したい場合は、executeSql関数を使用して下さい。
   *
   * @memberOf H5Transaction
   * @param {String} tableName テーブル名
   * @param {Object} condition 条件(WHERE) ({カラム名 条件記号:値})
   * @returns {Promise} promiseオブジェクト
   */
  H5Transaction.prototype.del = function(tableName, condition) {
    if (tableName == null) {
      return;
    }
    var df = getDeferred();
    var parameter = [];
    var query = 'DELETE FROM ' + tableName;
    if (!$.isEmptyObject(condition)) {
      var conditionAr = [];
      for (var key in condition) {
        var param = key.replace(/ +/g, ' ').split(' ');
        if (param.length === 0 || param[0] === "") {
          continue;
        }
        param.splice(2, 0, '?');
        conditionAr.push(param.join(' '));
        parameter.push(condition[key]);
      }
      query += ' WHERE ' + conditionAr.join(' AND ');
    }
    var that = this;
    this._tx.executeSql(query, parameter, function(tx, rs) {
      df.resolveWith(that, [rs]);
    }, function(tx, error) {
      var sqlError = new H5SQLError(error, query);
      var rollback = function() {
        that._df.reject(sqlError);
        $.error('[' + sqlError.detail + ']' + sqlError.message + ' クエリ:' + sqlError.query);
      };
      df.rejectWith(that, [sqlError, rollback]);
    });
    return df.promise();
  };

  /**
   * 指定されたテーブルに対して、検索処理(SELECT)を行います。
   * <p>
   * 関数の記述方法<br>
   * <h4> select(テーブル名, [取得カラム名], {カラム名 オペレータ:値}) </h4>
   * 例. USERテーブル(ID, ADDRESS, NAME, REGISTERED_TIMESTAMP)の、IDが5から15までのレコードを取得する
   *
   * <pre>
   * select('USER', '*', {
   * 	'ID &gt;=': 5,
   * 	'ID &lt;=': 15
   * }).done(function(resultSet) {
   * // {Object} resultSet 実行結果
   * 		}).fail(function(error, rollback) {
   * // {Object} error エラー内容
   * 		// {Function} rollback トランザクション全体の処理をロールバックしたい場合この関数を実行する。
   * 		});
   * </pre>
   *
   * オペレータで使用可能な文字は以下の通りです。
   * <ul>
   * <li> <=
   * <li> <
   * <li> >=
   * <li> >
   * <li> =
   * <li> !=
   * <li> like (sqliteの仕様上大文字・小文字を区別しない)
   * </ul>
   * 条件を複数指定した場合は、全てAND句で結合されます。 この関数が対応していないクエリを実行したい場合は、executeSql関数を使用して下さい。
   *
   * @memberOf H5Transaction
   * @param {String} tableName テーブル名
   * @param {Array} columns 取得するカラム名 (配列で指定。省略した場合は全カラム(*)取得する)
   * @param {Object} condition 条件(WHERE) ({カラム名 条件記号:値})
   * @returns {Promise} promiseオブジェクト
   */
  H5Transaction.prototype.select = function(tableName, columns, condition) {
    if (tableName == null) {
      return;
    }
    var df = getDeferred();
    var parameter = [];
    var query = null;
    var column = ['*'];
    if ($.isArray(columns)) {
      column = columns;
    }
    query = 'SELECT ' + column.join(', ') + ' FROM ' + tableName;
    if (!$.isEmptyObject(condition)) {
      var conditionAr = [];
      for (var key in condition) {
        var param = key.replace(/ +/g, ' ').split(' ');
        if (param.length === 0 || param[0] === "") {
          continue;
        }
        param.splice(2, 0, '?');
        conditionAr.push(param.join(' '));
        parameter.push(condition[key]);
      }
      query += ' WHERE ' + conditionAr.join(' AND ');
    }
    var that = this;
    this._tx.executeSql(query, parameter, function(tx, rs) {
      df.resolveWith(that, [rs]);
    }, function(tx, error) {
      var sqlError = new H5SQLError(error, query);
      var rollback = function() {
        that._df.reject(sqlError);
        $.error('[' + sqlError.detail + ']' + sqlError.message + ' クエリ:' + sqlError.query);
      };
      df.rejectWith(that, [sqlError, rollback]);
    });
    return df.promise();
  };

  /**
   * Database拡張クラス
   *
   * @class
   * @name H5Database
   * @param {Database} db Databaseオブジェクト
   */
  var H5Database = function(db) {
    this._db = db;
  };

  /**
   * トランザクションをコールバック関数で取得します。
   *
   * @memberOf H5Database
   * @param {Function} callback コールバック関数
   * @param {Function} failCallback このトランザクション内の処理で、エラーが発生した場合に実行するコールバック関数
   * @param {Function} doneCallback このトランザクション内の処理が、全て正常に処理された場合に実行するコールバック関数
   */
  H5Database.prototype.h5Transaction = function(callback, failCallback, doneCallback) {
    if (!this._db || !callback) {
      return null;
    }
    this._db.transaction(function(tx) {
      var h5tx = new H5Transaction(tx);
      callback.call(h5tx, null);
    }, function(error) {
      if (failCallback !== undefined) {
        failCallback(error);
      }
    }, function() {
      if (doneCallback !== undefined) {
        doneCallback();
      }
    });
  };

  /**
   * トランザクションをコールバック関数で取得します。
   *
   * @memberOf H5Database
   * @param {Function} callback コールバック関数
   * @returns {Promise} promiseオブジェクト
   */
  H5Database.prototype.h5Transaction = function(callback) {
    if (!this._db || !callback) {
      return null;
    }
    var df = getDeferred();
    this._db.transaction(function(tx) {
      var h5tx = new H5Transaction(tx, df);
      callback.call(h5tx, null);
    }, function(error) {
      df.reject(error);
    }, function() {
      df.resolve();
    });
    return df.promise();
  };
  var H5ApiWebSqlDatabase = function() {
    //
  };

  /**
   * Web SQL Database
   *
   * @memberOf h5.api
   * @name sqldb
   * @namespace
   */
  H5ApiWebSqlDatabase.prototype = {

    /**
     * Web SQL Databaseが使用可能であるかの判定結果
     *
     * @memberOf h5.api.sqldb
     * @name isSupported
     * @type Boolean
     */
    isSupported: !!window.openDatabase,

    /**
     * データベースに接続します。
     *
     * @memberOf h5.api.sqldb
     * @name open
     * @function
     * @param {String} name データベース名
     * @param {String} version バージョン
     * @param {Number} estimatedSize 見込み容量(バイト)
     * @returns {H5DataBase} H5データベースオブジェクト
     */
    open: function(name, version, displayName, estimatedSize) {
      if (!this.isSupported) {
        return;
      }
      var conn = openDatabase(name, version, displayName, estimatedSize);
      return new H5Database(conn);
    }
  };
  h5.u.obj.expose('h5.api', {
    sqldb: new H5ApiWebSqlDatabase()
  });
  // #delete begin
})(jQuery);
// #delete end
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All rights reserved.
 */
(function($) {
  // #delete end
  var H5ApiWebStorage = function() {
    //
  };

  /**
   * Web Storage
   *
   * @memberOf h5.api
   * @name storage
   * @namespace
   */
  H5ApiWebStorage.prototype = {

    /**
     * ローカルストレージ
     *
     * @memberOf h5.api.storage
     * @name local
     * @namespace
     */
    local: {

      /**
       * Local Storageが使用可能であるかの判定結果。
       *
       * @memberOf h5.api.storage.local
       * @name isSupported
       * @type Boolean
       */
      isSupported: !!window.localStorage,

      /**
       * ローカルストレージに保存されている、キーと値のペアの数を取得します。
       *
       * @memberOf h5.api.storage.local
       * @name length
       * @function
       * @returns {Number} キーとペアの数
       */
      length: function() {
        if (!this.isSupported) {
          return;
        }
        return localStorage.length;
      },

      /**
       * 指定されたインデックスにあるキーを、ローカルストレージから取得します。
       *
       * @memberOf h5.api.storage.local
       * @name key
       * @function
       * @param {Number} index インデックス
       * @returns {String} キー
       */
      key: function(index) {
        if (!this.isSupported) {
          return;
        }
        return localStorage.key(index);
      },

      /**
       * 指定されたキーに紐付く値を、ローカルストレージから取得します。 このメソッドはストレージから値を取得する際、JSON.parse()で変換を行います。
       * 変換に成功した場合そのオブジェクトを返却し、失敗した場合もしくはオブジェクトでない場合は、文字列をそのまま返却します。
       *
       * @memberOf h5.api.storage.local
       * @name getItem
       * @function
       * @param {String} key キー
       * @param {Boolean} isDeep オブジェクト内のオブジェクトも型判定するか(true: する(未指定の場合はこのモードで実行される) / false:
       *            しない)
       * @returns {Any} キーに紐付く値
       */
      getItem: function(key, isDeep) {
        if (!this.isSupported) {
          return;
        }
        return h5.u.obj.deserialize(localStorage.getItem(key), isDeep);
      },

      /**
       * 指定されたキーで、値をローカルストレージに保存します。 オブジェクトは、JSON.stringify()で文字列に変換して保存します。 保存可能な型は、以下のとおりです。
       * <ul>
       * <li>String(文字列)
       * <li>Number(数値)
       * <li>Boolean(真偽値)
       * <li>Array(配列)
       * <li>Object(連想配列・JSON形式)
       * <li>Date(日付)
       * <li>RegExp(正規表現)
       * <li>Function(関数)
       * <li>undefined
       * <li>null
       * <li>NaN
       * <li>Infinity
       * <li>-Infinity
       * </ul>
       *
       * @memberOf h5.api.storage.local
       * @name setItem
       * @function
       * @param {String} key キー
       * @param {Any} value 値
       * @param {Boolean} isDeep オブジェクト内のオブジェクトも型判定するか(true: する(未指定の場合はこのモードで実行される) / false:
       *            しない)
       */
      setItem: function(key, value, isDeep) {
        if (!this.isSupported) {
          return;
        }
        localStorage.setItem(key, h5.u.obj.serialize(value, isDeep));
      },

      /**
       * 指定されたキーに紐付く値を、ローカルストレージから削除します。
       *
       * @memberOf h5.api.storage.local
       * @name removeItem
       * @function
       * @param {String} key キー
       */
      removeItem: function(key) {
        if (!this.isSupported) {
          return;
        }
        localStorage.removeItem(key);
      },

      /**
       * ローカルストレージに保存されている全てのキーとそれに紐付く値を全て削除します。
       *
       * @memberOf h5.api.storage.local
       * @name clear
       * @function
       */
      clear: function() {
        if (!this.isSupported) {
          return;
        }
        localStorage.clear();
      },

      /**
       * 現在ローカルストレージに保存されているオブジェクト数分、キーと値をペアで取得します。
       *
       * @memberOf h5.api.storage.local
       * @name eatch
       * @function
       * @param {Function} callback インデックス, キー, 値 を引数に持つコールバック関数
       */
      each: function(callback) {
        if (!this.isSupported) {
          return;
        }
        for (var i = 0, len = localStorage.length; i < len; i++) {
          var k = localStorage.key(i);
          callback(i, k, this.getItem(k));
        }
      }
    },

    /**
     * セッションストレージ
     *
     * @memberOf h5.api.storage
     * @name session
     * @namespace
     */
    session: {

      /**
       * Session Storageが使用可能であるかの判定結果。
       *
       * @memberOf h5.api.storage.session
       * @name isSupported
       * @type Boolean
       */
      isSupported: !!window.sessionStorage,

      /**
       * セッションストレージに保存されている、キーと値のペアの数を取得します。
       *
       * @memberOf h5.api.storage.session
       * @name length
       * @function
       * @returns {Number} キーとペアの数
       */
      length: function() {
        if (!this.isSupported) {
          return;
        }
        return sessionStorage.length;
      },

      /**
       * 指定されたインデックスにあるキーを、セッションストレージから取得します。
       *
       * @memberOf h5.api.storage.session
       * @name key
       * @function
       * @param {Number} index インデックス
       * @returns {String} キー
       */
      key: function(index) {
        if (!this.isSupported) {
          return;
        }
        return sessionStorage.key(index);
      },

      /**
       * 指定されたキーに紐付く値を、セッションストレージから取得します。 このメソッドはストレージから値を取得する際、JSON.parse()で変換を行います。
       * 変換に成功した場合そのオブジェクトを返却し、失敗した場合もしくはオブジェクトでない場合は、文字列をそのまま返却します。
       *
       * @memberOf h5.api.storage.session
       * @name getItem
       * @function
       * @param {String} key キー
       * @param {Boolean} isDeep オブジェクト内のオブジェクトも型判定するか(true: する(未指定の場合はこのモードで実行される) / false:
       *            しない)
       * @returns {Any} キーに紐付く値
       */
      getItem: function(key, isDeep) {
        if (!this.isSupported) {
          return;
        }
        return h5.u.obj.deserialize(sessionStorage.getItem(key), isDeep);
      },

      /**
       * 指定されたキーで、値をローカルストレージに保存します。 オブジェクトは、JSON.stringify()で文字列に変換して保存します。 保存可能な型は、以下のとおりです。
       * <ul>
       * <li>String(文字列)
       * <li>Number(数値)
       * <li>Boolean(真偽値)
       * <li>Array(配列)
       * <li>Object(連想配列・JSON形式)
       * <li>Date(日付)
       * <li>RegExp(正規表現)
       * <li>Function(関数)
       * <li>undefined
       * <li>null
       * <li>NaN
       * <li>Infinity
       * <li>-Infinity
       * </ul>
       *
       * @memberOf h5.api.storage.session
       * @name setItem
       * @function
       * @param {String} key キー
       * @param {Any} value 値
       * @param {Boolean} isDeep オブジェクト内のオブジェクトも型判定するか(true: する(未指定の場合はこのモードで実行される) / false:
       *            しない)
       */
      setItem: function(key, value, isDeep) {
        if (!this.isSupported) {
          return;
        }
        sessionStorage.setItem(key, h5.u.obj.serialize(value, isDeep));
      },

      /**
       * 指定されたキーに紐付く値を、セッションストレージから削除します。
       *
       * @memberOf h5.api.storage.session
       * @name removeItem
       * @function
       * @param {String} key キー
       */
      removeItem: function(key) {
        if (!this.isSupported) {
          return;
        }
        sessionStorage.removeItem(key);
      },

      /**
       * セッションストレージに保存されている全てのキーとそれに紐付く値を全て削除します。
       *
       * @memberOf h5.api.storage.session
       * @function
       * @name clear
       */
      clear: function() {
        if (!this.isSupported) {
          return;
        }
        sessionStorage.clear();
      },

      /**
       * セッションストレージに保存されているオブジェクト数分、キーと値をペアで取得します。
       *
       * @memberOf h5.api.storage.session
       * @name each
       * @function
       * @param {Function} callback インデックス, キー, 値 を引数に持つコールバック関数
       */
      each: function(callback) {
        if (!this.isSupported) {
          return;
        }
        for (var i = 0, len = sessionStorage.length; i < len; i++) {
          var k = sessionStorage.key(i);
          callback(i, k, this.getItem(k));
        }
      }
    }
  };
  h5.u.obj.expose('h5.api', {
    storage: new H5ApiWebStorage()
  });
  // #delete begin
})(jQuery);
// #delete end
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */

/**
 * h5initイベントのトリガ.
 *
 * @param {Object} jQuery
 */
(function(window, $) {
  // h5initイベントをトリガ.
  $(window.document).trigger('h5init');
})(window, jQuery);
