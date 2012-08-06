/* hifive ver.0.5.7 (2012/02/24 20:44:22) */
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */
(function(window, $) {
  /// <summary>
  ///  hifiveビルドの中心
  /// </summary>
  /// <param  name = "jQuery" type = "Object" >
  /// </param>
  // 二重読み込み防止
  if (window.h5 !== undefined) {
    return;
  }
  // ns関数で名前空間を作ったときに参照が変わらないように、先にwindowにh5を紐づけておく
  window.h5 = {};
  // グローバルコンテキストを作成
  h5.pageContext = ns('h5.core.globalContext');
  ns('h5.u');
  ns('h5.settings');
  ns('h5.ui');
  ns('h5.api');
  h5.settings = {
    allowDevMode: false,
    commonFailHandler: null,
    aspects: null
  };
  function ns(namespace) {
    /// <summary>
    ///  ドット区切りで名前空間オブジェクトを生成します。
    ///  （ns(&apos;com.htmlhifive&apos;)と呼ぶと、window.com.htmlhifiveとオブジェクトを生成します。）
    ///  すでにオブジェクトが存在した場合は、それをそのまま使用します。 引数にString以外が渡された場合はエラーとします。
    /// </summary>
    /// <param  name = "namespace" type = "String" >
    ///  名前空間
    /// </param>
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
  function expose(namespace, object) {
    /// <summary>
    ///  オブジェクトを指定された名前空間に登録し、グローバルに公開します。
    ///  引数namespaceの型がObjectでそのObjectがグローバルに紐付いていない場合は公開されません。
    /// </summary>
    /// <param  name = "namespace" type = "String|Object" >
    ///  名前空間
    /// </param>
    /// <param  name = "object" type = "Object" >
    ///  登録するオブジェクト
    /// </param>
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
  var lapInterceptor = function(invocation) {
    /// <summary>
    ///  実行時間の計測を行うインターセプタ。
    /// </summary>
    /// <param  name = "invocation" type = "Function" >
    ///  次に実行する関数
    /// </param>
    /// <returns  type = "Any" >
    ///  invocationの戻り値
    /// </returns>
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
  var logInterceptor = function(invocation) {
    /// <summary>
    ///  イベントコンテキストに格納されているものをコンソールに出力するインターセプタ。
    /// </summary>
    /// <param  name = "invocation" type = "Function" >
    ///  次に実行する関数
    /// </param>
    /// <returns  type = "Any" >
    ///  invocationの戻り値
    /// </returns>
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
  var errorInterceptor = function(invocation) {
    /// <summary>
    ///  invocationからあがってきたエラーを受け取りcommonFailHandlerに処理を任せるインターセプタ。
    /// </summary>
    /// <param  name = "invocation" type = "Function" >
    ///  次に実行する関数
    /// </param>
    /// <returns  type = "Any" >
    ///  invocationの戻り値
    /// </returns>
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
(function(window, $) {
  /// <summary>
  ///  基本はこれ(オブジェクトが1つあればよいパターン)
  /// </summary>
  /// <param  name = "jQuery" type = "Object" >
  /// </param>
  // #delete end
  var logLevel = {
    ERROR: 50,
    WARN: 40,
    INFO: 30,
    DEBUG: 20,
    TRACE: 10,
    ALL: 0
  };
  function ConsoleLogTarget() {
    /// <summary>
    ///  コンソールにログを出力するログターゲット
    /// </summary>
    //
  }
  ConsoleLogTarget.prototype = {
    log: function(logObj) {
      /// <summary>
      ///  ログをコンソールに出力します。
      /// </summary>
      /// <param  name = "logObj" type = "Object" >
      ///  ログ情報を保持するオブジェクト
      /// </param>
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
    _logMsg: function(logObj) {
      /// <summary>
      ///  指定された文字列をコンソールに出力します。
      /// </summary>
      /// <param  name = "logObj" type = "Object" >
      ///  ログ情報を保持するオブジェクト
      /// </param>
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
    _getLogPrefix: function(logObj) {
      /// <summary>
      ///  出力するログのプレフィックスを作成します。
      /// </summary>
      /// <param  name = "logObj" type = "Object" >
      ///  ログ情報を保持するオブジェクト
      /// </param>
      /// <returns  >
      ///  ログのプレフィックス
      /// </returns>
      return '[' + logObj.levelString + ']' + logObj.date.getHours() + ':' + logObj.date.getMinutes() + ':' + logObj.date.getSeconds() + ',' + logObj.date.getMilliseconds() + ': ';
    },
    _logObj: function(logObj) {
      /// <summary>
      ///  指定されたオブジェクトをコンソールに出力します。
      /// </summary>
      /// <param  name = "logObj" type = "Object" >
      ///  ログ情報を保持するオブジェクト
      /// </param>
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
  function RemoteLogTarget() {
    /// <summary>
    ///  リモートサーバにログ出力するログターゲット
    /// </summary>
    this.url = null;
  }
  RemoteLogTarget.prototype = {
    log: function(logObj) {
      /// <summary>
      ///  ログを指定されたURLに送信します。
      /// </summary>
      /// <param  name = "logObj" type = "Object" >
      ///  ログ情報を保持するオブジェクト
      /// </param>
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
    _sendTimerId: null,
    _pendingLogs: [],
    _sendLog: function() {
      /// <summary>
      ///  ログをサーバに送信します。
      /// </summary>
      this._sendTimerId = null;
      $.post(this.url, {
        logs: this._pendingLogs,
        count: this._pendingLogs.length
      }, 'json');
      this._pendingLogs = [];
    }
  };
  function Log(category) {
    /// <summary>
    ///  ログを生成するクラス
    /// </summary>
    this.category = (category === undefined) ? null : category;
    this._levelThreshold = 0;
    this.consoleLogTarget = new ConsoleLogTarget();
    this.remoteLogTarget = new RemoteLogTarget();
    this.logTarget = [this.consoleLogTarget];
  }
  Log.prototype = {
    enableStackTrace: false,
    maxStackSize: 10,
    LEVEL: logLevel,
    setLevel: function(level) {
      /// <summary>
      ///  ログの出力レベルを設定します。
      ///  
      ///  ログレベルの値は、以下の名前空間で保持しています。
      ///  
      ///  Log.LEVEL.ERROR
      ///  Log.LEVEL.WARN
      ///  Log.LEVEL.INFO
      ///  Log.LEVEL.DEBUG
      ///  Log.LEVEL.TRACE
      ///  Log.LEVEL.ALL
      ///  
      /// </summary>
      /// <param  name = "level" type = "Number" >
      ///  ログレベル
      /// </param>
      this._levelThreshold = level;
    },
    error: function(var_args) {
      /// <summary>
      ///  LEVEL.ERROR
      ///  レベルのログを出力します。
      ///  
      ///  引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
      ///  
      ///  書式については、h5.u.str.format関数のドキュメントを参照下さい。
      /// </summary>
      /// <param  name = "var_args" type = "Any" >
      /// </param>
      this._log({
        level: this.LEVEL.ERROR,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.error) : ''
      });
    },
    warn: function(var_args) {
      /// <summary>
      ///  LEVEL.WARN
      ///  レベルのログを出力します。
      ///  
      ///  引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
      ///  
      ///  書式については、h5.u.str.format関数のドキュメントを参照下さい。
      /// </summary>
      /// <param  name = "var_args" type = "Any" >
      /// </param>
      this._log({
        level: this.LEVEL.WARN,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.warn) : ''
      });
    },
    info: function(var_args) {
      /// <summary>
      ///  LEVEL.INFO
      ///  レベルのログを出力します。
      ///  
      ///  引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
      ///  
      ///  書式については、h5.u.str.format関数のドキュメントを参照下さい。
      /// </summary>
      /// <param  name = "var_args" type = "Any" >
      /// </param>
      this._log({
        level: this.LEVEL.INFO,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.info) : ''
      });
    },
    debug: function(var_args) {
      /// <summary>
      ///  LEVEL.DEBUG
      ///  レベルのログを出力します。
      ///  
      ///  引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
      ///  
      ///  書式については、h5.u.str.format関数のドキュメントを参照下さい。
      /// </summary>
      /// <param  name = "var_args" type = "Any" >
      /// </param>
      this._log({
        level: this.LEVEL.DEBUG,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.debug) : ''
      });
    },
    trace: function(var_args) {
      /// <summary>
      ///  LEVEL.TRACE
      ///  レベルのログを出力します。
      ///  
      ///  引数がObject型の場合はオブジェクト構造を、String型の場合は引数の書式に合わせてログを出力します。
      ///  
      ///  書式については、h5.u.str.format関数のドキュメントを参照下さい。
      /// </summary>
      /// <param  name = "var_args" type = "Any" >
      /// </param>
      this._log({
        level: this.LEVEL.TRACE,
        args: h5.u.obj.argsToArray(arguments),
        funcTrace: this.enableStackTrace ? this._traceFunctionName(this.trace) : ''
      });
    },
    _traceFunctionName: function(fn) {
      /// <summary>
      ///  スタックトレース(関数呼び出し関係)を取得します。
      /// </summary>
      /// <param  name = "トレース対象の関数" type = "Function" >
      ///  fn
      ///   
      /// </param>
      /// <returns  type = "String" >
      ///  スタックトレース
      /// </returns>
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
    _getFunctionName: function(fn) {
      /// <summary>
      ///  トレースした関数名を取得します。
      /// </summary>
      /// <param  name = "名前を取得したい関数" type = "Function" >
      ///  fn
      ///   
      /// </param>
      /// <returns  type = "String" >
      ///  関数名
      /// </returns>
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
    _parseArgs: function(args) {
      /// <summary>
      ///  トレースした関数の引数の型を調べます。
      /// </summary>
      /// <param  >
      ///  args
      ///  トレースした関数のarguments
      /// </param>
      /// <returns  type = "String" >
      ///  文字列化された型情報
      /// </returns>
      var argArray = h5.u.obj.argsToArray(args);
      var result = [];
      for (var i = 0; i < argArray.length; i++) {
        result.push($.type(argArray[i]));
      }
      return result.join(', ');
    },
    _log: function(logObj) {
      /// <summary>
      ///  ログ情報を保持するオブジェクトに以下の情報を付与し、コンソールまたはリモートサーバにログを出力しま
      ///  す。
      ///  
      ///  時刻
      ///  ログの種別を表す文字列(ERROR, WARN, INFO, DEBUG, TRACE, OTHER)
      ///  
      /// </summary>
      /// <param  name = "logObj" type = "Object" >
      ///  ログオブジェクト
      /// </param>
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
    _levelToString: function(level) {
      /// <summary>
      ///  ログレベルを判定して、ログの種別を表す文字列を取得します。
      /// </summary>
      /// <param  name = "level" type = "Object" >
      /// </param>
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
  var createLogger = function(category) {
    /// <summary>
    ///  ロガーを作成します。
    /// </summary>
    /// <param  name = "[category=null]" type = "String" >
    ///  カテゴリ.
    /// </param>
    /// <returns  type = "Log" >
    ///  ロガー.
    /// </returns>
    return new Log(category);
  };
  var toAbsoluteUrl = function(relativePath) {
    /// <summary>
    ///  相対URLを絶対URLに変換します。
    /// </summary>
    /// <param  name = "relativePath" type = "String" >
    ///  相対URL
    /// </param>
    var e = document.createElement('span');
    e.innerHTML = '<a href="' + relativePath + '" />';
    return e.firstChild.href;
  };
  var addedJS = [];
  var loadScript = function(src) {
    /// <summary>
    ///  スクリプトをheadに追加します。
    ///  即時に実行させるため、$.getScriptを使用せず、headにSCRIPTタグを挿入している。
    /// </summary>
    /// <param  name = "src" type = "String|String[]" >
    ///  ソースパス
    /// </param>
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
  var startsWith = function(str, prefix) {
    /// <summary>
    ///  文字列のプレフィックスが指定したものかどうかを返します。
    /// </summary>
    /// <param  name = "str" type = "String" >
    ///  文字列
    /// </param>
    /// <param  name = "prefix" type = "String" >
    ///  プレフィックス
    /// </param>
    /// <returns  type = "Boolean" >
    ///  文字列のプレフィックスが指定したものかどうか
    /// </returns>
    return str.lastIndexOf(prefix, 0) === 0;
  };
  var endsWith = function(str, suffix) {
    /// <summary>
    ///  文字列のサフィックスが指定したものかどうかを返します。
    /// </summary>
    /// <param  name = "str" type = "String" >
    ///  文字列
    /// </param>
    /// <param  name = "suffix" type = "String" >
    ///  サフィックス
    /// </param>
    /// <returns  type = "Boolean" >
    ///  文字列のサフィックスが指定したものかどうか
    /// </returns>
    var sub = str.length - suffix.length;
    return (sub >= 0) && (str.lastIndexOf(suffix) === sub);
  };
  var format = function(str, var_args) {
    /// <summary>
    ///  第一引数の文字列に含まれる{0}、{1}、{2}...{n}
    ///  (nは数字)を、第2引数以降に指定されたパラメータに置換します。
    ///  
    ///  例：
    ///  var myValue = 10;
    ///  h5.u.str.format(&apos;{0} is {1}&apos;, &apos;myValue&apos;, myValue);
    ///  
    ///  実行結果: myValue is 10
    /// </summary>
    /// <param  name = "str" type = "String" >
    ///  文字列
    /// </param>
    /// <param  name = "var_args" type = "Any" >
    ///  可変長引数
    /// </param>
    /// <returns  type = "String" >
    ///  フォーマット済み文字列
    /// </returns>
    var args = arguments;
    return str.replace(/\{(\d)\}/g, function(m, c) {
      return args[parseInt(c, 10) + 1];
    });
  };
  var escapeJs = function(str) {
    /// <summary>
    ///  指定されたJavaScript文字列をエスケープします。
    /// </summary>
    /// <param  name = "str" type = "String" >
    ///  対象文字列
    /// </param>
    /// <returns  type = "String" >
    ///  エスケープされた文字列
    /// </returns>
    if ($.type(str) !== 'string') {
      return str;
    }
    return str.replace(/\\/g, '\\\\').replace(/"/g, '"').replace(/'/g, "'").replace(/\//g, '/').replace(/</g, '&#x3c;').replace(/>/g, '&#x3e;').replace(/&#x0d/g, '\r').replace(/&#x0a/g, '\n');
  };
  var escapeHtml = function(str) {
    /// <summary>
    ///  指定されたHTML文字列をエスケープします。
    /// </summary>
    /// <param  name = "str" type = "String" >
    ///  HTML文字列
    /// </param>
    /// <returns  type = "String" >
    ///  エスケープ済HTML文字列
    /// </returns>
    if ($.type(str) !== 'string') {
      return str;
    }
    return str.replace(/&/g, "&amp;").replace(/'/g, '&apos;').replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  };
  var serialize = function(value, isDeep) {
    /// <summary>
    ///  オブジェクトを文字列化します。
    /// </summary>
    /// <param  name = "value" type = "Object" >
    ///  オブジェクト
    /// </param>
    /// <returns  type = "String" >
    ///  文字列化されたオブジェクト
    /// </returns>
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
  var deserialize = function(value, isDeep) {
    /// <summary>
    ///  文字列化されたオブジェクトを復元します。
    /// </summary>
    /// <param  name = "value" type = "String" >
    ///  文字列化されたオブジェクト
    /// </param>
    /// <returns  type = "Object" >
    ///  復元されたオブジェクト
    /// </returns>
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
  var isJQueryObject = function(obj) {
    /// <summary>
    ///  オブジェクトがjQueryオブジェクトかどうかを返します。
    /// </summary>
    /// <param  name = "obj" type = "Object" >
    ///  オブジェクト
    /// </param>
    /// <returns  type = "Boolean" >
    ///  jQueryオブジェクトかどうか
    /// </returns>
    if (!obj.jquery) {
      return false;
    }
    return (obj.jquery === $().jquery);
  };
  var argsToArray = function(args) {
    /// <summary>
    ///  argumentsを配列に変換します。
    /// </summary>
    /// <param  name = "args" type = "Arguments" >
    ///  Arguments
    /// </param>
    /// <returns  type = "Any[]" >
    ///  argumentsを変換した配列
    /// </returns>
    return Array.prototype.slice.call(args);
  };
  var isIOS = function() {
    /// <summary>
    ///  ユーザエージェントからiOSであるかどうかを返します。
    /// </summary>
    /// <returns  type = "Boolean" >
    ///  実行中の端末がiOSであるかどうか
    /// </returns>
    var ua = navigator.userAgent;
    return !!ua.match(/iPhone/i) || ua.match(/iPad/i);
  };
  var isAndroid = function() {
    /// <summary>
    ///  ユーザエージェントからAndroidであるかどうかを返します。
    /// </summary>
    /// <returns  type = "Boolean" >
    ///  実行中の端末がAndroidであるかどうか
    /// </returns>
    var ua = navigator.userAgent.toLowerCase();
    return !!ua.match(/android/i);
  };
  h5.u.obj.expose('h5.u', {
    createLogger: createLogger,
    loadScript: loadScript
  });
  h5.u.obj.expose('h5.u.str', {
    startsWith: startsWith,
    endsWith: endsWith,
    format: format,
    escapeJs: escapeJs,
    escapeHtml: escapeHtml
  });
  h5.u.obj.expose('h5.u.obj', {
    serialize: serialize,
    deserialize: deserialize,
    isJQueryObject: isJQueryObject,
    argsToArray: argsToArray
  });
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
(function(window, $) {
  /// <summary>
  ///  h5.async名前空間
  /// </summary>
  /// <param  name = "window" type = "Object" >
  /// </param>
  /// <param  name = "jQuery" type = "Object" >
  /// </param>
  var getCommonFailHandler = function() {
    return h5.settings.commonFailHandler;
  };
  var jqVersion = parseFloat($().jquery);
  var deferred = function() {
    /// <summary>
    ///  登録された共通のエラー処理を実行できるDeferredオブジェクトを返します。
    ///  使用しているjQueryのバージョンが1.7以降の場合は、Deferredに notify() notifyWith() progress()を、
    ///  Deferred.Promiseにprogress()を追加したDeferredオブジェクトを返します。
    /// </summary>
    /// <returns  type = "Deferred" >
    ///  Deferredオブジェクト
    /// </returns>
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
  var isPromise = function(object) {
    /// <summary>
    ///  オブジェクトがプロミスであるかどうかを返します。
    /// </summary>
    /// <param  name = "object" type = "Object" >
    ///  オブジェクト
    /// </param>
    /// <returns  type = "Boolean" >
    ///  オブジェクトがプロミスであるかどうか
    /// </returns>
    return object != null && object['done'] && object['fail'] && !object['resolve'] && !object['reject'];
  };
  function OrderedFunction(thisArg, func, args, retry) {
    this.that = thisArg;
    this.func = func;
    this.args = args;
    this.retry = retry;
  }
  function Order(thisArg) {
    /// <summary>
    ///  Orderクラスのコンストラクタ
    /// </summary>
    /// <param  name = "thisArg" type = "Object" >
    ///  実行時にthisとしたいオブジェクト.
    /// </param>
    this.that = thisArg;
    this.resultObj = {
      data: []
    };
    this.orderFunctions = [];
  }
  var constructor = Order.prototype.constructor;
  Order.prototype = {
    constructor: constructor,
    next: function(func, args) {
      /// <summary>
      ///  関数をセットします。
      /// </summary>
      /// <param  name = "func" type = "Function" >
      ///  関数.
      /// </param>
      /// <param  name = "[args]" type = "Any|Any[]" >
      ///  関数に渡す引数.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
      var of;
      if (this._isOrder(func)) {
        of = this._createOrderedFunction(func, func.execute);
      } else {
        of = this._createOrderedFunction(null, func, args);
      }
      this.orderFunctions.push(of);
      return this;
    },
    nextAndSet: function(key, func, args) {
      /// <summary>
      ///  関数をセットします。&lt;br
      ///  /&gt;
      ///  関数が実行されるとresultObjの指定したキーに戻り値をセットします。
      /// </summary>
      /// <param  name = "key" type = "String" >
      ///  キー.
      /// </param>
      /// <param  name = "func" type = "Function" >
      ///  関数.
      /// </param>
      /// <param  name = "[args]" type = "Any|Any[]" >
      ///  関数に渡す引数.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
      var of;
      if (this._isOrder(func)) {
        of = this._createOrderedFunction(func, this._wrap(key, func.execute));
      } else {
        of = this._createOrderedFunction(null, this._wrap(key, func), args);
      }
      this.orderFunctions.push(of);
      return this;
    },
    nextWith: function(thisArg, func, args) {
      /// <summary>
      ///  関数と実行コンテキストをセットします。
      /// </summary>
      /// <param  name = "thisArg" type = "Object" >
      ///  実行時にthisとしたいオブジェクト.
      /// </param>
      /// <param  name = "func" type = "Function" >
      ///  関数.
      /// </param>
      /// <param  name = "[args]" type = "Any|Any[]" >
      ///  関数に渡す引数.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
      var of = this._createOrderedFunction(thisArg, func, args);
      this.orderFunctions.push(of);
      return this;
    },
    nextAndSetWith: function(key, thisArg, func, args) {
      /// <summary>
      ///  関数と実行コンテキストをセットします。&lt;br
      ///  /&gt;
      ///  関数が実行されるとresultObjの指定したキーに戻り値をセットします。
      /// </summary>
      /// <param  name = "key" type = "String" >
      ///  キー.
      /// </param>
      /// <param  name = "thisArg" type = "Object" >
      ///  実行時にthisとしたいオブジェクト.
      /// </param>
      /// <param  name = "func" type = "Function" >
      ///  関数.
      /// </param>
      /// <param  name = "[args]" type = "Any|Any[]" >
      ///  関数に渡す引数.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
      var of = this._createOrderedFunction(thisArg, this._wrap(key, func), args);
      this.orderFunctions.push(of);
      return this;
    },
    parallel: function(functions) {
      /// <summary>
      ///  パラレルに実行する関数をセットします。
      /// </summary>
      /// <param  name = "functions" type = "Any[]" >
      ///  実行したい関数セットの配列.
      ///  関数セットは配列で[Function, Any|Any[]]となる.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
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
    parallelAndSet: function(functions) {
      /// <summary>
      ///  パラレルに実行する関数をセットします。&lt;br
      ///  /&gt;
      ///  関数が実行されるとresultObjの指定したキーに戻り値をセットします。
      /// </summary>
      /// <param  name = "functions" type = "Any[]" >
      ///  実行したい関数セットの配列.
      ///  関数セットは配列で[String, Function, Any|Any[]]となる.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
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
    parallelWith: function(functions) {
      /// <summary>
      ///  パラレルに実行する関数と実行コンテキストをセットします。
      /// </summary>
      /// <param  name = "functions" type = "Any[]" >
      ///  実行したい関数セットの配列.
      ///  関数セットは配列で[Object, Function, Any|Any[]]となる.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
      var funcLen = functions.length;
      var array = [];
      for (var i = 0; i < funcLen; i++) {
        var of = this._createOrderedFunction.apply(this, functions[i]);
        array.push(of);
      }
      this.orderFunctions.push(array);
      return this;
    },
    parallelAndSetWith: function(functions) {
      /// <summary>
      ///  パラレルに実行する関数と実行コンテキストをセットします。&lt;br
      ///  /&gt;
      ///  関数が実行されるとresultObjの指定したキーに戻り値をセットします。
      /// </summary>
      /// <param  name = "functions" type = "Any[]" >
      ///  実行したい関数セットの配列.
      ///  関数セットは配列で[String, Object, Function, Any|Any[]]となる.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
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
    retry: function(func, args, retry) {
      /// <summary>
      ///  指定した回数分、リトライを行う関数をセットします。
      /// </summary>
      /// <param  name = "func" type = "Function" >
      ///  関数.
      /// </param>
      /// <param  name = "args" type = "Any|Any[]" >
      ///  関数に渡す引数.
      /// </param>
      /// <param  name = "retry" type = "Number" >
      ///  リトライ回数.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
      var of;
      if (this._isOrder(func)) {
        of = this._createOrderedFunction(func, func.execute, null, retry);
      } else {
        of = this._createOrderedFunction(null, func, args, retry);
      }
      this.orderFunctions.push(of);
      return this;
    },
    retryAndSet: function(key, func, args, retry) {
      /// <summary>
      ///  指定した回数分、リトライを行う関数をセットします。&lt;br
      ///  /&gt;
      ///  関数が実行されるとresultObjの指定したキーに戻り値をセットします。
      /// </summary>
      /// <param  name = "key" type = "String" >
      ///  キー.
      /// </param>
      /// <param  name = "func" type = "Function" >
      ///  関数.
      /// </param>
      /// <param  name = "args" type = "Any|Any[]" >
      ///  関数に渡す引数.
      /// </param>
      /// <param  name = "retry" type = "Number" >
      ///  リトライ回数.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
      var of;
      if (this._isOrder(func)) {
        of = this._createOrderedFunction(func, this._wrap(key, func), null, retry);
      } else {
        of = this._createOrderedFunction(null, this._wrap(key, func), args, retry);
      }
      this.orderFunctions.push(of);
      return this;
    },
    retryWith: function(thisArg, func, args, retry) {
      /// <summary>
      ///  指定した回数分、リトライを行う関数と実行コンテキストをセットします。
      /// </summary>
      /// <param  name = "thisArg" type = "Object" >
      ///  実行時にthisとしたいオブジェクト.
      /// </param>
      /// <param  name = "func" type = "Function" >
      ///  関数.
      /// </param>
      /// <param  name = "args" type = "Any|Any[]" >
      ///  関数に渡す引数.
      /// </param>
      /// <param  name = "retry" type = "Number" >
      ///  リトライ回数.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
      var of = this._createOrderedFunction(thisArg, func, args, retry);
      this.orderFunctions.push(of);
      return this;
    },
    retryAndSetWith: function(key, thisArg, func, args, retry) {
      /// <summary>
      ///  指定した回数分、リトライを行う関数と実行コンテキストをセットします。&lt;br
      ///  /&gt;
      ///  関数が実行されるとresultObjの指定したキーに戻り値をセットします。
      /// </summary>
      /// <param  name = "key" type = "String" >
      ///  キー.
      /// </param>
      /// <param  name = "thisArg" type = "Object" >
      ///  実行時にthisとしたいオブジェクト.
      /// </param>
      /// <param  name = "func" type = "Function" >
      ///  関数.
      /// </param>
      /// <param  name = "args" type = "Any|Any[]" >
      ///  関数に渡す引数.
      /// </param>
      /// <param  name = "retry" type = "Number" >
      ///  リトライ回数.
      /// </param>
      /// <returns  type = "Order" >
      ///  Orderオブジェクト
      /// </returns>
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
    execute: function() {
      /// <summary>
      ///  セットされた関数を実行する.
      /// </summary>
      /// <returns  type = "Promise" >
      ///  Promiseオブジェクト.
      /// </returns>
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
  var order = function(thisArg) {
    /// <summary>
    ///  Orderオブジェクトを作成します。
    /// </summary>
    /// <param  name = "[thisArg=null]" type = "Object" >
    ///  thisとして実行したいオブジェクト.
    /// </param>
    /// <returns  type = "Order" >
    ///  Orderオブジェクト
    /// </returns>
    return new Order(thisArg);
  };
  h5.u.obj.expose('h5.async', {
    deferred: deferred,
    isPromise: isPromise,
    order: order
  });
  // TODO ここからOrder2の定義
  // Order2にはOrder2クラスのネスト、リトライ機能は実装していない。
  function Order2(thisArg) {
    /// <summary>
    /// </summary>
    this.that = thisArg;
    this.resultObj = {
      data: []
    };
    this.orderFunctions = [];
  }
  var constructor = Order2.prototype.constructor;
  Order2.prototype = {
    constructor: constructor,
    next: function(param) {
      /// <summary>
      /// </summary>
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
    parallel: function(functions) {
      /// <summary>
      /// </summary>
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
    execute: function() {
      /// <summary>
      ///  セットされた関数を実行する.
      /// </summary>
      /// <returns  type = "Promise" >
      ///  Promiseオブジェクト.
      /// </returns>
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
  var order2 = function(thisArg) {
    /// <summary>
    /// </summary>
    return new Order2(thisArg);
  };
  h5.u.obj.expose('h5.async', {
    order2: order2
  });
})(window, jQuery);
// #delete begin
/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */
(function(window, $) {
  /// <summary>
  ///  基本はこれ(オブジェクトが1つあればよいパターン)
  /// </summary>
  /// <param  name = "jQuery" type = "Object" >
  /// </param>
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
  var ThrobberVML = function(option) {
    /// <summary>
    /// </summary>
    this.style = $.extend(true, {}, option);
    var w = this.style.base.width;
    var h = this.style.base.height;
    this.group = createVMLElement('group', {
      width: w + 'px',
      height: h + 'px',
      display: 'inline-block',
      verticalAlign: 'middle',
      textAlign: 'left'
          // IEのバグ回避
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
  var ThrobberCanvas = function(option) {
    /// <summary>
    /// </summary>
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
  function Indicator(target, option) {
    /// <summary>
    ///  インジケータ(メッセージ・画面ブロック・進捗表示)の表示や非表示を行うクラス。
    /// </summary>
    /// <param  name = "target" type = "String|Object" >
    ///  インジケータを表示する対象のDOMオブジェクトまたはセレクタ
    /// </param>
    /// <param  name = "[option]" type = "Object" >
    ///  オプション
    /// </param>
    /// <param  name = "[option.message]" type = "String" >
    ///  メッセージ
    /// </param>
    /// <param  name = "[option.percent]" type = "Number" >
    ///  進捗を0～100の値で指定する。
    /// </param>
    /// <param  name = "[option.block]" type = "Boolean" >
    ///  操作できないよう画面をブロックするか (true:する/false:しない)
    /// </param>
    /// <param  name = "[option.promises]" type = "Promise|Promise[]" >
    ///  Promiseオブジェクト (Promiseの状態と合わせてインジケータの表示・非表示する)
    /// </param>
    /// <param  name = "[options.cssClass]" type = "String" >
    ///  インジケータの基点となるクラス名 (CSSでテーマごとにスタイルをする場合に使用する)
    /// </param>
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
    show: function() {
      /// <summary>
      ///  画面上にインジケータ(メッセージ・画面ブロック・進捗表示)を表示します。
      /// </summary>
      /// <returns  type = "Indicator" >
      ///  インジケータオブジェクト
      /// </returns>
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
    _setPosition: function() {
      /// <summary>
      ///  インジケータの表示位置を中央に設定します。
      /// </summary>
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
    _isGlobalBlockTarget: function() {
      /// <summary>
      ///  指定された要素がウィンドウ領域全体をブロックすべき要素か判定します。
      /// </summary>
      /// <returns  type = "Boolean" >
      ///  領域全体に対してブロックする要素か
      ///  (true:対象要素 false: 非対象要素)
      /// </returns>
      return this.target === document || this.target === window || this.target === document.body;
    },
    hide: function() {
      /// <summary>
      ///  画面上に表示されているインジケータ(メッセージ・画面ブロック・進捗表示)を除去します。
      /// </summary>
      /// <returns  type = "Indicator" >
      ///  インジケータオブジェクト
      /// </returns>
      if (this._isGlobalBlockTarget()) {
        $.unblockUI();
      } else {
        $(this.target).unblock();
      }
      return this;
    },
    percent: function(param) {
      /// <summary>
      ///  進捗のパーセント値を指定された値に更新します。
      /// </summary>
      /// <param  name = "param" type = "Number" >
      ///  進捗率(0～100%)
      /// </param>
      /// <returns  type = "Indicator" >
      ///  インジケータオブジェクト
      /// </returns>
      if ($.type(param) !== 'number') {
        return;
      }
      this.throbber.setPercent(param);
      return this;
    },
    message: function(param) {
      /// <summary>
      ///  メッセージを指定された値に更新します。
      /// </summary>
      /// <param  name = "param" type = "String" >
      ///  メッセージ
      /// </param>
      /// <returns  type = "Indicator" >
      ///  インジケータオブジェクト
      /// </returns>
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
  var indicator = function(target, option) {
    /// <summary>
    ///  指定された要素に対して、インジケータ(メッセージ・画面ブロック・進捗)の表示や非表示を行うためのオブジェクトを取得します。
    ///  使用例
    ///  画面全体をブロックする場合
    ///  ・画面全体をブロックする場合、targetオプションにdocument、windowまたはbodyを指定する。
    ///  
    ///  var indicator = h5.ui.indicator({
    ///  target: document,
    ///  }).show();
    ///  
    ///  li要素にスロバー(くるくる回るアイコン)を表示してブロックを表示しないる場合
    ///  
    ///  var indicator = h5.ui.indicator(&apos;li&apos;,
    ///  block: false
    ///  }).show();
    ///  
    ///  パラメータにPromiseオブジェクトを指定して、done()/fail()の実行と同時にインジケータを除去する
    ///  resolve() または resolve() が実行されると、画面からインジケータを除去します。
    ///  
    ///  var df = $.Deferred();
    ///  var indicator = h5.ui.indicator(document,
    ///  promises: df.promise()
    ///  }).show();
    ///  setTimeout(function()
    ///  df.resolve() // ここでイジケータが除去される
    ///  }, 2000);
    ///  
    ///  パラメータに複数のPromiseオブジェクトを指定して、done()/fail()の実行と同時にインジケータを除去する
    ///  Promiseオブジェクトを複数指定すると、全てのPromiseオブジェクトでresolve()が実行されるか、またはいずれかのPromiseオブジェクトでfail()が実行されるタイミングでインジケータを画面から除去します。
    ///  
    ///  var df = $.Deferred();
    ///  var df2 = $.Deferred();
    ///  var indicator = h5.ui.indicator(document,
    ///  promises: [df.promise(), df2.promise()]
    ///  }).show();
    ///  setTimeout(function()
    ///  df.resolve()
    ///  }, 2000);
    ///  setTimeout(function()
    ///  df.resolve() // ここでイジケータが除去される
    ///  }, 4000);
    ///  
    /// </summary>
    /// <param  name = "target" type = "String|Object" >
    ///  インジケータを表示する対象のDOMオブジェクトまたはセレクタ
    /// </param>
    /// <param  name = "[option.message]" type = "String" >
    ///  メッセージ
    /// </param>
    /// <param  name = "[option.percent]" type = "Number" >
    ///  進捗を0～100の値で指定する。
    /// </param>
    /// <param  name = "[option.block]" type = "Boolean" >
    ///  操作できないよう画面をブロックするか (true:する/false:しない)
    /// </param>
    /// <param  name = "[option.style]" type = "Object" >
    ///  スタイルオプション (詳細はIndicatorクラスのドキュメントを参照)
    /// </param>
    /// <param  name = "[option.promises]" type = "Promise|Promise[]" >
    ///  Promiseオブジェクト (Promiseの状態と合わせてインジケータの表示・非表示する)
    /// </param>
    /// <param  name = "[options.cssClass]" type = "String" >
    ///  インジケータの基点となるクラス名 (CSSでテーマごとにスタイルをする場合に使用する)
    /// </param>
    return new Indicator(target, option);
  };
  var isInView = function(element, container) {
    /// <summary>
    ///  要素が可視範囲内であるかどうかを返します。
    /// </summary>
    /// <param  name = "element" type = "String|Element|jQuery" >
    ///  要素
    /// </param>
    /// <param  name = "container" type = "Object" >
    ///  コンテナ
    /// </param>
    /// <returns  type = "Boolean" >
    ///  要素が可視範囲内にあるかどうか
    /// </returns>
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
  var scrollToTop = function(wait) {
    /// <summary>
    ///  ブラウザのトップにスクロールします。
    /// </summary>
    /// <param  name = "wait" type = "Number" >
    ///  スクロールを開始するまでのディレイ時間
    /// </param>
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
(function(window, $) {
  /// <summary>
  ///  h5.ajax関数
  /// </summary>
  /// <param  name = "window" type = "Object" >
  /// </param>
  /// <param  name = "jQuery" type = "Object" >
  /// </param>
  var jqVersion = parseFloat($().jquery);
  var ajax = function(var_args) {
    /// <summary>
    ///  jQuery.ajax()の戻り値、jqXHRオブジェクトにprogressメソッドを追加して返します。
    /// </summary>
    /// <param  name = "var_args" type = "Any" >
    ///  jQuery.ajaxに渡す引数
    /// </param>
    /// <returns  type = "jqXHR" >
    ///  jqXHRオブジェクト
    /// </returns>
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
  var helperExtras = {
    escapeJs: function(str) {
      /// <summary>
      ///  Javascript文字列をエスケープします。
      /// </summary>
      /// <param  name = "str" type = "String" >
      ///  エスケープ対象文字列
      /// </param>
      /// <returns  >
      ///  エスケープされた文字列
      /// </returns>
    /// <summary>
    /// </summary>
      return h5.u.str.escapeJs(str);
    },
    escapeHtml: function(str) {
      /// <summary>
      ///  HTML文字列をエスケープします。
      /// </summary>
      /// <param  name = "str" type = "String" >
      ///  エスケープ対象文字列
      /// </param>
      /// <returns  >
      ///  エスケープされた文字列
      /// </returns>
      return h5.u.str.escapeHtml(str);
    }
  };
  function isJQueryObject(obj) {
    /// <summary>
    ///  jQueryオブジェクトか判定します。
    /// </summary>
    /// <param  name = "obj" type = "Object" >
    ///  DOM要素
    /// </param>
    return h5.u.obj.isJQueryObject(obj);
  }
  var View = function() {
    /// <summary>
    ///  ビュー操作全般の処理を行います。
    /// </summary>
    this.cachedTemplate = {};
  };
  View.prototype.load = function(resourcePaths, sync) {
    /// <summary>
    ///  画面HTMLに記述されているテンプレートと、指定されたパスのテンプレートファイルを読み込みキャッシュします。
    ///  パラメータを指定しない場合は、画面HTMLに記述されているテンプレートのみ読み込みます。
    /// </summary>
    /// <param  name = "resourcePaths" type = "String|Array[String]" >
    ///  テンプレートファイル(.ejs)のパス
    ///  (配列で複数指定可能)
    /// </param>
    /// <param  name = "sync" type = "Boolean" >
    ///  テンプレートの読み込みを同期的に行うか。デフォルトは非同期(false)。 (true:同期 false:非同期)
    /// </param>
    /// <returns  type = "Promise" >
    ///  promiseオブジェクト
    /// </returns>
    var that = this;
    var resultObj = {
      fault: [],
      success: []
    };
    var cacheCompiledTemplates = function($templateElements, filePath) {
      /// <summary>
      ///  テンプレートをEJS用にコンパイルされたテンプレートに変換し、キャッシュします。
      /// </summary>
      /// <param  name = "$templateElements" type = "jQuery" >
      ///  テンプレートが記述されている要素(...)
      /// </param>
      /// <param  name = "filePath" type = "String" >
      ///  ファイルパス
      /// </param>
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
    var loadTemplate = function(paths) {
      /// <summary>
      ///  指定されたテンプレートファイルからテンプレートを非同期で読み込みます。
      /// </summary>
      /// <param  name = "paths" type = "Array[String]" >
      ///  テンプレートパス
      /// </param>
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
    var loadTemplateSync = function(paths) {
      /// <summary>
      ///  指定されたテンプレートファイルからテンプレートを同期で読み込みます。
      /// </summary>
      /// <param  name = "paths" type = "Array[String]" >
      ///  テンプレートパス
      /// </param>
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
  View.prototype.get = function(templateId, param) {
    /// <summary>
    ///  パラメータで置換された、指定されたテンプレートIDのテンプレートを取得します。
    ///  
    ///  取得するテンプレート内に置換要素([%= %])が存在する場合、パラメータを全て指定してください。
    /// </summary>
    /// <param  name = "templateId" type = "String" >
    ///  テンプレートID
    /// </param>
    /// <param  name = "[param]" type = "Object" >
    ///  パラメータ(オブジェクトリテラルで指定)
    /// </param>
    /// <returns  type = "String" >
    ///  テンプレート文字列
    /// </returns>
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
  View.prototype.update = function(element, templateId, param) {
    /// <summary>
    ///  要素を指定されたIDのテンプレートで書き換えます。
    /// </summary>
    /// <param  name = "element" type = "String|Element|jQuery" >
    ///  DOM要素(セレクタ文字列,
    ///  DOM要素, jQueryオブジェクト)
    /// </param>
    /// <param  name = "templateId" type = "String" >
    ///  テンプレートID
    /// </param>
    /// <param  name = "[param]" type = "Object" >
    ///  パラメータ
    /// </param>
    /// <returns  type = "Object" >
    ///  テンプレートが適用されたDOM要素 (jQueryオブジェクト)
    /// </returns>
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
  View.prototype.append = function(element, templateId, param) {
    /// <summary>
    ///  要素の末尾に指定されたIDのテンプレートを挿入します。
    /// </summary>
    /// <param  name = "element" type = "String|Element|jQuery" >
    ///  DOM要素(セレクタ文字列,
    ///  DOM要素, jQueryオブジェクト)
    /// </param>
    /// <param  name = "templateId" type = "String" >
    ///  テンプレートID
    /// </param>
    /// <param  name = "[param]" type = "Object" >
    ///  パラメータ
    /// </param>
    /// <returns  type = "Object" >
    ///  テンプレートが適用されたDOM要素 (jQueryオブジェクト)
    /// </returns>
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
  View.prototype.prepend = function(element, templateId, param) {
    /// <summary>
    ///  要素の先頭に指定されたIDのテンプレートを挿入します。
    /// </summary>
    /// <param  name = "element" type = "String|Element|jQuery" >
    ///  DOM要素(セレクタ文字列,
    ///  DOM要素, jQueryオブジェクト)
    /// </param>
    /// <param  name = "templateId" type = "String" >
    ///  テンプレートID
    /// </param>
    /// <param  name = "[param]" type = "Object" >
    ///  パラメータ
    /// </param>
    /// <returns  type = "Object" >
    ///  テンプレートが適用されたDOM要素 (jQueryオブジェクト)
    /// </returns>
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
  View.prototype.isAvailable = function(templateId) {
    /// <summary>
    ///  指定されたテンプレートIDのテンプレートが存在するか判定します。
    /// </summary>
    /// <param  name = "templateId" type = "String" >
    ///  テンプレートID
    /// </param>
    /// <returns  type = "Boolean" >
    ///  判定結果(存在する: true 存在しない: false)
    /// </returns>
    return this.cachedTemplate[templateId] != null;
  };
  View.prototype.clear = function() {
    /// <summary>
    ///  キャッシュされている全てのテンプレートを削除します。
    /// </summary>
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
  var internal = {
    checkExecuteListeners: function(invocation) {
      /// <summary>
      ///  コントローラのexecuteListenersを見てリスナーを実行するかどうかを決定します。
      /// </summary>
      /// <param  name = "invocation" type = "Object" >
      ///  インヴォケーション.
      /// </param>
    /// <summary>
    /// </summary>
      if (!this.__controllerContext.executeListeners) {
        return;
      }
      return invocation.proceed();
    },
    weaveControllerAspect: function(obj, prop, eventHandler) {
      /// <summary>
      ///  指定されたオブジェクトの関数にアスペクトを織り込みます。
      /// </summary>
      /// <param  name = "obj" type = "Object" >
      ///  オブジェクト.
      /// </param>
      /// <param  name = "aops" type = "Object" >
      ///  AOP用関数配列.
      /// </param>
      /// <param  name = "eventHandler" type = "Boolean" >
      ///  イベントハンドラかどうか.
      /// </param>
      /// <returns  type = "Object" >
      ///  AOPに必要なメソッドを織り込んだオブジェクト.
      /// </returns>
      var interceptors = this.getInterceptors(obj.__name, prop);
      if (eventHandler) {
        interceptors.push(this.checkExecuteListeners);
      }
      return this.createWeavedFunction(obj[prop], prop, interceptors);
    },
    isLifecycleEvent: function(obj, prop) {
      return (prop === '__ready' || prop === '__construct' || prop === '__init') && $.isFunction(obj[prop]);
    },
    weaveLogicAspect: function(obj) {
      /// <summary>
      ///  指定されたオブジェクトの関数にアスペクトを織り込みます。
      /// </summary>
      /// <param  name = "obj" type = "Object" >
      ///  オブジェクト.
      /// </param>
      /// <returns  type = "Object" >
      ///  AOPに必要なメソッドを織り込んだオブジェクト.
      /// </returns>
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
    createWeavedFunction: function(base, funcName, aspects) {
      /// <summary>
      ///  基本となる関数にアスペクトを織り込んだ関数を返します。
      /// </summary>
      /// <param  name = "baseFunc" type = "Function" >
      ///  基本関数.
      /// </param>
      /// <param  name = "funcName" type = "String" >
      ///  基本関数名.
      /// </param>
      /// <param  name = "aspects" type = "Function" >
      ///  AOP用関数配列.
      /// </param>
      /// <returns  type = "Function" >
      ///  AOP用関数を織り込んだ関数.
      /// </returns>
      var f = base;
      for (var i = 0, l = aspects.length; i < l; i++) {
        f = this.weave(f, funcName, aspects[i]);
      }
      return f;
    },
    weave: function(base, funcName, aspect) {
      /// <summary>
      ///  アスペクトを織り込んだ関数を返します。
      /// </summary>
      /// <param  name = "var_args" type = "Any" >
      ///  可変長引数
      /// </param>
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
    getInterceptors: function(targetName, pcName) {
      /// <summary>
      ///  関数名とポイントカットを比べて、条件に合致すればインターセプタを返す.
      /// </summary>
      /// <param  name = "バインドする必要のある関数名." type = "String" >
      /// </param>
      /// <param  name = "aops" type = "Object" >
      ///  AOP用関数配列.
      /// </param>
      /// <returns  type = "Array" >
      ///  AOP用関数配列.
      /// </returns>
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
    isOutOfController: function(selector) {
      /// <summary>
      ///  セレクタがコントローラの外側の要素を指しているかどうかを返します。
      ///  (外側の要素 = true)
      /// </summary>
      /// <param  >
      ///  selector セレクタ
      /// </param>
      /// <returns  >
      ///  コントローラの外側の要素を指しているかどうか
      /// </returns>
      return selector.match(/^\{.*\}$/);
    },
    unwrapOutOfController: function(selector) {
      /// <summary>
      ///  セレクタから{}を外した文字列を返します。
      /// </summary>
      /// <param  >
      ///  selector セレクタ
      /// </param>
      /// <returns  >
      ///  セレクタから{}を外した文字列
      /// </returns>
      return $.trim(selector.substring(1, selector.length - 1));
    },
    getOutOfControllerTarget: function(selector, rootElement) {
      /// <summary>
      ///  指定されたセレクタがwindow,
      ///  window., document, document., navidator, navigator. で
      ///  始まっていればそのオブジェクトを、そうでなければそのまま文字列を返します。
      /// </summary>
      /// <param  >
      ///  selector セレクタ
      /// </param>
      /// <param  >
      ///  rootElement ルートエレメント
      /// </param>
      /// <returns  >
      ///  オブジェクト、もしくはセレクタ
      /// </returns>
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
    useBind: function(eventName) {
      /// <summary>
      ///  イベント名がjQuery.bindを使って要素にイベントをバインドするかどうかを返します。
      /// </summary>
      /// <param  >
      ///  eventName イベント名
      /// </param>
      /// <returns  >
      ///  jQuery.bindを使って要素にイベントをバインドするかどうか
      /// </returns>
      return eventName.match(/^\[.*\]$/);
    },
    unwrapBindEventName: function(eventName) {
      /// <summary>
      ///  イベント名から[]を外した文字列を返す
      /// </summary>
      /// <param  >
      ///  eventName イベント名
      /// </param>
      /// <returns  >
      ///  イベント名から[]を外した文字列
      /// </returns>
      return $.trim(eventName.substring(1, eventName.length - 1));
    },
    isEventCallback: function(obj, prop) {
      /// <summary>
      ///  指定されたプロパティがイベントコールバックかどうかを返します。
      /// </summary>
      /// <param  >
      ///  obj オブジェクト
      /// </param>
      /// <param  >
      ///  prop プロパティ名
      /// </param>
      /// <returns  type = "Boolean" >
      ///  プロパティがイベントコールバックかどうか
      /// </returns>
      return prop.indexOf(' ') != -1 && $.isFunction(obj[prop]);
    },
    isGlobalController: function(targetElement, controllerName) {
      /// <summary>
      ///  グローバルコントローラかどうかを返します。
      /// </summary>
      /// <param  >
      ///  targetElement
      /// </param>
      /// <param  >
      ///  controllerName
      /// </param>
      return targetElement === document && controllerName === 'GlobalController';
    },
    checkDelegateCircular: function(object, array) {
      /// <summary>
      ///  xxxControllerの循環参照のチェックを行います。
      /// </summary>
      /// <param  >
      ///  object
      /// </param>
      /// <param  >
      ///  array
      /// </param>
      /// <returns  type = "Boolean" >
      /// </returns>
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
    checkLogicCircular: function(object, array) {
      /// <summary>
      ///  xxxControllerの循環参照のチェックを行います。
      /// </summary>
      /// <param  >
      ///  object
      /// </param>
      /// <param  >
      ///  array
      /// </param>
      /// <returns  type = "Boolean" >
      /// </returns>
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
    executeLifecycleEvent: function(controller, init) {
      /// <summary>
      ///  __init,
      ///  __readyイベントを実行する.
      /// </summary>
      /// <param  >
      ///  ｛Object} controller コントローラ.
      /// </param>
      /// <param  name = "init" type = "Booelan" >
      ///  __initイベントを実行するかどうか.
      /// </param>
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
    normalizeEventObjext: function(event) {
      /// <summary>
      ///  イベントオブジェクトを正規化します。
      /// </summary>
      /// <param  name = "event" type = "Object" >
      ///  イベントオブジェクト
      /// </param>
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
    setupContext: function(controller, args) {
      /// <summary>
      ///  イベントコンテキストをセットアップします。
      /// </summary>
      /// <param  name = "controller" type = "Object" >
      ///  コントローラ
      /// </param>
      /// <param  name = "args" type = "Object" >
      ///  1番目にはjQuery.Eventオブジェクト、2番目はjQuery.triggerに渡した引数
      /// </param>
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
    setupInitializedContext: function(controller) {
      /// <summary>
      ///  イニシャライズドイベントコンテキストをセットアップします。
      /// </summary>
      /// <param  name = "rootController" type = "Object" >
      ///  ルートコントローラ
      /// </param>
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
    // fwOptは内部的に使用している.
  function createController(targetElement, baseObj, param, fwOpt) {
    /// <summary>
    ///  コントローラのファクトリ
    /// </summary>
    /// <param  name = "targetElement" type = "String|Element|jQuery" >
    ///  バインド対象とする要素のセレクタ、DOMエレメント、もしくはjQueryオブジェクト.
    /// </param>
    /// <param  name = "baseObj" type = "Object" >
    ///  コントローラの元となるオブジェクト
    /// </param>
    /// <param  name = "[param]" type = "Object" >
    ///  初期化パラメータ.
    /// </param>
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
    function Controller(rootElement) {
      /// <summary>
      ///  コントローラのコンストラクタ
      /// </summary>
      /// <param  name = "rootElement" type = "Element" >
      ///  コントローラをバインドした要素
      /// </param>
      this.__name = controllerName;
      this.___templates = null;
      this.isGlobal = targetElement === document && controllerName === 'GlobalController';
      this.rootElement = rootElement;
      this.__controllerContext = {
        executeListeners: true,
        isRoot: !fwOpt || !fwOpt.isInternal,
        useEventEmulation: eventEmulation,
        eventEmulation: eventEmulation
      };
      if (param) {
        this.__controllerContext.args = $.extend(true, {}, param);
      }
      if (this.isGlobal) {
        this.__controllerContext.controllers = [];
        this.__controllerContext.controllersStructure = {};
      }
      this.__isInit = false;
      this.__isReady = false;
      this.__rootController = null;
      this.__parentController = null;
      this.__initPromise = null;
      this.__readyPromise = null;
      this.log = h5.u.createLogger(baseObj.__name);
    }
    var constructor = Controller.prototype.constructor;
    Controller.prototype = {
      constructor: constructor,
      $find: function(selector) {
        /// <summary>
        ///  コントローラがバインドされた要素内から要素を選択します。
        /// </summary>
        /// <param  name = "selector" type = "String" >
        ///  セレクタ
        /// </param>
        /// <returns  type = "jQuery" >
        ///  セレクタにマッチするjQueryオブジェクト
        /// </returns>
        return $(this.rootElement).find(selector);
      },
      getView: function(templateId, data) {
        /// <summary>
        ///  指定されたテンプレートを使ってHTML文字列を作成します。
        /// </summary>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <param  name = "[data]" type = "Object" >
        ///  テンプレートで使用するデータ
        /// </param>
        /// <returns  type = "String" >
        ///  HTML文字列
        /// </returns>
        return getView.get(templateId, data);
      },
      updateView: function(element, templateId, data) {
        /// <summary>
        ///  指定されたテンプレートを使ってHTML文字列を作成し、対象を更新します。
        /// </summary>
        /// <param  name = "element" type = "String" >
        ///  要素
        /// </param>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <param  name = "[data]" type = "Object" >
        ///  テンプレートで使用するデータ
        /// </param>
        var target = internal.getTarget(element, this.rootElement, true);
        getView.update(target, templateId, data);
      },
      appendView: function(element, templateId, data) {
        /// <summary>
        ///  指定されたテンプレートを使ってHTML文字列を作成し、対象に要素を追加します。
        /// </summary>
        /// <param  name = "element" type = "String" >
        ///  要素
        /// </param>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <param  name = "[data]" type = "Object" >
        ///  テンプレートで使用するデータ
        /// </param>
        var target = internal.getTarget(element, this.rootElement, true);
        getView.append(target, templateId, data);
      },
      prependView: function(element, templateId, data) {
        /// <summary>
        ///  指定されたテンプレートを使ってHTML文字列を作成し、対象の先頭に要素を追加します。
        /// </summary>
        /// <param  name = "element" type = "String" >
        ///  要素
        /// </param>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <param  name = "[data]" type = "Object" >
        ///  テンプレートで使用するデータ
        /// </param>
        var target = internal.getTarget(element, this.rootElement, true);
        getView.prepend(target, templateId, data);
      },
      deferred: function() {
        /// <summary>
        ///  Deferredオブジェクトを返します。
        /// </summary>
        /// <returns  type = "Deferred" >
        ///  Deferredオブジェクト
        /// </returns>
        return getDeferred();
      },
      trigger: function(eventName, parameter) {
        /// <summary>
        ///  ルート要素を起点に指定されたイベントを実行します。
        /// </summary>
        /// <param  name = "eventName" type = "String" >
        ///  イベント名
        /// </param>
        /// <param  name = "[parameter]" type = "Object" >
        ///  パラメータ
        /// </param>
        $(this.rootElement).trigger(eventName, [parameter]);
      },
      own: function(func) {
        /// <summary>
        ///  指定された関数に対して、コンテキスト(this)をコントローラに変更して実行する関数を返します。
        /// </summary>
        /// <param  name = "func" type = "Function" >
        ///  関数
        /// </param>
        /// <returns  type = "Function" >
        ///  コンテキスト(this)をコントローラに変更した関数
        /// </returns>
        var that = this;
        return function(var_args) {
          func.apply(that, arguments);
        };
      },
      ownWithOrg: function(func) {
        /// <summary>
        ///  指定された関数に対して、コンテキスト(this)をコントローラに変更し、元々のthisを第1引数に加えて実行する関数を返します。
        /// </summary>
        /// <param  name = "func" type = "Function" >
        ///  関数
        /// </param>
        /// <returns  type = "Function" >
        ///  コンテキスト(this)をコントローラに変更し、元々のthisを第1引数に加えた関数
        /// </returns>
        var that = this;
        return function(var_args) {
          var args = h5.u.obj.argsToArray(arguments);
          args.unshift(this);
          func.apply(that, args);
        };
      },
      unbind: function() {
        /// <summary>
        ///  コントローラのバインドを解除します。
        /// </summary>
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
      triggerIndicator: function(opt, evName) {
        /// <summary>
        ///  コントローラのインジケータイベントを実行します。
        /// </summary>
        /// <param  name = "option" type = "Object" >
        ///  オプション(message, percent, block)
        /// </param>
        /// <param  name = "ev" type = "String" >
        ///  イベント名
        /// </param>
        var ev = evName;
        if (!ev || ev.length === 0) {
          ev = 'triggerIndicator';
        }
        $(this.rootElement).trigger(ev, [opt]);
      },
      indicator: function(option) {
        /// <summary>
        ///  指定された要素に対して、インジケータ(メッセージ・画面ブロック・進捗)の表示や非表示を行うためのオブジェクトを取得します。
        ///  
        ///  targetには、インジケータを表示するDOMオブジェクト、またはセレクタを指定して下さい。
        ///  targetを指定しない場合、コントローラを割り当てた要素(rootElement)に対してインジケータを表示します。
        ///  
        ///  注意:
        ///  targetにセレクタを指定した場合、以下の制約があります。
        ///  
        ///  コントローラがバインドされた要素内に存在する要素が対象となります。
        ///  マッチした要素が複数存在する場合、最初にマッチした要素が対象となります。
        ///  
        ///  コントローラがバインドされた要素よりも外にある要素にインジケータを表示したい場合は、セレクタではなくDOMオブジェクトを指定して下さい。
        ///  使用例
        ///  画面全体をブロックする場合
        ///  ・画面全体をブロックする場合、targetオプションにdocument、windowまたはbodyを指定する。
        ///  
        ///  var indicator = this.indicator({
        ///  target: document
        ///  }).show();
        ///  
        ///  li要素にスロバー(くるくる回るアイコン)を表示してブロックを表示しないる場合
        ///  
        ///  var indicator = this.indicator({
        ///  target: &apos;li&apos;,
        ///  block: false
        ///  }).show();
        ///  
        ///  パラメータにPromiseオブジェクトを指定して、done()/fail()の実行と同時にインジケータを除去する
        ///  resolve() または resolve() が実行されると、画面からインジケータを除去します。
        ///  
        ///  var df = $.Deferred();
        ///  var indicator = this.indicator({
        ///  target: document,
        ///  promises: df.promise()
        ///  }).show();
        ///  setTimeout(function()
        ///  df.resolve() // ここでイジケータが除去される
        ///  }, 2000);
        ///  
        ///  パラメータに複数のPromiseオブジェクトを指定して、done()/fail()の実行と同時にインジケータを除去する
        ///  Promiseオブジェクトを配列で複数指定すると、全てのPromiseオブジェクトでresolve()が実行されるか、またはいずれかのPromiseオブジェクトでfail()が実行されるタイミングでインジケータを画面から除去します。
        ///  
        ///  var df = $.Deferred();
        ///  var df2 = $.Deferred();
        ///  var indicator = this.indicator({
        ///  target: document,
        ///  promises: [df.promise(), df2.promise()]
        ///  }).show();
        ///  setTimeout(function()
        ///  df.resolve()
        ///  }, 2000);
        ///  setTimeout(function()
        ///  df.resolve() // ここでイジケータが除去される
        ///  }, 4000);
        ///  
        /// </summary>
        /// <param  name = "[option]" type = "Object" >
        /// </param>
        /// <param  name = "[option.message]" type = "String" >
        ///  メッセージ
        /// </param>
        /// <param  name = "[option.percent]" type = "Number" >
        ///  進捗を0～100の値で指定する。
        /// </param>
        /// <param  name = "[option.block]" type = "Boolean" >
        ///  操作できないよう画面をブロックするか (true:する/false:しない)
        /// </param>
        /// <param  name = "[option.promises]" type = "Promise|Promise[]" >
        ///  Promiseオブジェクト
        ///  (Promiseの状態と合わせてインジケータの表示・非表示する)
        /// </param>
        /// <param  name = "[options.cssClass]" type = "String" >
        ///  インジケータの基点となるクラス名 (CSSでテーマごとにスタイルをする場合に使用する)
        /// </param>
        /// <returns  type = "Indicator" >
        ///  インジケータオブジェクト
        /// </returns>
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
      enableListeners: function() {
        /// <summary>
        ///  コントローラに定義されているリスナーの実行を許可します。
        /// </summary>
        internal.changeExecuteListeners(this, true);
      },
      disableListeners: function() {
        /// <summary>
        ///  コントローラに定義されているリスナーの実行を禁止します。
        /// </summary>
        internal.changeExecuteListeners(this, false);
      },
      throwError: function(parameter, var_args) {
        /// <summary>
        ///  フォーマット済みメッセージを詰めたエラーをthrowします。
        /// </summary>
        /// <param  name = "parameter" type = "String|Object" >
        ///  文字列の場合、第2引数以降をパラメータとしてフォーマットします。
        ///  オブジェクトの場合、そのままErrorクラスへ格納します。
        /// </param>
        /// <param  name = "[var_args]" type = "Any" >
        ///  第1引数が文字列の場合のパラメータ
        /// </param>
        var error = null;
        if (parameter && typeof parameter === 'string') {
          error = new Error(format.apply(null, argsToArray(arguments)));
        } else {
          error = Error.apply(null, arguments);
        }
        error.customType = null;
        throw error;
      },
      throwCustomError: function(customType, parameter, var_args) {
        /// <summary>
        ///  エラータイプとフォーマット済みメッセージを詰めたエラーをthrowします。
        /// </summary>
        /// <param  name = "customType" type = "String" >
        ///  エラータイプ
        /// </param>
        /// <param  name = "parameter" type = "String|Object" >
        ///  文字列の場合、第3引数以降をパラメータとしてフォーマットします。
        ///  オブジェクトの場合、そのままErrorクラスへ格納します。
        /// </param>
        /// <param  name = "[var_args]" type = "Any" >
        ///  第2引数が文字列の場合のパラメータ
        /// </param>
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
      GlobalController = Controller;
      GlobalController.prototype.getAllControllers = function() {
        /// <summary>
        ///  すべてのコントローラのインスタンスの配列を返します。
        /// </summary>
        /// <returns  type = "Controller[]" >
        ///  コントローラ配列
        /// </returns>
        return this.__controllerContext.controllers;
      };
      GlobalController.prototype.getAllControllerStructure = function() {
        /// <summary>
        ///  すべてのコントローラの構造マップを返します。
        /// </summary>
        /// <returns  >
        ///  コントローラの構造マップ
        /// </returns>
        return this.__controllerContext.controllersStructure;
      };
      GlobalController.prototype.getController = function(rootElement) {
        /// <summary>
        ///  指定した要素にバインドされているコントローラを返します。
        /// </summary>
        /// <param  name = "rootElement" type = "String|Element|jQuery" >
        ///  要素
        /// </param>
        /// <returns  type = "Controller" >
        ///  コントローラ
        /// </returns>
        var target = typeof rootElement === 'string' ? $(rootElement).get(0) : rootElement.get ? rootElement.get(0) : rootElement;
        var controllers = this.__controllerContext.controllers;
        for (var i = 0, len = controllers.length; i < len; i++) {
          if (target === controllers[i].rootElement) {
            return controllers[i];
          }
        }
      };
      GlobalController.prototype.addController = function(obj) {
        /// <summary>
        ///  グローバルコントローラで管理するコントローラを追加します。
        /// </summary>
        /// <param  name = "obj" type = "Controller" >
        ///  コントローラ
        /// </param>
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
      GlobalController.prototype.removeController = function(rootElement) {
        /// <summary>
        ///  指定された要素にバインドされているコントローラを管理対象から外します。
        /// </summary>
        /// <param  name = "rootElement" type = "Element" >
        ///  要素
        /// </param>
        var allControllers = this.__controllerContext.controllers;
        var controllers = $.grep(allControllers, function(el) {
          return el.rootElement !== rootElement;
        });
        this.__controllerContext.controllers = controllers;
      };
      GlobalController.prototype['* triggerIndicator'] = function(context) {
        /// <summary>
        ///  triggerIndicatorイベントハンドラ
        /// </summary>
        /// <param  name = "context" type = "EventContext" >
        /// </param>
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
  function bindController(element, controller, param) {
    /// <summary>
    ///  コントローラを要素にバインドします。
    /// </summary>
    /// <param  name = "targetElement" type = "String|Element|jQuery" >
    ///  バインド対象とする要素のセレクタ、DOMエレメント、もしくはjQueryオブジェクト.
    ///  セレクタで指定したときにバインド対象となる要素が存在しない、もしくは2つ以上存在する場合、エラーとなります。
    /// </param>
    /// <param  name = "controller" type = "Controller" >
    ///  コントローラ
    /// </param>
    /// <param  name = "[param]" type = "Object" >
    ///  初期化パラメータ.
    ///  初期化パラメータは __init, __readyの引数として渡されるオブジェクトの argsプロパティとして格納されます。
    /// </param>
    /// <returns  type = "Controller" >
    ///  コントローラ.
    /// </returns>
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
  function Logic() {
    /// <summary>
    ///  ロジッククラス
    /// </summary>
    this.log = h5.u.createLogger(baseObj.__name);
  }
  Logic.prototype.deferred = getDeferred;
  function createLogic(object) {
    /// <summary>
    ///  オブジェクトのロジック化を行います。
    /// </summary>
    /// <param  name = "object" type = "Object" >
    ///  ロジック定義オブジェクト
    /// </param>
    /// <returns  type = "Logic" >
    /// </returns>
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
  h5.u.obj.expose('h5.core', {
    controller: createController,
    bindController: bindController,
    logic: createLogic,
    expose: function(obj) {
      /// <summary>
      ///  コントローラ、ロジックを__nameで公開します。&lt;br
      ///  /&gt;
      ///  例：__nameが&quot;com.htmlhifive.controller.TestController&quot;の場合、window.com.htmlhifive.controller.TestController
      ///  で グローバルから辿れるようにします。
      /// </summary>
      /// <param  name = "obj" type = "Controller|Logic" >
      ///  コントローラ、もしくはロジック
      /// </param>
    /// <summary>
    ///  Core
    ///  MVCの名前空間
    /// </summary>
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
  H5ApiApplicationCache.prototype = {
    isSupported: !!window.applicationCache,
    init: function() {
      /// <summary>
      ///  初期化処理.
      /// </summary>
    /// <summary>
    ///  オフラインキャッシュ
    /// </summary>
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
    update: function() {
      /// <summary>
      ///  キャッシュ更新イベントを実行する。
      /// </summary>
      if (!this.isSupported) {
        return;
      }
      try {
        applicationCache.update();
      }
      catch (e) {
      }
    },
    getStatus: function(num) {
      /// <summary>
      ///  ステータス番号からステータス名を取得する。
      /// </summary>
      /// <param  name = "num" type = "Number" >
      ///  ステータス番号
      /// </param>
      /// <returns  type = "String" >
      ///  ステータス名
      /// </returns>
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
  function WatchPositionPromise(watchId) {
    /// <summary>
    ///  watchPositionを呼ぶと、このオブジェクトをプロミス化して返します。unWatch()を呼んで位置の監視を止めます。
    /// </summary>
    /// <param  name = "watchId" type = "int" >
    ///  watchPositionが返すId
    /// </param>
    this.watchId = watchId;
  }
  WatchPositionPromise.prototype = {
    unWatch: function() {
      clearInterval(this.watchId);
      navigator.geolocation.clearWatch(this.watchId);
    }
  };
  var h5apiGeoInternal = {
    getIEVersion: function() {
      /// <summary>
      ///  ブラウザがIEか判定し、IEのバージョン番号を取得します。
      /// </summary>
      /// <returns  type = "Number" >
      ///  IEのバージョン番号
      /// </returns>
      var appVer = navigator.appVersion;
      if (appVer.indexOf('MSIE') === -1) {
        return -1;
      }
      return parseFloat(appVer.split('MSIE')[1]);
    },
    HUBENY: {
      GRS80: {
        OBLATENESS: 298.257222,
        SEMIMAJOR_AXIS: 6378137.0
      },
      BESSEL: {
        OBLATENESS: 299.152813,        // 扁平率
        SEMIMAJOR_AXIS: 6377397.155
      }
    }
  };
  var H5ApiGeolocation = function() {
    //
  };
  H5ApiGeolocation.prototype = {
    isSupported: h5apiGeoInternal.getIEVersion() >= 9 ? true : !!navigator.geolocation,
    getCurrentPosition: function(option) {
      /// <summary>
      ///  現在地の緯度・経度を取得します。
      /// </summary>
      /// <param  name = "[option]" type = "Object" >
      ///  設定情報
      /// </param>
      /// <param  name = "[option.enableHighAccuracy]" type = "Boolean" >
      ///  正確な位置を取得するか (ただし消費電力の増加や応答が遅延する)
      /// </param>
      /// <param  name = "[option.timeout]" type = "Number" >
      ///  位置情報を取得するまで待機する時間 (ミリ秒)
      /// </param>
      /// <param  name = "[option.maximumAge]" type = "Number" >
      ///  キャッシュされた位置情報の有効期間を指定する (ミリ秒)
      /// </param>
      /// <returns  >
      ///  Promise Promiseオブジェクト
      /// </returns>
    /// <summary>
    ///  Geolocation
    ///  API
    /// </summary>
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
    watchPosition: function(option) {
      /// <summary>
      ///  現在地の緯度・経度を定期的に送信します。
      /// </summary>
      /// <param  name = "[option]" type = "Object" >
      ///  設定情報
      /// </param>
      /// <param  name = "[option.enableHighAccuracy]" type = "Boolean" >
      ///  正確な位置を取得するか (ただし消費電力の増加や応答が遅延する)
      /// </param>
      /// <param  name = "[option.timeout]" type = "Number" >
      ///  位置情報を取得するまで待機する時間 (ミリ秒)
      /// </param>
      /// <param  name = "[option.maximumAge]" type = "Number" >
      ///  キャッシュされた位置情報の有効期間を指定する (ミリ秒)
      /// </param>
      /// <returns  >
      ///  WatchPositionPromise
      /// </returns>
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
    getDistance: function(lat1, lng1, lat2, lng2, mode) {
      /// <summary>
      ///  ヒュベニの法則を使用して、2点間の緯度・経度から直線距離(m)を取得します。
      ///  
      ///  定数に使用している長半径・扁平率は国土地理院で紹介されている値を使用。
      ///  
      ///  注意:アルゴリズム上、長距離(100km以上)の地点を図る場合1m以上の誤差が出てしまいます。
      ///  
      ///  TODO 長距離の場合も考えて、距離によって誤差が大きくならない『測地線航海算法』で計算するメソッドの追加も要検討
      /// </summary>
      /// <param  name = "lat1" type = "Number" >
      ///  地点1の緯度
      /// </param>
      /// <param  name = "lng1" type = "Number" >
      ///  地点1の経度
      /// </param>
      /// <param  name = "lat2" type = "Number" >
      ///  地点2の緯度
      /// </param>
      /// <param  name = "lng2" type = "Number" >
      ///  地点2の経度
      /// </param>
      /// <param  name = "mode" type = "Boolean" >
      ///  計算モード(false: 世界測地系[GRS80](特に指定が無い場合このモードで計算する) true: 日本測地系[ベッセル])
      /// </param>
      /// <returns  type = "Number" >
      ///  2点間の直線距離
      /// </returns>
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
  var H5SQLError = function(sqlError, query) {
    /// <summary>
    ///  SQLError拡張クラス
    /// </summary>
    /// <param  name = "sqlError" type = "SQLError" >
    ///  SQLエラーオブジェクト
    /// </param>
    /// <param  name = "errorQuery" type = "String" >
    ///  エラーが発生したクエリ
    /// </param>
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
  var H5Transaction = function(tx, df) {
    /// <summary>
    ///  SQLTransaction拡張クラス
    /// </summary>
    /// <param  name = "tx" type = "Object" >
    ///  SQLTransaction
    /// </param>
    /// <param  name = "_df" type = "Object" >
    ///  Deferred
    /// </param>
    this._tx = tx;
    this._df = df;
    this.context = {};
  };
  H5Transaction.prototype.notify = function(var_args) {
    /// <summary>
    ///  トランザクションの処理中に、指定した値をトランザクションの外に渡します。
    ///  
    ///  値は H5Transaction#onprogress(arg) 関数で受け取ることができます。
    ///  
    ///  使用例.
    ///  
    ///  db.H5Transaction(function()
    ///  // 成功したことを通知する例
    ///  this.insert(&apos;USER&apos;,
    ///  &apos;NAME&apos;: &apos;TANAKA&apos;,
    ///  &apos;AGE&apos;: 25
    ///  }).done(function(rs)
    ///  this.notify(&apos;insert1 成功&apos;);
    ///  });
    ///  // 失敗したことを通知する例
    ///  this.insert(&apos;USER&apos;,
    ///  &apos;NAME&apos;: &apos;SUZUKI&apos;,
    ///  &apos;NOT_EXIST_COLUMN&apos;: 10
    ///  }).fail(function(error, rollback)
    ///  this.notify(&apos;insert2 失敗&apos;);
    ///  });
    ///  }).progress(function(param) // this.notifyで指定した値が、引数paramに設定されている。
    ///  alert(param); // アラートに『insert1 成功』と『insert2 失敗』が表示される。
    ///  }).done(function()
    ///  // コミット済処理
    ///  }).fail(function()
    ///  // ロールバック済処理
    ///  });
    ///  
    /// </summary>
    /// <param  name = "var_args" type = "Any" >
    ///  onprogressに渡す値(可変長)
    /// </param>
    var df = this._df;
    df.notify.apply(df, arguments);
  };
  H5Transaction.prototype.executeSql = function(query, param) {
    /// <summary>
    ///  指定されたクエリを実行します。
    ///  
    ///  関数の記述方法.
    ///   insert(テーブル名, {登録対象のカラム名: 登録する値}) 
    ///  例)
    ///  
    ///  db.executeSql(&apos;SELECT FROM TABLE1 WHERE NAME = ?&apos;, [&apos;HOGE&apos;]).done(resultSet)
    ///  //  resultSet 問合せ結果
    ///  }).fail(error, rollback)
    ///  //  error エラー内容
    ///  //  rollback トランザクション全体の処理をロールバックしたい場合は、この関数を実行する。
    ///  });
    ///  
    /// </summary>
    /// <param  name = "query" type = "String" >
    ///  クエリ
    /// </param>
    /// <param  name = "param" type = "Array" >
    ///  置換パラメータ (配列で指定)
    /// </param>
    /// <returns  type = "Promise" >
    ///  promiseオブジェクト
    /// </returns>
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
  H5Transaction.prototype.insert = function(tableName, param) {
    /// <summary>
    ///  指定されたテーブルに対して、登録処理(INSERT)を行います。
    ///  
    ///  関数の記述方法
    ///   insert(テーブル名, 登録対象のカラム名: 登録する値 }) 
    ///  例)
    ///  
    ///  db.insert(&apos;TABLE1&apos;, {&apos;ID&apos;: 10, &apos;NAME =&apos;: &apos;TANAKA&apos;, &apos;ADDRESS&apos;: &apos;東京都&apos;}).done(resultSet)
    ///  //  resultSet 実行結果
    ///  }).fail(error, rollback)
    ///  //  error エラー内容
    ///  //  rollback トランザクション全体の処理をロールバックしたい場合この関数を実行する。
    ///  });
    ///  
    /// </summary>
    /// <param  name = "tableName" type = "String" >
    ///  テーブル名
    /// </param>
    /// <param  name = "param" type = "Object|Array" >
    ///  パラメータ ({カラム名:値} または、全カラム対象の場合は配列を指定)
    /// </param>
    /// <returns  type = "Promise" >
    ///  promiseオブジェクト
    /// </returns>
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
  H5Transaction.prototype.update = function(tableName, param, condition) {
    /// <summary>
    ///  指定されたテーブルに対して、更新処理(UPDATE)を行います。
    ///  
    ///  関数の記述方法
    ///   update(テーブル名, {更新対象のカラム名:更新後の値}, {カラム名 オペレータ:値}) 
    ///  例. USERテーブル(ID, ADDRESS, NAME, REGISTERED_TIMESTAMP)の、IDが5から15までのレコードのNAMEを&apos;YAMADA&apos;に更新する
    ///  
    ///  update(&apos;USER&apos;,
    ///  &apos;NAME&apos;: &apos;YAMADA&apos;
    ///  },
    ///  &apos;ID &amp;gt;=&apos;: 5,
    ///  &apos;ID &amp;lt;=&apos;: 15
    ///  }).done(function(resultSet)
    ///  //  resultSet 実行結果
    ///  }).fail(function(error, rollback)
    ///  //  error エラー内容
    ///  //  rollback トランザクション全体の処理をロールバックしたい場合この関数を実行する。
    ///  });
    ///  
    ///  オペレータで使用可能な文字は以下の通りです。
    ///  
    ///   &lt;=
    ///   &lt;
    ///   &gt;=
    ///   &gt;
    ///   =
    ///   !=
    ///   like (sqliteの仕様上大文字・小文字を区別しない)
    ///  
    ///  オペレータは省略可能です。省略した場合は等価(=)として処理されます。 なお、条件を複数指定した場合は、全てAND句で結合されます。
    ///  この関数が対応していないクエリを実行したい場合は、executeSql関数を使用して下さい。
    /// </summary>
    /// <param  name = "tableName" type = "String" >
    ///  テーブル名
    /// </param>
    /// <param  name = "param" type = "Object" >
    ///  パラメータ ({カラム名:値})
    /// </param>
    /// <param  name = "condition" type = "Object" >
    ///  条件(WHERE) ({カラム名 オペレータ:値})
    /// </param>
    /// <returns  type = "Promise" >
    ///  promiseオブジェクト
    /// </returns>
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
  H5Transaction.prototype.del = function(tableName, condition) {
    /// <summary>
    ///  指定されたテーブルに対して、削除処理(DELETE)を行います。
    ///  
    ///  関数の記述方法
    ///   del(テーブル名, {カラム名 オペレータ:値}) 
    ///  例. USERテーブル(ID, ADDRESS, NAME, REGISTERED_TIMESTAMP)の、IDが5から15までのレコードを削除する
    ///  
    ///  del(&apos;USER&apos;,
    ///  &apos;ID &amp;gt;=&apos;: 5,
    ///  &apos;ID &amp;lt;=&apos;: 15
    ///  }).done(function(resultSet)
    ///  //  resultSet 実行結果
    ///  }).fail(function(error, rollback)
    ///  //  error エラー内容
    ///  //  rollback トランザクション全体の処理をロールバックしたい場合この関数を実行する。
    ///  });
    ///  
    ///  オペレータで使用可能な文字は以下の通りです。
    ///  
    ///   &lt;=
    ///   &lt;
    ///   &gt;=
    ///   &gt;
    ///   =
    ///   !=
    ///   like (sqliteの仕様上大文字・小文字を区別しない)
    ///  
    ///  条件を複数指定した場合は、全てAND句で結合されます。 この関数が対応していないクエリを実行したい場合は、executeSql関数を使用して下さい。
    /// </summary>
    /// <param  name = "tableName" type = "String" >
    ///  テーブル名
    /// </param>
    /// <param  name = "condition" type = "Object" >
    ///  条件(WHERE) ({カラム名 条件記号:値})
    /// </param>
    /// <returns  type = "Promise" >
    ///  promiseオブジェクト
    /// </returns>
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
  H5Transaction.prototype.select = function(tableName, columns, condition) {
    /// <summary>
    ///  指定されたテーブルに対して、検索処理(SELECT)を行います。
    ///  
    ///  関数の記述方法
    ///   select(テーブル名, [取得カラム名], {カラム名 オペレータ:値}) 
    ///  例. USERテーブル(ID, ADDRESS, NAME, REGISTERED_TIMESTAMP)の、IDが5から15までのレコードを取得する
    ///  
    ///  select(&apos;USER&apos;, &apos;*&apos;,
    ///  &apos;ID &amp;gt;=&apos;: 5,
    ///  &apos;ID &amp;lt;=&apos;: 15
    ///  }).done(function(resultSet)
    ///  //  resultSet 実行結果
    ///  }).fail(function(error, rollback)
    ///  //  error エラー内容
    ///  //  rollback トランザクション全体の処理をロールバックしたい場合この関数を実行する。
    ///  });
    ///  
    ///  オペレータで使用可能な文字は以下の通りです。
    ///  
    ///   &lt;=
    ///   &lt;
    ///   &gt;=
    ///   &gt;
    ///   =
    ///   !=
    ///   like (sqliteの仕様上大文字・小文字を区別しない)
    ///  
    ///  条件を複数指定した場合は、全てAND句で結合されます。 この関数が対応していないクエリを実行したい場合は、executeSql関数を使用して下さい。
    /// </summary>
    /// <param  name = "tableName" type = "String" >
    ///  テーブル名
    /// </param>
    /// <param  name = "columns" type = "Array" >
    ///  取得するカラム名 (配列で指定。省略した場合は全カラム(*)取得する)
    /// </param>
    /// <param  name = "condition" type = "Object" >
    ///  条件(WHERE) ({カラム名 条件記号:値})
    /// </param>
    /// <returns  type = "Promise" >
    ///  promiseオブジェクト
    /// </returns>
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
  var H5Database = function(db) {
    /// <summary>
    ///  Database拡張クラス
    /// </summary>
    /// <param  name = "db" type = "Database" >
    ///  Databaseオブジェクト
    /// </param>
    this._db = db;
  };
  H5Database.prototype.h5Transaction = function(callback, failCallback, doneCallback) {
    /// <summary>
    ///  トランザクションをコールバック関数で取得します。
    /// </summary>
    /// <param  name = "callback" type = "Function" >
    ///  コールバック関数
    /// </param>
    /// <param  name = "failCallback" type = "Function" >
    ///  このトランザクション内の処理で、エラーが発生した場合に実行するコールバック関数
    /// </param>
    /// <param  name = "doneCallback" type = "Function" >
    ///  このトランザクション内の処理が、全て正常に処理された場合に実行するコールバック関数
    /// </param>
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
  H5Database.prototype.h5Transaction = function(callback) {
    /// <summary>
    ///  トランザクションをコールバック関数で取得します。
    /// </summary>
    /// <param  name = "callback" type = "Function" >
    ///  コールバック関数
    /// </param>
    /// <returns  type = "Promise" >
    ///  promiseオブジェクト
    /// </returns>
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
  H5ApiWebSqlDatabase.prototype = {
    isSupported: !!window.openDatabase,
    open: function(name, version, displayName, estimatedSize) {
      /// <summary>
      ///  データベースに接続します。
      /// </summary>
      /// <param  name = "name" type = "String" >
      ///  データベース名
      /// </param>
      /// <param  name = "version" type = "String" >
      ///  バージョン
      /// </param>
      /// <param  name = "estimatedSize" type = "Number" >
      ///  見込み容量(バイト)
      /// </param>
      /// <returns  type = "H5DataBase" >
      ///  H5データベースオブジェクト
      /// </returns>
    /// <summary>
    ///  Web
    ///  SQL Database
    /// </summary>
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
  H5ApiWebStorage.prototype = {
    local: {
      isSupported: !!window.localStorage,
      length: function() {
        /// <summary>
        ///  ローカルストレージに保存されている、キーと値のペアの数を取得します。
        /// </summary>
        /// <returns  type = "Number" >
        ///  キーとペアの数
        /// </returns>
      /// <summary>
      ///  ローカルストレージ
      /// </summary>
    /// <summary>
    ///  Web
    ///  Storage
    /// </summary>
        if (!this.isSupported) {
          return;
        }
        return localStorage.length;
      },
      key: function(index) {
        /// <summary>
        ///  指定されたインデックスにあるキーを、ローカルストレージから取得します。
        /// </summary>
        /// <param  name = "index" type = "Number" >
        ///  インデックス
        /// </param>
        /// <returns  type = "String" >
        ///  キー
        /// </returns>
        if (!this.isSupported) {
          return;
        }
        return localStorage.key(index);
      },
      getItem: function(key, isDeep) {
        /// <summary>
        ///  指定されたキーに紐付く値を、ローカルストレージから取得します。
        ///  このメソッドはストレージから値を取得する際、JSON.parse()で変換を行います。
        ///  変換に成功した場合そのオブジェクトを返却し、失敗した場合もしくはオブジェクトでない場合は、文字列をそのまま返却します。
        /// </summary>
        /// <param  name = "key" type = "String" >
        ///  キー
        /// </param>
        /// <param  name = "isDeep" type = "Boolean" >
        ///  オブジェクト内のオブジェクトも型判定するか(true: する(未指定の場合はこのモードで実行される) false:
        ///  しない)
        /// </param>
        /// <returns  type = "Any" >
        ///  キーに紐付く値
        /// </returns>
        if (!this.isSupported) {
          return;
        }
        return h5.u.obj.deserialize(localStorage.getItem(key), isDeep);
      },
      setItem: function(key, value, isDeep) {
        /// <summary>
        ///  指定されたキーで、値をローカルストレージに保存します。
        ///  オブジェクトは、JSON.stringify()で文字列に変換して保存します。 保存可能な型は、以下のとおりです。
        ///  
        ///  String(文字列)
        ///  Number(数値)
        ///  Boolean(真偽値)
        ///  Array(配列)
        ///  Object(連想配列・JSON形式)
        ///  Date(日付)
        ///  RegExp(正規表現)
        ///  Function(関数)
        ///  undefined
        ///  null
        ///  NaN
        ///  Infinity
        ///  -Infinity
        ///  
        /// </summary>
        /// <param  name = "key" type = "String" >
        ///  キー
        /// </param>
        /// <param  name = "value" type = "Any" >
        ///  値
        /// </param>
        /// <param  name = "isDeep" type = "Boolean" >
        ///  オブジェクト内のオブジェクトも型判定するか(true: する(未指定の場合はこのモードで実行される) false:
        ///  しない)
        /// </param>
        if (!this.isSupported) {
          return;
        }
        localStorage.setItem(key, h5.u.obj.serialize(value, isDeep));
      },
      removeItem: function(key) {
        /// <summary>
        ///  指定されたキーに紐付く値を、ローカルストレージから削除します。
        /// </summary>
        /// <param  name = "key" type = "String" >
        ///  キー
        /// </param>
        if (!this.isSupported) {
          return;
        }
        localStorage.removeItem(key);
      },
      clear: function() {
        /// <summary>
        ///  ローカルストレージに保存されている全てのキーとそれに紐付く値を全て削除します。
        /// </summary>
        if (!this.isSupported) {
          return;
        }
        localStorage.clear();
      },
      each: function(callback) {
        /// <summary>
        ///  現在ローカルストレージに保存されているオブジェクト数分、キーと値をペアで取得します。
        /// </summary>
        /// <param  name = "callback" type = "Function" >
        ///  インデックス,
        ///  キー, 値 を引数に持つコールバック関数
        /// </param>
        if (!this.isSupported) {
          return;
        }
        for (var i = 0, len = localStorage.length; i < len; i++) {
          var k = localStorage.key(i);
          callback(i, k, this.getItem(k));
        }
      }
    },
    session: {
      isSupported: !!window.sessionStorage,
      length: function() {
        /// <summary>
        ///  セッションストレージに保存されている、キーと値のペアの数を取得します。
        /// </summary>
        /// <returns  type = "Number" >
        ///  キーとペアの数
        /// </returns>
      /// <summary>
      ///  セッションストレージ
      /// </summary>
        if (!this.isSupported) {
          return;
        }
        return sessionStorage.length;
      },
      key: function(index) {
        /// <summary>
        ///  指定されたインデックスにあるキーを、セッションストレージから取得します。
        /// </summary>
        /// <param  name = "index" type = "Number" >
        ///  インデックス
        /// </param>
        /// <returns  type = "String" >
        ///  キー
        /// </returns>
        if (!this.isSupported) {
          return;
        }
        return sessionStorage.key(index);
      },
      getItem: function(key, isDeep) {
        /// <summary>
        ///  指定されたキーに紐付く値を、セッションストレージから取得します。
        ///  このメソッドはストレージから値を取得する際、JSON.parse()で変換を行います。
        ///  変換に成功した場合そのオブジェクトを返却し、失敗した場合もしくはオブジェクトでない場合は、文字列をそのまま返却します。
        /// </summary>
        /// <param  name = "key" type = "String" >
        ///  キー
        /// </param>
        /// <param  name = "isDeep" type = "Boolean" >
        ///  オブジェクト内のオブジェクトも型判定するか(true: する(未指定の場合はこのモードで実行される) false:
        ///  しない)
        /// </param>
        /// <returns  type = "Any" >
        ///  キーに紐付く値
        /// </returns>
        if (!this.isSupported) {
          return;
        }
        return h5.u.obj.deserialize(sessionStorage.getItem(key), isDeep);
      },
      setItem: function(key, value, isDeep) {
        /// <summary>
        ///  指定されたキーで、値をローカルストレージに保存します。
        ///  オブジェクトは、JSON.stringify()で文字列に変換して保存します。 保存可能な型は、以下のとおりです。
        ///  
        ///  String(文字列)
        ///  Number(数値)
        ///  Boolean(真偽値)
        ///  Array(配列)
        ///  Object(連想配列・JSON形式)
        ///  Date(日付)
        ///  RegExp(正規表現)
        ///  Function(関数)
        ///  undefined
        ///  null
        ///  NaN
        ///  Infinity
        ///  -Infinity
        ///  
        /// </summary>
        /// <param  name = "key" type = "String" >
        ///  キー
        /// </param>
        /// <param  name = "value" type = "Any" >
        ///  値
        /// </param>
        /// <param  name = "isDeep" type = "Boolean" >
        ///  オブジェクト内のオブジェクトも型判定するか(true: する(未指定の場合はこのモードで実行される) false:
        ///  しない)
        /// </param>
        if (!this.isSupported) {
          return;
        }
        sessionStorage.setItem(key, h5.u.obj.serialize(value, isDeep));
      },
      removeItem: function(key) {
        /// <summary>
        ///  指定されたキーに紐付く値を、セッションストレージから削除します。
        /// </summary>
        /// <param  name = "key" type = "String" >
        ///  キー
        /// </param>
        if (!this.isSupported) {
          return;
        }
        sessionStorage.removeItem(key);
      },
      clear: function() {
        /// <summary>
        ///  セッションストレージに保存されている全てのキーとそれに紐付く値を全て削除します。
        /// </summary>
        if (!this.isSupported) {
          return;
        }
        sessionStorage.clear();
      },
      each: function(callback) {
        /// <summary>
        ///  セッションストレージに保存されているオブジェクト数分、キーと値をペアで取得します。
        /// </summary>
        /// <param  name = "callback" type = "Function" >
        ///  インデックス,
        ///  キー, 値 を引数に持つコールバック関数
        /// </param>
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
(function(window, $) {
  /// <summary>
  ///  h5initイベントのトリガ.
  /// </summary>
  /// <param  name = "jQuery" type = "Object" >
  /// </param>
  // h5initイベントをトリガ.
  $(window.document).trigger('h5init');
})(window, jQuery);
