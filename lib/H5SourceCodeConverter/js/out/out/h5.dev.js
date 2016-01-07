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
 * hifive
 *   version 1.0.1
 *   build at 2012/05/09 13:47:31.491 (+0900)
 *   (util,controller,view,ui,api.geo,api.sqldb,api.storage)
 */
(function($) {
  // =========================================================================
  //
  // Prelude
  //
  // =========================================================================
  var savedH5 = undefined;
  //h5存在チェック
  if (window.h5) {
    if (window.h5.env && (window.h5.env.version === '1.0.1')) {
      // 既にロード済みのhifiveと同じバージョンをロードしようとした場合は何もしない
      return;
    }
    //coexistのために既存のh5を退避
    savedH5 = window.h5;
  }
  // h5空間を新規に作成。クロージャでくるんでいるので
  // 以降の各モジュールが見るh5はここで定義された(新しい)h5になる
  var h5 = {};
  // =============================
  // Expose to window
  // =============================
  window.h5 = h5;
  h5.coexist = function() {
    window.h5 = savedH5;
    return h5;
  };
  h5.env = {
    version: '1.0.1'
  };
  // =========================================================================
  //
  // Extenal Library
  //
  // =========================================================================
  // =========================================================================
  //
  // Modules
  //
  // =========================================================================
  /* h5scopedglobals */
  // =========================================================================
  //
  // Scoped Globals
  //
  // =========================================================================
  // =============================
  // Misc Variables
  // =============================
  var errorCodeToMessageMap = {};
  // =============================
  // Misc Functions
  // =============================
  function throwFwError(code, msgParam, detail) {
    /// <summary>
    ///  フレームワークエラーを発生させます。
    /// </summary>
    /// <param  name = "エラーコード" type = "Number" >
    ///  code
    ///   
    /// </param>
    /// <param  name = "フォーマットパラメータ" type = "Any[]" >
    ///  msgParam  
    /// </param>
    /// <param  name = "追加のデータ(内容はAPIごとに異なる)" type = "Any" >
    ///  detail  
    /// </param>
    var e = new Error();
    if (code) {
      e.code = code;      // TODO codeは必須、ない場合は…throw Error
    }
    var msg = errorCodeToMessageMap[code];
    if (msg) {
      var args = [msg].concat(msgParam);
      e.message = h5.u.str.format.apply(null, args);
    }
    if (detail) {
      e.detail = detail;
    }
    throw e;
  }
  function addFwErrorCodeMap(mapObj) {
    /// <summary>
    ///  エラーコードとエラーメッセージのマップを追加します。
    /// </summary>
    /// <param  type = "Object" >
    ///  mapObj
    ///   (エラーコード): (フォーマット文字列) }という構造のオブジェクト
    /// </param>
    for (code in mapObj) {
      if (mapObj.hasOwnProperty(code)) {
        errorCodeToMessageMap[code] = mapObj[code];
      }
    }
  }
  function createRejectReason(code, msgParam, detail) {
    /// <summary>
    ///  非同期APIのReject時の理由オブジェクトを作成します。
    /// </summary>
    /// <param  name = "エラーコード" type = "Number" >
    ///  code
    ///   
    /// </param>
    /// <param  name = "フォーマットパラメータ" type = "Any[]" >
    ///  msgParam  
    /// </param>
    /// <param  name = "追加のデータ(内容はAPIごとに異なる)" type = "Any" >
    ///  detail  
    /// </param>
    /// <returns  type = "Object" >
    ///  理由オブジェクト
    /// </returns>
    var msg = null;
    var f = errorCodeToMessageMap[code];
    if (f) {
      var args = [f].concat(msgParam);
      msg = h5.u.str.format.apply(null, args);
    }
    return {
      code: code,
      message: msg,
      detail: detail
    };
  }
  function wrapInArray(value) {
    /// <summary>
    ///  引数を配列化します。既に配列だった場合はそれをそのまま返し、
    ///  配列以外だった場合は配列にして返します。 ただし、nullまたはundefinedの場合はそのまま返します。
    /// </summary>
    /// <param  >
    ///  value
    ///  値
    /// </param>
    /// <returns  >
    ///  配列化された値、ただし引数がnullまたはundefinedの場合はそのまま
    /// </returns>
    if (!value) {
      return value;
    }
    return $.isArray(value) ? value : [value];
  }
  function toAbsoluteUrl(relativePath) {
    /// <summary>
    ///  相対URLを絶対URLに変換します。
    /// </summary>
    /// <param  name = "relativePath" type = "String" >
    ///  相対URL
    /// </param>
    /// <returns  type = "String" >
    ///  絶対パス
    /// </returns>
    var e = document.createElement('span');
    e.innerHTML = '<a href="' + relativePath + '" />';
    return e.firstChild.href;
  }
  // =============================
  // ロガー・アスペクトで使用する共通処理
  // =============================
  function escapeRegex(str) {
    /// <summary>
    ///  文字列の正規表現記号をエスケープします。
    /// </summary>
    /// <param  name = "str" type = "String" >
    ///  文字列
    /// </param>
    /// <returns  type = "String" >
    ///  エスケープ済文字列
    /// </returns>
    return str.replace(/\W/g, '\\$&');
  }
    function getRegex(target) {
    /// <summary>
    ///  引数がStringの場合、RegExpオブジェクトにして返します。
    ///  引数がRegExpオブジェクトの場合はそのまま返します。
    /// </summary>
    /// <param  name = "target" type = "String|RegExp" >
    ///  値
    /// </param>
    /// <returns  type = "RegExp" >
    ///  オブジェクト
    /// </returns>
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
  }
    /* ------ h5.u ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    var TYPE_OF_UNDEFINED = 'undefined';
    var CURRENT_SEREALIZER_VERSION = '1';
    var ERR_CODE_NAMESPACE_INVALID = 11000;
    var ERR_CODE_NAMESPACE_EXIST = 11001;
    var ERR_CODE_SERIALIZE_FUNCTION = 11002;
    var ERR_CODE_SERIALIZE_VERSION = 11003;
    var ERR_CODE_DESERIALIZE = 11004;
    var ERR_CODE_REFERENCE_CYCLE = 11005;
    var ERR_CODE_INVALID_VALUE = 11006;
    var errMsgMap = {};
    errMsgMap[ERR_CODE_NAMESPACE_INVALID] = '{0} 第一引数には空文字で無い文字列を指定して下さい。';
    errMsgMap[ERR_CODE_NAMESPACE_EXIST] = '名前空間"{0}"には、プロパティ"{1}"が既に存在します。';
    errMsgMap[ERR_CODE_SERIALIZE_FUNCTION] = 'Function型のオブジェクトは変換できません。';
    errMsgMap[ERR_CODE_SERIALIZE_VERSION] = 'シリアライザのバージョンが違います。シリアライズされたバージョン：{0} 現行のバージョン：{1}';
    errMsgMap[ERR_CODE_DESERIALIZE] = '型情報の判定に失敗したため、デシリアライズできませんでした。';
    errMsgMap[ERR_CODE_REFERENCE_CYCLE] = '循環参照が含まれています。';
    errMsgMap[ERR_CODE_INVALID_VALUE] = '不正な値が含まれるため、デシリアライズできませんでした。';
    // メッセージの登録
    addFwErrorCodeMap(errMsgMap);
    // =============================
    // Development Only
    // =============================
    /* del begin */
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    var addedJS = [];
    var htmlEscapeRules = {
      '&': '&amp;',
      '"': '&quot;',
      '<': '&lt;',
      '>': '&gt;',
      "'": '&apos;'
    };
    // =============================
    // Functions
    // =============================
    function typeToCode(typeStr) {
      /// <summary>
      ///  型情報の文字列をコードに変換します。
      /// </summary>
      /// <returns  type = "String" >
      ///  型を表すコード（１字）
      /// </returns>
      switch (typeStr) {
        case 'string':
          return 's';
        case 'number':
          return 'n';
        case 'boolean':
          return 'b';
        case 'String':
          return 'S';
        case 'Number':
          return 'N';
        case 'Boolean':
          return 'B';
        case 'infinity':
          return 'i';
        case '-infinity':
          return 'I';
        case 'nan':
          return 'x';
        case 'date':
          return 'd';
        case 'regexp':
          return 'r';
        case 'array':
          return 'a';
        case 'object':
          return 'o';
        case 'function':
          return 'f';
        case 'null':
          return 'l';
        case TYPE_OF_UNDEFINED:
          return 'u';
        case 'undefElem':
          return '_';
        case 'objElem':
          return '@';
      }
    }
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    var ns = function(namespace) {
      /// <summary>
      ///  ドット区切りで名前空間オブジェクトを生成します。
      ///  （h5.u.obj.ns(&apos;com.htmlhifive&apos;)と呼ぶと、window.com.htmlhifiveとオブジェクトを生成します。）
      ///  すでにオブジェクトが存在した場合は、それをそのまま使用します。 引数にString以外が渡された場合はエラーとします。
      /// </summary>
      /// <param  name = "namespace" type = "String" >
      ///  名前空間
      /// </param>
      /// <returns  type = "Object" >
      ///  作成した名前空間オブジェクト
      /// </returns>
      if (!namespace) {
        throwFwError(ERR_CODE_NAMESPACE_INVALID, 'h5.u.obj.ns()');
      }
      if ($.type(namespace) !== 'string') {
        throwFwError(ERR_CODE_NAMESPACE_INVALID, 'h5.u.obj.ns()');
      }
      var nsArray = namespace.split('.');
      var parentObj = window;
      for (var i = 0, len = nsArray.length; i < len; i++) {
        if (parentObj[nsArray[i]] === undefined) {
          parentObj[nsArray[i]] = {};
        }
        parentObj = parentObj[nsArray[i]];
      }
      // ループが終了しているので、parentObjは一番末尾のオブジェクトを指している
      return parentObj;
    };
    var expose = function(namespace, object) {
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
            throwFwError(ERR_CODE_NAMESPACE_EXIST, namespace, prop);
          }
          nsObj[prop] = object[prop];
        }
      }
    };
    var loadScript = function(path, opt) {
      /// <summary>
      ///  指定されたスクリプトをロードします。
      /// </summary>
      /// <param  name = "path" type = "String|String[]" >
      ///  ソースパス
      /// </param>
      /// <param  name = "[opt]" type = "Object" >
      ///  オプション
      /// </param>
      /// <param  name = "[opt.async]" type = "Boolean" >
      ///  非同期で読み込むかどうかを指定します。デフォルトはfalse(同期)です。
      ///  trueの場合は、戻り値としてPromiseオブジェクトを返します。
      /// </param>
      /// <param  name = "[opt.force]" type = "Boolean" >
      ///  既に読み込み済みのスクリプトを再度読み込むかどうかを指定します。
      ///  読み込み済みかどうかの判定は相対パスではなく、絶対パスで行います。デフォルトはfalse(読み込まない)です。
      /// </param>
      /// <param  name = "[opt.parallel]" type = "Boolean" >
      ///  非同期で読み込む場合にパラレルに読み込むかどうかを指定します。
      ///  trueの場合は、指定した順番を考慮せずに読み込みます。デフォルトはfalse(シーケンシャルに読み込む)です。
      /// </param>
      /// <returns  type = "Promise" >
      ///  Promiseオブジェクト。第2引数optのasyncプロパティがtrueである場合のみ戻り値としてPromiseオブジェクトを返します。
      /// </returns>
      var resource = wrapInArray(path);
      if (resource.length === 0) {
        return;
      }
      var force = opt && opt.force === true;
      var srcLen = resource.length;
      // 同期読み込みの場合
      if (!opt || opt.async !== true) {
        var $head = $('head');
        for (var i = 0; i < srcLen; i++) {
          var s = toAbsoluteUrl(resource[i]);
          if (force || $.inArray(s, addedJS) === -1) {
            $head.append('<script type="text/javascript" src="' + s + '"></script>');
            addedJS.push(s);
          }
        }
        // 同期の場合は何も返さない。
        return;
      }
      // 非同期読み込みの場合
      var parallel = opt && opt.parallel === true;
      var promises = [];
      var dfd = h5.async.deferred();
      var head = document.head || document.getElementsByTagName('head')[0];
      var load = function(index) {
        var count = index;
        if (srcLen <= count) {
          // 読み込み終了
          $.when.apply($, promises).done(function() {
            dfd.resolve();
          });
          return;
        }
        var s = toAbsoluteUrl(resource[count]);
        if (!force && $.inArray(s, addedJS) !== -1) {
          load(++count);
          return;
        }
        var script = document.createElement('script');
        script.type = 'text/javascript';
        var scriptDfd = parallel ? h5.async.deferred() : null;
        if (window.ActiveXObject) {
          script.onreadystatechange = function() {
            if (script.readyState == 'complete' || script.readyState == 'loaded') {
              script.onreadystatechange = null;
              if (scriptDfd) {
                scriptDfd.resolve();
              } else {
                load(++count);
              }
            }
          };
        } else {
          script.onerror = function() {
            script.onerror = null;
            if (scriptDfd) {
              scriptDfd.resolve();
            } else {
              load(++count);
            }
          };
          script.onload = function() {
            script.onload = null;
            if (scriptDfd) {
              scriptDfd.resolve();
            } else {
              load(++count);
            }
          };
        }
        script.src = s;
        head.appendChild(script);
        addedJS.push(s);
        if (scriptDfd) {
          promises.push(scriptDfd.promise());
          load(++count);
        }
      };
      setTimeout(function() {
        load(0);
      }, 0);
      return dfd.promise();
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
      if (str == null) {
        return '';
      }
      var args = arguments;
      return str.replace(/\{(\d+)\}/g, function(m, c) {
        var rep = args[parseInt(c, 10) + 1];
        if (typeof rep === TYPE_OF_UNDEFINED) {
          return TYPE_OF_UNDEFINED;
        }
        return rep;
      });
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
      return str.replace(/[&"'<>]/g, function(c) {
        return htmlEscapeRules[c];
      });
    };
    var serialize = function(value) {
      /// <summary>
      ///  オブジェクトを、型情報を付与した文字列に変換します。
      ///  
      ///  このメソッドが判定可能な型は、以下のとおりです。
      ///  
      ///  string(文字列)
      ///  number(数値)
      ///  boolean(真偽値)
      ///  String(文字列のラッパークラス型)
      ///  Number(数値のラッパークラス型)
      ///  Boolean(真偽値のラッパークラス型)
      ///  array(配列)
      ///  object(プレーンオブジェクト [new Object() または  のリテラルで作られたオブジェクト])
      ///  Date(日付)
      ///  RegExp(正規表現)
      ///  undefined
      ///  null
      ///  NaN
      ///  Infinity
      ///  -Infinity
      ///  
      ///  
      ///  このメソッドで文字列化したオブジェクトはdeseriarizeメソッドで元に戻すことができます。
      ///  
      ///  
      ///  object型はプレーンオブジェクトとしてシリアライズします。 渡されたオブジェクトがプレーンオブジェクトで無い場合、そのprototypeやconstructorは無視します。
      ///  
      ///  
      ///  array型は連想配列として保持されているプロパティもシリアライズします。
      ///  
      ///  
      ///  循環参照を含むarray型およびobject型はシリアライズできません。例外をスローします。
      ///  
      ///  
      ///  内部に同一インスタンスを持つarray型またはobject型は、別インスタンスとしてシリアライズします。以下のようなarray型オブジェクトaにおいて、a[0]とa[1]が同一インスタンスであるという情報は保存しません。
      ///  
      ///  a = [];
      ///  a[0] = a[1] = [];
      ///  
      ///  
      ///  注意
      ///  
      ///  function型のオブジェクトは変換できません。例外をスローします。
      ///  array型にfunction型のオブジェクトが存在する場合は、undefinedとしてシリアライズします。object型または連想配列にfunction型のオブジェクトが存在する場合は、無視します。
      ///  
      /// </summary>
      /// <param  name = "value" type = "Object" >
      ///  オブジェクト
      /// </param>
      /// <returns  type = "String" >
      ///  型情報を付与した文字列
      /// </returns>
      if ($.isFunction(value)) {
        throwFwError(ERR_CODE_SERIALIZE_FUNCTION);
      }
      // 循環参照チェック用配列
      var objStack = [];
      function existStack(obj) {
        for (var i = 0, len = objStack.length; i < len; i++) {
          if (obj === objStack[i]) {
            return true;
          }
        }
        return false;
      }
            function popStack(obj) {
        for (var i = 0, len = objStack.length; i < len; i++) {
          if (obj === objStack[i]) {
            objStack.splice(i, 1);
          }
        }
      }
            function func(val) {
        var ret = val;
        var type = $.type(val);
        // プリミティブラッパークラスを判別する
        if (typeof val === 'object') {
          if (val instanceof String) {
            type = 'String';
          } else if (val instanceof Number) {
            type = 'Number';
          } else if (val instanceof Boolean) {
            type = 'Boolean';
          }
        }
        // オブジェクトや配列の場合、JSON.stringify()を使って書けるが、json2.jsのJSON.stringify()を使った場合に不具合があるため自分で実装した。
        switch (type) {
          case 'String':
          case 'string':
            ret = typeToCode(type) + ret;
            break;
          case 'Boolean':
            ret = ret.valueOf();
          case 'boolean':
            ret = typeToCode(type) + ((ret) ? 1 : 0);
            break;
          case 'Number':
            ret = ret.valueOf();
            if (($.isNaN && $.isNaN(val)) || ($.isNumeric && !$.isNumeric(val))) {
              if (val.valueOf() === Infinity) {
                ret = typeToCode('infinity');
              } else if (val.valueOf() === -Infinity) {
                ret = typeToCode('-infinity');
              } else {
                ret = typeToCode('nan');
              }
            }
            ret = typeToCode(type) + ret;
            break;
          case 'number':
            if (($.isNaN && $.isNaN(val)) || ($.isNumeric && !$.isNumeric(val))) {
              if (val === Infinity) {
                ret = typeToCode('infinity');
              } else if (val === -Infinity) {
                ret = typeToCode('-infinity');
              } else {
                ret = typeToCode('nan');
              }
            } else {
              ret = typeToCode(type) + ret;
            }
            break;
          case 'regexp':
            ret = typeToCode(type) + ret.toString();
            break;
          case 'date':
            ret = typeToCode(type) + (+ret);
            break;
          case 'array':
            if (existStack(val)) {
              throwFwError(ERR_CODE_REFERENCE_CYCLE);
            }
            objStack.push(val);
            var indexStack = [];
            ret = typeToCode(type) + '[';
            for (var i = 0, len = val.length; i < len; i++) {
              indexStack[i.toString()] = true;
              var elm;
              if (!val.hasOwnProperty(i)) {
                elm = typeToCode('undefElem');
              } else if ($.type(val[i]) === 'function') {
                elm = typeToCode(TYPE_OF_UNDEFINED);
              } else {
                elm = (func(val[i])).replace(/\\/g, '\\\\').replace(/"/g, '\\"');
              }
              ret += '"' + elm + '"';
              if (i !== val.length - 1) {
                ret += ',';
              }
            }
            var hash = '';
            for (var key in val) {
              if (indexStack[key]) {
                continue;
              }
              if ($.type(val[key]) !== 'function') {
                hash += '"' + key + '":"' + (func(val[key])).replace(/\\/g, '\\\\').replace(/"/g, '\\"') + '",';
              }
            }
            if (hash) {
              ret += ((val.length) ? ',' : '') + '"@{' + hash.replace(/\\/g, '\\\\').replace(/"/g, '\\"');
              ret = ret.replace(/,$/, '');
              ret += '}"';
            }
            ret += ']';
            popStack(val);
            break;
          case 'object':
            if (existStack(val)) {
              throwFwError('循環参照が含まれています。');
            }
            objStack.push(val);
            ret = typeToCode(type) + '{';
            for (var key in val) {
              if (val.hasOwnProperty(key)) {
                if ($.type(val[key]) === 'function') {
                  continue;
                }
                ret += '"' + key + '":"' + (func(val[key])).replace(/\\/g, '\\\\').replace(/"/g, '\\"') + '",';
              }
            }
            ret = ret.replace(/,$/, '');
            ret += '}';
            popStack(val);
            break;
          case 'null':
          case TYPE_OF_UNDEFINED:
            ret = typeToCode(type);
            break;
        }
        return ret;
      }
            return CURRENT_SEREALIZER_VERSION + '|' + func(value);
    };
    var deserialize = function(value) {
      /// <summary>
      ///  型情報が付与された文字列をオブジェクトを復元します。
      /// </summary>
      /// <param  name = "value" type = "String" >
      ///  型情報が付与された文字列
      /// </param>
      /// <returns  type = "Any" >
      ///  復元されたオブジェクト
      /// </returns>
      if (typeof value !== 'string') {
        return value;
      }
      value.match(/^(.)\|(.*)/);
      var version = RegExp.$1;
      if (version !== CURRENT_SEREALIZER_VERSION) {
        throwFwError(ERR_CODE_SERIALIZE_VERSION, [version, CURRENT_SEREALIZER_VERSION]);
      }
      var ret = RegExp.$2;
      function func(val) {
        function codeToType(typeStr) {
          /// <summary>
          ///  型情報のコードを文字列に変換します。
          /// </summary>
          /// <returns  type = "String" >
          ///  型を表す文字列
          /// </returns>
          switch (typeStr) {
            case 's':
              return 'string';
            case 'n':
              return 'number';
            case 'b':
              return 'boolean';
            case 'S':
              return 'String';
            case 'N':
              return 'Number';
            case 'B':
              return 'Boolean';
            case 'i':
              return 'infinity';
            case 'I':
              return '-infinity';
            case 'x':
              return 'nan';
            case 'd':
              return 'date';
            case 'r':
              return 'regexp';
            case 'a':
              return 'array';
            case 'o':
              return 'object';
            case 'f':
              return 'function';
            case 'l':
              return 'null';
            case 'u':
              return TYPE_OF_UNDEFINED;
            case '_':
              return 'undefElem';
            case '@':
              return 'objElem';
          }
        }
        try {
          val.match(/^(.)(.*)/);
          type = RegExp.$1;
          ret = (RegExp.$2) ? RegExp.$2 : '';
          if (type !== undefined && type !== '') {
            var value = ret;            // ret.substring(repPos, ret.length);
            switch (codeToType(type)) {
              case 'String':
                ret = new String(ret);
                break;
              case 'string':
                break;
              case 'Boolean':
                if (ret === '0' || ret === '1') {
                  ret = new Boolean(ret === '1');
                } else {
                  throwFwError(ERR_CODE_INVALID_VALUE);
                }
                break;
              case 'boolean':
                if (ret === '0' || ret === '1') {
                  ret = ret === '1';
                } else {
                  throwFwError(ERR_CODE_INVALID_VALUE);
                }
                break;
              case 'Number':
                if (codeToType(ret) === 'infinity') {
                  ret = new Number(Infinity);
                } else if (codeToType(ret) === '-infinity') {
                  ret = new Number(-Infinity);
                } else if (codeToType(ret) === 'nan') {
                  ret = new Number(NaN);
                } else {
                  ret = new Number(ret);
                }
                break;
              case 'number':
                ret = parseFloat(ret);
                break;
              case 'array':
                var obj = $.parseJSON(ret);
                for (var i = 0; i < obj.length; i++) {
                  if (obj[i].match(new RegExp('^' + typeToCode('undefElem')))) {
                    delete obj[i];
                    continue;
                  }
                  if (obj[i].match(new RegExp('^' + typeToCode('objElem')))) {
                    var extendObj = func(obj[i].replace(new RegExp('^' + typeToCode('objElem')), typeToCode('object')));
                    var tempObj = [];
                    for (var i = 0, l = obj.length - 1; i < l; i++) {
                      tempObj[i] = obj[i];
                    }
                    obj = tempObj;
                    for (var key in extendObj) {
                      obj[key] = extendObj[key];
                    }
                  } else {
                    obj[i] = func(obj[i]);
                  }
                }
                ret = obj;
                break;
              case 'object':
                var obj = $.parseJSON(ret);
                for (var key in obj) {
                  obj[key] = func(obj[key]);
                }
                ret = obj;
                break;
              case 'date':
                ret = new Date(parseInt(value, 10));
                break;
              case 'regexp':
                value.match(/^\/(.*)\/(.*)$/);
                var regStr = RegExp.$1;
                var flg = RegExp.$2;
                ret = new RegExp(regStr, flg);
                break;
              case 'null':
              case 'function':                // Function型はnullにする
                ret = null;
                break;
              case TYPE_OF_UNDEFINED:
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
            }
          }
        }
        catch (e) {
          // 型情報の判定(復元)に失敗した場合、値をそのまま返すので何もしない
          // throwFwError(ERR_CODE_DESERIALIZE);
        }
        return ret;
      }
            return func(ret);
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
      if (!obj || !obj.jquery) {
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
    var getByPath = function(namespace) {
      /// <summary>
      ///  指定された名前空間に存在するオブジェクトを取得します。
      /// </summary>
      /// <param  name = "名前空間" type = "String" >
      /// </param>
      /// <returns  type = "Any" >
      ///  その名前空間に存在するオブジェクト
      /// </returns>
      if (typeof namespace !== 'string') {
        throwFwError(ERR_CODE_NAMESPACE_INVALID, 'h5.u.obj.getByPath()');
      }
      var names = namespace.split('.');
      if (names[0] === 'window') {
        names.unshift();
      }
      var ret = window;
      for (var i = 0, len = names.length; i < len; i++) {
        ret = ret[names[i]];
        if (ret == null) {          // nullまたはundefinedだったら辿らない
          break;
        }
      }
      return ret;
    };
    var createInterceptor = function(pre, post) {
      /// <summary>
      ///  インターセプタを作成します。
      /// </summary>
      /// <param  name = "pre" type = "Function" >
      ///  インターセプト先関数の実行前に呼ばれる関数です。
      /// </param>
      /// <param  name = "post" type = "Function" >
      ///  インターセプト先関数の実行後に呼ばれる関数です。
      ///  
      ///  pre(), post()には引数としてinvocationとdata(preからpostへ値を渡すための入れ物オブジェクト)が渡されます。
      ///  post()は、呼び出した関数の戻り値がPromiseオブジェクトかどうかをチェックし、Promiseオブジェクトの場合は対象のDeferredが完了した後に呼ばれます。
      ///  pre()の中でinvocation.proceed()が呼ばれなかった場合、post()は呼ばれません。
      ///  invocation.resultプロパティに呼び出した関数の戻り値が格納されます。
      ///  pre()が指定されていない場合、invocation.proceed()を実行した後にpost()を呼びます。
      ///  
      ///  コード例(h5.core.interceptor.lapInterceptor)を以下に示します。
      ///  
      ///  var lapInterceptor = h5.u.createInterceptor(function(invocation, data)
      ///  // 開始時間をdataオブジェクトに格納
      ///  data.start = new Date();
      ///  // invocationを実行
      ///  return invocation.proceed();
      ///  }, function(invocation, data)
      ///  // 終了時間を取得
      ///  var end = new Date();
      ///  // ログ出力
      ///  this.log.info(&apos;{0} &amp;quot;{1}&amp;quot;: {2}ms&apos;, this.__name, invocation.funcName, (end data.start));
      ///  });
      ///  
      /// </param>
      /// <returns  type = "Function" >
      ///  インターセプタ
      /// </returns>
      return function(invocation) {
        var data = {};
        var ret = pre ? pre.call(this, invocation, data) : invocation.proceed();
        invocation.result = ret;
        if (!post) {
          return ret;
        }
        if (h5.async.isPromise(ret)) {
          var that = this;
          ret.always(function() {
            post.call(that, invocation, data);
          });
          return ret;
        }
        post.call(this, invocation, data);
        return ret;
      };
    };
    // =============================
    // Expose to window
    // =============================
    expose('h5.u', {
      loadScript: loadScript,
      createInterceptor: createInterceptor
    });
    expose('h5.u.str', {
      startsWith: startsWith,
      endsWith: endsWith,
      format: format,
      escapeHtml: escapeHtml
    });
    expose('h5.u.obj', {
      expose: expose,
      ns: ns,
      serialize: serialize,
      deserialize: deserialize,
      isJQueryObject: isJQueryObject,
      argsToArray: argsToArray,
      getByPath: getByPath
    });
  })();
  /* ------ h5.log ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    var ERR_CODE_LOG_TARGET = 10000;
    var ERR_CODE_OUT_CATEGORY_IS_NONE = 10001;
    var ERR_CODE_CATEGORY_NAMED_MULTIPLE_TIMES = 10002;
    var ERR_CODE_LEVEL_INVALID = 10003;
    var ERR_CODE_LOG_TARGET_NAMED_MULTIPLE_TIMES = 10003;
    var ERR_CODE_LOG_TARGET_IS_NONE = 10004;
    var ERR_CODE_CATEGORY_INVALID = 10005;
    var errMsgMap = {};
    errMsgMap[ERR_CODE_LOG_TARGET] = 'ログターゲットのtypeには、オブジェクト、もしくは"console"のみ指定可能です。';
    errMsgMap[ERR_CODE_OUT_CATEGORY_IS_NONE] = 'out.categoryは必須項目です。';
    errMsgMap[ERR_CODE_CATEGORY_NAMED_MULTIPLE_TIMES] = 'category"{0}"が複数回指定されています。';
    errMsgMap[ERR_CODE_LEVEL_INVALID] = 'level"{0}"の指定は不正です。Number、もしくはtrace, info, debug, warn, errorを指定してください。';
    errMsgMap[ERR_CODE_LOG_TARGET_NAMED_MULTIPLE_TIMES] = 'ログターゲット"{0}"が複数回指定されています。';
    errMsgMap[ERR_CODE_LOG_TARGET_IS_NONE] = '"{0}"という名前のログターゲットはありません。';
    errMsgMap[ERR_CODE_CATEGORY_INVALID] = 'categoryは必須項目です。1文字以上の文字列を指定してください。';
    // メッセージの登録
    addFwErrorCodeMap(errMsgMap);
    // =============================
    // Development Only
    // =============================
    /* del begin */
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    var logLevel = {
      ERROR: 50,
      WARN: 40,
      INFO: 30,
      DEBUG: 20,
      TRACE: 10,
      ALL: 0,
      NONE: -1
    };
    // コンパイル済ログ設定
    var compiledLogSettings = null;
    // =============================
    // Functions
    // =============================
    function levelToString(level) {
      /// <summary>
      /// </summary>
      if (level === logLevel.ERROR) {
        return 'ERROR';
      } else if (level === logLevel.WARN) {
        return 'WARN';
      } else if (level === logLevel.INFO) {
        return 'INFO';
      } else if (level === logLevel.DEBUG) {
        return 'DEBUG';
      } else if (level === logLevel.TRACE) {
        return 'TRACE';
      } else if (level === logLevel.ALL) {
        return 'ALL';
      } else if (level === logLevel.NONE) {
        return 'NONE';
      } else {
        return 'OTHER';
      }
    }
    function stringToLevel(str) {
      /// <summary>
      /// </summary>
      if (str.match(/^error$/i)) {
        return logLevel.ERROR;
      } else if (str.match(/^warn$/i)) {
        return logLevel.WARN;
      } else if (str.match(/^info$/i)) {
        return logLevel.INFO;
      } else if (str.match(/^debug$/i)) {
        return logLevel.DEBUG;
      } else if (str.match(/^trace$/i)) {
        return logLevel.TRACE;
      } else if (str.match(/^all$/i)) {
        return logLevel.ALL;
      } else if (str.match(/^none$/i)) {
        return logLevel.NONE;
      } else {
        return null;
      }
    }
        function getTraceResult(recentTraces, detailTraces) {
      /// <summary>
      /// </summary>
      var COUNT = 3;
      var result = {};
      if ($.isArray(recentTraces)) {
        var recent = recentTraces.slice(0, COUNT).join(' <- ');
        if (recentTraces.slice(COUNT).length > 0) {
          recent += ' ...';
        }
        result.recent = recent;
        result.all = detailTraces.join('\n');
      } else {
        result.recent = recentTraces;
        result.all = detailTraces;
      }
      return result;
    }
    function getFunctionName(fn) {
      /// <summary>
      ///  指定されたFunction型のオブジェクトから、名前を取得します。
      /// </summary>
      /// <param  name = "fn" type = "Function" >
      /// </param>
      var ret = '';
      if (!fn.name) {
        var regExp = /^\s*function\s*([\w\-\$]+)?\s*\(/i;
        regExp.test(fn.toString());
        ret = RegExp.$1;
      } else {
        ret = fn.name;
      }
      return ret;
    }
    function parseArgs(args) {
      /// <summary>
      /// </summary>
      var argArray = h5.u.obj.argsToArray(args);
      var result = [];
      for (var i = 0, len = argArray.length; i < len; i++) {
        result.push($.type(argArray[i]));
      }
      return result.join(', ');
    }
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    function ConsoleLogTarget() {
      /// <summary>
      ///  コンソールにログを出力するログターゲット
      /// </summary>
      // 空コンストラクタ
    }
    ConsoleLogTarget.prototype = {
      init: function(param) {
        /// <summary>
        ///  コンソールログターゲットの初期化を行います。
        /// </summary>
        /// <param  name = "param" type = "Object" >
        ///  初期化パラメータ
        /// </param>
        // 今は特定のパラメータはない
      },
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
          logMsg += '  [' + logObj.stackTrace.recent + ']';
        }
        if (logObj.logger.enableStackTrace && console.groupCollapsed) {
          console.groupCollapsed(logMsg);
        } else {
          this._consoleOut(logObj.level, logMsg);
        }
        if (logObj.logger.enableStackTrace) {
          // if (console.trace) {
          // console.trace();
          // } else {
          this._consoleOut(logObj.level, logObj.stackTrace.all);
                  // }
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
    var configure = function() {
      /// <summary>
      ///  h5.settings.logにあるログ設定を適用します。
      /// </summary>
      // defaultOutのデフォルト
      var defaultOut = {
        level: 'NONE',
        targets: null
      };
      /* del begin */
      // h5.dev.jsではデフォルトのdefaultOutをログ出力するようにしておく。
      defaultOut = {
        level: 'debug',
        targets: 'console'
      };
      /* del end */
      function compileLogTarget(targets) {
        for (var prop in targets) {
          var obj = targets[prop];
          var type = $.type(obj.type);
          // 今は"remote"でもエラーとなる
          if (type === 'object' || (type === 'string' && obj.type !== 'console')) {
            throwFwError(ERR_CODE_LOG_TARGET);
          }
          var compiledTarget = null;
          if (obj.type === 'console') {
            compiledTarget = new ConsoleLogTarget();
          } else {
            // typeがオブジェクトの場合
            var clone = $.extend(true, {}, obj.type);
            compiledTarget = clone;
          }
          if (compiledTarget.init) {
            compiledTarget.init(obj);
          }
          obj.compiledTarget = compiledTarget;
        }
        targets.console = {
          type: 'console',
          compiledTarget: new ConsoleLogTarget()
        };
      }
      var categoryCache = [];
      function compileOutput(_logTarget, out, _dOut) {
        var isDefault = _dOut == null;
        if (!isDefault) {
          var category = $.trim(out.category);
          if (category.length === 0 && !_dOut) {
            throwFwError(ERR_CODE_OUT_CATEGORY_IS_NONE);
          }
          if ($.inArray(category, categoryCache) !== -1) {
            throwFwError(ERR_CODE_CATEGORY_NAMED_MULTIPLE_TIMES, out.category);
          }
          out.compiledCategory = getRegex(category);
        }
        var level = $.trim(out.level);
        if (level.length === 0) {
          level = isDefault ? defaultOut.level : _dOut.level;
        }
        var compiledLevel = $.type(level) === 'number' ? level : stringToLevel(level);
        if (compiledLevel == null) {
          throwFwError(ERR_CODE_LEVEL_INVALID, level);
        }
        out.compiledLevel = compiledLevel;
        var compiledTargets = [];
        var targets = out.targets;
        if (!isDefault && !targets) {
          compiledTargets = _dOut.compiledTargets;
        } else if (!isDefault || targets) {
          var targetNames = [];
          targets = wrapInArray(targets);
          for (var i = 0, len = targets.length; i < len; i++) {
            var targetName = targets[i];
            if (!targetName) {
              continue;
            }
            if ($.inArray(targetName, targetNames) !== -1) {
              throwFwError(ERR_CODE_LOG_TARGET_NAMED_MULTIPLE_TIMES, targetName);
            }
            var l = _logTarget[targetName];
            if (!l) {
              throwFwError(ERR_CODE_LOG_TARGET_IS_NONE, targetName);
            }
            targetNames.push(targetName);
            compiledTargets.push(l.compiledTarget);
          }
          if (!isDefault) {
            var defaultTargets = _dOut.targets;
            if (defaultTargets != null) {
              defaultTargets = wrapInArray(defaultTargets);
              for (var i = 0, len = defaultTargets.length; i < len; i++) {
                var targetName = defaultTargets[i];
                if ($.inArray(targetName, targetNames) === -1) {
                  compiledTargets.push(_dOut.compiledTargets[i]);
                  targetNames.push(targetName);
                }
              }
            }
          }
        }
        out.compiledTargets = compiledTargets;
      }
      compiledLogSettings = $.extend(true, {}, h5.settings.log ? h5.settings.log : {
        defaultOut: defaultOut
      });
      var logTarget = compiledLogSettings.target;
      if (!logTarget) {
        logTarget = {};
        compiledLogSettings.target = logTarget;
      }
      compileLogTarget(logTarget);
      var dOut = compiledLogSettings.defaultOut;
      if (!dOut) {
        dOut = defaultOut;
        compiledLogSettings.defaultOut = dOut;
      }
      compileOutput(logTarget, dOut);
      var outs = compiledLogSettings.out;
      if (outs) {
        outs = wrapInArray(outs);
        for (var i = 0, len = outs.length; i < len; i++) {
          compileOutput(logTarget, outs[i], dOut);
        }
      }
    };
    function Log(category) {
      /// <summary>
      ///  ログを生成するクラス
      /// </summary>
      // 0は大丈夫なので category == null で判断する
      if (category == null || $.trim(category).length === 0) {
        throwFwError(ERR_CODE_CATEGORY_INVALID);
      }
      this.category = $.trim(category);
    }
    Log.prototype = {
      enableStackTrace: false,
      maxStackSize: 10,
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
        ///  コンソールに出力する内容
        /// </param>
        this._log(logLevel.ERROR, arguments, this.error);
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
        ///  コンソールに出力する内容
        /// </param>
        this._log(logLevel.WARN, arguments, this.warn);
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
        ///  コンソールに出力する内容
        /// </param>
        this._log(logLevel.INFO, arguments, this.info);
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
        ///  コンソールに出力する内容
        /// </param>
        this._log(logLevel.DEBUG, arguments, this.debug);
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
        ///  コンソールに出力する内容
        /// </param>
        this._log(logLevel.TRACE, arguments, this.trace);
      },
      _traceFunctionName: function(fn) {
        /// <summary>
        ///  スタックトレース(関数呼び出し関係)を取得します。
        /// </summary>
        /// <param  name = "トレース対象の関数" type = "Function" >
        ///  fn
        ///   
        /// </param>
        /// <returns  type = "Object" >
        ///  スタックトレース
        ///  
        ///  all: 全てトレースした文字列,
        ///  recent: Logクラス/LogTargetクラスのメソッドは省いた最大3件トレースした文字列
        ///  &amp;quot;[func1_2 () &amp;lt;- func1_1 () &amp;lt;- func1 () ...]&amp;quot;
        ///  
        /// </returns>
        var e = new Error();
        var errMsg = e.stack || e.stacktrace;
        var result = {};
        var traces = [];
        if (errMsg) {
          // トレースされたログのうち、トレースの基点から3メソッド分(_traceFunction、_log、
          // debug|info|warn|error|trace)はログに出力しない。
          var DROP_TRACE_COUNT = 3;
          // Chrome, FireFox, Opera
          traces = errMsg.replace(/\r\n/, '\n').replace(/at\b|@|Error\b|\t|\[arguments not available\]/ig, '').replace(/(http|https|file):.+[0-9]/g, '').replace(/ +/g, ' ').split('\n');
          var ret = null;
          traces = $.map(traces, function(value) {
            if (value.length === 0) {
              ret = null;              // 不要なデータ(Chromeは配列の先頭, FireFoxは配列の末尾に存在する)
            } else if ($.trim(value) === '') {
              ret = '{anonymous}';              // ログとして出力されたが関数名が無い
            } else {
              ret = $.trim(value);
            }
            return ret;
          });
          result = getTraceResult(traces.slice(DROP_TRACE_COUNT, traces.length), traces.slice(0, this.maxStackSize));
        } else {
          // IE, Safari
          var currentCaller = fn.caller;
          var index = 0;
          if (!currentCaller) {
            getTraceResult('{unable to trace}', '{unable to trace}');
          } else {
            while (true) 
            {
                var argStr = parseArgs(currentCaller.arguments);
                var funcName = getFunctionName(currentCaller);
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
                  result = getTraceResult(traces, traces);
                  break;
                }
                currentCaller = currentCaller.caller;
                index++;
              }
          }
        }
        return result;
      },
      _log: function(level, args, func) {
        /// <summary>
        ///  ログ情報を保持するオブジェクトに以下の情報を付与し、コンソールまたはリモートサーバにログを出力します。
        ///  
        ///  時刻
        ///  ログの種別を表す文字列(ERROR, WARN, INFO, DEBUG, TRACE, OTHER)
        ///  
        /// </summary>
        /// <param  name = "level" type = "Number" >
        ///  ログレベル
        /// </param>
        /// <param  name = "args" type = "Arguments" >
        ///  引数
        /// </param>
        /// <param  name = "func" type = "Function" >
        ///  元々呼ばれた関数
        /// </param>
        var logObj = {
          level: level,
          args: h5.u.obj.argsToArray(args),
          stackTrace: this.enableStackTrace ? this._traceFunctionName(func) : ''
        };
        var outs = compiledLogSettings.out;
        var defaultOut = compiledLogSettings.defaultOut;
        var targetOut = null;
        if (outs) {
          outs = wrapInArray(outs);
          for (var i = 0, len = outs.length; i < len; i++) {
            var out = outs[i];
            if (!out.compiledCategory.test(this.category)) {
              continue;
            }
            targetOut = out;
            break;
          }
        }
        if (!targetOut) {
          targetOut = defaultOut;
        }
        var levelThreshold = targetOut.compiledLevel;
        var logTarget = targetOut.compiledTargets;
        if (level < levelThreshold || levelThreshold < 0) {
          return;
        }
        logObj.logger = this;
        logObj.date = new Date();
        logObj.levelString = this._levelToString(level);
        if (!logTarget || logTarget.length === 0) {
          return;
        }
        for (var i = 0, len = logTarget.length; i < len; i++) {
          logTarget[i].log(logObj);
        }
      },
      _levelToString: levelToString
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
      return new Log($.trim(category));
    };
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.log', {
      createLogger: createLogger,
      configure: configure
    });
  })();
  /* ------ (h5) ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    // TODO エラーコード定数等Minify版（製品利用版）でも必要なものはここに書く
    // =============================
    // Development Only
    // =============================
    // var fwLogger = h5.log.createLogger(); //TODO カテゴリ名(ファイル名から拡張子を除いたもの)を入れる
    /* del begin */
    // TODO Minify時にプリプロセッサで削除されるべきものはこの中に書く
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // TODO 高速化のために他で定義されている関数などを変数に入れておく場合はここに書く
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    // TODO モジュールレベルのプライベート変数はここに書く
    // =============================
    // Functions
    // =============================
    function compileAspects(aspects) {
      /// <summary>
      ///  すべてのアスペクト設定をコンパイルします。
      /// </summary>
      /// <param  name = "aspects" type = "Object|Object[]" >
      ///  アスペクト設定
      /// </param>
      var compile = function(aspect) {
        if (aspect.target) {
          aspect.compiledTarget = getRegex(aspect.target);
        }
        if (aspect.pointCut) {
          aspect.compiledPointCut = getRegex(aspect.pointCut);
        }
        return aspect;
      };
      h5.settings.aspects = $.map(wrapInArray(aspects), function(n) {
        return compile(n);
      });
    }
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    h5.u.obj.ns('h5.settings');
    h5.settings = {
      commonFailHandler: null,
      aspects: null,
      log: null
    };
    // h5preinitでglobalAspectsの設定をしている関係上、別ファイルではなく、ここに置いている。
    var lapInterceptor = h5.u.createInterceptor(function(invocation, data) {
      /// <summary>
      ///  実行時間の計測を行うインターセプタ。
      /// </summary>
      /// <param  name = "invocation" type = "Function" >
      ///  次に実行する関数
      /// </param>
      /// <returns  type = "Any" >
      ///  invocationの戻り値
      /// </returns>
      // 開始時間をdataオブジェクトに格納
      data.start = new Date();
      // invocationを実行
      return invocation.proceed();
    }, function(invocation, data) {
      // 終了時間を取得
      var end = new Date();
      // ログ出力
      this.log.info('{0} "{1}": {2}ms', this.__name, invocation.funcName, (end - data.start));
    });
    var logInterceptor = h5.u.createInterceptor(function(invocation) {
      /// <summary>
      ///  イベントコンテキストに格納されているものをコンソールに出力するインターセプタ。
      /// </summary>
      /// <param  name = "invocation" type = "Function" >
      ///  次に実行する関数
      /// </param>
      /// <returns  type = "Any" >
      ///  invocationの戻り値
      /// </returns>
      this.log.info('{0} "{1}"が開始されました。', this.__name, invocation.funcName);
      this.log.info(invocation.args);
      return invocation.proceed();
    }, function(invocation) {
      this.log.info('{0} "{1}"が終了しました。', this.__name, invocation.funcName);
    });
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
      return ret;
    };
    // ここで公開しないとh5preinit時にデフォルトインターセプタが定義されていないことになる
    h5.u.obj.expose('h5.core.interceptor', {
      lapInterceptor: lapInterceptor,
      logInterceptor: logInterceptor,
      errorInterceptor: errorInterceptor
    });
    // h5preinitイベントをトリガ.
    $(window.document).trigger('h5preinit');
    if (h5.settings.aspects) {
      compileAspects(h5.settings.aspects);
    }
    // ログ設定の適用
    h5.log.configure();
    // =============================
    // Expose to window
    // =============================
    /* del begin */
    // テストのために公開している。
    h5.u.obj.expose('h5.core', {
      _compileAspects: compileAspects
    });
    /* del end */
  })();
  /* ------ h5.env ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    // TODO エラーコード定数等Minify版（製品利用版）でも必要なものはここに書く
    // =============================
    // Development Only
    // =============================
    /* del begin */
    // TODO Minify時にプリプロセッサで削除されるべきものはこの中に書く
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // TODO 高速化のために他で定義されている関数などを変数に入れておく場合はここに書く
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    // TODO モジュールレベルのプライベート変数はここに書く
    // =============================
    // Functions
    // =============================
    function check(ua) {
      var isiPhone = !!ua.match(/iPhone/i);
      var isiPad = !!ua.match(/iPad/i);
      var isiOS = isiPhone || isiPad;
      var isAndroid = !!ua.match(/android/i);
      var isWindowsPhone = !!ua.match(/Windows Phone/i);
      var isIE = !!ua.match(/MSIE/);
      var isFirefox = !!ua.match(/Firefox/i);
      var isChrome = !!ua.match(/Chrome/i) || !!ua.match(/CrMo/);
      var isSafari = !isAndroid && !!ua.match(/Safari/i) && !isChrome;
      var isWebkit = !!ua.match(/Webkit/i);
      var isOpera = !!ua.match(/Opera/i);
      var isAndroidDefaultBrowser = isAndroid && !!ua.match(/Safari/i) && !isChrome;
      var isSmartPhone = !!(isiPhone || isWindowsPhone || (isAndroidDefaultBrowser && ua.match(/Mobile/) && !ua.match(/SC-01C/)) || (isAndroid && isChrome && ua.match(/Mobile/)) || ua.match(/Fennec/i) || ua.match(/Opera Mobi/i));
      var isTablet = !!(isiPad || (isAndroidDefaultBrowser && !ua.match(/Mobile/)) || (isAndroid && isChrome && !ua.match(/Mobile/)) || ua.match(/SC-01C/) || ua.match(/Fennec/i) || ua.match(/Opera Tablet/i));
      var isDesktop = !isSmartPhone && !isTablet;
      var osVersion = null;
      var osVersionFull = null;
      var getiOSVersion = function(pre, post) {
        return $.trim(ua.substring(ua.indexOf(pre) + pre.length, ua.indexOf(post))).split('_');
      };
      var getVersion = function(target, end, ignoreCase) {
        var r = ignoreCase === false ? new RegExp(target + end) : new RegExp(target + end, 'i');
        return $.trim(ua.match(r));
      };
      var spaceSplit = function(target, ignoreCase) {
        var v = getVersion(target, '[^;)]*', ignoreCase).split(' ');
        return v[v.length - 1];
      };
      var slashSplit = function(target, ignoreCase) {
        var v = getVersion(target, '[^;) ]*', ignoreCase).split('/');
        return v[v.length - 1];
      };
      var getMainVersion = function(target) {
        return parseInt(target.split('.')[0]);
      };
      if (isiPhone) {
        var s = getiOSVersion('iPhone OS', 'like');
        osVersion = parseInt(s[0]);
        osVersionFull = s.join('.');
      } else if (isiPad) {
        var s = getiOSVersion('CPU OS', 'like');
        osVersion = parseInt(s[0]);
        osVersionFull = s.join('.');
      } else if (isAndroid && isFirefox) {
        // FennecはAndroidのバージョンを取得することができない。
      } else if (isAndroid) {
        var s = spaceSplit('Android');
        osVersion = getMainVersion(s);
        osVersionFull = s;
      } else if (isWindowsPhone) {
        var s = spaceSplit('Windows Phone OS');
        if (!s) {
          s = spaceSplit('Windows Phone');
        }
        osVersion = getMainVersion(s);
        osVersionFull = s;
      }
      // デスクトップの場合。osVersion, osVersionFullはnull
      var browserVersion = null;
      var browserVersionFull = null;
      if (isiOS || (isAndroid && isAndroidDefaultBrowser)) {
        browserVersion = osVersion;
        browserVersionFull = osVersionFull;
      } else {
        var version = null;
        if (isIE) {
          version = spaceSplit('MSIE', false);
        } else if (isChrome) {
          version = slashSplit('Chrome', false);
          if (!version) {
            version = slashSplit('CrMo', false);
          }
        } else if (isSafari) {
          version = slashSplit('Version');
        } else if (isFirefox) {
          version = slashSplit('Firefox');
        } else if (isOpera) {
          version = slashSplit('Version');
          if (!version) {
            version = slashSplit('Opera');
          }
          if (!version) {
            version = spaceSplit('Opera');
          }
        }
        if (version) {
          browserVersion = getMainVersion(version);
          browserVersionFull = version;
        }
      }
      return {
        osVersion: osVersion,
        osVersionFull: osVersionFull,
        browserVersion: browserVersion,
        browserVersionFull: browserVersionFull,
        isiPhone: isiPhone,
        isiPad: isiPad,
        isiOS: isiOS,
        isAndroid: isAndroid,
        isWindowsPhone: isWindowsPhone,
        isIE: isIE,
        isFirefox: isFirefox,
        isChrome: isChrome,
        isSafari: isSafari,
        isOpera: isOpera,
        isAndroidDefaultBrowser: isAndroidDefaultBrowser,
        isSmartPhone: isSmartPhone,
        isTablet: isTablet,
        isDesktop: isDesktop,
        isWebkit: isWebkit
      };
    }
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.env', {
      ua: check(navigator.userAgent)
    });
    /* del begin */
    // テストのためにグローバルに公開。プリプロセッサで削除される。
    h5.u.obj.expose('h5.env', {
      __check: check
    });
    /* del end */
  })();
  /* ------ h5.async ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    var ERR_CODE_NOT_ARRAY = 5000;
    var errMsgMap = {};
    errMsgMap[ERR_CODE_NOT_ARRAY] = 'h5.async.each() の第1引数は配列のみを扱います。';
    // メッセージの登録
    addFwErrorCodeMap(errMsgMap);
    // =============================
    // Development Only
    // =============================
    /* del begin */
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    // =============================
    // Functions
    // =============================
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    var deferred = function() {
      /// <summary>
      ///  登録された共通のエラー処理を実行できるDeferredオブジェクトを返します。
      ///  Deferredに notify() notifyWith() progress() メソッドがない場合は、追加したオブジェクトを返します。
      /// </summary>
      /// <returns  type = "Deferred" >
      ///  Deferredオブジェクト
      /// </returns>
      var dfd = $.Deferred();
      // jQuery1.6.xにはDeferred.notify/notifyWith/progressがない
      if (!dfd.notify && !dfd.notifyWith && !dfd.progress) {
        // 既にnorify/notifyWithが呼ばれたかどうかのフラグ
        var notified = false;
        // 最後に指定された実行コンテキスト
        var lastNotifyContext = null;
        // 最後に指定されたパラメータ
        var lastNotifyParam = null;
        // progressCallbacksを格納するための配列
        dfd.__h5__progressCallbacks = [];
        // progressCallbacksに対応したprogressFilterの配列を格納するための配列
        dfd.__h5__progressFilters = [];
        var progress = function(progressCallback) {
          // 既にnorify/notifyWithが呼ばれていた場合、jQuery1.7.xの仕様と同じにするためにコールバックの登録と同時に実行する必要がある
          var filters = this.__h5__progressPipeFilters;
          if (notified) {
            var params = lastNotifyParam;
            // pipe()でprogressFilterが登録されいたら値をフィルタに通す
            if (filters && filters.length > 0) {
              for (var i = 0, fLen = filters.length; i < fLen; i++) {
                params = filters[i].apply(this, wrapInArray(params));
              }
            }
            if (params !== lastNotifyParam) {
              params = wrapInArray(params);
            }
            progressCallback.apply(lastNotifyContext, params);
          }
          dfd.__h5__progressCallbacks.push(progressCallback);
          dfd.__h5__progressFilters.push(filters);
          return this;
        };
        dfd.progress = progress;
        var originalPromise = dfd.promise;
        dfd.promise = function(obj) {
          var promise = originalPromise.call(this, obj);
          // プロミスにprogress()を追加
          promise.progress = progress;
          return promise;
        };
        dfd.notify = function()           /* var_args */
{
          notified = true;
          if (arguments.length !== -1) {
            lastNotifyContext = this;
            lastNotifyParam = h5.u.obj.argsToArray(arguments);
          }
          var callbacks = dfd.__h5__progressCallbacks;
          var filters = dfd.__h5__progressFilters;
          var args = h5.u.obj.argsToArray(arguments);
          // progressコールバックが登録されていたら全て実行する
          if (callbacks.length > 0) {
            for (var i = 0, callbackLen = callbacks.length; i < callbackLen; i++) {
              var params = args;
              // pipe()でprogressFilterが登録されいたら値をフィルタに通す
              if (filters[i] && filters[i].length > 0) {
                for (var j = 0, fLen = filters[i].length; j < fLen; j++) {
                  params = filters[i][j].apply(this, wrapInArray(params));
                }
              }
              if (params !== arguments) {
                params = wrapInArray(params);
              }
              callbacks[i].apply(this, params);
            }
          }
          return this;
        };
        dfd.notifyWith = function(context, args) {
          notified = true;
          lastNotifyContext = context;
          lastNotifyParam = args;
          var callbacks = this.__h5__progressCallbacks;
          var filters = this.__h5__progressFilters;
          // progressコールバックが登録されていたら全て実行する
          if (callbacks.length > 0) {
            for (var i = 0, callbackLen = callbacks.length; i < callbackLen; i++) {
              var params = args;
              // pipe()でprogressFilterが登録されいたら値をフィルタに通す
              if (filters[i] && filters[i].length > 0) {
                for (var j = 0, fLen = filters[i].length; j < fLen; j++) {
                  params = filters[i][j].apply(this, wrapInArray(params));
                }
              }
              if (params !== args) {
                params = wrapInArray(params);
              }
              callbacks[i].apply(context, params);
            }
          }
          return this;
        };
        var originalPipe = dfd.pipe;
        dfd.pipe = function(doneFilter, failFilter, progressFilter) {
          // pipe()の戻り値であるfilteredは元のDeferredオブジェクトとはインスタンスが異なる
          var filtered = originalPipe.call(this, doneFilter, failFilter);
          if (progressFilter) {
            if (!this.__h5__progressPipeFilters) {
              filtered.__h5__progressPipeFilters = [progressFilter];
            } else {
              filtered.__h5__progressPipeFilters = this.__h5__progressPipeFilters.concat([progressFilter]);
            }
          }
          filtered.pipe = dfd.pipe;
          filtered.progress = dfd.progress;
          return filtered;
        };
      }
      // failコールバックが1つ以上登録されたかどうかのフラグ
      var existFailHandler = false;
      var originalFail = dfd.fail;
      var fail = function()         /* var_args */
{
        if (arguments.length > 0) {
          existFailHandler = true;
        }
        return originalFail.apply(this, arguments);
      };
      dfd.fail = fail;
      var originalAlways = dfd.always;
      var always = function()         /* var_args */
{
        if (arguments.length > 0) {
          existFailHandler = true;
        }
        return originalAlways.apply(this, arguments);
      };
      dfd.always = always;
      var then = function(doneCallbacks, failCallbacks, progressCallbacks) {
        if (doneCallbacks) {
          this.done.apply(this, wrapInArray(doneCallbacks));
        }
        if (failCallbacks) {
          this.fail.apply(this, wrapInArray(failCallbacks));
        }
        if (progressCallbacks) {
          this.progress.apply(this, wrapInArray(progressCallbacks));
        }
        return this;
      };
      dfd.then = then;
      var originalReject = dfd.reject;
      var reject = function()         /* var_args */
{
        var commonFailHandler = h5.settings.commonFailHandler;
        // failコールバックが1つもない、かつcommonFailHandlerがある場合は、commonFailHandlerを登録する
        if (!existFailHandler && commonFailHandler) {
          originalFail.call(this, commonFailHandler);
        }
        return originalReject.apply(this, arguments);
      };
      dfd.reject = reject;
      var originalRejectWith = dfd.rejectWith;
      var rejectWith = function()         /* var_args */
{
        var commonFailHandler = h5.settings.commonFailHandler;
        // failコールバックが1つもない、かつcommonFailHandlerがある場合は、commonFailHandlerを登録する
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
      ///  オブジェクトがPromiseオブジェクトであるかどうかを返します。&lt;br
      ///  /&gt;
      ///  オブジェクトがDeferredオブジェクトの場合、falseが返ります。
      /// </summary>
      /// <param  name = "object" type = "Object" >
      ///  オブジェクト
      /// </param>
      /// <returns  type = "Boolean" >
      ///  オブジェクトがPromiseオブジェクトであるかどうか
      /// </returns>
      return !!object && object.done && object.fail && !object.resolve && !object.reject;
    };
    var loop = function(array, callback, suspendOnTimes) {
      /// <summary>
      ///  指定された回数ごとにループを抜けブラウザに制御を戻すユーティリティメソッドです。
      /// </summary>
      /// <param  name = "array" type = "Any[]" >
      ///  配列
      /// </param>
      /// <param  name = "callback" type = "Function" >
      ///  コールバック関数。
      ///  コールバックには引数として現在のインデックス、現在の値、ループコントローラが渡されます。
      ///  callback(index, value, loopControl) 
      ///  loopControlは以下の3つのメソッドを持っています。
      ///  
      ///  pause 処理の途中でポーズをかけます。
      ///  resume ポーズを解除し処理を再開します。
      ///  stop 処理を中断します。1度stopで中断すると再開することはできません。
      ///  
      /// </param>
      /// <param  name = "[suspendOnTimes=20]" type = "Number" >
      ///  何回ごとにループを抜けるか。デフォルトは20回です。
      /// </param>
      /// <returns  type = "Promise" >
      ///  Promiseオブジェクト
      /// </returns>
      if (!$.isArray(array)) {
        throwFwError(ERR_CODE_NOT_ARRAY);
      }
      var dfd = deferred();
      // 何回ごとにループを抜けるか。デフォルトは20回
      var st = $.type(suspendOnTimes) === 'number' ? suspendOnTimes : 20;
      var userReject = false;
      var index = 0;
      var len = array.length;
      var execute,
        loopControl = null;
      var each = function() {
        if (index === len) {
          dfd.resolve(array);
          return;
        } else if (userReject) {
          dfd.reject(array);
          return;
        }
        var ret = callback.call(array, index, array[index], loopControl);
        index++;
        if (isPromise(ret)) {
          ret.done(function() {
            execute();
          }).fail(function() {
            userReject = true;
            execute();
          });
        } else {
          execute();
        }
      };
      var async = function() {
        setTimeout(function() {
          var i = index - 1;
          if (index > 0) {
            dfd.notify({
              data: array,
              index: i,
              value: array[i]
            });
          }
          each();
        }, 0);
      };
      var pause = false;
      execute = function() {
        if (pause) {
          return;
        }
        index % st === 0 ? async() : each();
      };
      var stopFlag = false;
      loopControl = {
        resume: function() {
          if (!stopFlag && pause) {
            pause = false;
            execute();
          }
        },
        pause: function() {
          pause = true;
        },
        stop: function() {
          stopFlag = true;
          dfd.resolve(array);
        }
      };
      async();
      return dfd.promise();
    };
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.async', {
      deferred: deferred,
      isPromise: isPromise,
      loop: loop
    });
  })();
  /* ------ h5.ajax ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    // TODO エラーコード定数等Minify版（製品利用版）でも必要なものはここに書く
    // =============================
    // Development Only
    // =============================
    /* del begin */
    // TODO Minify時にプリプロセッサで削除されるべきものはこの中に書く
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // TODO 高速化のために他で定義されている関数などを変数に入れておく場合はここに書く
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    // TODO モジュールレベルのプライベート変数はここに書く
    // =============================
    // Functions
    // =============================
    // TODO モジュールレベルのプライベート関数はここに書く
    // 関数は関数式ではなく function myFunction(){} のように関数定義で書く
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    var ajax = function(var_args) {
      /// <summary>
      ///  HTTP通信を行います。&lt;br
      ///  /&gt;
      ///  基本的に使い方は、jQuery.ajax()と同じです。
      ///  jQuery.ajax()と異なる点は共通のエラーハンドラが定義できることです。
      ///  h5.settings.commonFailHandlerに関数を設定し、h5.ajax()に引数として渡すオプションにerror/completeコールバックが設定されていない、
      ///  もしくは戻り値のPromiseオブジェクトに対するfail/alwaysコールバックが設定されていない場合にエラーが発生すると 
      ///  h5.settings.commonFailHandlerに設定した関数が呼ばれます。
      /// </summary>
      /// <param  name = "var_args" type = "Any" >
      ///  jQuery.ajaxに渡す引数
      /// </param>
      /// <returns  type = "Promise" >
      ///  Promiseオブジェクト
      /// </returns>
      var opt = typeof arguments[0] === 'string' ? arguments[1] : arguments[0];
      var hasFailCallback = opt && (opt.error || opt.fail || opt.complete || opt.always);
      var jqXHR = $.ajax.apply($, arguments);
      if (!jqXHR.progress) {
        jqXHR.progress = function() {
          // notifyされることはないので空にしている
        };
      }
      var callFail = false;
      var commonFailHandler = h5.settings.commonFailHandler;
      if (!hasFailCallback && commonFailHandler) {
        jqXHR.fail(function()           /* var_args */
{
          if (!callFail) {
            commonFailHandler.apply(null, arguments);
          }
        });
        var originalFail = jqXHR.fail;
        jqXHR.fail = function()           /* var_args */
{
          callFail = true;
          return originalFail.apply(jqXHR, arguments);
        };
        jqXHR.error = jqXHR.fail;
        var originalAlways = jqXHR.always;
        jqXHR.always = function()           /* var_args */
{
          callFail = true;
          return originalAlways.apply(jqXHR, arguments);
        };
        jqXHR.complete = jqXHR.always;
        jqXHR.then = function(doneCallbacks, failCallbacks, progressCallbacks) {
          if (doneCallbacks) {
            jqXHR.done.apply(jqXHR, wrapInArray(doneCallbacks));
          }
          if (failCallbacks) {
            jqXHR.fail.apply(jqXHR, wrapInArray(failCallbacks));
          }
          if (progressCallbacks) {
            jqXHR.progress.apply(jqXHR, wrapInArray(progressCallbacks));
          }
          return jqXHR;
        };
      }
      return jqXHR;
    };
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5', {
      ajax: ajax
    });
  })();
  /* ------ h5.core.controller ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    var TEMPLATE_LOAD_RETRY_COUNT = 3;
    var TEMPLATE_LOAD_RETRY_INTERVAL = 3000;
    var TYPE_OF_UNDEFINED = 'undefined';
    var SUFFIX_CONTROLLER = 'Controller';
    var SUFFIX_LOGIC = 'Logic';
    var EVENT_NAME_H5_TRACKSTART = 'h5trackstart';
    var EVENT_NAME_H5_TRACKMOVE = 'h5trackmove';
    var EVENT_NAME_H5_TRACKEND = 'h5trackend';
    var ROOT_ELEMENT_NAME = 'rootElement';
    // エラーコード
    var ERR_CODE_INVALID_TEMPLATE_SELECTOR = 6000;
    var ERR_CODE_BIND_TARGET_REQUIRED = 6001;
    var ERR_CODE_BIND_NOT_CONTROLLER = 6002;
    var ERR_CODE_BIND_NOT_TARGET = 6003;
    var ERR_CODE_BIND_TARGET_COMPLEX = 6004;
    var ERR_CODE_CUSTOM_ERROR_TYPE_REQUIRED = 6005;
    var ERR_CODE_CONTROLLER_NAME_REQUIRED = 6006;
    var ERR_CODE_CONTROLLER_INVALID_INIT_PARAM = 6007;
    var ERR_CODE_CONTROLLER_ALREADY_CREATED = 6008;
    var ERR_CODE_CONTROLLER_CIRCULAR_REF = 6009;
    var ERR_CODE_LOGIC_CIRCULAR_REF = 6010;
    var ERR_CODE_CONTROLLER_SAME_PROPERTY = 6011;
    var ERR_CODE_EVENT_HANDLER_SELECTOR_THIS = 6012;
    var ERR_CODE_SAME_EVENT_HANDLER = 6013;
    var ERR_CODE_CONTROLLER_META_KEY_INVALID = 6014;
    var ERR_CODE_CONTROLLER_META_KEY_NULL = 6015;
    var ERR_CODE_CONTROLLER_META_KEY_NOT_CONTROLLER = 6016;
    var ERR_CODE_LOGIC_NAME_REQUIRED = 6017;
    var ERR_CODE_LOGIC_ALREADY_CREATED = 6018;
    var ERR_CODE_EXPOSE_NAME_REQUIRED = 6019;
    var ERR_CODE_NOT_VIEW = 6029;
    // エラーコードマップ
    var errMsgMap = {};
    errMsgMap[ERR_CODE_INVALID_TEMPLATE_SELECTOR] = 'update/append/prepend() の第1引数に"window", "window.", "navigator", "navigator."で始まるセレクタは指定できません。';
    errMsgMap[ERR_CODE_BIND_TARGET_REQUIRED] = 'バインド対象となる要素を指定して下さい。';
    errMsgMap[ERR_CODE_BIND_NOT_CONTROLLER] = 'コントローラ化したオブジェクトを指定して下さい。';
    errMsgMap[ERR_CODE_BIND_NOT_TARGET] = 'コントローラ"{0}"のバインド対象となる要素が存在しません。';
    errMsgMap[ERR_CODE_BIND_TARGET_COMPLEX] = 'コントローラ"{0}"のバインド対象となる要素が2つ以上存在します。バインド対象は1つのみにしてください。';
    errMsgMap[ERR_CODE_CUSTOM_ERROR_TYPE_REQUIRED] = 'エラータイプを指定してください。';
    errMsgMap[ERR_CODE_CONTROLLER_NAME_REQUIRED] = 'コントローラの名前が定義されていません。__nameにコントローラ名を設定して下さい。';
    errMsgMap[ERR_CODE_CONTROLLER_INVALID_INIT_PARAM] = 'コントローラ"{0}"の初期化パラメータがプレーンオブジェクトではありません。初期化パラメータにはプレーンオブジェクトを設定してください。';
    errMsgMap[ERR_CODE_CONTROLLER_ALREADY_CREATED] = '指定されたオブジェクトは既にコントローラ化されています。';
    errMsgMap[ERR_CODE_CONTROLLER_CIRCULAR_REF] = 'コントローラ"{0}"で、参照が循環しているため、コントローラを生成できません。';
    errMsgMap[ERR_CODE_LOGIC_CIRCULAR_REF] = 'コントローラ"{0}"のロジックで、参照が循環しているため、ロジックを生成できません。';
    errMsgMap[ERR_CODE_CONTROLLER_SAME_PROPERTY] = 'コントローラ"{0}"のプロパティ"{1}"はコントローラ化によって追加されるプロパティと名前が重複しています。';
    errMsgMap[ERR_CODE_EVENT_HANDLER_SELECTOR_THIS] = 'コントローラ"{0}"でセレクタ名にthisが指定されています。コントローラをバインドした要素自身を指定したい時はrootElementを指定してください。';
    errMsgMap[ERR_CODE_SAME_EVENT_HANDLER] = 'コントローラ"{0}"のセレクタ"{1}"に対して"{2}"というイベントハンドラが重複して設定されています。';
    errMsgMap[ERR_CODE_CONTROLLER_META_KEY_INVALID] = 'コントローラ"{0}"には__metaで指定されたプロパティ"{1}"がありません。';
    errMsgMap[ERR_CODE_CONTROLLER_META_KEY_NULL] = 'コントローラ"{0}"の__metaに指定されたキー"{1}"の値がnullです。コントローラを持つプロパティキー名を指定してください。';
    errMsgMap[ERR_CODE_CONTROLLER_META_KEY_NOT_CONTROLLER] = 'コントローラ"{0}"の__metaに指定されたキー"{1}"の値はコントローラではありません。コントローラを持つプロパティキー名を指定してください。';
    errMsgMap[ERR_CODE_LOGIC_NAME_REQUIRED] = 'ロジック名が定義されていません。__nameにロジック名を設定して下さい。';
    errMsgMap[ERR_CODE_LOGIC_ALREADY_CREATED] = '指定されたオブジェクトは既にロジック化されています。';
    errMsgMap[ERR_CODE_EXPOSE_NAME_REQUIRED] = 'コントローラ、もしくはロジックの __name が設定されていません。';
    errMsgMap[ERR_CODE_NOT_VIEW] = 'テンプレートはViewモジュールがなければ使用できません。';
    addFwErrorCodeMap(errMsgMap);
    // =============================
    // Development Only
    // =============================
    var fwLogger = h5.log.createLogger('h5.core');
    /* del begin */
    // TODO Minify時にプリプロセッサで削除されるべきものはこの中に書く
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // TODO 高速化のために他で定義されている関数などを変数に入れておく場合はここに書く
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    var getDeferred = h5.async.deferred;
    var startsWith = h5.u.str.startsWith;
    var endsWith = h5.u.str.endsWith;
    var format = h5.u.str.format;
    var argsToArray = h5.u.obj.argsToArray;
    var getByPath = h5.u.obj.getByPath;
    // =============================
    // Functions
    // =============================
    function executeListenersInterceptor(invocation) {
      /// <summary>
      ///  コントローラのexecuteListenersを見てリスナーを実行するかどうかを決定するインターセプタ。
      /// </summary>
      /// <param  name = "invocation" type = "Object" >
      ///  インヴォケーション.
      /// </param>
      if (!this.__controllerContext.executeListeners) {
        return;
      }
      return invocation.proceed();
    }
    function weaveControllerAspect(controllerDefObject, prop, isEventHandler) {
      /// <summary>
      ///  指定されたオブジェクトの関数にアスペクトを織り込みます。
      /// </summary>
      /// <param  name = "controllerDefObject" type = "Object" >
      ///  オブジェクト.
      /// </param>
      /// <param  name = "prop" type = "Object" >
      ///  プロパティ名.
      /// </param>
      /// <param  name = "isEventHandler" type = "Boolean" >
      ///  イベントハンドラかどうか.
      /// </param>
      /// <returns  type = "Object" >
      ///  AOPに必要なメソッドを織り込んだオブジェクト.
      /// </returns>
      var interceptors = getInterceptors(controllerDefObject.__name, prop);
      // イベントハンドラの場合、 enable/disableListeners()のために一番外側に制御用インターセプタを織り込む
      if (isEventHandler) {
        interceptors.push(executeListenersInterceptor);
      }
      return createWeavedFunction(controllerDefObject[prop], prop, interceptors);
    }
    function getInterceptors(targetName, pcName) {
      /// <summary>
      ///  関数名とポイントカットを比べて、条件に合致すればインターセプタを返す.
      /// </summary>
      /// <param  name = "targetName" type = "String" >
      ///  バインドする必要のある関数名.
      /// </param>
      /// <param  name = "pcName" type = "Object" >
      ///  ポイントカットで判別する対象名.
      /// </param>
      /// <returns  type = "Function[]" >
      ///  AOP用関数配列.
      /// </returns>
      var ret = [];
      var aspects = h5.settings.aspects;
      // 織り込むべきアスペクトがない場合はそのまま空の配列を返す
      if (!aspects || aspects.length === 0) {
        return ret;
      }
      aspects = wrapInArray(aspects);
      for (var i = aspects.length - 1; -1 < i; i--) {
        var aspect = aspects[i];
        if (aspect.target && !aspect.compiledTarget.test(targetName)) {
          continue;
        }
        var interceptors = aspect.interceptors;
        if (aspect.pointCut && !aspect.compiledPointCut.test(pcName)) {
          continue;
        }
        if (!$.isArray(interceptors)) {
          ret.push(interceptors);
          continue;
        }
        for (var j = interceptors.length - 1; -1 < j; j--) {
          ret = ret.concat(interceptors[j]);
        }
      }
      return ret;
    }
    function createWeavedFunction(base, funcName, aspects) {
      /// <summary>
      ///  基本となる関数にアスペクトを織り込んだ関数を返します。
      /// </summary>
      /// <param  name = "baseFunc" type = "Function" >
      ///  基本関数.
      /// </param>
      /// <param  name = "funcName" type = "String" >
      ///  基本関数名.
      /// </param>
      /// <param  name = "aspects" type = "Function[]" >
      ///  AOP用関数配列.
      /// </param>
      /// <returns  type = "Function" >
      ///  AOP用関数を織り込んだ関数.
      /// </returns>
      // 関数のウィービングを行う
      var weave = function(baseFunc, fName, aspect) {
        return function()           /* var_args */
{
          var that = this;
          var invocation = {
            target: that,
            func: baseFunc,
            funcName: fName,
            args: arguments,
            proceed: function() {
              return baseFunc.apply(that, this.args);
            }
          };
          return aspect.call(that, invocation);
        };
      };
      var f = base;
      for (var i = 0, l = aspects.length; i < l; i++) {
        f = weave(f, funcName, aspects[i]);
      }
      return f;
    }
    function weaveLogicAspect(logic) {
      /// <summary>
      ///  指定されたオブジェクトの関数にアスペクトを織り込みます。
      /// </summary>
      /// <param  name = "logic" type = "Object" >
      ///  ロジック.
      /// </param>
      /// <returns  type = "Object" >
      ///  AOPに必要なメソッドを織り込んだロジック.
      /// </returns>
      for (var prop in logic) {
        if ($.isFunction(logic[prop])) {
          logic[prop] = createWeavedFunction(logic[prop], prop, getInterceptors(logic.__name, prop));
        } else {
          logic[prop] = logic[prop];
        }
      }
      return logic;
    }
    function isLifecycleProperty(controllerDefObject, prop) {
      /// <summary>
      ///  コントローラ定義オブジェクトのプロパティがライフサイクルイベントどうかを返します。
      /// </summary>
      /// <param  name = "controllerDefObject" type = "Object" >
      ///  コントローラ定義オブジェクト
      /// </param>
      /// <param  name = "prop" type = "String" >
      ///  プロパティ名
      /// </param>
      /// <returns  type = "Boolean" >
      ///  コントローラ定義オブジェクトのプロパティがライフサイクルイベントかどうか
      /// </returns>
      // $.isFunction()による判定はいらないかも。
      return (prop === '__ready' || prop === '__construct' || prop === '__init') && $.isFunction(controllerDefObject[prop]);
    }
    function isGlobalSelector(selector) {
      /// <summary>
      ///  セレクタがコントローラの外側の要素を指しているかどうかを返します。
      ///  (外側の要素 = true)
      /// </summary>
      /// <param  name = "selector" type = "String" >
      ///  セレクタ
      /// </param>
      /// <returns  type = "Boolean" >
      ///  コントローラの外側の要素を指しているかどうか
      /// </returns>
      return !!selector.match(/^\{.*\}$/);
    }
    function isBindRequested(eventName) {
      /// <summary>
      ///  イベント名がjQuery.bindを使って要素にイベントをバインドするかどうかを返します。
      /// </summary>
      /// <param  name = "eventName" type = "String" >
      ///  イベント名
      /// </param>
      /// <returns  type = "Boolean" >
      ///  jQuery.bindを使って要素にイベントをバインドするかどうか
      /// </returns>
      return !!eventName.match(/^\[.*\]$/);
    }
    function trimGlobalSelectorBracket(selector) {
      /// <summary>
      ///  セレクタから{}を外した文字列を返します。
      /// </summary>
      /// <param  name = "selector" type = "String" >
      ///  セレクタ
      /// </param>
      /// <returns  type = "String" >
      ///  セレクタから{}を外した文字列
      /// </returns>
      return $.trim(selector.substring(1, selector.length - 1));
    }
    function trimBindEventBracket(eventName) {
      /// <summary>
      ///  イベント名から[]を外した文字列を返す
      /// </summary>
      /// <param  name = "eventName" type = "String" >
      ///  イベント名
      /// </param>
      /// <returns  type = "String" >
      ///  イベント名から[]を外した文字列
      /// </returns>
      return $.trim(eventName.substring(1, eventName.length - 1));
    }
    function getGlobalSelectorTarget(selector) {
      /// <summary>
      ///  指定されたセレクタがwindow,
      ///  window., document, document., navidator, navigator. で
      ///  始まっていればそのオブジェクトを、そうでなければそのまま文字列を返します。
      /// </summary>
      /// <param  name = "selector" type = "String" >
      ///  セレクタ
      /// </param>
      /// <returns  type = "DOM|String" >
      ///  DOM要素、もしくはセレクタ
      /// </returns>
      var retSelector = selector;
      if (startsWith(selector, 'window') || startsWith(selector, 'document') || startsWith(selector, 'navigator')) {
        // セレクタではなく、オブジェクトがターゲットの場合
        return getByPath(selector);
      }
      return retSelector;
    }
    function isEventHandler(controllerDefObject, prop) {
      /// <summary>
      ///  指定されたプロパティがイベントハンドラかどうかを返します。
      /// </summary>
      /// <param  name = "controllerDefObject" type = "Object" >
      ///  コントローラ定義オブジェクト
      /// </param>
      /// <param  name = "prop" type = "String" >
      ///  プロパティ名
      /// </param>
      /// <returns  type = "Boolean" >
      ///  プロパティがイベントハンドラかどうか
      /// </returns>
      return prop.indexOf(' ') !== -1 && $.isFunction(controllerDefObject[prop]);
    }
    function checkControllerCircularRef(controllerDefObject) {
      /// <summary>
      ///  コントローラ定義オブジェクトの子孫コントローラ定義が循環参照になっているかどうかをチェックします。
      /// </summary>
      /// <param  name = "controllerDefObject" type = "Object" >
      ///  コントローラ定義オブジェクト
      /// </param>
      /// <returns  type = "Boolean" >
      ///  循環参照になっているかどうか(true=循環参照)
      /// </returns>
      var checkCircular = function(controllerDef, ancestors) {
        for (var prop in controllerDef) if ($.inArray(controllerDef, ancestors) >= 0 || endsWith(prop, SUFFIX_CONTROLLER) && checkCircular(controllerDef[prop], ancestors.concat([controllerDef]))) {
          return true;
        }
        return false;
      };
      return checkCircular(controllerDefObject, []);
    }
    function checkLogicCircularRef(controllerDefObj) {
      /// <summary>
      ///  コントローラ定義オブジェクトのロジック定義が循環参照になっているかどうかをチェックします。
      /// </summary>
      /// <param  name = "controllerDefObject" type = "Object" >
      ///  コントローラ定義オブジェクト
      /// </param>
      /// <returns  type = "Boolean" >
      ///  循環参照になっているかどうか(true=循環参照)
      /// </returns>
      var checkCircular = function(controllerDef, ancestors) {
        for (var prop in controllerDef) if ($.inArray(controllerDef, ancestors) >= 0 || endsWith(prop, SUFFIX_LOGIC) && checkCircular(controllerDef[prop], ancestors.concat([controllerDef]))) {
          return true;
        }
        return false;
      };
      return checkCircular(controllerDefObj, []);
    }
    function isChildController(controller, prop) {
      /// <summary>
      ///  コントローラのプロパティが子コントローラかどうかを返します。
      /// </summary>
      /// <param  name = "controller" type = "Object" >
      ///  コントローラ
      /// </param>
      /// <param  name = "プロパティ名" type = "String" >
      /// </param>
      /// <returns  type = "Boolean" >
      ///  コントローラのプロパティが子コントローラかどうか(true=子コントローラである)
      /// </returns>
      var target = controller[prop];
      return endsWith(prop, SUFFIX_CONTROLLER) && prop !== 'rootController' && prop !== 'parentController' && !$.isFunction(target) && (target && !target.__controllerContext.isRoot);
    }
    function getDescendantControllerPromises(controller, propertyName, aquireFromControllerContext) {
      /// <summary>
      ///  指定されたコントローラの子孫コントローラのPromiseオブジェクトを全て取得します。
      /// </summary>
      /// <param  name = "controller" type = "Object" >
      ///  コントローラ
      /// </param>
      /// <param  name = "propertyName" type = "String" >
      ///  プロパティ名(initPromise,readyPromise)
      /// </param>
      /// <param  name = "aquireFromControllerContext" type = "Object" >
      ///  コントローラコンテキストのプロパティかどうか
      /// </param>
      /// <returns  type = "Promise[]" >
      ///  Promiseオブジェクト配列
      /// </returns>
      var promises = [];
      var targets = [];
      var getPromisesInner = function(object) {
        targets.push(object);
        for (var prop in object) {
          if (isChildController(object, prop)) {
            var c = object[prop];
            var promise = aquireFromControllerContext ? c.__controllerContext[propertyName] : c[propertyName];
            if (promise) {
              promises.push(promise);
            }
            if ($.inArray(c, targets) === -1) {
              getPromisesInner(c);
            }
          }
        }
      };
      getPromisesInner(controller);
      return promises;
    }
    function bindDescendantHandlers(controller) {
      /// <summary>
      ///  子孫コントローラのイベントハンドラをバインドします。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      var targets = [];
      var execute = function(controllerInstance) {
        if (controllerInstance.isReady || $.inArray(controllerInstance, targets) !== -1) {
          return;
        }
        targets.push(controllerInstance);
        var meta = controllerInstance.__meta;
        var notBindControllers = {};
        if (meta) {
          for (var prop in meta) {
            if (meta[prop].useHandlers === false) {
              // trueより文字数が少ないため1を代入。機能的には"true"を表せば何を代入しても良い。
              notBindControllers[prop] = 1;
            }
          }
        }
        for (var prop in controllerInstance) {
          var c = controllerInstance[prop];
          if (!isChildController(controllerInstance, prop)) {
            continue;
          }
          execute(c);
          if (!notBindControllers[prop]) {
            bindByBindMap(c);
          }
        }
      };
      execute(controller);
    }
    function bindByBindMap(controller) {
      /// <summary>
      ///  バインドマップに基づいてイベントハンドラをバインドします。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      var bindMap = controller.__controllerContext.bindMap;
      for (var s in bindMap) {
        for (var e in bindMap[s]) {
          (function(selector, eventName) {
            bindEventHandler(controller, selector, eventName);
          })(s, e);
        }
      }
    }
    function bindEventHandler(controller, selector, eventName) {
      /// <summary>
      ///  イベントハンドラのバインドを行います。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <param  name = "selector" type = "String" >
      ///  セレクタ
      /// </param>
      /// <param  name = "eventName" type = "String" >
      ///  イベント名
      /// </param>
      // bindMapに格納しておいたハンドラを取得
      var func = controller.__controllerContext.bindMap[selector][eventName];
      var event = eventName;
      var bindRequested = isBindRequested(eventName);
      if (bindRequested) {
        event = trimBindEventBracket(eventName);
      }
      var bindObj = null;
      switch (event) {
        case 'mousewheel':
          bindObj = getNormalizeMouseWheelBindObj(controller, selector, event, func);
          break;
        case EVENT_NAME_H5_TRACKSTART:
        case EVENT_NAME_H5_TRACKMOVE:
        case EVENT_NAME_H5_TRACKEND:
          bindObj = getH5TrackBindObj(controller, selector, eventName, func);
          break;
        default:
          bindObj = getNormalBindObj(controller, selector, event, func);
          break;
      }
      if (!bindObj) {
        return;
      }
      if (!$.isArray(bindObj)) {
        useBindObj(bindObj, bindRequested);
        return;
      }
      for (var i = 0, l = bindObj.length; i < l; i++) {
        useBindObj(bindObj[i], bindRequested);
      }
    }
    function bindByBindObject(bindObj) {
      /// <summary>
      ///  バインドオブジェクトに基づいてイベントハンドラをバインドします。
      /// </summary>
      /// <param  name = "bindObj" type = "Object" >
      ///  バインドオブジェクト
      /// </param>
      var controller = bindObj.controller;
      var rootElement = controller.rootElement;
      var selector = bindObj.selector;
      var eventName = bindObj.eventName;
      var handler = bindObj.handler;
      var useBind = isBindRequested(eventName);
      var event = useBind ? trimBindEventBracket(eventName) : eventName;
      if (isGlobalSelector(selector)) {
        // グローバルなセレクタの場合
        var selectTarget = trimGlobalSelectorBracket(selector);
        var isSelf = false;
        if (selectTarget === ROOT_ELEMENT_NAME) {
          selectTarget = rootElement;
          isSelf = true;
        } else {
          selectTarget = getGlobalSelectorTarget(selectTarget);
        }
        // バインド対象がdocument, windowの場合、live, delegateではイベントが拾えないことへの対応
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
    }
    function useBindObj(bindObj, bindRequested) {
      /// <summary>
      ///  バインドオブジェクトに対して必要であればイベント名を修正し、アンバインドマップにハンドラを追加した後、
      ///  実際にバインドを行います。
      /// </summary>
      /// <param  name = "bindObj" type = "Object" >
      ///  バインドオブジェクト
      /// </param>
      /// <param  name = "bindRequested" type = "Boolean" >
      ///  イベントハンドラをバインド([]記法)すべきかどうか
      /// </param>
      if (bindRequested) {
        bindObj.eventName = '[' + bindObj.eventName + ']';
      }
      // アンバインドマップにハンドラを追加
      registerUnbindMap(bindObj.controller, bindObj.selector, bindObj.eventName, bindObj.handler);
      bindByBindObject(bindObj);
    }
    function unbindDescendantHandlers(controller) {
      /// <summary>
      ///  子孫コントローラのイベントハンドラをアンバインドします。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      var targets = [];
      var execute = function(controllerInstance) {
        if ($.inArray(controllerInstance, targets) !== -1) {
          return;
        }
        targets.push(controllerInstance);
        var meta = controllerInstance.__meta;
        var notBindControllers = {};
        if (meta) {
          for (var prop in meta) {
            if (meta[prop].useHandlers === false) {
              // trueより文字数が少ないため1を代入。機能的には"true"を表せば何を代入しても良い。
              notBindControllers[prop] = 1;
            }
          }
        }
        for (var prop in controllerInstance) {
          var c = controllerInstance[prop];
          if (!isChildController(controllerInstance, prop)) {
            continue;
          }
          execute(c);
          if (!notBindControllers[prop]) {
            unbindByBindMap(c);
          }
        }
      };
      execute(controller);
    }
    function unbindByBindMap(controller) {
      /// <summary>
      ///  バインドマップに基づいてイベントハンドラをアンバインドします。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      var rootElement = controller.rootElement;
      var unbindMap = controller.__controllerContext.unbindMap;
      for (var selector in unbindMap) {
        for (var eventName in unbindMap[selector]) {
          var handler = unbindMap[selector][eventName];
          var useBind = isBindRequested(eventName);
          var event = useBind ? trimBindEventBracket(eventName) : eventName;
          if (isGlobalSelector(selector)) {
            var selectTarget = trimGlobalSelectorBracket(selector);
            var isSelf = false;
            if (selectTarget === ROOT_ELEMENT_NAME) {
              selectTarget = rootElement;
              isSelf = true;
            } else {
              selectTarget = getGlobalSelectorTarget(selectTarget);
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
    }
    function setExecuteListenersFlag(controller, flag) {
      /// <summary>
      ///  指定されたフラグで子コントローラを含む全てのコントローラのexecuteListenersフラグを変更します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <param  name = "flag" type = "Boolean" >
      ///  フラグ
      /// </param>
      controller.__controllerContext.executeListeners = flag;
      var targets = [];
      var changeFlag = function(controllerInstance) {
        targets.push(controllerInstance);
        for (var prop in controllerInstance) {
          if (isChildController(controllerInstance, prop)) {
            var c = controllerInstance[prop];
            c.__controllerContext.executeListeners = flag;
            if ($.inArray(c, targets) === -1) {
              changeFlag(c);
            }
          }
        }
      };
      changeFlag(controller);
    }
    function initRootAndParentController(controller) {
      /// <summary>
      ///  rootControllerとparentControllerをセットします。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      var targets = [];
      var init = function(controllerInstance, root, parent) {
        controllerInstance.rootController = root;
        controllerInstance.parentController = parent;
        targets.push(controllerInstance);
        for (var prop in controllerInstance) {
          if (isChildController(controllerInstance, prop)) {
            var c = controllerInstance[prop];
            if ($.inArray(c, targets) === -1) {
              init(c, root, controllerInstance);
            }
          }
        }
      };
      init(controller, controller, null);
    }
    function executeLifecycleEventChain(controller, isInitEvent) {
      /// <summary>
      ///  __init,
      ///  __readyイベントを実行する.
      /// </summary>
      /// <param  >
      ///  ｛Object} controller コントローラ.
      /// </param>
      /// <param  name = "isInitEvent" type = "Booelan" >
      ///  __initイベントを実行するかどうか.
      /// </param>
      var targets = [];
      var flagName = isInitEvent ? 'isInit' : 'isReady';
      var funcName = isInitEvent ? '__init' : '__ready';
      var leafDfd = getDeferred();
      setTimeout(function() {
        leafDfd.resolve();
      }, 0);
      var leafPromise = leafDfd.promise();
      var execInner = function(controllerInstance) {
        if ($.inArray(controllerInstance, targets) !== -1) {
          return;
        }
        targets.push(controllerInstance);
        // 既にライフサイクルイベントを実行済みであれば何もしない
        if (controllerInstance[flagName]) {
          return;
        }
        var isLeafController = true;
        for (var prop in controllerInstance) {
          // 子コントローラがあれば再帰的に処理
          if (isChildController(controllerInstance, prop)) {
            isLeafController = false;
            execInner(controllerInstance[prop]);
          }
        }
        // 子孫コントローラの準備ができた時に実行させる関数を定義
        var func = function() {
          // 既にライフサイクルイベントを実行済みであれば何もしない
          // 数行上で同じチェックを行っているが、非同期の場合ここでのチェックも必須となるはず
          if (controllerInstance[flagName]) {
            return;
          }
          var ret = null;
          var lifecycleFunc = controllerInstance[funcName];
          if (lifecycleFunc) {
            ret = controllerInstance[funcName](createInitializationContext(controllerInstance));
          }
          // ライフサイクルイベント実行後に呼ぶべきコールバック関数を作成
          var callback = isInitEvent ? createCallbackForInit(controllerInstance) : createCallbackForReady(controllerInstance);
          if (h5.async.isPromise(ret)) {
            ret.done(function() {
              callback();
            });
          } else {
            callback();
          }
        };
        // getPromisesForXXXの戻り値が空の配列の場合はfunc()は同期的に呼ばれる
        var promises = isInitEvent ? getPromisesForInit(controllerInstance) : getPromisesForReady(controllerInstance);
        if (isInitEvent && isLeafController) {
          promises.push(leafPromise);
        }
        $.when.apply($, promises).done(function() {
          func();
        });
      };
      execInner(controller);
    }
    function getPromisesForInit(controller) {
      /// <summary>
      ///  __initイベントを実行するために必要なPromiseを返します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <returns  type = "Promise[]" >
      ///  Promiseオブジェクト
      /// </returns>
      // 子孫コントローラのinitPromiseオブジェクトを取得
      var initPromises = getDescendantControllerPromises(controller, 'initPromise');
      // 自身のテンプレート用Promiseオブジェクトを取得
      initPromises.push(controller.__controllerContext.templatePromise);
      return initPromises;
    }
    function getPromisesForReady(controller) {
      /// <summary>
      ///  __readyイベントを実行するために必要なPromiseを返します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <returns  type = "Promise[]" >
      ///  Promiseオブジェクト
      /// </returns>
      // 子孫コントローラのreadyPromiseオブジェクトを取得
      return getDescendantControllerPromises(controller, 'readyPromise');
    }
    function createCallbackForInit(controller) {
      /// <summary>
      ///  __initイベントで実行するコールバック関数を返します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      return function() {
        if (controller.isInit) {
          return;
        }
        controller.isInit = true;
        var initDfd = controller.__controllerContext.initDfd;
        // FW、ユーザともに使用しないので削除
        delete controller.__controllerContext.templatePromise;
        delete controller.__controllerContext.initDfd;
        initDfd.resolve();
        if (controller.__controllerContext && controller.__controllerContext.isRoot) {
          // ルートコントローラであれば次の処理(イベントハンドラのバインドと__readyの実行)へ進む
          bindAndTriggerReady(controller);
        }
      };
    }
    function createCallbackForReady(controller) {
      /// <summary>
      ///  __readyイベントで実行するコールバック関数を返します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      return function() {
        if (controller.isReady) {
          return;
        }
        controller.isReady = true;
        var readyDfd = controller.__controllerContext.readyDfd;
        // FW、ユーザともに使用しないので削除
        delete controller.__controllerContext.readyDfd;
        readyDfd.resolve();
        if (controller.__controllerContext && controller.__controllerContext.isRoot) {
          // ルートコントローラであれば全ての処理が終了したことを表すイベント"h5controllerready"をトリガ
          if (!controller.rootElement || !controller.isInit || !controller.isReady) {
            return;
          }
          $(controller.rootElement).trigger('h5controllerready', [controller]);
        }
      };
    }
    function isCorrectTemplatePrefix(selector) {
      /// <summary>
      ///  テンプレートに渡すセレクタとして正しいかどうかを返します。
      /// </summary>
      /// <param  name = "selector" type = "String" >
      ///  セレクタ
      /// </param>
      /// <returns  type = "Boolean" >
      ///  テンプレートに渡すセレクタとして正しいかどうか(true=正しい)
      /// </returns>
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
    }
    function getTarget(element, rootElement, isTemplate) {
      /// <summary>
      ///  指定された要素が文字列があれば、ルートエレメント、{}記法を考慮した要素をjQueryオブジェクト化して返します。
      ///  DOM要素、jQueryオブジェクトであれば、
      ///  jQueryオブジェクト化して(指定要素がjQueryオブジェクトの場合、無駄な処理になるがコスト的には問題ない)返します。
      /// </summary>
      /// <param  name = "セレクタ、DOM要素、jQueryオブジェクト" type = "String|DOM|jQuery" >
      /// </param>
      /// <param  name = "rootElement" type = "DOM" >
      ///  ルートエレメント
      /// </param>
      /// <param  name = "isTemplate" type = "Boolean" >
      ///  テンプレートで使用するかどうか
      /// </param>
      /// <returns  type = "jQuery" >
      ///  jQueryオブジェクト
      /// </returns>
      if (typeof element !== 'string') {
        return $(element);
      }
      var $targets;
      var selector = $.trim(element);
      if (isGlobalSelector(selector)) {
        var s = trimGlobalSelectorBracket(selector);
        if (isTemplate && isCorrectTemplatePrefix(s)) {
          throwFwError(ERR_CODE_INVALID_TEMPLATE_SELECTOR);
        }
        $targets = $(getGlobalSelectorTarget(s));
      } else {
        $targets = $(rootElement).find(element);
      }
      return $targets;
    }
    function registerUnbindMap(controller, selector, eventName, handler) {
      /// <summary>
      ///  ハンドラをアンバインドマップに登録します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <param  name = "selector" type = "String" >
      ///  セレクタ
      /// </param>
      /// <param  name = "eventName" type = "String" >
      ///  イベント名
      /// </param>
      /// <param  name = "handler" type = "Function" >
      ///  ハンドラ
      /// </param>
      if (!controller.__controllerContext.unbindMap[selector]) {
        controller.__controllerContext.unbindMap[selector] = {};
      }
      controller.__controllerContext.unbindMap[selector][eventName] = handler;
    }
    function getNormalBindObj(controller, selector, eventName, func) {
      /// <summary>
      ///  バインドオブジェクトを返します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <param  name = "selector" type = "String" >
      ///  セレクタ
      /// </param>
      /// <param  name = "eventName" type = "String" >
      ///  イベント名
      /// </param>
      /// <param  name = "func" type = "Function" >
      ///  ハンドラとして登録したい関数
      /// </param>
      /// <returns  type = "Object" >
      ///  バインドオブジェクト
      ///  
      ///  bindObj.controller コントローラ
      ///  bindObj.selector セレクタ
      ///  bindObj.eventName イベント名
      ///  bindObj.handler イベントハンドラ
      ///  
      /// </returns>
      return {
        controller: controller,
        selector: selector,
        eventName: eventName,
        handler: function()           /* var_args */
{
          func.call(controller, createEventContext(controller, arguments));
        }
      };
    }
    function getNormalizeMouseWheelBindObj(controller, selector, eventName, func) {
      /// <summary>
      ///  クラスブラウザな&quot;mousewheel&quot;イベントのためのバインドオブジェクトを返します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <param  name = "selector" type = "String" >
      ///  セレクタ
      /// </param>
      /// <param  name = "eventName" type = "String" >
      ///  イベント名
      /// </param>
      /// <param  name = "func" type = "Function" >
      ///  ハンドラとして登録したい関数
      /// </param>
      /// <returns  type = "Object" >
      ///  バインドオブジェクト
      ///  
      ///  bindObj.controller コントローラ
      ///  bindObj.selector セレクタ
      ///  bindObj.eventName イベント名
      ///  bindObj.handler イベントハンドラ
      ///  
      /// </returns>
      return {
        controller: controller,
        selector: selector,
        // Firefoxには"mousewheel"イベントがない
        eventName: typeof document.onmousewheel === TYPE_OF_UNDEFINED ? 'DOMMouseScroll' : eventName,
        handler: function()           /* var_args */
{
          var eventContext = createEventContext(controller, arguments);
          var event = eventContext.event;
          // Firefox
          if (event.detail) {
            event.wheelDelta = -event.detail * 40;
          }
          func.call(controller, eventContext);
        }
      };
    }
    function getH5TrackBindObj(controller, selector, eventName, func) {
      /// <summary>
      ///  hifiveの独自イベント&quot;h5trackstart&quot;,
      ///  &quot;h5trackmove&quot;, &quot;h5trackend&quot;のためのバインドオブジェクトを返します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <param  name = "selector" type = "String" >
      ///  セレクタ
      /// </param>
      /// <param  name = "eventName" type = "String" >
      ///  イベント名
      /// </param>
      /// <param  name = "func" type = "Function" >
      ///  ハンドラとして登録したい関数
      /// </param>
      /// <returns  type = "Object[]" >
      ///  バインドオブジェクト
      ///  
      ///  bindObj.controller コントローラ
      ///  bindObj.selector セレクタ
      ///  bindObj.eventName イベント名
      ///  bindObj.handler イベントハンドラ
      ///  
      /// </returns>
      // タッチイベントがあるかどうか
      var hasTouchEvent = typeof document.ontouchstart !== TYPE_OF_UNDEFINED;
      if (eventName !== EVENT_NAME_H5_TRACKSTART) {
        if (hasTouchEvent) {
          return getNormalBindObj(controller, selector, eventName, func);
        }
        // イベントオブジェクトの正規化
        return getNormalBindObj(controller, selector, eventName, function(context) {
          var event = context.event;
          var offset = $(event.currentTarget).offset();
          event.offsetX = event.pageX - offset.left;
          event.offsetY = event.pageY - offset.top;
          func.apply(this, arguments);
        });
      }
      var getEventType = function(en) {
        switch (en) {
          case 'touchstart':
          case 'mousedown':
            return EVENT_NAME_H5_TRACKSTART;
          case 'touchmove':
          case 'mousemove':
            return EVENT_NAME_H5_TRACKMOVE;
          case 'touchend':
          case 'mouseup':
            return EVENT_NAME_H5_TRACKEND;
          default:
            return;
        }
      };
      // jQuery.Eventオブジェクトのプロパティをコピーする。
      // 1.6.xの場合, "liveFired"というプロパティがあるがこれをコピーしてしまうとtriggerしてもイベントが発火しない。
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
        // h5trackendイベントの最後でハンドラの除去を行う関数を格納するための変数
        var removeHandlers = null;
        var execute = false;
        var getHandler = function(en, eventTarget, setup) {
          return function(var_args) {
            var type = getEventType(en);
            var isStart = type === EVENT_NAME_H5_TRACKSTART;
            if (isStart && execute) {
              return;
            }
            var eventContext = createEventContext(controller, arguments);
            var event = eventContext.event;
            if (hasTouchEvent) {
              // タッチイベントの場合、イベントオブジェクトに座標系のプロパティを付加
              initTouchEventObject(event, en);
            }
            var newEvent = new $.Event(type);
            copyEventObject(event, newEvent);
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
                // 直前のh5track系イベントとの位置の差分を格納
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
                var $bindTarget = hasTouchEvent ? $(nt) : $document;
                removeHandlers = function() {
                  $bindTarget.unbind(move, moveHandler);
                  $bindTarget.unbind(end, upHandler);
                };
                $bindTarget.bind(move, moveHandler);
                $bindTarget.bind(end, upHandler);
              } else {
                execute = false;
              }
            }
            if (type === EVENT_NAME_H5_TRACKEND) {
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
        var bindObjects = [getNormalBindObj(controller, selector, eventName, func)];
        bindObjects.push(createBindObj(start));
        return bindObjects;
      };
      return getBindObjects();
    }
    function initTouchEventObject(event, eventName) {
      /// <summary>
      ///  タッチイベントのイベントオブジェクトにpageXやoffsetXといった座標系のプロパティを追加します。
      /// </summary>
      /// <param  name = "event" type = "Object" >
      ///  jQuery.Eventオブジェクト
      /// </param>
      /// <param  name = "eventName" type = "String" >
      ///  イベント名
      /// </param>
      var originalEvent = event.originalEvent;
      var touches = eventName === 'touchend' || eventName === 'touchcancel' ? originalEvent.changedTouches[0] : originalEvent.touches[0];
      var pageX = touches.pageX;
      var pageY = touches.pageY;
      event.pageX = originalEvent.pageX = pageX;
      event.pageY = originalEvent.pageY = pageY;
      event.screenX = originalEvent.screenX = touches.screenX;
      event.screenY = originalEvent.screenY = touches.screenY;
      event.clientX = originalEvent.clientX = touches.clientX;
      event.clientY = originalEvent.clientY = touches.clientY;
      var target = event.target;
      if (target.ownerSVGElement) {
        target = target.farthestViewportElement;
      } else if (target === window || target === document) {
        target = document.body;
      }
      var offset = $(target).offset();
      if (offset) {
        var offsetX = pageX - offset.left;
        var offsetY = pageY - offset.top;
        event.offsetX = originalEvent.offsetX = offsetX;
        event.offsetY = originalEvent.offsetY = offsetY;
      }
    }
    function normalizeEventObjext(event) {
      /// <summary>
      ///  イベントオブジェクトを正規化します。
      /// </summary>
      /// <param  name = "event" type = "Object" >
      ///  jQuery.Eventオブジェクト
      /// </param>
      // ここはnull, undefinedの場合にtrueとしたいため、あえて厳密等価を使用していない
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
    }
    function createEventContext(controller, args) {
      /// <summary>
      ///  イベントコンテキストを作成します。
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
      normalizeEventObjext(event);
      return {
        controller: controller,
        rootElement: controller.rootElement,
        event: event,
        evArg: evArg
      };
    }
    function createInitializationContext(rootController) {
      /// <summary>
      ///  初期化イベントコンテキストをセットアップします。
      /// </summary>
      /// <param  name = "rootController" type = "Object" >
      ///  ルートコントローラ
      /// </param>
      return {
        args: rootController.__controllerContext.args
      };
    }
    function unbindRootElement(controller) {
      /// <summary>
      ///  コントローラとその子孫コントローラのrootElementにnullをセットします。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      for (var prop in controller) {
        var c = controller[prop];
        if (isChildController(controller, prop)) {
          c.rootElement = null;
          c.view.__controller = null;
          unbindRootElement(c);
        }
      }
    }
    function copyAndSetRootElement(controller) {
      /// <summary>
      ///  コントローラとｓの子孫コントローラのrootElementをセットします。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      var rootElement = controller.rootElement;
      var meta = controller.__meta;
      for (var prop in controller) {
        var c = controller[prop];
        if (isChildController(controller, prop)) {
          // __metaが指定されている場合、__metaのrootElementを考慮した要素を取得する
          if (meta && meta[prop] && meta[prop].rootElement) {
            c.rootElement = getBindTarget(meta[prop].rootElement, rootElement, c);
          } else {
            c.rootElement = rootElement;
          }
          c.view.__controller = c;
          copyAndSetRootElement(c);
        }
      }
    }
    function getBindTarget(element, rootElement, controller) {
      /// <summary>
      ///  コントローラをバインドする対象となる要素を返します。
      /// </summary>
      /// <param  name = "element" type = "String|DOM|jQuery" >
      ///  セレクタ、DOM要素、もしくはjQueryオブジェクト
      /// </param>
      /// <param  name = "[rootElement]" type = "DOM" >
      ///  ルートエレメント
      /// </param>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <returns  type = "DOM" >
      ///  コントローラのバインド対象である要素
      /// </returns>
      if (!element) {
        throwFwError(ERR_CODE_BIND_TARGET_REQUIRED);
      } else if (!controller) {
        throwFwError(ERR_CODE_BIND_NOT_CONTROLLER);
      }
      var $targets;
      if (rootElement) {
        $targets = getTarget(element, rootElement);
      } else {
        $targets = $(element);
      }
      if ($targets.length === 0) {
        throwFwError(ERR_CODE_BIND_NOT_TARGET, [controller.__name]);
      }
      if ($targets.length > 1) {
        throwFwError(ERR_CODE_BIND_TARGET_COMPLEX, [controller.__name]);
      }
      return $targets.get(0);
    }
    function bindAndTriggerReady(controller) {
      /// <summary>
      ///  イベントハンドラのバインドと__readyイベントを実行します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      bindByBindMap(controller);
      bindDescendantHandlers(controller);
      // コントローラマネージャの管理対象に追加
      var controllers = h5.core.controllerManager.controllers;
      if ($.inArray(controller, controllers) === -1) {
        controllers.push(controller);
      }
      // h5controllerboundイベントをトリガ.
      $(controller.rootElement).trigger('h5controllerbound', [controller]);
      // コントローラの__ready処理を実行
      var initPromises = getDescendantControllerPromises(controller, 'initPromise');
      initPromises.push(controller.initPromise);
      $.when.apply($, initPromises).done(function() {
        executeLifecycleEventChain(controller, false);
      });
    }
    function setRootAndTriggerInit(controller) {
      /// <summary>
      ///  rootController,
      ///  parentControllerのセットと__initイベントを実行します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      if (controller.rootController === null) {
        // rootControllerとparentControllerのセット
        initRootAndParentController(controller);
      }
      copyAndSetRootElement(controller);
      // __initイベントの実行
      executeLifecycleEventChain(controller, true);
    }
    function initInternalProperty(controller, param) {
      /// <summary>
      ///  h5.core.bindController()のために必要なプロパティをコントローラに追加します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <param  name = "param" type = "Object" >
      ///  初期化パラメータ
      /// </param>
      var templateDfd = getDeferred();
      templateDfd.resolve();
      controller.__controllerContext.templatePromise = templateDfd.promise();
      controller.__controllerContext.initDfd = getDeferred();
      controller.initPromise = controller.__controllerContext.initDfd.promise();
      controller.__controllerContext.readyDfd = getDeferred();
      controller.readyPromise = controller.__controllerContext.readyDfd.promise();
      controller.isInit = false;
      controller.isReady = false;
      controller.__controllerContext.args = param;
      for (var prop in controller) {
        if (isChildController(controller, prop)) {
          initInternalProperty(controller[prop]);
        }
      }
    }
    function callIndicator(controller, option) {
      /// <summary>
      ///  インジケータを呼び出します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <param  name = "option" type = "Object" >
      ///  インジケータのオプション
      /// </param>
      var target = null;
      var opt = option;
      if ($.isPlainObject(opt)) {
        target = opt.target;
      } else {
        opt = {};
      }
      target = target ? getTarget(target, controller.rootElement, true) : controller.rootElement;
      return h5.ui.indicator.call(controller, target, opt);
    }
    function executeLifeEndChain(controller, property) {
      /// <summary>
      ///  __unbind,
      ///  __disposeイベントを実行します。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      /// <param  name = "property" type = "String" >
      ///  プロパティ名(__unbind | __dispose)
      /// </param>
      /// <returns  type = "Promise[]" >
      ///  Promiseオブジェクト
      /// </returns>
      var promises = [];
      var targets = [];
      var execute = function(parentController) {
        targets.push(parentController);
        for (var prop in parentController) {
          if (isChildController(parentController, prop)) {
            var c = parentController[prop];
            if ($.inArray(c, targets) === -1) {
              execute(c);
            }
          }
        }
        if (parentController[property] && $.isFunction(parentController[property])) {
          promises.push(parentController[property]());
        }
      };
      execute(controller);
      return promises;
    }
    function disposeController(controller) {
      /// <summary>
      ///  コントローラのリソース解放処理を行います。
      /// </summary>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      var targets = [];
      var dispose = function(parentController) {
        targets.push(parentController);
        if (getByPath('h5.core.view')) {
          parentController.view.clear();
        }
        for (var prop in parentController) {
          if (isChildController(parentController, prop)) {
            var c = parentController[prop];
            if ($.inArray(c, targets) === -1) {
              dispose(c);
            }
          }
          parentController[prop] = null;
        }
      };
      dispose(controller);
    }
    function getView(templateId, controller) {
      /// <summary>
      ///  指定されたIDを持つViewインスタンスを返します。
      ///  自身が持つViewインスタンスが指定されたIDを持っていない場合、parentControllerのViewインスタンスに対して
      ///  持っているかどうか問い合わせ、持っていればそのインスタンスを、持っていなければ更に上に問い合わせます。
      ///  ルートコントローラのViewインスタンスも持っていない場合、h5.core.viewに格納された最上位のViewインスタンスを返します。
      /// </summary>
      /// <param  name = "templateId" type = "String" >
      ///  テンプレートID
      /// </param>
      /// <param  name = "controller" type = "Controller" >
      ///  コントローラ
      /// </param>
      if (controller.view.__view.isAvailable(templateId)) {
        return controller.view.__view;
      } else if (controller.parentController) {
        return getView(templateId, controller.parentController);
      }
      return h5.core.view;
    }
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    function controllerFactory(controller, rootElement, controllerName, param, isRoot) {
      controller.__name = controllerName;
      controller.__templates = null;
      controller.rootElement = rootElement;
      controller.__controllerContext = {
        executeListeners: true,
        isRoot: isRoot,
        bindMap: {},
        unbindMap: {}
      };
      // 初期化パラメータがあれば、クローンしてコントローラコンテキストに格納
      if (param) {
        controller.__controllerContext.args = $.extend(true, {}, param);
      }
      controller.isInit = false;
      controller.isReady = false;
      controller.rootController = null;
      controller.parentController = null;
      controller.initPromise = null;
      controller.readyPromise = null;
      controller.log = h5.log.createLogger(controllerName);
      controller.view = new View(controller);
    }
    function View(controller) {
      // 利便性のために循環参照になってしまうがコントローラの参照を持つ
      this.__controller = controller;
      // Viewモジュールがなければインスタンスを作成しない(できない)
      if (getByPath('h5.core.view')) {
        this.__view = h5.core.view.createView();
      }
    }
    $.extend(View.prototype, {
      get: function(templateId, param) {
        /// <summary>
        ///  パラメータで置換された、指定されたテンプレートIDのテンプレートを取得します。
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
        return getView(templateId, this.__controller).get(templateId, param);
      },
      update: function(element, templateId, param) {
        /// <summary>
        ///  要素を指定されたIDのテンプレートで書き換えます。
        /// </summary>
        /// <param  name = "element" type = "String|Element|jQuery" >
        ///  DOM要素(セレクタ文字列, DOM要素, jQueryオブジェクト)
        /// </param>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <param  name = "[param]" type = "Object" >
        ///  パラメータ(オブジェクトリテラルで指定)
        /// </param>
        var target = getTarget(element, this.__controller.rootElement, true);
        getView(templateId, this.__controller).update(target, templateId, param);
      },
      append: function(element, templateId, param) {
        /// <summary>
        ///  要素の末尾に指定されたIDのテンプレートを挿入します。
        /// </summary>
        /// <param  name = "element" type = "String|Element|jQuery" >
        ///  DOM要素(セレクタ文字列, DOM要素, jQueryオブジェクト)
        /// </param>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <param  name = "[param]" type = "Object" >
        ///  パラメータ(オブジェクトリテラルで指定)
        /// </param>
        var target = getTarget(element, this.__controller.rootElement, true);
        getView(templateId, this.__controller).append(target, templateId, param);
      },
      prepend: function(element, templateId, param) {
        /// <summary>
        ///  要素の先頭に指定されたIDのテンプレートを挿入します。
        /// </summary>
        /// <param  name = "element" type = "String|Element|jQuery" >
        ///  DOM要素(セレクタ文字列, DOM要素, jQueryオブジェクト)
        /// </param>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <param  name = "[param]" type = "Object" >
        ///  パラメータ(オブジェクトリテラルで指定)
        /// </param>
        var target = getTarget(element, this.__controller.rootElement, true);
        getView(templateId, this.__controller).prepend(target, templateId, param);
      },
      load: function(resourcePaths) {
        /// <summary>
        ///  指定されたパスのテンプレートファイルを非同期で読み込みキャッシュします。
        /// </summary>
        /// <param  name = "resourcePaths" type = "String|String[]" >
        ///  テンプレートファイル(.ejs)のパス (配列で複数指定可能)
        /// </param>
        /// <returns  type = "Promise" >
        ///  Promiseオブジェクト
        /// </returns>
        return this.__view.load(resourcePaths);
      },
      register: function(templateId, templateString) {
        /// <summary>
        ///  Viewインスタンスに、指定されたIDとテンプレート文字列からテンプレートを1件登録します。
        /// </summary>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <param  name = "templateString" type = "String" >
        ///  テンプレート文字列
        /// </param>
        this.__view.register(templateId, templateString);
      },
      isValid: function(templateString) {
        /// <summary>
        ///  テンプレート文字列が、コンパイルできるかどうかを返します。
        /// </summary>
        /// <param  name = "templateString" type = "String" >
        ///  テンプレート文字列
        /// </param>
        /// <returns  type = "Boolean" >
        ///  渡されたテンプレート文字列がコンパイル可能かどうか。
        /// </returns>
        return this.__view.isValid(templateString);
      },
      isAvailable: function(templateId) {
        /// <summary>
        ///  指定されたテンプレートIDのテンプレートが存在するか判定します。
        /// </summary>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <returns  type = "Boolean" >
        ///  判定結果(存在する: true 存在しない: false)
        /// </returns>
        return getView(templateId, this.__controller).isAvailable(templateId);
      },
      clear: function(templateIds) {
        /// <summary>
        ///  引数に指定されたテンプレートIDをもつテンプレートをキャッシュから削除します。
        ///  
        ///  引数を指定しない場合はキャッシュされている全てのテンプレートを削除します。
        /// </summary>
        /// <param  name = "[templateId]" type = "String|String[]" >
        ///  テンプレートID
        /// </param>
        this.__view.clear(templateIds);
      }
    });
    function Controller(rootElement, controllerName, param, isRoot) {
      /// <summary>
      ///  コントローラのコンストラクタ
      /// </summary>
      /// <param  name = "rootElement" type = "Element" >
      ///  コントローラをバインドした要素
      /// </param>
      /// <param  name = "controllerName" type = "String" >
      ///  コントローラ名
      /// </param>
      /// <param  name = "param" type = "Object" >
      ///  初期化パラメータ
      /// </param>
      /// <param  name = "isRoot" type = "Boolean" >
      ///  ルートコントローラかどうか
      /// </param>
      return controllerFactory(this, rootElement, controllerName, param, isRoot);
    }
    $.extend(Controller.prototype, {
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
        return function()           /* var_args */
{
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
        return function()           /* var_args */
{
          var args = h5.u.obj.argsToArray(arguments);
          args.unshift(this);
          func.apply(that, args);
        };
      },
      bind: function(targetElement, param) {
        /// <summary>
        ///  コントローラを要素へバインドします。
        /// </summary>
        /// <param  name = "targetElement" type = "String|Element|jQuery" >
        ///  バインド対象とする要素のセレクタ、DOMエレメント、もしくはjQueryオブジェクト.
        ///  セレクタで指定したときにバインド対象となる要素が存在しない、もしくは2つ以上存在する場合、エラーとなります。
        /// </param>
        /// <param  name = "[param]" type = "Object" >
        ///  初期化パラメータ.
        ///  初期化パラメータは __init, __readyの引数として渡されるオブジェクトの argsプロパティとして格納されます。
        /// </param>
        /// <returns  type = "Controller" >
        ///  コントローラ.
        /// </returns>
        var target = getBindTarget(targetElement, null, this);
        this.rootElement = target;
        this.view.__controller = this;
        var args = null;
        if (param) {
          args = $.extend(true, {}, param);
        }
        initInternalProperty(this, args);
        setRootAndTriggerInit(this);
        return this;
      },
      unbind: function() {
        /// <summary>
        ///  コントローラのバインドを解除します。
        /// </summary>
        executeLifeEndChain(this, '__unbind');
        unbindByBindMap(this);
        unbindDescendantHandlers(this);
        this.__controllerContext.unbindMap = {};
        // コントローラマネージャの管理対象から外す.
        var targetRootElement = this.rootElement;
        var controllers = h5.core.controllerManager.controllers;
        h5.core.controllerManager.controllers = $.grep(controllers, function(controllerInstance) {
          return controllerInstance.rootElement !== targetRootElement;
        });
        // h5controllerunboundイベントをトリガ
        $(this.rootElement).trigger('h5controllerunbound');
        // rootElemetnのアンバインド
        this.rootElement = null;
        this.view.__controller = null;
        unbindRootElement(this);
      },
      dispose: function() {
        /// <summary>
        ///  コントローラのリソースをすべて削除します。&lt;br
        ///  /&gt;
        ///  Controller#unbind() の処理を包含しています。
        /// </summary>
        /// <returns  type = "Promise" >
        ///  Promiseオブジェクト
        /// </returns>
        var dfd = this.deferred();
        this.unbind();
        var that = this;
        var promises = executeLifeEndChain(this, '__dispose');
        $.when.apply($, promises).done(function() {
          disposeController(that);
          dfd.resolve();
        });
        return dfd.promise();
      },
      triggerIndicator: function(opt, evName) {
        /// <summary>
        ///  コントローラのインジケータイベントを実行します。
        /// </summary>
        /// <param  name = "opt" type = "Object" >
        ///  オプション
        /// </param>
        /// <param  name = "[opt.message]" type = "String" >
        ///  メッセージ
        /// </param>
        /// <param  name = "[opt.percent]" type = "Number" >
        ///  進捗を0～100の値で指定する。
        /// </param>
        /// <param  name = "[opt.block]" type = "Boolean" >
        ///  操作できないよう画面をブロックするか (true:する/false:しない)
        /// </param>
        /// <param  name = "ev" type = "String" >
        ///  イベント名
        /// </param>
        /// <returns  type = "Indicator" >
        ///  インジケータオブジェクト
        /// </returns>
        var option = $.extend(true, {}, opt);
        var ev = evName;
        if (!ev || ev.length === 0) {
          ev = 'triggerIndicator';
        }
        $(this.rootElement).trigger(ev, [option]);
        return option.indicator;
      },
      indicator: function(opt) {
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
        /// <param  name = "[opt]" type = "Object" >
        /// </param>
        /// <param  name = "[opt.message]" type = "String" >
        ///  メッセージ
        /// </param>
        /// <param  name = "[opt.percent]" type = "Number" >
        ///  進捗を0～100の値で指定する。
        /// </param>
        /// <param  name = "[opt.block]" type = "Boolean" >
        ///  操作できないよう画面をブロックするか (true:する/false:しない)
        /// </param>
        /// <param  name = "[opt.promises]" type = "Promise|Promise[]" >
        ///  Promiseオブジェクト (Promiseの状態と合わせてインジケータの表示・非表示する)
        /// </param>
        /// <param  name = "[opt.theme]" type = "String" >
        ///  インジケータの基点となるクラス名 (CSSでテーマごとにスタイルをする場合に使用する)
        /// </param>
        /// <returns  type = "Indicator" >
        ///  インジケータオブジェクト
        /// </returns>
        return callIndicator(this, opt);
      },
      enableListeners: function() {
        /// <summary>
        ///  コントローラに定義されているリスナーの実行を許可します。
        /// </summary>
        setExecuteListenersFlag(this, true);
      },
      disableListeners: function() {
        /// <summary>
        ///  コントローラに定義されているリスナーの実行を禁止します。
        /// </summary>
        setExecuteListenersFlag(this, false);
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
        // null, undefinedの場合をtrueとしたいため、あえて厳密等価にしていない
        if (customType == null) {
          throwFwError(ERR_CODE_CUSTOM_ERROR_TYPE_REQUIRED);
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
      }
    });
    function ControllerManager() {
      /// <summary>
      ///  コントローラマネージャクラス
      /// </summary>
      this.rootElement = document;
      this.controllers = [];
      $(document).bind('triggerIndicator', function(event, opt) {
        /// <summary>
        ///  triggerIndicatorイベントハンドラ
        /// </summary>
        /// <param  name = "context" type = "EventContext" >
        /// </param>
        opt.indicator = callIndicator(this, opt).show();
        event.stopPropagation();
      });
    }
    $.extend(ControllerManager.prototype, {
      getAllControllers: function() {
        /// <summary>
        ///  すべてのコントローラのインスタンスの配列を返します。
        /// </summary>
        /// <returns  type = "Controller[]" >
        ///  コントローラ配列
        /// </returns>
        return this.controllers;
      },
      getController: function(rootElement) {
        /// <summary>
        ///  指定した要素にバインドされているコントローラを返します。
        /// </summary>
        /// <param  name = "rootElement" type = "String|Element|jQuery" >
        ///  要素
        /// </param>
        /// <returns  type = "Controller" >
        ///  コントローラ
        /// </returns>
        var target = $(rootElement).get(0);
        var controllers = this.controllers;
        for (var i = 0, len = controllers.length; i < len; i++) {
          if (target === controllers[i].rootElement) {
            return controllers[i];
          }
        }
      }
    });
    h5.u.obj.expose('h5.core', {
      controllerManager: new ControllerManager()
    });
    // プロパティ重複チェック用のコントローラプロパティマップ
    var controllerPropertyMap = {};
    var c = new Controller(null, 'a');
    for (var p in c) {
      if (c.hasOwnProperty(p) && p !== '__name' && p !== '__templates' && p !== '__meta') {
        controllerPropertyMap[p] = 1;
      }
    }
    var proto = Controller.prototype;
    for (var p in proto) {
      if (proto.hasOwnProperty(p)) {
        controllerPropertyMap[p] = 1;
      }
    }
      // fwOptは内部的に使用している.
    function createAndBindController(targetElement, controllerDefObj, param, fwOpt) {
      /// <summary>
      ///  コントローラのファクトリ
      /// </summary>
      /// <param  name = "targetElement" type = "String|Element|jQuery" >
      ///  バインド対象とする要素のセレクタ、DOMエレメント、もしくはjQueryオブジェクト.
      /// </param>
      /// <param  name = "controllerDefObj" type = "Object" >
      ///  コントローラ定義オブジェクト
      /// </param>
      /// <param  name = "[param]" type = "Object" >
      ///  初期化パラメータ.
      /// </param>
      // コントローラ名
      var controllerName = controllerDefObj.__name;
      if (!controllerName || $.trim(controllerName).length === 0) {
        throwFwError(ERR_CODE_CONTROLLER_NAME_REQUIRED, null, {
          controllerDefObj: controllerDefObj
        });
      }
      // 初期化パラメータがオブジェクトかどうかチェック
      if (param && !$.isPlainObject(param)) {
        throwFwError(ERR_CODE_CONTROLLER_INVALID_INIT_PARAM, [controllerName], {
          controllerDefObj: controllerDefObj
        });
      }
      // 既にコントローラ化されているかどうかチェック
      if (controllerDefObj.__controllerContext) {
        throwFwError(ERR_CODE_CONTROLLER_ALREADY_CREATED, null, {
          controllerDefObj: controllerDefObj
        });
      }
      // バインド対象となる要素のチェック
      if (targetElement) {
        var $bindTargetElement = $(targetElement);
        if ($bindTargetElement.length === 0) {
          throwFwError(ERR_CODE_BIND_NOT_TARGET, [controllerName], {
            controllerDefObj: controllerDefObj
          });
        }
        if ($bindTargetElement.length > 1) {
          throwFwError(ERR_CODE_BIND_TARGET_COMPLEX, [controllerName], {
            controllerDefObj: controllerDefObj
          });
        }
      }
      // コントローラの循環参照チェック
      if (checkControllerCircularRef(controllerDefObj)) {
        throwFwError(ERR_CODE_CONTROLLER_CIRCULAR_REF, [controllerName], {
          controllerDefObj: controllerDefObj
        });
      }
      // ロジックの循環参照チェック
      if (checkLogicCircularRef(controllerDefObj)) {
        throwFwError(ERR_CODE_LOGIC_CIRCULAR_REF, [controllerName], {
          controllerDefObj: controllerDefObj
        });
      }
      var isRoot = !fwOpt || !fwOpt.isInternal;
      var clonedControllerDef = $.extend(true, {}, controllerDefObj);
      var controller = new Controller(targetElement ? $(targetElement).get(0) : null, controllerName, param, isRoot);
      var templates = controllerDefObj.__templates;
      var templateDfd = getDeferred();
      var templatePromise = templateDfd.promise();
      controller.__controllerContext.templatePromise = templatePromise;
      controller.__controllerContext.initDfd = getDeferred();
      controller.initPromise = controller.__controllerContext.initDfd.promise();
      controller.__controllerContext.readyDfd = getDeferred();
      controller.readyPromise = controller.__controllerContext.readyDfd.promise();
      if (templates && templates.length > 0) {
        // テンプレートがあればロード
        var viewLoad = function(count) {
          // Viewモジュールがなければエラーログを出力する。
          // この直後のloadでエラーになるはず。
          if (!getByPath('h5.core.view')) {
          }
          var vp = controller.view.load(templates);
          vp.then(function(result) {
            /* del begin */
            if (templates && templates.length > 0) {
            }
            /* del end */
            templateDfd.resolve();
          }, function(result) {
            // テンプレートのロードをリトライする条件は、リトライ回数が上限回数未満、かつ
            // jqXhr.statusが"0"、もしくは"12029"であること。
            // jqXhr.statusの値の根拠は、IE以外のブラウザだと通信エラーの時に"0"になっていること、
            // IEの場合は、コネクションが繋がらない時のコードが"12029"であること。
            // 12000番台すべてをリトライ対象としていないのは、何度リトライしても成功しないエラーが含まれていることが理由。
            // WinInet のエラーコード(12001 - 12156): http://support.microsoft.com/kb/193625/ja
            var jqXhrStatus = result.detail.error.status;
            if (count === TEMPLATE_LOAD_RETRY_COUNT || jqXhrStatus !== 0 || jqXhrStatus !== 12029) {
              result.controllerDefObject = controllerDefObj;
              templateDfd.reject(result);
              // controller.__controllerContext.initDfd.reject();
              return;
            }
            setTimeout(function() {
              viewLoad(++count);
            }, TEMPLATE_LOAD_RETRY_INTERVAL);
          });
        };
        viewLoad(0);
      } else {
        // テンプレートがない場合は、resolve()しておく
        templateDfd.resolve();
      }
      for (var prop in clonedControllerDef) {
        if (controllerPropertyMap[prop]) {
          throwFwError(ERR_CODE_CONTROLLER_SAME_PROPERTY, [controllerName, prop], {
            controllerDefObj: controllerDefObj
          });
        } else if (isLifecycleProperty(clonedControllerDef, prop)) {
          // ライフサイクルイベント
          controller[prop] = weaveControllerAspect(clonedControllerDef, prop);
        } else if (isEventHandler(clonedControllerDef, prop)) {
          // イベントハンドラ
          var lastIndex = $.trim(prop).lastIndexOf(' ');
          var selector = $.trim(prop.substring(0, lastIndex));
          var eventName = $.trim(prop.substring(lastIndex + 1, prop.length));
          if (isBindRequested(eventName)) {
            eventName = '[' + $.trim(trimBindEventBracket(eventName)) + ']';
          }
          if (isGlobalSelector(selector)) {
            var selectTarget = trimGlobalSelectorBracket(selector);
            if (selectTarget === 'this') {
              throwFwError(ERR_CODE_EVENT_HANDLER_SELECTOR_THIS, [controllerName], {
                controllerDefObj: controllerDefObj
              });
            }
          }
          var bindMap = controller.__controllerContext.bindMap;
          if (!bindMap[selector]) {
            bindMap[selector] = {};
          }
          if (bindMap[selector][eventName]) {
            throwFwError(ERR_CODE_SAME_EVENT_HANDLER, [controllerName, selector, eventName], {
              controllerDefObj: controllerDefObj
            });
          }
          var weavedFunc = weaveControllerAspect(clonedControllerDef, prop, true);
          bindMap[selector][eventName] = weavedFunc;
          controller[prop] = weavedFunc;
        } else if (endsWith(prop, SUFFIX_CONTROLLER) && clonedControllerDef[prop] && !$.isFunction(clonedControllerDef[prop])) {
          // 子コントローラ
          var controllerTarget = clonedControllerDef[prop];
          if (!controllerTarget) {
            controller[prop] = controllerTarget;
            continue;
          }
          var c = createAndBindController(null, $.extend(true, {}, clonedControllerDef[prop]), param, $.extend({
            isInternal: true
          }, fwOpt));
          controller[prop] = c;
        } else if (endsWith(prop, SUFFIX_LOGIC) && clonedControllerDef[prop] && !$.isFunction(clonedControllerDef[prop])) {
          // ロジック
          var logicTarget = clonedControllerDef[prop];
          var logic = createLogic(logicTarget);
          controller[prop] = logic;
        } else if ($.isFunction(clonedControllerDef[prop])) {
          // イベントハンドラではないメソッド
          controller[prop] = weaveControllerAspect(clonedControllerDef, prop);
        } else {
          // その他プロパティ
          controller[prop] = clonedControllerDef[prop];
        }
      }
      // __metaのチェック
      var meta = controller.__meta;
      if (meta) {
        for (var prop in meta) {
          var c = controller[prop];
          if (c === undefined) {
            throwFwError(ERR_CODE_CONTROLLER_META_KEY_INVALID, [controllerName, prop], {
              controllerDefObj: controllerDefObj
            });
          }
          if (c === null) {
            throwFwError(ERR_CODE_CONTROLLER_META_KEY_NULL, [controllerName, prop], {
              controllerDefObj: controllerDefObj
            });
          }
          if (Controller.prototype.constructor !== c.constructor) {
            throwFwError(ERR_CODE_CONTROLLER_META_KEY_NOT_CONTROLLER, [controllerName, prop], {
              controllerDefObj: controllerDefObj
            });
          }
        }
      }
      // __constructがあれば実行。ここまでは完全に同期処理になる。
      if (controller.__construct) {
        controller.__construct(createInitializationContext(controller));
      }
      // ルートコントローラではない場合、インスタンスを戻す
      if (!controller.__controllerContext.isRoot) {
        return controller;
      }
      setRootAndTriggerInit(controller);
      return controller;
    }
    function createLogic(logicDefObj) {
      /// <summary>
      ///  オブジェクトのロジック化を行います。
      /// </summary>
      /// <param  name = "logicDefObj" type = "Object" >
      ///  ロジック定義オブジェクト
      /// </param>
      /// <returns  type = "Logic" >
      /// </returns>
      var logicName = logicDefObj.__name;
      if (!logicName || $.trim(logicName.length) === 0) {
        throwFwError(ERR_CODE_LOGIC_NAME_REQUIRED, null, {
          logicDefObj: logicDefObj
        });
      }
      if (logicDefObj.__logicContext) {
        throwFwError(ERR_CODE_LOGIC_ALREADY_CREATED, null, {
          logicDefObj: logicDefObj
        });
      }
      var logic = weaveLogicAspect($.extend(true, {}, logicDefObj));
      logic.deferred = getDeferred;
      logic.log = h5.log.createLogger(logicName);
      logic.__logicContext = {};
      for (var prop in logic) {
        if (logic.hasOwnProperty(prop) && endsWith(prop, SUFFIX_LOGIC)) {
          var target = logic[prop];
          logic[prop] = createLogic(target);
        }
      }
      return logic;
    }
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.core', {
      controller: createAndBindController,
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
          throwFwError(ERR_CODE_EXPOSE_NAME_REQUIRED, null, {
            target: obj
          });
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
  })();
  /* ------ h5.core.view ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    var ERR_CODE_TEMPLATE_COMPILE = 7000;
    var ERR_CODE_TEMPLATE_FILE = 7001;
    var ERR_CODE_TEMPLATE_INVALID_ID = 7002;
    var ERR_CODE_TEMPLATE_AJAX = 7003;
    var ERR_CODE_INVALID_FILE_PATH = 7004;
    var ERR_CODE_TEMPLATE_ID_UNAVAILABLE = 7005;
    var ERR_CODE_TEMPLATE_PROPATY_UNDEFINED = 7006;
    var errMsgMap = {};
    errMsgMap[ERR_CODE_TEMPLATE_COMPILE] = 'テンプレートをコンパイルできませんでした。{0}';
    errMsgMap[ERR_CODE_TEMPLATE_FILE] = 'テンプレートファイルが不正です。{0}';
    errMsgMap[ERR_CODE_TEMPLATE_INVALID_ID] = 'テンプレートIDが指定されていません';
    errMsgMap[ERR_CODE_TEMPLATE_AJAX] = 'テンプレートファイルを取得できませんでした。';
    errMsgMap[ERR_CODE_INVALID_FILE_PATH] = 'テンプレートファイルが指定されていません。';
    errMsgMap[ERR_CODE_TEMPLATE_ID_UNAVAILABLE] = 'テンプレートID:{0} テンプレートがありません。';
    errMsgMap[ERR_CODE_TEMPLATE_PROPATY_UNDEFINED] = '{0} テンプレートにパラメータが設定されていません。';
    // メッセージの登録
    addFwErrorCodeMap(errMsgMap);
    var ERR_REASON_TEMPLATE_IS_NOT_STRING = 'テンプレートには文字列を指定してください';
    var ERR_REASON_SCRIPT_ELEMENT_IS_NOT_EXIST = 'scriptタグが見つかりません。テンプレート文字列はscriptタグで囲って記述して下さい。';
    var ERR_REASON_SYNTAX_ERR = '構文エラー {0}{1}';
    var DELIMITER = '[';
    // =============================
    // Development Only
    // =============================
    var fwLogger = h5.log.createLogger('h5.core.view');
    /* del begin */
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    var getDeferred = h5.async.deferred;
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    var helperExtras = {
      escapeHtml: function(str) {
        /// <summary>
        ///  HTML文字列をエスケープします。
        /// </summary>
        /// <param  name = "str" type = "String" >
        ///  エスケープ対象文字列
        /// </param>
        /// <returns  type = "String" >
        ///  エスケープされた文字列
        /// </returns>
      /// <summary>
      /// </summary>
        return h5.u.str.escapeHtml(str);
      }
    };
    var cacheManager = {
      MAX_CACHE: 10,
      cache: {},
      cacheUrls: [],
      accessingUrls: [],
      append: function(url, compiled, path) {
        /// <summary>
        ///  コンパイル済みテンプレートオブジェクトをキャッシュします。
        /// </summary>
        /// <param  name = "url" type = "String" >
        ///  URL(絶対パス)
        /// </param>
        /// <param  name = "compiled" type = "Object" >
        ///  コンパイル済みテンプレートオブジェクト
        /// </param>
        /// <param  name = "[path]" type = "String" >
        ///  相対パス
        /// </param>
      /// <summary>
      /// </summary>
        if (this.cacheUrls.length >= this.MAX_CACHE) {
          this.deleteCache(this.cacheUrls[0]);
        }
        this.cache[url] = {};
        this.cache[url].templates = compiled;
        this.cache[url].path = path;
        this.cacheUrls.push(url);
      },
      /* del begin */
      getCacheInfo: function() {
        /// <summary>
        ///  テンプレートのグローバルキャッシュが保持しているURL、指定された相対パス、テンプレートIDを持ったオブジェクトを返します。
        ///  この関数は開発版でのみ利用できます。
        /// </summary>
        /// <returns  type = "Array[Object]" >
        ///  グローバルキャッシュが保持しているテンプレート情報オブジェクトの配列。 [{path:(指定されたパス、相対パス),
        ///  absoluteUrl:(絶対パス), ids:(ファイルから取得したテンプレートのIDの配列)} ,...]
        /// </returns>
        var ret = [];
        for (var url in this.cache) {
          var obj = cache[url];
          var ids = [];
          for (var id in obj.templates) {
            ids.push(id);
          }
          ret.push({
            path: obj.path,
            absoluteUrl: url,
            ids: ids
          });
        }
        return ret;
      },
      /* del end */
      deleteCache: function(url, isOnlyUrls) {
        /// <summary>
        ///  指定されたURLのキャッシュを削除します。
        /// </summary>
        /// <param  name = "url" type = "String" >
        ///  URL
        /// </param>
        /// <param  name = "isOnlyUrls" type = "Boolean" >
        ///  trueを指定された場合、キャッシュは消さずに、キャッシュしているURLリストから引数に指定されたURLを削除します。
        /// </param>
        if (!isOnlyUrls) {
          delete this.cache[url];
        }
        for (var i = 0, len = this.cacheUrls.length; i < len; i++) {
          if (this.cacheUrls[i] === url) {
            this.cacheUrls.splice(i, 1);
            break;
          }
        }
      },
      getTemplateByUrls: function(resourcePaths) {
        /// <summary>
        ///  指定されたテンプレートパスからテンプレートを非同期で読み込みます。
        ///  テンプレートパスがキャッシュに存在する場合はキャッシュから読み込みます。
        /// </summary>
        /// <param  name = "resourcePaths" type = "Array[String]" >
        ///  テンプレートパス
        /// </param>
        /// <returns  type = "Object" >
        ///  Promiseオブジェクト
        /// </returns>
        var ret = {};
        var tasks = [];
        var datas = [];
        var that = this;
        var getTemplateByURL = function(url) {
          /// <summary>
          ///  キャッシュからテンプレートを取得します。
          /// </summary>
          /// <param  name = "url" type = "String" >
          ///  ファイルの絶対パス
          /// </param>
          /// <returns  type = "Object" >
          ///  テンプレートIDがkeyである、コンパイル済みテンプレートオブジェクトを持つオブジェクト
          /// </returns>
          var ret = that.cache[url].templates;
          that.deleteCache(url, true);
          that.cacheUrls.push(url);
          return ret;
        };
        function compileTemplatesByElements($templateElements) {
          /// <summary>
          ///  テンプレートをEJS用にコンパイルされたテンプレートに変換します。
          /// </summary>
          /// <param  name = "$templateElements" type = "jQuery" >
          ///  テンプレートが記述されている要素(...)
          /// </param>
          /// <returns  type = "Object" >
          ///  テンプレートIDがkeyである、コンパイル済みテンプレートオブジェクトを持つオブジェクトと、テンプレートを取得したファイルパスと絶対パス(URL)を保持するオブジェクト
          /// </returns>
          if ($templateElements.length === 0) {
            return;
          }
          var compiled = {};
          var ids = [];
          $templateElements.each(function() {
            var templateId = $.trim(this.id);
            var templateString = $.trim(this.innerHTML);
            if (templateId == null) {              // 空文字は許容する。
              throwFwError(ERR_CODE_TEMPLATE_INVALID_ID, null, {});
            }
            try {
              var compiledTemplate = new EJS.Compiler(templateString, DELIMITER);
              compiledTemplate.compile();
              compiled[templateId] = compiledTemplate.process;
              ids.push(templateId);
            }
            catch (e) {
              var lineNo = e.lineNumber;
              var msg = lineNo ? ' line:' + lineNo : '';
              throwFwError(ERR_CODE_TEMPLATE_COMPILE, [h5.u.str.format(ERR_REASON_SYNTAX_ERR, msg, e.message)], {
                id: templateId,
                error: e
              });
            }
          });
          return {
            compiled: compiled,
            data: {
              ids: ids
            }
          };
        }
                // キャッシュにあればそれを結果に格納し、なければajaxで取得する。
        for (var i = 0; i < resourcePaths.length; i++) {
          var path = resourcePaths[i];
          var absolutePath = toAbsoluteUrl(path);
          if (this.cache[absolutePath]) {
            $.extend(ret, getTemplateByURL(absolutePath));
            datas.push({
              absoluteUrl: absolutePath
            });
            continue;
          }
          tasks.push(path);
        }
        var df = getDeferred();
        function load(task, count) {
          var step = count || 0;
          if (task.length == step) {
            df.resolve();
            return;
          }
          var filePath = task[step];
          var absolutePath = toAbsoluteUrl(filePath);
          if (!that.accessingUrls[absolutePath]) {
            that.accessingUrls[absolutePath] = h5.ajax(filePath);
          }
          that.accessingUrls[absolutePath].then(function(result, statusText, obj) {
            delete that.accessingUrls[absolutePath];
            var templateText = obj.responseText;
            // IE8以下で、テンプレート要素内にSCRIPTタグが含まれていると、jQueryが</SCRIPT>をunknownElementとして扱ってしまうため、ここで除去する
            var $elements = $(templateText).filter(function() {
              // nodeType:8 コメントノード
              return (this.tagName && this.tagName.indexOf('/') === -1) && this.nodeType !== 8;
            });
            var filePath = this.url;
            if ($elements.not('script[type="text/ejs"]').length > 0) {
              df.reject(createRejectReason(ERR_CODE_TEMPLATE_FILE, [ERR_REASON_SCRIPT_ELEMENT_IS_NOT_EXIST], {
                url: absolutePath,
                path: filePath
              }));
            }
            var compileData = null;
            try {
              compileData = compileTemplatesByElements($elements.filter('script[type="text/ejs"]'));
            }
            catch (e) {
              e.detail.url = absolutePath;
              e.detail.path = filePath;
              df.reject(e);
            }
            try {
              var compiled = compileData.compiled;
              var data = compileData.data;
              data.path = filePath;
              data.absoluteUrl = absolutePath;
              $.extend(ret, compiled);
              datas.push(data);
              that.append(absolutePath, compiled, filePath);
              load(task, ++step);
            }
            catch (e) {
              df.reject(createRejectReason(ERR_CODE_TEMPLATE_FILE, null, {
                error: e,
                url: absolutePath,
                path: filePath
              }));
            }
          }).fail(function(e) {
            df.reject(createRejectReason(ERR_CODE_TEMPLATE_AJAX, null, {
              url: absolutePath,
              path: filePath,
              error: e
            }));
            return;
          });
          return df.promise();
        }
        var parentDf = getDeferred();
        $.when(load(tasks)).done(function() {
          parentDf.resolve(ret, datas);
        }).fail(function(e) {
          parentDf.reject(e);
        });
        return parentDf.promise();
      }
    };
    // =============================
    // Functions
    // =============================
    function getJQueryObj(obj) {
      /// <summary>
      ///  jQueryオブジェクトか判定し、jQueryオブジェクトならそのまま、そうでないならjQueryオブジェクトに変換して返します。
      /// </summary>
      /// <param  name = "obj" type = "Object" >
      ///  DOM要素
      /// </param>
      /// <returns  type = "Object" >
      ///  jQueryObject
      /// </returns>
      return h5.u.obj.isJQueryObject(obj) ? obj : $(obj);
    }
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    function View() {
      /// <summary>
      ///  テンプレートを扱うクラス。
      ///  
      ///  コントローラは内部にViewインスタンスを持ち、コントローラ内であればthis.viewで参照することができます。
      ///  
      /// </summary>
      this.__cachedTemplates = {};
    }
        $.extend(View.prototype, {
      load: function(resourcePaths) {
        /// <summary>
        ///  指定されたパスのテンプレートファイルを非同期で読み込みキャッシュします。
        /// </summary>
        /// <param  name = "resourcePaths" type = "String|Array[String]" >
        ///  テンプレートファイル(.ejs)のパス
        ///  (配列で複数指定可能)
        /// </param>
        /// <returns  type = "Promise" >
        ///  promiseオブジェクト
        /// </returns>
        var dfd = getDeferred();
        var that = this;
        var paths = null;
        // resourcePathsが文字列か配列でなかったらエラーを投げます。
        switch ($.type(resourcePaths)) {
          case 'string':
            if (!resourcePaths) {
              throwFwError(ERR_CODE_INVALID_FILE_PATH, []);
            }
            paths = [resourcePaths];
            break;
          case 'array':
            paths = resourcePaths;
            if (paths.length === 0) {
              throwFwError(ERR_CODE_INVALID_FILE_PATH, []);
            }
            break;
          default:
            throwFwError(ERR_CODE_INVALID_FILE_PATH, []);
            break;
        }
        cacheManager.getTemplateByUrls(paths).done(function(result, datas) {
          /* del begin */
          for (var id in result) {
            if (that.__cachedTemplates[id]) {
            }
          }
          /* del end */
          $.extend(that.__cachedTemplates, result);
          dfd.resolve(datas);
        }).fail(function(e) {
          dfd.reject(e);
        });
        return dfd.promise();
      },
      getAvailableTemplates: function() {
        /// <summary>
        ///  Viewインスタンスに登録されている、利用可能なテンプレートのIDの配列を返します。
        /// </summary>
        /// <returns  type = "Array[String]" >
        ///  テンプレートIDの配列
        /// </returns>
        var ids = [];
        for (var id in this.__cachedTemplates) {
          ids.push(id);
        }
        return ids;
      },
      register: function(templateId, templateString) {
        /// <summary>
        ///  Viewインスタンスに、指定されたIDとテンプレート文字列からテンプレートを1件登録します。
        ///  
        ///  指定されたIDのテンプレートがすでに存在する場合は上書きします。 templateStringが不正な場合はエラーを投げます。
        ///  
        /// </summary>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <param  name = "templateString" type = "String" >
        ///  テンプレート文字列
        /// </param>
        if ($.type(templateString) !== 'string') {
          throwFwError(ERR_CODE_TEMPLATE_COMPILE, [ERR_REASON_TEMPLATE_IS_NOT_STRING], {
            id: templateId
          });
        } else if (!templateId) {
          throwFwError(ERR_CODE_TEMPLATE_INVALID_ID, []);
        }
        try {
          var compiledTemplate = new EJS.Compiler(templateString, DELIMITER);
          compiledTemplate.compile();
          this.__cachedTemplates[templateId] = compiledTemplate.process;
        }
        catch (e) {
          var lineNo = e.lineNumber;
          var msg = lineNo ? ' line:' + lineNo : '';
          throwFwError(ERR_CODE_TEMPLATE_COMPILE, [h5.u.str.format(ERR_REASON_SYNTAX_ERR, msg, e.message)], {
            id: templateId
          });
        }
      },
      isValid: function(templateString) {
        /// <summary>
        ///  テンプレート文字列が、コンパイルできるかどうかを返します。
        /// </summary>
        /// <returns  type = "Boolean" >
        ///  第一引数に渡されたテンプレート文字列がコンパイル可能かどうか。
        /// </returns>
        try {
          new EJS.Compiler(templateString, DELIMITER).compile();
          return true;
        }
        catch (e) {
          return false;
        }
      },
      get: function(templateId, param) {
        /// <summary>
        ///  パラメータで置換された、指定されたテンプレートIDのテンプレートを取得します。
        ///  
        ///  取得するテンプレート内に置換要素([%= %])が存在する場合、パラメータを全て指定してください。
        ///  
        ///  
        ///  templateIdがこのViewインスタンスで利用可能でなければエラーを投げます。
        ///  
        ///   ※ ただし、コントローラが持つviewインスタンスから呼ばれた場合、templateIdが利用可能でない場合は再帰的に親コントローラをたどり、
        ///  親コントローラが持つViewインスタンスで利用可能かどうか確認します。 利用可能であれば、そのインスタンスのview.get()を実行します。
        ///  
        ///  
        ///  一番上の親のViewインスタンスまで辿ってもtemplateId利用可能でなければ場合はh5.core.view.get()を実行します。
        ///  h5.core.viewでtemplateIdが利用可能でなければエラーを投げます。
        ///  
        ///  
        ///  update(), append(), &lt;a
        ///  href=&quot;#prepend&quot;&gt;prepend()についても同様です。
        ///  
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
        var cache = this.__cachedTemplates;
        if ($.isEmptyObject(cache)) {
          return null;
        }
        if (!templateId) {
          throwFwError(ERR_CODE_TEMPLATE_INVALID_ID);
        }
        var template = cache[templateId];
        if (!template) {
          throwFwError(ERR_CODE_TEMPLATE_ID_UNAVAILABLE, templateId);
        }
        var p = (param) ? $.extend(true, {}, param) : {};
        var helper = p.hasOwnProperty('_h') ? new EJS.Helpers(p) : new EJS.Helpers(p, {
          _h: helperExtras
        });
        var ret = null;
        try {
          ret = template.call(p, p, helper);
        }
        catch (e) {
          throwFwError(ERR_CODE_TEMPLATE_PROPATY_UNDEFINED, e.toString(), e);
        }
        return ret;
      },
      update: function(element, templateId, param) {
        /// <summary>
        ///  要素を指定されたIDのテンプレートで書き換えます。
        ///  
        ///  templateIdがこのViewインスタンスで利用可能でなければエラーを投げますが、
        ///  コントローラが持つviewインスタンスから呼ばれた場合は親コントローラのviewを再帰的にたどります。詳細はget()をご覧ください。
        ///  
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
        return getJQueryObj(element).html(this.get(templateId, param));
      },
      append: function(element, templateId, param) {
        /// <summary>
        ///  要素の末尾に指定されたIDのテンプレートを挿入します。
        ///  
        ///  templateIdがこのViewインスタンスで利用可能でなければエラーを投げますが、
        ///  コントローラが持つviewインスタンスから呼ばれた場合は親コントローラのviewを再帰的にたどります。詳細はget()をご覧ください。
        ///  
        /// </summary>
        /// <param  name = "element" type = "Element|jQuery" >
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
        ///  テンプレートが適用されたDOM要素
        /// </returns>
        return getJQueryObj(element).append(this.get(templateId, param));
      },
      prepend: function(element, templateId, param) {
        /// <summary>
        ///  要素の先頭に指定されたIDのテンプレートを挿入します。
        ///  
        ///  templateIdがこのViewインスタンスで利用可能でなければエラーを投げますが、
        ///  コントローラが持つviewインスタンスから呼ばれた場合は親コントローラのviewを再帰的にたどります。詳細はget()をご覧ください。
        ///  
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
        return getJQueryObj(element).prepend(this.get(templateId, param));
      },
      isAvailable: function(templateId) {
        /// <summary>
        ///  指定されたテンプレートIDのテンプレートが存在するか判定します。
        /// </summary>
        /// <param  name = "templateId" type = "String" >
        ///  テンプレートID
        /// </param>
        /// <returns  type = "Boolean" >
        ///  判定結果(存在する: true 存在しない: false)
        /// </returns>
        return !!this.__cachedTemplates[templateId];
      },
      clear: function(templateIds) {
        /// <summary>
        ///  引数に指定されたテンプレートIDをもつテンプレートをキャッシュから削除します。
        ///  引数を指定しない場合はキャッシュされている全てのテンプレートを削除します。
        /// </summary>
        /// <param  name = "templateIds" type = "String|String[]" >
        ///  テンプレートID
        /// </param>
        if (templateIds === undefined) {
          this.__cachedTemplates = {};
          return;
        }
        var templateIdsArray = null;
        switch ($.type(templateIds)) {
          case 'string':
            templateIdsArray = [templateIds];
            break;
          case 'array':
            templateIdsArray = templateIds;
            break;
          default:
            templateIdsArray = [];
            break;
        }
        for (var i = 0, len = templateIdsArray.length; i < len; i++) {
          delete this.__cachedTemplates[templateIdsArray[i]];
        }
      }
    });
    var view = new View();
    view.createView = function() {
      /// <summary>
      ///  &lt;a
      ///  href=&quot;./View.html&quot;&gt;Viewクラスのインスタンスを生成します。
      ///  
      ///  この関数はh5.core.viewに公開されたViewインスタンスのみが持ちます。この関数で作られたViewインスタンスはcreateView()を持ちません。
      ///  
      /// </summary>
      return new View();
    };
    $(function() {
      /// <summary>
      /// </summary>
      $('script[type="text/ejs"]').each(function() {
        var templateId = $.trim(this.id);
        var templateText = $.trim(this.innerHTML);
        if (templateText.length === 0 || !templateId) {
          return;
        }
        var compiledTemplate = new EJS.Compiler(templateText, DELIMITER);
        compiledTemplate.compile();
        view.__cachedTemplates[templateId] = compiledTemplate.process;
      });
    });
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.core', {
      view: view
    });
    /* del begin */
    // 開発支援用にcacheManagerをグローバルに出す。
    h5.u.obj.expose('h5.dev.core.view', {
      cacheManager: cacheManager
    });
    /* del end */
  })();
  /* ------ h5.ui ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    var CLASS_INDICATOR_THROBBER = 'indicator-throbber';
    var CLASS_INDICATOR_MESSAGE = 'indicator-message';
    var CLASS_THROBBER_PERCENT = 'throbber-percent';
    var CLASS_VML_ROOT = 'vml-root';
    var FORMAT_THROBBER_MESSAGE_AREA = '<span class="' + CLASS_INDICATOR_THROBBER + '"></span><span class="' + CLASS_INDICATOR_MESSAGE + '" {0}>{1}</span>';
    // =============================
    // Production
    // =============================
    // =============================
    // Development Only
    // =============================
    /* del begin */
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    var isPromise = h5.async.isPromise;
    var h5ua = h5.env.ua;
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    // キーにスタイルクラス名、値に読み込む対象のプロパティを保持するマップ
    var throbberStyleMap = {
      throbber: ['width', 'height'],
      'throbber-line': ['width', 'color']
    };
    var isCanvasSupported = true;
      // 機能ベースでの判定方法が無いため、ブラウザの種類で判定する
    var isVMLSupported = h5ua.isIE;
    // =============================
    // Functions
    // =============================
    function readThrobberStyle(theme) {
      /// <summary>
      /// </summary>
      var readStyles = {};
      for (var prop in throbberStyleMap) {
        var $elem = $('<div></div>').addClass(theme).addClass(prop).appendTo('body');
        var propCamel = $.camelCase(prop);
        readStyles[propCamel] = {};
        $.map(throbberStyleMap[prop], function(item, idx) {
          if (item === 'width' || item === 'height') {
            readStyles[propCamel][item] = parseInt($elem.css(item).replace(/\D/g, ''), 10);
          } else {
            readStyles[propCamel][item] = $elem.css(item);
          }
        });
        $elem.remove();
      }
      return readStyles;
    }
    function createVMLElement(tagName, opt) {
      /// <summary>
      /// </summary>
      var elem = window.document.createElement('v:' + tagName);
      for (var prop in opt) {
        elem.style[prop] = opt[prop];
      }
      return elem;
    }
    function calculateLineCoords(size, line) {
      /// <summary>
      /// </summary>
      var positions = [];
      var centerPos = size / 2;
      var radius = size * 0.8 / 2;
      var eachRadian = 360 / line * Math.PI / 180;
      for (var j = 1; j <= line; j++) {
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
      return positions;
    }
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    // VMLとCanvasのサポート判定
    $(function() {
      // Cnavasがサポートされているかチェック
      isCanvasSupported = !!document.createElement("canvas").getContext;
      if (!isCanvasSupported && isVMLSupported) {
        document.namespaces.add('v', 'urn:schemas-microsoft-com:vml');
        document.createStyleSheet().cssText = ['v\\:stroke', 'v\\:line', 'v\\:textbox'].join(',') + '{behavior: url(#default#VML);}';
      }
    });
    function ThrobberVML(opt) {
      /// <summary>
      /// </summary>
      this.style = $.extend(true, {}, opt);
      var w = this.style.throbber.width;
      var h = this.style.throbber.height;
      this.group = createVMLElement('group', {
        width: w + 'px',
        height: h + 'px'
      });
      this.group.className = CLASS_VML_ROOT;
      var positions = calculateLineCoords(w, this.style.throbber.lines);
      var lineColor = this.style.throbberLine.color;
      var lineWidth = this.style.throbberLine.width;
      for (var i = 0, len = positions.length; i < len; i++) {
        var pos = positions[i];
        var from = pos.from;
        var to = pos.to;
        var e = createVMLElement('line');
        e.strokeweight = lineWidth;
        e.strokecolor = lineColor;
        e.fillcolor = lineColor;
        e.from = from.x + ',' + from.y;
        e.to = to.x + ',' + to.y;
        var ce = createVMLElement('stroke');
        ce.opacity = 1;
        e.appendChild(ce);
        this.group.appendChild(e);
      }
      this._createPercentArea();
    }
    ThrobberVML.prototype = {
      show: function(root) {
        if (!root) {
          return;
        }
        this.root = root;
        this.highlightPos = 1;
        this.hide();
        this.root.appendChild(this.group);
        this._run();
      },
      hide: function() {
        if (!this.root) {
          return;
        }
        this.root.innerHTML = "";
        if (this._runId) {
          clearTimeout(this._runId);
          this._runId = null;
        }
      },
      _run: function() {
        var lineCount = this.style.throbber.lines;
        var roundTime = this.style.throbber.roundTime;
        var highlightPos = this.highlightPos;
        var lines = this.group.childNodes;
        for (var i = 0, len = lines.length; i < len; i++) {
          var child = lines[i];
          if (child.nodeName === 'textbox') {
            continue;
          }
          var lineNum = i + 1;
          var line = child.firstChild;
          if (lineNum == highlightPos) {
            line.opacity = "1";
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
        var textPath = createVMLElement('textbox');
        var $table = $('<table><tr><td></td></tr></table>');
        var $td = $table.find('td');
        $td.width(this.group.style.width);
        $td.height(this.group.style.height);
        $td.css('line-height', this.group.style.height);
        $td.addClass(CLASS_THROBBER_PERCENT);
        textPath.appendChild($table[0]);
        this.group.appendChild(textPath);
      },
      setPercent: function(percent) {
        $(this.group).find('.' + CLASS_THROBBER_PERCENT).html(percent);
      }
    };
    var ThrobberCanvas = function(opt) {
      /// <summary>
      /// </summary>
      this.style = $.extend(true, {}, opt);
      this.canvas = document.createElement('canvas');
      this.baseDiv = document.createElement('div');
      this.percentDiv = document.createElement('div');
      var canvas = this.canvas;
      var baseDiv = this.baseDiv;
      var percentDiv = this.percentDiv;
      // CSSファイルから読み取ったスタイルをCanvasに適用する
      canvas.width = this.style.throbber.width;
      canvas.height = this.style.throbber.height;
      canvas.style.display = 'block';
      canvas.style.position = 'absolute';
      baseDiv.style.width = this.style.throbber.width + 'px';
      baseDiv.style.height = this.style.throbber.height + 'px';
      baseDiv.appendChild(canvas);
      // パーセント表示用DIV
      percentDiv.style.width = this.style.throbber.width + 'px';
      percentDiv.style.height = this.style.throbber.height + 'px';
      percentDiv.style.lineHeight = this.style.throbber.height + 'px';
      percentDiv.className = CLASS_THROBBER_PERCENT;
      baseDiv.appendChild(percentDiv);
      this.positions = calculateLineCoords(canvas.width, this.style.throbber.lines);
    };
    ThrobberCanvas.prototype = {
      show: function(root) {
        if (!root) {
          return;
        }
        this.root = root;
        this.highlightPos = 1;
        this.hide();
        root.appendChild(this.baseDiv);
        this._run();
      },
      hide: function() {
        if (!this.root) {
          return;
        }
        this.root.innerHTML = "";
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
        var lineColor = this.style.throbberLine.color;
        var lineWidth = this.style.throbberLine.width;
        var lineCount = this.style.throbber.lines;
        var roundTime = this.style.throbber.roundTime;
        canvas.width = canvas.width;
        for (var i = 0, len = positions.length; i < len; i++) {
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
          var pos = positions[i];
          var from = pos.from;
          var to = pos.to;
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
        this.percentDiv.innerHTML = percent;
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
      ///  Promiseオブジェクト (Promiseの状態に合わせて自動でインジケータの非表示を行う)
      /// </param>
      /// <param  name = "[option.theme]" type = "String" >
      ///  インジケータの基点となるクラス名 (CSSでテーマごとにスタイルを変更する場合に使用する)
      /// </param>
      $.blockUI.defaults.css = {};
      $.blockUI.defaults.overlayCSS = {};
      this.target = h5.u.obj.isJQueryObject(target) ? target.get(0) : target;
      var that = this;
      var $target = this._isGlobalBlockTarget() ? $('body') : $(this.target);
      var targetPositionStatic = $target.css('position');
      var targetZoom = $target.css('zoom');
      // optionのデフォルト値
      var opts = $.extend(true, {}, {
        message: '',
        percent: -1,
        block: true,
        promises: null,
        theme: 'a'
      }, option);
      // BlockUIのスタイル定義
      var blockUISetting = {
        message: h5.u.str.format(FORMAT_THROBBER_MESSAGE_AREA, (opts.message === '') ? 'style="display: none;"' : '', opts.message),
        css: {},
        overlayCSS: {},
        blockMsgClass: opts.theme,
        showOverlay: opts.block,
        centerX: false,
        centerY: false,
        onUnblock: function() {          // blockUIが、画面ブロックの削除時に実行するコールバック関数
          // インジケータを表示する要素のpositionがstaticの場合、blockUIがroot要素のpositionをrelativeに書き換えるため、インジケータを表示する前の状態に戻す
          $target.css('position', targetPositionStatic);
          // IEの場合、blockUIがroot要素にzoom:1を設定するため、インジケータを表示する前の状態に戻す
          $target.css('zoom', targetZoom);
          that.throbber.hide();
        }
      };
      // スロバーのスタイル定義 (基本的にはCSSで記述する。ただし固定値はここで設定する)
      var throbberSetting = {
        throbber: {
          roundTime: 1000,
          lines: 12
        },
        throbberLine: {},
        percent: {}
      };
      var promises = opts.promises;
      var promiseCallback = $.proxy(function() {
        this.hide();
      }, this);
      if ($.isArray(promises)) {
        $.map(promises, function(item, idx) {
          return isPromise(item) ? item : null;
        });
        if (promises.length > 0) {
          $.when.apply(null, promises).pipe(promiseCallback, promiseCallback);
        }
      } else if (isPromise(promises)) {
        promises.pipe(promiseCallback, promiseCallback);
      }
      var canvasStyles = readThrobberStyle(opts.theme);
      throbberSetting = $.extend(true, throbberSetting, canvasStyles);
      this._style = $.extend(true, {}, blockUISetting, throbberSetting);
      if (isCanvasSupported) {
        this.throbber = new ThrobberCanvas(this._style);
      } else if (isVMLSupported) {
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
        if (this._isGlobalBlockTarget()) {
          $.blockUI(setting);
          $blockElement = $('body').children('.blockUI.' + setting.blockMsgClass + '.blockPage');
        } else {
          var $target = $(this.target);
          $target.block(setting);
          $blockElement = $target.children('.blockUI.' + setting.blockMsgClass + '.blockElement');
        }
        this.throbber.show($blockElement.children('.' + CLASS_INDICATOR_THROBBER)[0]);
        this._setPositionAndResizeWidth();
        return this;
      },
      _setPositionAndResizeWidth: function() {
        /// <summary>
        ///  内部のコンテンツ納まるようイジケータの幅を調整し、表示位置(topとleft)が中央になるよう設定します。
        /// </summary>
        var setting = this._style;
        var $blockParent = null;
        var $blockElement = null;
        var width = 0;
        if (this._isGlobalBlockTarget()) {
          $blockParent = $('body');
          $blockElement = $blockParent.children('.blockUI.' + setting.blockMsgClass + '.blockPage');
          // 画面全体をブロックするので、windowからheightを取得する
          $blockElement.css('top', (($(window).height() - $blockElement.outerHeight()) / 2) + 'px');
        } else {
          $blockParent = $(this.target);
          $blockElement = $blockParent.children('.blockUI.' + setting.blockMsgClass + '.blockElement');
          $blockElement.css('top', (($blockParent.height() - $blockElement.outerHeight()) / 2) + 'px');
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
      percent: function(percent) {
        /// <summary>
        ///  進捗のパーセント値を指定された値に更新します。
        /// </summary>
        /// <param  name = "percent" type = "Number" >
        ///  進捗率(0～100%)
        /// </param>
        /// <returns  type = "Indicator" >
        ///  インジケータオブジェクト
        /// </returns>
        if (typeof percent === 'number' && percent >= 0 && percent <= 100) {
          this.throbber.setPercent(percent);
        }
        return this;
      },
      message: function(message) {
        /// <summary>
        ///  メッセージを指定された値に更新します。
        /// </summary>
        /// <param  name = "message" type = "String" >
        ///  メッセージ
        /// </param>
        /// <returns  type = "Indicator" >
        ///  インジケータオブジェクト
        /// </returns>
        if (typeof message === 'string') {
          var setting = this._style;
          var $blockElement = null;
          if (this._isGlobalBlockTarget()) {
            $blockElement = $('body').children('.blockUI.' + setting.blockMsgClass + '.blockPage');
          } else {
            $blockElement = $(this.target).children('.blockUI.' + setting.blockMsgClass + '.blockElement');
          }
          $blockElement.children('.' + CLASS_INDICATOR_MESSAGE).css('display', 'inline-block').text(message);
          this._setPositionAndResizeWidth();
        }
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
      ///  
      ///  コントローラのindicator()の仕様については、Controller.indicatorのドキュメント
      ///  を参照下さい。
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
      ///  Promiseオブジェクト (Promiseの状態に合わせて自動でインジケータの非表示を行う)
      /// </param>
      /// <param  name = "[options.theme]" type = "String" >
      ///  インジケータの基点となるクラス名 (CSSでテーマごとにスタイルをする場合に使用する)
      /// </param>
      return new Indicator(target, option);
    };
    var isInView = function(element, container) {
      /// <summary>
      ///  要素が可視範囲内、または指定した親要素内にあるかどうかを返します。
      ///  
      ///  第2引数を省略した場合、要素がウィンドウ内に見えているかどうかを返します。 elementが他のDOM要素によって隠れていても、範囲内にあればtrueを返します。
      ///  
      ///  
      ///  第2引数を指定した場合、elementがcontaienrの表示範囲内で見えているかどうかを返します。 containerがウィンドウ内に見えているかどうかは関係ありません。
      ///  elementがcontainerの子孫要素で無ければundefinedを返します。
      ///  
      ///  
      ///  いずれの場合も、要素が非表示の場合の動作は保障されません。
      ///  
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
      var viewTop,
        viewBottom,
        viewLeft,
        viewRight;
      var $element = $(element);
      var height,
        width;
      var $container;
      // containerの位置を取得。borderの内側の位置で判定する。
      if (container === undefined) {
        // containerが指定されていないときは、画面表示範囲内にあるかどうか判定する
        height = h5.env.ua.isiOS ? window.innerHeight : $(window).height();
        width = h5.env.ua.isiOS ? window.innerWidth : $(window).width();
        viewTop = $(window).scrollTop();
        viewLeft = $(window).scrollLeft();
      } else {
        $container = $(container);
        if ($container.find($element).length === 0) {
          // elementとcontaienrが親子関係でなければundefinedを返す
          return undefined;
        }
        viewTop = $container.offset().top + parseInt($container.css('border-top-width'));
        viewLeft = $container.offset().left + parseInt($container.css('border-left-width'));
        height = $container.innerHeight();
        width = $container.innerWidth();
      }
      viewBottom = viewTop + height;
      viewRight = viewLeft + width;
      // elementの位置を取得。borderの外側の位置で判定する。
      var positionTop = $element.offset().top;
      var positionLeft = $element.offset().left;
      var positionBottom = positionTop + $element.outerHeight();
      var positionRight = positionLeft + $element.outerWidth();
      return ((viewTop <= positionTop && positionTop < viewBottom) || (viewTop < positionBottom && positionBottom <= viewBottom)) && ((viewLeft <= positionLeft && positionLeft < viewRight) || (viewLeft < positionRight && positionRight <= viewRight));
    };
    var scrollToTop = function(wait) {
      /// <summary>
      ///  ブラウザのトップにスクロールします。
      /// </summary>
      var waitCount = 3;
      var waitMillis = 500;
      function fnScroll() {
        if (window.scrollY === 1) {
          waitCount = 0;
        }
        if (waitCount > 0) {
          window.scrollTo(0, 1);
          waitCount--;
          setTimeout(fnScroll, waitMillis);
        }
      }
      window.scrollTo(0, 1);
      if (window.scrollY !== 1) {
        setTimeout(fnScroll, waitMillis);
      }
    };
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.ui', {
      indicator: indicator,
      isInView: isInView,
      scrollToTop: scrollToTop
    });
  })();
  /* ------ h5.ui.jqm.manager ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    // =============================
    // Development Only
    // =============================
    var fwLogger = h5.log.createLogger('h5.ui.jqm.manager');
    /* del begin */
    // TODO Minify時にプリプロセッサで削除されるべきものはこの中に書く
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // TODO 高速化のために他で定義されている関数などを変数に入れておく場合はここに書く
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    var jqmControllerInstance = null;
    var controllerMap = {};
    var controllerInstanceMap = {};
    var initParamMap = {};
    var excludeDispose = [];
    var cssMap = {};
    // =============================
    // Functions
    // =============================
    function bindToActivePage() {
      /// <summary>
      /// </summary>
      var activePage = $.mobile.activePage;
      if (!activePage) {
        return;
      }
      var id = activePage.attr('id');
      var controllers = controllerInstanceMap[id];
      if (controllerMap[id] && (!controllers || controllers.length === 0)) {
        jqmControllerInstance.addCSS(id);
        jqmControllerInstance.bindController(id);
      }
    }
    // TODO モジュールレベルのプライベート関数はここに書く
    // 関数は関数式ではなく function myFunction(){} のように関数定義で書く
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    h5.u.obj.ns('h5.ui.jqm');
    h5.ui.jqm.dataPrefix = 'h5';
    var jqmController = {
      __name: 'JQMController',
      __ready: function(context) {
        /// <summary>
        ///  __readyイベントのハンドラ
        /// </summary>
        /// <param  name = "context" type = "Object" >
        ///  コンテキスト
        /// </param>
      /// <summary>
      /// </summary>
        var that = this;
        excludeDispose.push(this.rootElement);
        $(':jqmData(role="page"), :jqmData(role="dialog")').each(function() {
          excludeDispose.push(this);
          that.loadScript(this.id);
        });
      },
      ':jqmData(role="page"), :jqmData(role="dialog") pageinit': function(context) {
        /// <summary>
        ///  pageinitイベントのハンドラ
        /// </summary>
        /// <param  name = "context" type = "Object" >
        ///  コンテキスト
        /// </param>
        var id = context.event.target.id;
        this.loadScript(id);
        this.addCSS(id);
        this.bindController(id);
      },
      '{document} pageremove': function(context) {
        /// <summary>
        ///  pageremoveイベントのハンドラ
        /// </summary>
        /// <param  name = "context" type = "Object" >
        ///  コンテキスト
        /// </param>
        var id = context.event.target.id;
        var controllers = controllerInstanceMap[id];
        if (!controllers) {
          return;
        }
        for (var i = 0, len = controllers.length; i < len; i++) {
          controllers[i].dispose();
        }
        controllerInstanceMap[id] = [];
      },
      '{document} pagebeforeshow': function(context) {
        /// <summary>
        ///  pagebeforeshowイベントのハンドラ
        /// </summary>
        /// <param  name = "context" type = "Object" >
        ///  コンテキスト
        /// </param>
        var id = context.event.target.id;
        this.addCSS(id);
        // リスナーの有効・無効の切り替え
        for (var prop in controllerInstanceMap) {
          var controllers = controllerInstanceMap[prop];
          var enable = id === prop;
          for (var i = 0, len = controllers.length; i < len; i++) {
            var c = controllers[i];
            enable ? c.enableListeners() : c.disableListeners();
          }
        }
      },
      '{document} pagehide': function(context) {
        /// <summary>
        ///  pagehideイベントのハンドラ
        /// </summary>
        /// <param  name = "context" type = "Object" >
        ///  コンテキスト
        /// </param>
        this.removeCSS(context.event.target.id);
      },
      '* h5controllerbound': function(context) {
        /// <summary>
        ///  h5controllerboundイベントを監視しコントローラインスタンスを管理するためのイベントハンドラ
        /// </summary>
        /// <param  name = "context" type = "Object" >
        ///  コンテキスト
        /// </param>
        var id = context.event.target.id;
        if (!controllerInstanceMap[id]) {
          controllerInstanceMap[id] = [];
        }
        controllerInstanceMap[id].push(context.evArg);
      },
      loadScript: function(id) {
        /// <summary>
        ///  指定されたページIDに紐付くスクリプトをロードする。
        /// </summary>
        /// <param  name = "id" type = "String" >
        ///  ページID
        /// </param>
        var page = $('#' + id);
        var script = $.trim(page.data(this.getDataAttribute('script')));
        if (script.length === 0) {
          return;
        }
        var src = $.map(script.split(','), function(n) {
          return $.trim(n);
        });
        var async = page.data(this.getDataAttribute('async')) == true;
        return h5.u.loadScript(src, {
          async: async
        });
      },
      getDataAttribute: function(attributeName) {
        /// <summary>
        ///  JQMコントローラが使用するdata属性にprefixを付けた属性名を返す。
        /// </summary>
        /// <param  name = "attributeName" type = "String" >
        ///  属性名
        /// </param>
        /// <returns  type = "String" >
        ///  prefixを付けた属性名
        /// </returns>
        var prefix = h5.ui.jqm.dataPrefix;
        if (prefix == null) {
          prefix = 'h5';
        }
        return prefix.length !== 0 ? prefix + '-' + attributeName : attributeName;
      },
      bindController: function(id) {
        /// <summary>
        ///  コントローラのバインドを行う
        /// </summary>
        /// <param  name = "id" type = "String" >
        ///  ページID
        /// </param>
        var controllers = controllerInstanceMap[id];
        if (!controllerMap[id] || (controllers && controllers.length > 0)) {
          return;
        }
        h5.core.controller('#' + id, controllerMap[id], initParamMap[id]);
      },
      addCSS: function(id) {
        /// <summary>
        ///  指定されたページIDに紐付くCSSを追加する。
        /// </summary>
        /// <param  name = "id" type = "String" >
        ///  ページID
        /// </param>
        if (this.firstAddCSS) {
          this.firstAddCSS = false;
        }
        var src = cssMap[id];
        if (!src) {
          return;
        }
        var head = document.getElementsByTagName('head')[0];
        var linkTags = head.getElementsByTagName('link');
        var linkLen = linkTags.length;
        src = wrapInArray(src);
        for (var i = 0, srcLen = src.length; i < srcLen; i++) {
          var path = $.mobile.path.parseUrl(cssMap[id][i]).filename;
          var isLoaded = false;
          for (var j = 0; j < linkLen; j++) {
            var loadedPath = $.mobile.path.parseUrl(linkTags[j].href).filename;
            if (loadedPath === path) {
              isLoaded = true;
              break;
            }
          }
          if (isLoaded) {
            continue;
          }
          var cssNode = document.createElement('link');
          cssNode.type = 'text/css';
          cssNode.rel = 'stylesheet';
          cssNode.href = cssMap[id][i];
          head.appendChild(cssNode);
        }
      },
      removeCSS: function(id) {
        /// <summary>
        ///  指定されたページIDに紐付くCSSを削除する。
        /// </summary>
        /// <param  name = "id" type = "String" >
        ///  ページID
        /// </param>
        var current = cssMap[id];
        if (!current) {
          return;
        }
        var activeId = $.mobile.activePage.attr('id');
        var active = cssMap[activeId];
        var src = wrapInArray(current);
        var activeSrc = wrapInArray(active);
        var css = $('link').filter(function() {
          var href = $(this).attr('href');
          return $.inArray(href, src) !== -1 && $.inArray(href, activeSrc) === -1;
        });
        css.remove();
      }
    };
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.ui.jqm.manager', {
      init: function() {
        /// <summary>
        ///  jQuery
        ///  Mobile用hifiveコントローラマネージャを初期化します。
        ///  2回目以降は何も処理を行いません。
        /// </summary>
      /// <summary>
      /// </summary>
        $(function() {
          if (jqmControllerInstance) {
          } else {
            jqmControllerInstance = h5.core.controller(document.body, jqmController);
          }
          bindToActivePage();
        });
      },
      define: function(id, cssSrc, controllerDefObject, initParam) {
        /// <summary>
        ///  jQuery
        ///  Mobile用hifiveコントローラマネージャにコントローラを登録します。
        ///  1画面1コントローラを想定しています。
        /// </summary>
        /// <param  name = "id" type = "String" >
        ///  ページID
        /// </param>
        /// <param  name = "cssSrc" type = "String|String[]" >
        ///  CSSファイルパス配列
        /// </param>
        /// <param  name = "controllerDefObject" type = "Object" >
        ///  コントローラを定義したオブジェクト
        /// </param>
        /// <param  name = "initParam" type = "Object" >
        ///  初期化パラメータ
        /// </param>
        controllerMap[id] = controllerDefObject;
        initParamMap[id] = initParam;
        cssMap[id] = wrapInArray(cssSrc);
        !jqmControllerInstance ? h5.ui.jqm.manager.init() : bindToActivePage();
      }
    });
  })();
  /* ------ h5.api.geo ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    var ERR_CODE_INVALID_COORDS = 2000;
    var ERR_CODE_INVALID_GEOSYSTEM_CONSTANT = 2001;
    var ERR_CODE_POSITIONING_FAILURE = 2002;
    var errMsgMap = {};
    errMsgMap[ERR_CODE_INVALID_COORDS] = '正しい緯度または経度を指定して下さい。';
    errMsgMap[ERR_CODE_INVALID_GEOSYSTEM_CONSTANT] = '正しい計算モード定数を指定して下さい';
    errMsgMap[ERR_CODE_POSITIONING_FAILURE] = '位置情報の取得に失敗しました。';
    addFwErrorCodeMap(errMsgMap);
    // =============================
    // Development Only
    // =============================
    /* del begin */
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // navigator.geolocationをキャッシュする変数
    var geo = null;
    function getGeo() {
      if (!geo) {
        geo = navigator.geolocation;
      }
      return geo;
    }
    var h5ua = h5.env.ua;
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    // =============================
    // Functions
    // =============================
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    function GeodeticSystemEnum(oblateness, semiMajorAxis) {
      /// <summary>
      ///  h5.api.geo.getDistance()
      ///  の計算モードを指定するための定数クラス
      ///  
      ///  このオブジェクトは自分でnewすることはありません。以下のオブジェクトにアクセスするとインスタンスが返されます。
      ///  
      ///  
      ///  h5.api.geo.GS_GRS80
      ///  h5.api.geo.GS_BESSEL
      ///  
      /// </summary>
      // 扁平率
      this.oblateness = oblateness;
      // 長(赤道)半径
      this.semiMajorAxis = semiMajorAxis;
    }
    GeodeticSystemEnum.prototype.getOblateness = function() {
      /// <summary>
      ///  扁平率を取得します。
      /// </summary>
      /// <returns  type = "Number" >
      ///  扁平率
      /// </returns>
      return this.oblateness;
    };
    GeodeticSystemEnum.prototype.getSemiMajorAxis = function() {
      /// <summary>
      ///  長(赤道)半径を取得します。
      /// </summary>
      /// <returns  type = "Number" >
      ///  長(赤道)半径
      /// </returns>
      return this.semiMajorAxis;
    };
    var GRS80 = new GeodeticSystemEnum(298.257222, 6378137);
    var BESSEL = new GeodeticSystemEnum(299.152813, 6377397.155);
    var DEGREES_PER_SECOND = Math.PI / 180;
    function Geolocation() {
      /// <summary>
      ///  Geolocation
      ///  API
      /// </summary>
      // 空コンストラクタ
    }
    $.extend(Geolocation.prototype, {
        // IE9の場合、navigator.geolocationにアクセスするとメモリーリークするのでエージェントで利用可能か判定する
      isSupported: (h5ua.isIE && h5ua.browserVersion >= 9) ? true : !!getGeo(),
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
        /// <returns  type = "Promise" >
        ///  Promiseオブジェクト
        /// </returns>
        var dfd = h5.async.deferred();
        getGeo().getCurrentPosition(function(geoPosition) {
          dfd.resolve(geoPosition);
        }, function(e) {
          dfd.reject(createRejectReason(ERR_CODE_POSITIONING_FAILURE, null, e));
        }, option);
        return dfd.promise();
      },
      watchPosition: function(option) {
        /// <summary>
        ///  現在地の緯度・経度を定期的に送信します。
        ///  
        ///  このメソッドは定期的に位置情報を取得するため、Deferred.progress()で値を取得します。
        ///  (Deferred.done()では値を取得できません。)
        ///  
        ///  実装例
        ///  
        ///  h5.api.geo.watchPosition().progress(function(pos)
        ///  // 変数 pos に位置情報が格納されている。
        ///  });
        ///  
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
        /// <returns  type = "WatchPositionPromise" >
        ///  WatchPositionPromiseオブジェクト
        /// </returns>
        var dfd = h5.async.deferred();
        var id = getGeo().watchPosition(function(pos) {
          dfd.notify(pos);
        }, function(e) {
          dfd.reject(createRejectReason(ERR_CODE_POSITIONING_FAILURE, null, e));
        }, option);
        function WatchPositionPromise() {
          /// <summary>
          ///  h5.api.geo.watchPositionがこのオブジェクトをプロミス化して返します。
          ///  
          ///  このオブジェクトは自分でnewすることはありません。h5.api.geo.watchPosition関数を呼び出すとインスタンスが返されます。
          ///  
          /// </summary>
          // 空コンストラクタ
        }
        WatchPositionPromise.prototype.unwatch = function() {
          /// <summary>
          ///  h5.api.geo.watchPositionで行っているユーザの位置監視を終了します。
          ///  
          ///  ユーザの位置監視を終了し、Deferred.done()が実行されます。
          ///  
          /// </summary>
          getGeo().clearWatch(id);
          dfd.resolve();
        };
        return dfd.promise(new WatchPositionPromise());
      },
        // TODO 長距離の場合も考えて、距離によって誤差が大きくならない『測地線航海算法』で計算するメソッドの追加も要検討
      getDistance: function(lat1, lng1, lat2, lng2, geoSystem) {
        /// <summary>
        ///  ヒュベニの法則を使用して、2点間の緯度・経度から直線距離(m)を取得します。
        ///  
        ///  定数に使用している長半径・扁平率は国土地理院で紹介されている値を使用。
        ///  
        ///  注意:アルゴリズム上、長距離(100km以上)の地点を図る場合1m以上の誤差が出てしまいます。
        ///  計算モードの指定方法
        ///  計算モードの指定は以下の定数クラスを使用します。
        ///  
        ///  
        ///  h5.api.geo.GS_GRS80
        ///  世界測地系
        ///  
        ///  
        ///  h5.api.geo.GS_BESSEL
        ///  日本測地系
        ///  
        ///  
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
        /// <param  name = "[geoSystem]" type = "GeodeticSystemEnum" >
        ///  計算モード定数
        ///  (h5.api.geo.GS_GRS80:世界測地系(未指定の場合このモードで計算する) h5.api.geo.GS_BESSEL: 日本測地系)
        /// </param>
        /// <returns  type = "Number" >
        ///  2点間の直線距離
        /// </returns>
        if (!isFinite(lat1) || !isFinite(lng1) || !isFinite(lat2) || !isFinite(lng2)) {
          throw new throwFwError(ERR_CODE_INVALID_COORDS);
        }
        var geodeticMode = geoSystem ? geoSystem : GRS80;
        if (!(geodeticMode instanceof GeodeticSystemEnum)) {
          throw new throwFwError(ERR_CODE_INVALID_GEOSYSTEM_CONSTANT);
        }
        // 長半径(赤道半径)
        var A = geodeticMode.getSemiMajorAxis();
        // 扁平率
        var O = geodeticMode.getOblateness();
        // 起点の緯度のラジアン
        var latRad1 = lat1 * DEGREES_PER_SECOND;
        // 起点の経度のラジアン
        var lngRad1 = lng1 * DEGREES_PER_SECOND;
        // 終点の緯度のラジアン
        var latRad2 = lat2 * DEGREES_PER_SECOND;
        // 終点の経度のラジアン
        var lngRad2 = lng2 * DEGREES_PER_SECOND;
        // 2点の平均緯度
        var avgLat = (latRad1 + latRad2) / 2;
        // 第一離心率
        var e = (Math.sqrt(2 * O - 1)) / O;
        var e2 = Math.pow(e, 2);
        var W = Math.sqrt(1 - e2 * Math.pow(Math.sin(avgLat), 2));
        // 短半径(極半径)
        var semiminorAxis = A * (1 - e2);
        // 子午線曲率半径
        var M = semiminorAxis / Math.pow(W, 3);
        // 卯酉船曲率半径
        var N = A / W;
        // 2点の緯度差
        var deltaLat = latRad1 - latRad2;
        // 2点の経度差
        var deltaLon = lngRad1 - lngRad2;
        return Math.sqrt(Math.pow(M * deltaLat, 2) + Math.pow(N * Math.cos(avgLat) * deltaLon, 2));
      },
      GS_GRS80: GRS80,
      GS_BESSEL: BESSEL
    });
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.api', {
      geo: new Geolocation()
    });
  })();
  /* ------ h5.api.sqldb ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    var INSERT_SQL_FORMAT = 'INSERT INTO {0} ({1}) VALUES ({2})';
    var INSERT_SQL_EMPTY_VALUES = 'INSERT INTO {0} DEFAULT VALUES';
    var SELECT_SQL_FORMAT = 'SELECT {0} FROM {1}';
    var UPDATE_SQL_FORMAT = 'UPDATE {0} SET {1}';
    var DELETE_SQL_FORMAT = 'DELETE FROM {0}';
    // =============================
    // Production
    // =============================
    var ERR_CODE_RETRY_SQL = 3000;
    var ERR_CODE_INVALID_TABLE_NAME = 3001;
    var ERR_CODE_INVALID_TRANSACTION_TYPE = 3002;
    var ERR_CODE_INVALID_OPERATOR = 3003;
    var ERR_CODE_INVALID_PARAM_TYPE = 3004;
    var ERR_CODE_INVALID_COLUMN_NAME = 3005;
    var ERR_CODE_INVALID_VALUES = 3006;
    var ERR_CODE_INVALID_STATEMENT = 3007;
    var ERR_CODE_TYPE_NOT_ARRAY = 3008;
    var ERR_CODE_INVALID_TRANSACTION_TARGET = 3009;
    var ERR_CODE_TRANSACTION_PROCESSING_FAILURE = 3010;
    var errMsgMap = {};
    errMsgMap[ERR_CODE_RETRY_SQL] = '同一オブジェクトによるSQLの再実行はできません。';
    errMsgMap[ERR_CODE_INVALID_TABLE_NAME] = '{0}: テーブル名を指定して下さい。';
    errMsgMap[ERR_CODE_INVALID_TRANSACTION_TYPE] = '{0}: トランザクションが不正です。';
    errMsgMap[ERR_CODE_INVALID_OPERATOR] = 'オペレータが不正です。 <= < >= > = != like のいずれかを使用して下さい。';
    errMsgMap[ERR_CODE_INVALID_PARAM_TYPE] = '{0}: {1}に指定したオブジェクトの型が不正です。';
    errMsgMap[ERR_CODE_INVALID_COLUMN_NAME] = '{0}: カラム名を指定して下さい。';
    errMsgMap[ERR_CODE_INVALID_VALUES] = '{0}: 値を指定して下さい。';
    errMsgMap[ERR_CODE_INVALID_STATEMENT] = '{0}: ステートメントが不正です。';
    errMsgMap[ERR_CODE_TYPE_NOT_ARRAY] = '{0}: パラメータは配列で指定して下さい。';
    errMsgMap[ERR_CODE_INVALID_TRANSACTION_TARGET] = '指定されたオブジェクトはトランザクションに追加できません。Insert/Update/Del/Select/Sqlクラスのインスタンスを指定して下さい。';
    errMsgMap[ERR_CODE_TRANSACTION_PROCESSING_FAILURE] = 'トランザクション処理中にエラーが発生しました。{0} {1}';
    addFwErrorCodeMap(errMsgMap);
    // =============================
    // Development Only
    // =============================
    /* del begin */
    var fwLogger = h5.log.createLogger('h5.api.sqldb');
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    var getDeferred = h5.async.deferred;
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    function getTransactionErrorMsg(e) {
      /// <summary>
      /// </summary>
      switch (e.code) {
        case e.CONSTRAINT_ERR:
          return '一意制約に反しています。';
        case e.DATABASE_ERR:
          return 'データベースエラー';
        case e.QUOTA_ERR:
          return '空き容量が不足しています。';
        case e.SYNTAX_ERR:
          return '構文に誤りがあります。';
        case e.TIMEOUT_ERR:
          return 'ロック要求がタイムアウトしました。';
        case e.TOO_LARGE_ERR:
          return '取得結果の行が多すぎます。';
        case e.UNKNOWN_ERR:
          return 'トランザクション内で例外がスローされました。';
        case e.VERSION_ERR:
          return 'データベースのバージョンが一致しません。';
        default:
          return '';
      }
    }
    // =============================
    // Variables
    // =============================
    // =============================
    // Functions
    // =============================
    function transactionErrorCallback(txw, e) {
      /// <summary>
      /// </summary>
      var results = txw._tasks;
      for (var i = results.length - 1; i >= 0; i--) {
        var result = results[i];
        var msgParam = getTransactionErrorMsg(e);
        result.deferred.reject(createRejectReason(ERR_CODE_TRANSACTION_PROCESSING_FAILURE, [msgParam, e.message], e));
      }
    }
    function transactionSuccessCallback(txw) {
      /// <summary>
      /// </summary>
      var results = txw._tasks;
      for (var i = results.length - 1; i >= 0; i--) {
        var result = results[i];
        result.deferred.resolve(result.result);
      }
    }
    function checkSqlExecuted(flag) {
      /// <summary>
      /// </summary>
      if (flag) {
        throw new throwFwError(ERR_CODE_RETRY_SQL);
      }
    }
    function checkTableName(funcName, tableName) {
      /// <summary>
      /// </summary>
      if (typeof tableName !== 'string') {
        throw new throwFwError(ERR_CODE_INVALID_TABLE_NAME, funcName);
      }
    }
    function checkTransaction(funcName, txw) {
      /// <summary>
      /// </summary>
      if (txw && !(txw instanceof SQLTransactionWrapper)) {
        throw new throwFwError(ERR_CODE_INVALID_TRANSACTION_TYPE, funcName);
      }
    }
    function createConditionAndParameters(whereObj, conditions, parameters) {
      /// <summary>
      /// </summary>
      if ($.isPlainObject(whereObj)) {
        for (var prop in whereObj) {
          var params = prop.replace(/ +/g, ' ').split(' ');
          var param = [];
          if (params.length === 0 || params[0] === "") {
            continue;
          } else if (params.length === 1) {
            param.push(params[0]);
            param.push('=');
            param.push('?');
          } else if (!/^(<=|<|>=|>|=|!=|like)$/i.test(params[1])) {
            throw new throwFwError(ERR_CODE_INVALID_OPERATOR);
          } else if (params.length === 3 && /^like$/i.test(params[1])) {
            param.push(params[0]);
            param.push(params[1]);
            param.push('?');
            param.push('ESCAPE');
            param.push('"' + params[2] + '"');
          } else {
            param.push(params[0]);
            param.push(params[1]);
            param.push('?');
          }
          conditions.push(param.join(' '));
          parameters.push(whereObj[prop]);
        }
      }
    }
    function SqlExecutor() {
      /// <summary>
      /// </summary>
      // 空コンストラクタ
    }
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    function SQLTransactionWrapper(db, tx) {
      /// <summary>
      ///  SQLTransaction拡張クラス
      ///  
      ///  このオブジェクトは自分でnewすることはありません。
      ///  Insert/Select/Update/Del/Sql/Transactionオブジェクトのexecute()が返す、Promiseオブジェクトのprogress()の引数に存在します。
      /// </summary>
      this._db = db;
      this._tx = tx;
      this._tasks = [];
    }
    $.extend(SQLTransactionWrapper.prototype, {
      _runTransaction: function() {
        /// <summary>
        ///  トランザクション処理中か判定します。
        /// </summary>
        /// <returns  type = "Boolean" >
        ///  true:実行中
        ///  false: 未実行
        /// </returns>
        return this._tx != null;
      },
      _execute: function(param1, param2, param3) {
        /// <summary>
        ///  トランザクション処理中か判定し、未処理の場合はトランザクションの開始を、処理中の場合はSQLの実行を行います。
        /// </summary>
        /// <param  name = "param1" type = "String|Function" >
        ///  パラメータ1
        /// </param>
        /// <param  name = "param2" type = "String|Function" >
        ///  パラメータ2
        /// </param>
        /// <param  name = "param3" type = "Function" >
        ///  パラメータ3
        /// </param>
        this._runTransaction() ? this._tx.executeSql(param1, param2, param3) : this._db.transaction(param1, param2, param3);
      },
      _addTask: function(df) {
        /// <summary>
        ///  トランザクション内で実行中のDeferredオブジェクトを管理対象として追加します。
        /// </summary>
        /// <param  name = "df" type = "Deferred" >
        ///  Deferredオブジェクト
        /// </param>
        this._tasks.push({
          deferred: df,
          result: null
        });
      },
      _setResult: function(result) {
        /// <summary>
        ///  SQLの実行結果を設定します。
        /// </summary>
        /// <param  name = "resul" type = "Any" >
        ///  SQL実行結果
        /// </param>
        this._tasks[this._tasks.length - 1].result = result;
      }
    });
    function createSelectStatementAndParameters(params, tableName, column, where, orderBy) {
      /// <summary>
      /// </summary>
      var statement = h5.u.str.format(SELECT_SQL_FORMAT, column, tableName);
      if ($.isPlainObject(where)) {
        var conditions = [];
        createConditionAndParameters(where, conditions, params);
        statement += (' WHERE ' + conditions.join(' AND '));
      } else if (typeof where === 'string') {
        statement += (' WHERE ' + where);
      }
      if ($.isArray(orderBy)) {
        statement += (' ORDER BY ' + orderBy.join(', '));
      }
      return statement;
    }
    function Select(txw, tableName, columns) {
      /// <summary>
      ///  指定されたテーブルに対して、検索処理(SELECT)を行うクラス。
      ///  
      ///  このオブジェクトは自分でnewすることはありません。
      ///  h5.api.sqldb.open().select()を呼び出すと、このクラスのインスタンスが返されます。
      /// </summary>
      this._txw = txw;
      this._tableName = tableName;
      this._columns = $.isArray(columns) ? columns.join(', ') : '*';
      this._where = null;
      this._orderBy = null;
      this._statement = null;
      this._params = [];
      this._df = getDeferred();
      this._executed = false;
    }
    Select.prototype = new SqlExecutor();
    $.extend(Select.prototype, {
      where: function(whereObj) {
        /// <summary>
        ///  WHERE句を設定します。
        ///  
        ///  条件は以下の方法で設定できます。
        ///  
        ///  オブジェクト
        ///  文字列
        ///  
        ///  オブジェクトの場合、キーに『カラム名[半角スペース]オペレータ』、バリューに値を指定します。
        ///  
        ///  例. IDが0以上100以下。
        ///  
        ///  db.select(&apos;USER&apos;, &apos;*&apos;).where({
        ///  &apos;ID &amp;gt;&apos;: 0,
        ///  &apos;ID &amp;lt;=&apos;: 100
        ///  })
        ///  
        ///  オペレータで使用可能な文字は以下の通りです。
        ///  
        ///   &amp;lt;=
        ///   &amp;lt;
        ///   &amp;gt;=
        ///   &amp;gt;
        ///   =
        ///   !=
        ///   like (sqliteの仕様上大文字・小文字を区別しない)
        ///  
        ///  条件を複数指定した場合、全てAND句で結合されます。 AND句以外の条件で結合したい場合は文字列で条件を指定して下さい。
        ///  
        ///  エスケープ文字の指定方法
        ///  キーに『カラム名[半角スペース]オペレータ[半角スペース]エスケープ文字』のように指定します。 
        ///  エスケープ文字はクォートやダブルクォートで囲わず、エスケープ文字のみ指定して下さい。
        ///  
        ///  例. $をエスケープ文字として指定する場合
        ///  
        ///  db.select(&apos;USER&apos;, &apos;*&apos;).where({
        ///  &apos;NAME like $&apos;: &apos;SUZUKI$&apos;
        ///  });
        ///  
        ///  
        ///  文字列の場合、SQLステートメントに追加するWHERE文を指定します。
        ///  
        ///  例. IDが0以上100以下。
        ///  
        ///  db.select(&apos;USER&apos;, &apos;*&apos;).where(&apos;ID &amp;gt;= 10 AND ID &amp;lt;= 100&apos;);
        ///  
        /// </summary>
        /// <param  name = "whereObj" type = "Object|String" >
        ///  条件
        /// </param>
        /// <returns  type = "Select" >
        ///  Selectオブジェクト
        /// </returns>
        if (!$.isPlainObject(whereObj) && typeof whereObj !== 'string') {
          throw new throwFwError(ERR_CODE_INVALID_PARAM_TYPE, ['Select', 'where']);
        }
        this._where = whereObj;
        return this;
      },
      orderBy: function(orderByObj) {
        /// <summary>
        ///  ORDER
        ///  BY句を設定します。
        ///  
        ///  ソート対象のカラムが一つの場合は文字列、複数の場合は配列で指定します。
        ///  
        ///  例.IDを降順でソートする場合
        ///  
        ///  db.select(&apos;USER&apos;, &apos;*&apos;).orderBy(&apos;ID DESC&apos;);
        ///  
        ///  例.IDを降順、NAMEを昇順でソートする場合
        ///  
        ///  db.select(&apos;USER&apos;, &apos;*&apos;).orderBy([&apos;ID DESC&apos;, &apos;NAME ASC&apos;]);
        ///  
        ///  なお、複数の条件が指定されている場合、ソートは配列の先頭に指定されたカラムから順番に実行されます。
        /// </summary>
        /// <param  name = "orderBy" type = "Array|String" >
        ///  条件
        /// </param>
        /// <returns  type = "Select" >
        ///  Selectオブジェクト
        /// </returns>
        if (!$.isPlainObject(orderByObj) && typeof orderByObj !== 'string') {
          throw new throwFwError(ERR_CODE_INVALID_PARAM_TYPE, ['Select', 'orderBy']);
        }
        this._orderBy = wrapInArray(orderByObj);
        return this;
      },
      execute: function() {
        /// <summary>
        ///  このオブジェクトに設定された情報からSQLステートメントとパラメータを生成し、SQLを実行します。
        ///  
        ///  実行結果は、Promiseオブジェクトのprogress()に指定したコールバック関数または、done()に指定したコールバック関数に、検索結果を保持するインスタンスが返されます。
        ///  
        ///  検索結果へのアクセスは以下のように実行します。
        ///  
        ///  db.insert(&apos;USER&apos;, {ID:10, NAME:&apos;TANAKA&apos;}).execute().done(function(rows)
        ///  　rows.item(0).ID // 検索にマッチした1件目のレコードのID
        ///  　rows.item(0).NAME // 検索にマッチした1件目のレコードのNAME
        ///  });
        ///  
        ///  また、progress()に指定したコールバック関数の第二引数には、トランザクションオブジェクトが格納され、このオブジェクトを使用することで、トランザクションを引き継ぐことができます。
        ///  
        ///  db.select(&apos;PRODUCT&apos;, [&apos;ID&apos;]).where({NAME: &apos;ball&apos;}).execute().progress(function(rs, tx)
        ///  　db.update(&apos;STOCK&apos;, {PRICE: 2000}, tx).where({ID: rs.item(0).ID}).execute();
        ///  });
        ///  
        ///  db.select().execute()で返ってきたトランザクションを、db.update()の第三引数に指定することで、db.selec()とdb.update()は同一トランザクションで実行されます。
        /// </summary>
        /// <returns  type = "Promise" >
        ///  Promiseオブジェクト
        /// </returns>
        var that = this;
        var build = function() {
          that._statement = createSelectStatementAndParameters(that._params, that._tableName, that._columns, that._where, that._orderBy);
        };
        var df = getDeferred();
        var txw = this._txw;
        var executed = this._executed;
        var resultSet = null;
        if (txw._runTransaction()) {
          txw._addTask(df);
          build();
          checkSqlExecuted(executed);
          txw._execute(this._statement, this._params, function(innerTx, rs) {
            resultSet = rs.rows;
            txw._setResult(resultSet);
            df.notify(resultSet, txw);
          });
        } else {
          txw._execute(function(tx) {
            txw._addTask(df);
            build();
            checkSqlExecuted(executed);
            txw._tx = tx;
            tx.executeSql(that._statement, that._params, function(innerTx, rs) {
              resultSet = rs.rows;
              txw._setResult(resultSet);
              df.notify(resultSet, txw);
            });
          }, function(e) {
            transactionErrorCallback(txw, e);
          }, function() {
            transactionSuccessCallback(txw);
          });
        }
        this._executed = true;
        return df.promise();
      }
    });
    function Insert(txw, tableName, values) {
      /// <summary>
      ///  指定されたテーブルに対して、登録処理(INSERT)を行うクラス。
      ///  
      ///  このオブジェクトは自分でnewすることはありません。
      ///  h5.api.sqldb.open().insert()を呼び出すと、このクラスのインスタンスが返されます。
      /// </summary>
      this._txw = txw;
      this._tableName = tableName;
      this._values = values ? wrapInArray(values) : [];
      this._statement = [];
      this._params = [];
      this._df = getDeferred();
      this._executed = false;
    }
    Insert.prototype = new SqlExecutor();
    $.extend(Insert.prototype, {
      execute: function() {
        /// <summary>
        ///  このオブジェクトに設定された情報からSQLステートメントとパラメータを生成し、SQLを実行します。
        ///  
        ///  実行結果は、Promiseオブジェクトのprogress()に指定したコールバック関数または、done()に指定したコールバック関数に、登録に成功したレコードのIDを持つ配列が返されます。
        ///  
        ///  検索結果へのアクセスは以下のように実行します。
        ///  
        ///  db.insert(&apos;USER&apos;, {ID:10, NAME:&apos;TANAKA&apos;}).execute().done(function(rows)
        ///  　rows.item(0).ID // 検索にマッチした1件目のレコードのID
        ///  　rows.item(0).NAME // 検索にマッチした1件目のレコードのNAME
        ///  });
        ///  
        ///  また、progress()に指定したコールバック関数の第二引数には、トランザクションオブジェクトが格納され、このオブジェクトを使用することで、トランザクションを引き継ぐことができます。
        ///  
        ///  db.select(&apos;STOCK&apos;, {ID:10, NAME:&apos;ballA&apos;}).execute().progress(function(rs, tx) // ※1
        ///  　db.insert(&apos;STOCK&apos;, {ID:11, NAME:&apos;ballB&apos;}, tx).execute(); // ※2
        ///  });
        ///  
        ///  ※1のprogress()で返ってきたトランザクション(tx)を、※2のinsert()の第三引数に指定することで、2つのdb.insert()は同一トランザクションで実行されます。
        /// </summary>
        /// <returns  type = "Promise" >
        ///  Promiseオブジェクト
        /// </returns>
        var that = this;
        var build = function() {
          var valueObjs = that._values;
          if (valueObjs.length === 0) {
            that._statement.push(h5.u.str.format(INSERT_SQL_EMPTY_VALUES, that._tableName));
            that._params.push([]);
            return;
          }
          for (var i = 0, len = valueObjs.length; i < len; i++) {
            var valueObj = valueObjs[i];
            if (!valueObj) {
              that._statement.push(h5.u.str.format(INSERT_SQL_EMPTY_VALUES, that._tableName));
              that._params.push([]);
            } else if ($.isPlainObject(valueObj)) {
              var values = [];
              var columns = [];
              var params = [];
              for (var prop in valueObj) {
                if (!valueObj.hasOwnProperty(prop)) {
                  continue;
                }
                values.push('?');
                columns.push(prop);
                params.push(valueObj[prop]);
              }
              that._statement.push(h5.u.str.format(INSERT_SQL_FORMAT, that._tableName, columns.join(', '), values.join(', ')));
              that._params.push(params);
            }
          }
        };
        var df = getDeferred();
        var txw = this._txw;
        var executed = this._executed;
        var resultSet = null;
        var insertRowIds = [];
        var index = 0;
        function executeSql() {
          if (that._statement.length === index) {
            resultSet = insertRowIds;
            txw._setResult(resultSet);
            df.notify(resultSet, txw);
            return;
          }
          txw._execute(that._statement[index], that._params[index], function(innerTx, rs) {
            index++;
            insertRowIds.push(rs.insertId);
            executeSql();
          });
        }
        if (txw._runTransaction()) {
          txw._addTask(df);
          build();
          checkSqlExecuted(executed);
          executeSql();
        } else {
          txw._execute(function(tx) {
            txw._addTask(df);
            build();
            checkSqlExecuted(executed);
            txw._tx = tx;
            executeSql();
          }, function(e) {
            transactionErrorCallback(txw, e);
          }, function() {
            transactionSuccessCallback(txw);
          });
        }
        this._executed = true;
        return df.promise();
      }
    });
    function Update(txw, tableName, value) {
      /// <summary>
      ///  指定されたテーブルに対して、更新処理(UPDATE)を行うクラス。
      ///  
      ///  このオブジェクトは自分でnewすることはありません。
      ///  h5.api.sqldb.open().update()を呼び出すと、このクラスのインスタンスが返されます。
      /// </summary>
      this._txw = txw;
      this._tableName = tableName;
      this._value = value;
      this._where = null;
      this._statement = null;
      this._params = [];
      this._df = getDeferred();
      this._executed = false;
    }
    Update.prototype = new SqlExecutor();
    $.extend(Update.prototype, {
      where: function(whereObj) {
        /// <summary>
        ///  WHERE句を設定します。
        ///  
        ///  条件は以下の方法で設定できます。
        ///  
        ///  オブジェク
        ///  文字列
        ///  
        ///  オブジェクトの場合、キーに『カラム名[半角スペース]オペレータ』、バリューに値を指定します。
        ///  
        ///  例. IDが0以上100以下。
        ///  
        ///  db.update(&apos;USER&apos;,
        ///  NAME: &apos;TANAKA&apos;
        ///  }).where({
        ///  &apos;ID &amp;gt;&apos;: 0,
        ///  &apos;ID &amp;lt;=&apos;: 100
        ///  })
        ///  
        ///  オペレータで使用可能な文字は以下の通りです。
        ///  
        ///   &amp;lt;=
        ///   &amp;lt;
        ///   &amp;gt;=
        ///   &amp;gt;
        ///   =
        ///   !=
        ///   like (sqliteの仕様上大文字・小文字を区別しない)
        ///  
        ///  条件を複数指定した場合、全てAND句で結合されます。 AND句以外の条件で結合したい場合は文字列で条件を指定して下さい。
        ///  
        ///  エスケープ文字の指定方法
        ///  キーに『カラム名[半角スペース]オペレータ[半角スペース]エスケープ文字』のように指定します。 
        ///  エスケープ文字はクォートやダブルクォートで囲わず、エスケープ文字のみ指定して下さい。
        ///  
        ///  例. $をエスケープ文字として指定する場合
        ///  
        ///  db.update(&apos;USER&apos;,
        ///  NAME: &apos;TANAKA&apos;
        ///  }).where({
        ///  &apos;NAME like $&apos;: &apos;SUZUKI$&apos;
        ///  });
        ///  
        ///  
        ///  文字列の場合、SQLステートメントに追加するWHERE文を指定します。
        ///  
        ///  例. IDが0以上100以下。
        ///  
        ///  db.update(&apos;USER&apos;).where(&apos;ID &amp;gt;= 10 AND ID &amp;lt;= 100&apos;)
        ///  
        /// </summary>
        /// <param  name = "whereObj" type = "Object|String" >
        ///  条件
        /// </param>
        /// <returns  type = "Update" >
        ///  Updateオブジェクト
        /// </returns>
        if (!$.isPlainObject(whereObj) && typeof whereObj !== 'string') {
          throw new throwFwError(ERR_CODE_INVALID_PARAM_TYPE, ['Update', 'where']);
        }
        this._where = whereObj;
        return this;
      },
      execute: function() {
        /// <summary>
        ///  このオブジェクトに設定された情報からSQLステートメントとパラメータを生成し、SQLを実行します。
        ///  
        ///  実行結果は、Promiseオブジェクトのprogress()に指定したコールバック関数または、done()に指定したコールバック関数に、更新されたレコードの件数が返されます。
        ///  
        ///  db.update(&apos;USER&apos;, {NAME:TANAKA}).where({ID:10}).execute().done(function(rowsAffected)
        ///  　rowsAffected // 更新されたレコードの行数(Number型)
        ///  });
        ///  
        ///  また、progress()に指定したコールバック関数の第二引数には、トランザクションオブジェクトが格納され、このオブジェクトを使用することで、トランザクションを引き継ぐことができます。
        ///  
        ///  db.select(&apos;PRODUCT&apos;, [&apos;ID&apos;]).where({NAME: &apos;ball&apos;}).execute().progress(function(rs, tx)
        ///  　db.update(&apos;STOCK&apos;, {PRICE: 2000}, tx).where({ID: rs.item(0).ID}).execute();
        ///  });
        ///  
        ///  db.select().execute()で返ってきたトランザクションを、db.update()の第三引数に指定することで、db.select()とdb.update()は同一トランザクションで実行されます。
        /// </summary>
        /// <returns  type = "Promise" >
        ///  Promiseオブジェクト
        /// </returns>
        var that = this;
        var build = function() {
          var whereObj = that._where;
          var valueObj = that._value;
          var columns = [];
          for (var prop in valueObj) {
            if (!valueObj.hasOwnProperty(prop)) {
              continue;
            }
            columns.push(prop + ' = ?');
            that._params.push(valueObj[prop]);
          }
          that._statement = h5.u.str.format(UPDATE_SQL_FORMAT, that._tableName, columns.join(', '));
          if ($.isPlainObject(whereObj)) {
            var conditions = [];
            createConditionAndParameters(whereObj, conditions, that._params);
            that._statement += (' WHERE ' + conditions.join(' AND '));
          } else if (typeof whereObj === 'string') {
            that._statement += (' WHERE ' + whereObj);
          }
        };
        var df = getDeferred();
        var txw = this._txw;
        var executed = this._executed;
        var resultSet = null;
        if (txw._runTransaction()) {
          txw._addTask(df);
          build();
          checkSqlExecuted(executed);
          txw._execute(this._statement, this._params, function(innerTx, rs) {
            resultSet = rs.rowsAffected;
            txw._setResult(resultSet);
            df.notify(resultSet, txw);
          });
        } else {
          txw._execute(function(tx) {
            txw._addTask(df);
            build();
            checkSqlExecuted(executed);
            txw._tx = tx;
            tx.executeSql(that._statement, that._params, function(innerTx, rs) {
              resultSet = rs.rowsAffected;
              txw._setResult(resultSet);
              df.notify(resultSet, txw);
            });
          }, function(e) {
            transactionErrorCallback(txw, e);
          }, function() {
            transactionSuccessCallback(txw);
          });
        }
        this._executed = true;
        return df.promise();
      }
    });
    function Del(txw, tableName) {
      /// <summary>
      ///  指定されたテーブルに対して、削除処理(DELETE)を行うクラス。
      ///  
      ///  このオブジェクトは自分でnewすることはありません。
      ///  h5.api.sqldb.open().del()を呼び出すと、このクラスのインスタンスが返されます。
      ///  
      ///  deleteは予約語なため、Delとしています。
      /// </summary>
      this._txw = txw;
      this._tableName = tableName;
      this._where = null;
      this._statement = null;
      this._params = [];
      this._df = getDeferred();
      this._executed = false;
    }
    Del.prototype = new SqlExecutor();
    $.extend(Del.prototype, {
      where: function(whereObj) {
        /// <summary>
        ///  WHERE句を設定します。
        ///  
        ///  条件は以下の方法で設定できます。
        ///  
        ///  オブジェクト
        ///  文字列
        ///  
        ///  オブジェクトの場合、キーに『カラム名[半角スペース]オペレータ』、バリューに値を指定します。
        ///  
        ///  例. IDが0以上100以下。
        ///  
        ///  db.delete(&apos;USER&apos;).where({&apos;ID &amp;gt;&apos;:0, &apos;ID &amp;lt;=&apos;:100})
        ///  
        ///  オペレータで使用可能な文字は以下の通りです。
        ///  
        ///   &amp;lt;=
        ///   &amp;lt;
        ///   &amp;gt;=
        ///   &amp;gt;
        ///   =
        ///   !=
        ///   like (sqliteの仕様上大文字・小文字を区別しない)
        ///  
        ///  条件を複数指定した場合、全てAND句で結合されます。 AND句以外の条件で結合したい場合は文字列で条件を指定して下さい。
        ///  
        ///  エスケープ文字の指定方法
        ///  キーに『カラム名[半角スペース]オペレータ[半角スペース]エスケープ文字』のように指定します。 
        ///  エスケープ文字はクォートやダブルクォートで囲わず、エスケープ文字のみ指定して下さい。
        ///  
        ///  例. $をエスケープ文字として指定する場合
        ///  
        ///  db.delete(&apos;USER&apos;).where({&apos;NAME like $&apos;: &apos;SUZUKI$&apos;});
        ///  
        ///  
        ///  文字列の場合、SQLステートメントに追加するWHERE文を指定します。
        ///  
        ///  例. IDが0以上100以下。
        ///  
        ///  db.delete(&apos;USER&apos;).where(&apos;ID &amp;gt;= 10 AND ID &amp;lt;= 100&apos;)
        ///  
        /// </summary>
        /// <param  name = "whereObj" type = "Object|String" >
        ///  条件
        /// </param>
        /// <returns  type = "Del" >
        ///  Delオブジェクト
        /// </returns>
        if (!$.isPlainObject(whereObj) && typeof whereObj !== 'string') {
          throw new throwFwError(ERR_CODE_INVALID_PARAM_TYPE, ['Del', 'where']);
        }
        this._where = whereObj;
        return this;
      },
      execute: function() {
        /// <summary>
        ///  このオブジェクトに設定された情報からSQLステートメントとパラメータを生成し、SQLを実行します。
        ///  
        ///  実行結果は、Promiseオブジェクトのprogress()に指定したコールバック関数または、done()に指定したコールバック関数に、削除されたレコードの件数が返されます。
        ///  
        ///  db.del(&apos;USER&apos;).where({ID:10}).execute().done(function(rowsAffected)
        ///  　rowsAffected // 削除されたレコードの行数(Number型)
        ///  });
        ///  
        ///  また、progress()に指定したコールバック関数の第二引数には、トランザクションオブジェクトが格納され、このオブジェクトを使用することで、トランザクションを引き継ぐことができます。
        ///  
        ///  db.select(&apos;PRODUCT&apos;, [&apos;ID&apos;]).where({NAME: &apos;ball&apos;}).execute().progress(function(rs, tx)
        ///  　db.del(&apos;STOCK&apos;, tx).where({ID: rs.item(0).ID}).execute();
        ///  });
        ///  
        ///  db.select().execute()で返ってきたトランザクションを、db.del()の第二引数に指定することで、db.select()とdb.del()は同一トランザクションで実行されます。
        /// </summary>
        /// <returns  type = "Promise" >
        ///  Promiseオブジェクト
        /// </returns>
        var that = this;
        var build = function() {
          var whereObj = that._where;
          that._statement = h5.u.str.format(DELETE_SQL_FORMAT, that._tableName);
          if ($.isPlainObject(whereObj)) {
            var conditions = [];
            createConditionAndParameters(whereObj, conditions, that._params);
            that._statement += (' WHERE ' + conditions.join(' AND '));
          } else if (typeof whereObj === 'string') {
            that._statement += (' WHERE ' + whereObj);
          }
        };
        var df = getDeferred();
        var txw = this._txw;
        var executed = this._executed;
        var resultSet = null;
        if (txw._runTransaction()) {
          txw._addTask(df);
          build();
          checkSqlExecuted(executed);
          txw._execute(this._statement, this._params, function(innerTx, rs) {
            resultSet = rs.rowsAffected;
            txw._setResult(resultSet);
            df.notify(resultSet, txw);
          });
        } else {
          txw._execute(function(tx) {
            txw._addTask(df);
            build();
            checkSqlExecuted(executed);
            txw._tx = tx;
            tx.executeSql(that._statement, that._params, function(innerTx, rs) {
              resultSet = rs.rowsAffected;
              txw._setResult(resultSet);
              df.notify(resultSet, txw);
            });
          }, function(e) {
            transactionErrorCallback(txw, e);
          }, function() {
            transactionSuccessCallback(txw);
          });
        }
        return df.promise();
      }
    });
    function Sql(txw, statement, params) {
      /// <summary>
      ///  指定されたSQLステートメントを実行するクラス。
      ///  
      ///  このオブジェクトは自分でnewすることはありません。
      ///  h5.api.sqldb.open().sql()を呼び出すと、このクラスのインスタンスが返されます。
      /// </summary>
      this._txw = txw;
      this._statement = statement;
      this._params = params || [];
      this._df = getDeferred();
      this._executed = false;
    }
    Sql.prototype = new SqlExecutor();
    $.extend(Sql.prototype, {
      execute: function() {
        /// <summary>
        ///  このオブジェクトに設定された情報からSQLステートメントとパラメータを生成し、SQLを実行します。
        ///  
        ///  実行結果は、戻り値であるPromiseオブジェクトのprogress()に指定したコールバック関数または、done()に指定したコールバック関数に、実行結果を保持するオブジェクトが返されます。
        ///  
        ///  実行結果オブジェクトは、以下のプロパティを持っています。
        ///  
        ///  
        ///  プロパティ名
        ///  説明
        ///  
        ///  
        ///  rows
        ///  検索(SELECT)を実行した場合、このプロパティに結果が格納されます。
        ///  
        ///  
        ///  insertId
        ///  登録(INSERT)を実行した場合、このプロパティに登録したレコードのIDが格納されます。
        ///  
        ///  
        ///  rowsAffected
        ///  削除(DELETE)や更新(UPDATE)した場合、このプロパティに変更のあったレコードの件数が格納されます。
        ///  
        ///  
        ///  
        ///  例.検索結果の取得
        ///  
        ///  db.sql(&apos;SELECT FROM USER&apos;).execute().done(function(rs)
        ///  　rs.rows // SQLResultSetRowList
        ///  　rs.insertId // Number
        ///  　rs.rowsAffected // Number
        ///  });
        ///  
        ///  
        ///  SQLResultSetRowListは、以下のプロパティを持っています。
        ///  
        ///  
        ///  プロパティ名
        ///  説明
        ///  
        ///  
        ///  length
        ///  検索にマッチしたレコードの件数
        ///  
        ///  
        ///  rows
        ///  検索結果
        ///  
        ///  
        ///  
        ///  例.検索結果の取得する
        ///  
        ///  db.sql(&apos;SELECT ID, NAME FROM USER&apos;).execute().done(function(rs)
        ///  　rs.rows.item(0).ID // 検索にマッチした1件目のレコードのID
        ///  　rs.rows.item(0).NAME // 検索にマッチした1件目のレコードのNAME
        ///  });
        ///  
        ///  また、progress()に指定したコールバック関数の第二引数には、トランザクションオブジェクトが格納され、このオブジェクトを使用することで、トランザクションを引き継ぐことができます。
        ///  
        ///  例.同一トランザクションでdb.insert()とdb.sql()を実行する
        ///  
        ///  db.select(&apos;PRODUCT&apos;, [&apos;ID&apos;]).where({NAME: &apos;ball&apos;}).execute().progress(function(rs, tx)
        ///  　db.sql(&apos;UPDATE STOCK SET PRICE = 2000&apos;, tx).where({ID: rs.item(0).ID}).execute();
        ///  });
        ///  
        ///  db.select().execute()で返ってきたトランザクションを、db.sql()の第三引数に指定することで、db.select()とdb.sql()は同一トランザクションで実行されます。
        /// </summary>
        /// <returns  type = "Promise" >
        ///  Promiseオブジェクト
        /// </returns>
        var df = getDeferred();
        var txw = this._txw;
        var executed = this._executed;
        var statement = this._statement;
        var params = this._params;
        var resultSet = null;
        if (txw._runTransaction()) {
          txw._addTask(df);
          checkSqlExecuted(executed);
          txw._execute(statement, params, function(tx, rs) {
            resultSet = rs;
            txw._setResult(resultSet);
            df.notify(resultSet, txw);
          });
        } else {
          txw._execute(function(tx) {
            txw._addTask(df);
            checkSqlExecuted(executed);
            txw._tx = tx;
            tx.executeSql(statement, params, function(innerTx, rs) {
              resultSet = rs;
              txw._setResult(resultSet);
              df.notify(resultSet, txw);
            });
          }, function(e) {
            transactionErrorCallback(txw, e);
          }, function() {
            transactionSuccessCallback(txw);
          });
        }
        this._executed = true;
        return df.promise();
      }
    });
    function Transaction(txw) {
      /// <summary>
      ///  指定された複数のSQLを同一トランザクションで実行するクラス。
      ///  
      ///  このオブジェクトは自分でnewすることはありません。
      ///  h5.api.sqldb.open().transaction()を呼び出すと、このクラスのインスタンスが返されます。
      /// </summary>
      this._txw = txw;
      this._queue = [];
      this._df = getDeferred();
      this._executed = false;
    }
    Transaction.prototype = new SqlExecutor();
    $.extend(Transaction.prototype, {
      add: function(task) {
        /// <summary>
        ///  1トランザクションで処理したいSQLをタスクに追加します。
        ///  
        ///  このメソッドには、以下のクラスのインスタンスを追加することができます。
        ///  
        ///  Insert
        ///  Update
        ///  Del
        ///  Select
        ///  Sql
        ///  
        /// </summary>
        /// <param  name = "task" type = "Any" >
        ///  Insert/Update/Del/Select/Sqlクラスのインスタンス
        /// </param>
        /// <returns  type = "Transaction" >
        ///  Transactionオブジェクト
        /// </returns>
        if (!(task instanceof SqlExecutor)) {
          throw new throwFwError(ERR_CODE_INVALID_TRANSACTION_TARGET);
        }
        this._queue.push(task);
        return this;
      },
      execute: function() {
        /// <summary>
        ///  add()で追加された順にSQLを実行します。
        ///  
        ///  実行結果は、戻り値であるPromiseオブジェクトのprogress()に指定したコールバック関数、またはdone()に指定したコールバック関数に返されます。
        ///  
        ///  db.transaction()
        ///  .add(db.insert(&apos;USER&apos;, {ID:10, NAME:TANAKA}))
        ///  .add(db.insert(&apos;USER&apos;, {ID:11, NAME:YOSHIDA}))
        ///  .add(db.insert(&apos;USER&apos;, {ID:12, NAME:SUZUKI})).execute().done(function(rs)
        ///  　rs // 第一引数: 実行結果
        ///  });
        ///  
        ///  実行結果は配列(Array)で返され、結果の格納順序は、add()で追加した順序に依存します。
        ///  上記例の場合、3件 db.insert()をadd()で追加しているので、実行結果rsには3つのROWIDが格納されています。( [1, 2, 3]のような構造になっている
        ///  
        ///  また、progress()に指定したコールバック関数の第二引数には、トランザクションオブジェクトが格納され、このオブジェクトを使用することで、トランザクションを引き継ぐことができます。
        ///  
        ///  db.select(&apos;PRODUCT&apos;, [&apos;ID&apos;]).where({NAME: &apos;ball&apos;}).execute().progress(function(rs, tx)
        ///  　db.transaction(tx)
        ///  　　.add(db.update(&apos;UPDATE STOCK SET PRICE = 2000&apos;).where({ID: rs.item(0).ID}))
        ///  　　.execute();
        ///  });
        ///  
        ///  select().execute()で返ってきたトランザクションを、db.transaction()の引数に指定することで、db.select()とdb.transaction()は同一トランザクションで実行されます。
        /// </summary>
        /// <returns  type = "Promise" >
        ///  Promiseオブジェクト
        /// </returns>
        var df = this._df;
        var txw = this._txw;
        var queue = this._queue;
        var executed = this._executed;
        var index = 0;
        var tasks = null;
        function createTransactionTask(txObj) {
          function TransactionTask(tx) {
            this._txw = new SQLTransactionWrapper(null, tx);
          }
          var ret = [];
          for (var i = 0, len = queue.length; i < len; i++) {
            TransactionTask.prototype = queue[i];
            ret.push(new TransactionTask(txObj));
          }
          return ret;
        }
        function executeSql() {
          if (tasks.length === index) {
            var results = [];
            for (var j = 0, len = tasks.length; j < len; j++) {
              var result = tasks[j]._txw._tasks;
              results.push(result[0].result);
            }
            txw._setResult(results);
            df.notify(results, txw);
            return;
          }
          tasks[index].execute().progress(function(rs, innerTx) {
            index++;
            executeSql();
          });
        }
        if (txw._runTransaction()) {
          txw._addTask(df);
          checkSqlExecuted(executed);
          tasks = createTransactionTask(txw._tx);
          executeSql();
        } else {
          txw._execute(function(tx) {
            txw._addTask(df);
            checkSqlExecuted(executed);
            tasks = createTransactionTask(tx);
            txw._tx = tx;
            executeSql();
          }, function(e) {
            transactionErrorCallback(txw, e);
          }, function() {
            transactionSuccessCallback(txw);
          });
        }
        this._executed = true;
        return df.promise();
      },
      promise: function() {
        return this._df.promise();
      }
    });
    function DatabaseWrapper(db) {
      /// <summary>
      ///  Database拡張クラス
      ///  
      ///  このオブジェクトは自分でnewすることはありません。
      ///  h5.api.sqldb.open()を呼び出すと、このクラスのインスタンスが返されます。
      /// </summary>
      /// <param  name = "db" type = "Database" >
      ///  openDatabase()が返すネイティブのDatabaseオブジェクト
      /// </param>
      this._db = db;
    }
    $.extend(DatabaseWrapper.prototype, {
      select: function(tableName, columns, txw) {
        /// <summary>
        ///  指定されたテーブルに対して、検索処理(SELECT)を行うためのオブジェクトを生成します。
        /// </summary>
        /// <param  name = "tableName" type = "String" >
        ///  テーブル名
        /// </param>
        /// <param  name = "columns" type = "Array" >
        ///  カラム
        /// </param>
        /// <param  name = "[txw]" type = "SQLTransactionWrapper" >
        ///  トランザクション
        /// </param>
        /// <returns  type = "Select" >
        ///  SELECTオブジェクト
        /// </returns>
        checkTableName('select', tableName);
        checkTransaction('select', txw);
        if (!$.isArray(columns) && columns !== '*') {
          throw new throwFwError(ERR_CODE_INVALID_COLUMN_NAME, 'select');
        }
        return new Select(txw ? txw : new SQLTransactionWrapper(this._db, null), tableName, columns);
      },
      insert: function(tableName, values, txw) {
        /// <summary>
        ///  指定されたテーブルに対して、登録処理(INSERT)を行うためのオブジェクトを生成します。
        ///  
        ///  第二引数valuesの指定方法
        ///  
        ///  1テーブルに1件INSERTを行う場合はオブジェクトで値を指定します。また、1テーブルに複数件INSERTを行う場合は配列で値を指定します。
        ///  
        ///  オブジェクトで指定する場合、シンタックスは以下のようになります。
        ///  
        ///  {カラム名:登録する値, ...}
        ///  
        ///  
        ///  例.USERテーブルに、1件レコードをINSERTする。
        ///  
        ///  db.insert(&apos;USER&apos;,
        ///  ID: 10,
        ///  NAME: &apos;TANAKA&apos;
        ///  }).execute();
        ///  
        ///  
        ///  配列で指定する場合、シンタックスは以下のようになります。
        ///  
        ///  [{カラム名:登録する値, ...}, {カラム名:登録する値, ...}, ...]
        ///  
        ///  
        ///  例.USERテーブルに、3件レコードをINSERTする。
        ///  
        ///  db.insert(&apos;USER&apos;, [{
        ///  ID: 1,
        ///  NAME: &apos;TANAKA&apos;
        ///  },
        ///  ID: 2,
        ///  NAME: &apos;YAMADA&apos;
        ///  },
        ///  ID: 3,
        ///  NAME: &apos;SUZUKI&apos;
        ///  }]).execute();
        ///  
        /// </summary>
        /// <param  name = "tableName" type = "String" >
        ///  テーブル名
        /// </param>
        /// <param  name = "values" type = "Object|Array" >
        ///  値(登録情報を保持するオブジェクトまたは、登録情報のオブジェクトを複数保持する配列)
        /// </param>
        /// <param  name = "[txw]" type = "SQLTransactionWrapper" >
        ///  トランザクション
        /// </param>
        /// <returns  type = "Insert" >
        ///  INSERTオブジェクト
        /// </returns>
        checkTableName('insert', tableName);
        checkTransaction('insert', txw);
        if (values && !$.isArray(values) && !$.isPlainObject(values)) {
          throw new throwFwError(ERR_CODE_INVALID_VALUES, 'insert');
        }
        return new Insert(txw ? txw : new SQLTransactionWrapper(this._db, null), tableName, values);
      },
      update: function(tableName, values, txw) {
        /// <summary>
        ///  指定されたテーブルに対して、更新処理(UPDATE)を行うためのオブジェクトを生成します。
        ///  
        ///  第二引数valuesの指定方法
        ///  
        ///  オブジェクトリテラルで以下のように指定します。
        ///  
        ///  カラム名: 更新後の値
        ///  
        ///  
        ///  例.USERテーブルのNAMEカラムを&quot;TANAKA&quot;に更新する。
        ///  
        ///  db.update(&apos;USER&apos;,
        ///  NAME: &apos;TANAKA&apos;
        ///  }).excute();
        ///  
        /// </summary>
        /// <param  name = "tableName" type = "String" >
        ///  テーブル名
        /// </param>
        /// <param  name = "values" type = "Object" >
        ///  カラム
        /// </param>
        /// <param  name = "[txw]" type = "SQLTransactionWrapper" >
        ///  トランザクション
        /// </param>
        /// <returns  type = "Update" >
        ///  Updateオブジェクト
        /// </returns>
        checkTableName('update', tableName);
        checkTransaction('update', txw);
        if (!$.isPlainObject(values)) {
          throw new throwFwError(ERR_CODE_INVALID_VALUES, 'update');
        }
        return new Update(txw ? txw : new SQLTransactionWrapper(this._db, null), tableName, values);
      },
      del: function(tableName, txw) {
        /// <summary>
        ///  指定されたテーブルに対して、削除処理(DELETE)を行うためのオブジェクトを生成します。
        ///  
        ///  deleteは予約語なため、delとしています。
        /// </summary>
        /// <param  name = "tableName" type = "String" >
        ///  テーブル名
        /// </param>
        /// <param  name = "[txw]" type = "SQLTransactionWrapper" >
        ///  トランザクション
        /// </param>
        /// <returns  type = "Del" >
        ///  Delオブジェクト
        /// </returns>
        checkTableName('del', tableName);
        checkTransaction('del', txw);
        return new Del(txw ? txw : new SQLTransactionWrapper(this._db, null), tableName);
      },
      sql: function(statement, parameters, txw) {
        /// <summary>
        ///  指定されたステートメントとパラメータから、SQLを実行するためのオブジェクトを生成します。
        /// </summary>
        /// <param  name = "statement" type = "String" >
        ///  SQLステートメント
        /// </param>
        /// <param  name = "parameters" type = "Array" >
        ///  パラメータ
        /// </param>
        /// <param  name = "[txw]" type = "SQLTransactionWrapper" >
        ///  トランザクション
        /// </param>
        /// <returns  type = "Sql" >
        ///  Sqlオブジェクト
        /// </returns>
        checkTransaction('sql', txw);
        if (typeof statement !== 'string') {
          throw new throwFwError(ERR_CODE_INVALID_STATEMENT, 'sql');
        }
        if (parameters && !$.isArray(parameters)) {
          throw new throwFwError(ERR_CODE_TYPE_NOT_ARRAY, 'sql');
        }
        return new Sql(txw ? txw : new SQLTransactionWrapper(this._db, null), statement, parameters);
      },
      transaction: function(txw) {
        /// <summary>
        ///  指定された複数のSQLを同一トランザクションで実行するためのオブジェクトを生成します。
        /// </summary>
        /// <param  name = "statement" type = "String" >
        ///  テーブル名
        /// </param>
        /// <param  name = "parameters" type = "Array" >
        ///  パラメータ
        /// </param>
        /// <returns  type = "Transaction" >
        ///  Transactionオブジェクト
        /// </returns>
        checkTransaction('sql', txw);
        return new Transaction(txw ? txw : new SQLTransactionWrapper(this._db, null));
      }
    });
    function WebSqlDatabase() {
      // 空コンストラクタ
    }
    $.extend(WebSqlDatabase.prototype, {
      isSupported: !!window.openDatabase,
      open: function(name, version, displayName, estimatedSize) {
        /// <summary>
        ///  データベースに接続します。
        /// </summary>
        /// <param  name = "name" type = "String" >
        ///  データベース名
        /// </param>
        /// <param  name = "[version]" type = "String" >
        ///  バージョン
        /// </param>
        /// <param  name = "[displayName]" type = "String" >
        ///  表示用データベース名
        /// </param>
        /// <param  name = "[estimatedSize]" type = "Number" >
        ///  見込み容量(バイト)
        /// </param>
        /// <returns  type = "DatabaseWrapper" >
        ///  Databaseオブジェクト
        /// </returns>
      /// <summary>
      ///  Web
      ///  SQL Database
      /// </summary>
        if (!this.isSupported) {
          return;
        }
        var conn = openDatabase(name, version, displayName, estimatedSize);
        return new DatabaseWrapper(conn);
      }
    });
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.api', {
      sqldb: new WebSqlDatabase()
    });
  })();
  /* ------ h5.api.storage ------ */
  (function() {
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    // =============================
    // Development Only
    // =============================
    var fwLogger = h5.log.createLogger('h5.api.storage');
    /* del begin */
    /* del end */
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    // =============================
    // Functions
    // =============================
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    function WebStorage(storage) {
      this._storage = storage;
    }
    $.extend(WebStorage.prototype, {
      getLength: function() {
        /// <summary>
        ///  ストレージに保存されている、キーと値のペアの数を取得します。
        /// </summary>
        /// <returns  type = "Number" >
        ///  キーとペアの数
        /// </returns>
      /// <summary>
      ///  Web
      ///  Storage
      /// </summary>
        return this._storage.length;
      },
      key: function(index) {
        /// <summary>
        ///  指定されたインデックスにあるキーを、ストレージから取得します。
        /// </summary>
        /// <param  name = "index" type = "Number" >
        ///  インデックス
        /// </param>
        /// <returns  type = "String" >
        ///  キー
        /// </returns>
        return this._storage.key(index);
      },
      getItem: function(key) {
        /// <summary>
        ///  指定されたキーに紐付く値を、ストレージから取得します。
        ///  
        ///  自動的にsetItem()実行時に保存したときの型に戻します。
        /// </summary>
        /// <param  name = "key" type = "String" >
        ///  キー
        /// </param>
        /// <returns  type = "Any" >
        ///  キーに紐付く値
        /// </returns>
        return h5.u.obj.deserialize(this._storage.getItem(key));
      },
      setItem: function(key, value) {
        /// <summary>
        ///  指定されたキーで、値をストレージに保存します。
        ///  
        ///  値は、シリアライズして保存します。保存できる型はh5.u.obj.serialize()を参照してください。
        ///  
        /// </summary>
        /// <param  name = "key" type = "String" >
        ///  キー
        /// </param>
        /// <param  name = "value" type = "Any" >
        ///  値
        /// </param>
        this._storage.setItem(key, h5.u.obj.serialize(value));
      },
      removeItem: function(key) {
        /// <summary>
        ///  指定されたキーに紐付く値を、ストレージから削除します。
        /// </summary>
        /// <param  name = "key" type = "String" >
        ///  キー
        /// </param>
        this._storage.removeItem(key);
      },
      clear: function() {
        /// <summary>
        ///  ストレージに保存されている全てのキーとそれに紐付く値を全て削除します。
        /// </summary>
        this._storage.clear();
      },
      each: function(callback) {
        /// <summary>
        ///  現在ストレージに保存されているオブジェクト数分、キーと値をペアで取得します。
        /// </summary>
        /// <param  name = "callback" type = "Function" >
        ///  インデックス,
        ///  キー, 値 を引数に持つコールバック関数
        /// </param>
        var storage = this._storage;
        for (var i = 0, len = storage.length; i < len; i++) {
          var k = storage.key(i);
          callback(i, k, this.getItem(k));
        }
      }
    });
    // =============================
    // Expose to window
    // =============================
    h5.u.obj.expose('h5.api.storage', {
        // APIはlocalStorageとsessionStorageに分かれており、本来であればそれぞれサポート判定する必要があるが、
        // 仕様ではStorage APIとして一つに扱われておりかつ、テストした限りでは片方のみ使用できるブラウザが見つからない為、一括りに判定している。
      isSupported: !!window.localStorage,
      local: new WebStorage(window.localStorage),
      session: new WebStorage(window.sessionStorage)
    });
  })();
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
   * hifive
   */
  /* ------ h5.dev.api.geo ------ */
  (function() {
    if (!h5) {
      return;
    }
    // =========================================================================
    //
    // Constants
    //
    // =========================================================================
    // =============================
    // Production
    // =============================
    // =============================
    // Development Only
    // =============================
    // =========================================================================
    //
    // Cache
    //
    // =========================================================================
    // =========================================================================
    //
    // Privates
    //
    // =========================================================================
    // =============================
    // Variables
    // =============================
    var originalAPI = {};
    var _dfds = [];
    var _dfdID = 0;
    var _timerID = null;
    var _watchPointer = 0;
    // =============================
    // Functions
    // =============================
    function createPosition(params) {
      /// <summary>
      ///  以下の構造の位置情報オブジェクトを生成します
      ///  
      ///  
      ///  
      ///  プロパティ名
      ///  説明
      ///  
      ///  
      ///  latitude
      ///  緯度
      ///  
      ///  
      ///  longitude
      ///  経度
      ///  
      ///  
      ///  accuracy
      ///  位置の誤差(m)
      ///  
      ///  
      ///  altitude
      ///  高度(m)
      ///  
      ///  
      ///  altitudeAccuracy
      ///  高度の誤差(m)
      ///  
      ///  
      ///  heading
      ///  方角(0～360)(度)
      ///  
      ///  
      ///  speed
      ///  速度 (m/s)
      ///  
      ///  
      ///  timestamp
      ///  時刻
      ///  
      ///  
      /// </summary>
      /// <returns  type = "Object" >
      ///  位置情報オブジェクト
      /// </returns>
      var param = params || {};
      param.timestamp = param.timestamp || new Date().getTime();
      var coords = param.coords ? param.coords : param;
      param.coords = {
        latitude: coords.latitude || 0,
        longitude: coords.longitude || 0,
        accuracy: coords.accuracy || 0,
        altitude: coords.altitude || null,
        altitudeAccuracy: coords.altitudeAccuracy || null,
        heading: coords.heading || null,
        speed: coords.speed || null
      };
      return param;
    }
    // =========================================================================
    //
    // Body
    //
    // =========================================================================
    // originalAPI に 元のgetCurrentPositionとwatchPositionをとっておく
    originalAPI.getCurrentPosition = h5.api.geo.getCurrentPosition;
    originalAPI.watchPosition = h5.api.geo.watchPosition;
    function DummyPositionError() {
      /// <summary>
      /// </summary>
      this.PERMISSION_DENIED = 1;
      this.POSITION_UNAVALABLE = 2;
      this.TIMEOUT = 3;
    }
    DummyPositionError.prototype.code = 1;
    DummyPositionError.prototype.message = '';
    function H5GeolocationSupport() {
      // 空コンストラクタ
    }
    $.extend(H5GeolocationSupport.prototype, {
      forceError: false,
      watchIntervalTime: 1000,
      dummyPositions: []
    });
    function getCurrentPosition(option) {
      /// <summary>
      ///  dummyPositionsの先頭の位置情報を返します。dummyPositionsがオブジェクトの場合はdummyPositionsを返します。
      ///  
      ///  このメソッドはh5.api.geo.getCurrentPosition()で呼びます。※ h5.dev.api.geo.getCurrentPosition()ではありません。
      ///  
      ///  
      ///  dummyPositionsに値が設定されていない場合は元のh5.api.geoのメソッドを実行します。
      ///  
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
      /// <returns  type = "Promise" >
      ///  Promiseオブジェクト
      /// </returns>
      var dfd = h5.async.deferred();
      if (h5.dev.api.geo.forceError) {
        setTimeout(function() {
          dfd.reject({
            code: 'forceError'
          });
        }, 0);
        return dfd.promise();
      }
      var dummyPositions = h5.dev.api.geo.dummyPositions;
      if (!dummyPositions || dummyPositions.length === 0) {
        return originalAPI.getCurrentPosition(option);
      }
      // dummyPositionsが配列でない場合も対応する
      var dummyPositions = $.isArray(h5.dev.api.geo.dummyPositions) ? h5.dev.api.geo.dummyPositions : [h5.dev.api.geo.dummyPositions];
      setTimeout(function() {
        if (dummyPositions.length > 0) {
          dfd.resolve(createPosition(dummyPositions[0]));
        } else {
          dfd.reject({
            code: new DummyPositionError().POSITION_UNAVALABLE
          });
        }
      }, 0);
      return dfd.promise();
    }
    function watchPosition(option) {
      /// <summary>
      ///  dummyPositionsの緯度・緯度を順番に返します。
      ///  dummyPositionsの末尾まで到達すると、末尾の要素を返し続けます。
      ///  
      ///  このメソッドはh5.api.geo.watchPosition()で呼びます。※ h5.dev.api.geo.watchtPosition()ではありません。
      ///  
      ///  
      ///  dummyPositionsに値が設定されていない場合は元のh5.api.geoのメソッドを実行します。
      ///  
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
      /// <returns  type = "WatchPositionPromise" >
      ///  WatchPositionPromiseオブジェクト
      /// </returns>
      var dfd = h5.async.deferred();
      if (h5.dev.api.geo.forceError) {
        setTimeout(function() {
          dfd.reject({
            code: 'forceError'
          });
        }, 0);
        return dfd.promise();
      }
      // dummyPositionsが配列でない場合も対応する
      var dummyPos = $.isArray(h5.dev.api.geo.dummyPositions) ? h5.dev.api.geo.dummyPositions : [h5.dev.api.geo.dummyPositions].slice(0);
      if (dummyPos.length === 0) {
        return originalAPI.watchPosition(option);
      }
      var that = this;
      var watchID = _dfdID++;
      // WatchPositionPromiseクラス
      // _watchPositionはこのクラスをプロミス化して返す。
      var WatchPositionPromise = function() {
        // コンストラクタ
      };
      // promiseオブジェクトにunwatchメソッドを付加
      WatchPositionPromise.prototype = {
        // unwatchを呼び出したdeferredを_dfds[]から削除
        unwatch: function() {
          if (!_dfds[watchID]) {
            // deferredオブジェクトが_dfdsに登録されていないのにunwatchが呼ばれる場合は
            // reject()済みであるため、resolve()する必要がない。
            return;
          }
          _dfds[watchID].resolve();
          delete _dfds[watchID];
          setTimeout(function() {
            // deferredオブジェクトがすべてなくなったらタイマーの停止
            // dummyPositionsの見ている位置を0に戻す。
            if ($.isEmptyObject(_dfds)) {
              clearInterval(_timerID);
              _timerID = null;
              _watchPointer = 0;
            }
          }, 0);
        }
      };
      setTimeout(function() {
        if (dummyPos.length > 0) {
          _dfds[watchID] = dfd;
          if (_timerID === null) {
            var intervalFunc = function() {
              var pos;
              if (_watchPointer >= dummyPos.length) {
                pos = dummyPos[dummyPos.length - 1];
              } else {
                pos = dummyPos[_watchPointer++];
              }
              for (var id in _dfds) {
                _dfds[id].notify(createPosition(pos));
              }
            };
            intervalFunc();
            _timerID = setInterval(intervalFunc, h5.dev.api.geo.watchIntervalTime);
          }
        } else {
          dfd.reject({
            code: new DummyPositionError().POSITION_UNAVALABLE
          });
        }
      }, 0);
      return dfd.promise(new WatchPositionPromise(watchID));
    }
    // =============================
    // Expose to window
    // =============================
    // geolocation
    var h5GeolocationSupport = new H5GeolocationSupport();
    // getCurrentPosition と watchPosition を上書きする。
    $.extend(h5.api.geo, {
      getCurrentPosition: getCurrentPosition,
      watchPosition: watchPosition
    });
    h5.u.obj.expose('h5.dev.api.geo', h5GeolocationSupport);
  })();
  /* del end */
  /* del begin */
  var fwLogger = h5.log.createLogger('h5');
  /* del end */
})(jQuery);
