@echo off
setlocal
cd /d "%~dp0"

rem 個別設定情報
set java_package_name=com.htmlhifive.tools.wizard.log.messages
rem set JAVA_HOME=C:\Program Files (x86)\Java\jdk1.6.0_31

rem 基本設定情報
set exe_mode=plugin
set inputfile=messages.txt
set outfile=messages.properties
set java_class_name=Messages
set java_base_class_package=%java_package_name%
set java_base_class_name=MessagesBase

rem java,resourceが分かれているのでJavaファイルを別の場所に出力する
set replace_str=%~dp0
set java_package_path=%replace_str:\src\main\resources\=\src\main\java\%
set java_class_path=%java_package_path%\


rem 補足
rem プロパティファイル内の#によるコメント文に=が含まれると正常出力出来ません。

if "%inputfile%"=="" (
 echo inputfile が定義されていません
 goto end_error
)

if "%outfile%"=="" (
 echo outfile が定義されていません
 goto end_error
)

if "%java_class_name%"=="" (
 echo java_class_name が定義されていません
 goto end_error
)

if "%java_package_name%"=="" (
 echo java_package_name が定義されていません
 goto end_error
)

if "%java_base_class_package%"=="" (
 echo java_base_class_package が定義されていません
 goto end_error
)

if "%java_base_class_name%"=="" (
 echo java_base_class_name が定義されていません
 goto end_error
)

if "%JAVA_HOME%"=="" (
 if not exist "C:\project\jre1.6\bin\native2ascii.exe" (
  echo JAVA_HOME が定義されていません
  goto end_error
 )
 set JAVA_HOME=C:\project\jre1.6
)

set java_file_ascii=%java_class_name%.ascii
set java_file=%java_class_path%%java_class_name%.java

rem native 2 ascii
"%JAVA_HOME%\bin\native2ascii" -encoding utf-8 %inputfile% %outfile%
echo %outfile%を更新しました

rem ascii 2 native
rem "%JAVA_HOME%\bin\native2ascii" -encoding utf-8 -reverse %outfile% %inputfile%


echo /*>>%java_file_ascii%
echo  * Copyright (C) 2012 NS Solutions Corporation>>%java_file_ascii%
echo  *>>%java_file_ascii%
echo  * Licensed under the Apache License, Version 2.0 (the "License");>>%java_file_ascii%
echo  * you may not use this file except in compliance with the License.>>%java_file_ascii%
echo  * You may obtain a copy of the License at>>%java_file_ascii%
echo  *>>%java_file_ascii%
echo  *    http://www.apache.org/licenses/LICENSE-2.0>>%java_file_ascii%
echo  *>>%java_file_ascii%
echo  * Unless required by applicable law or agreed to in writing, software>>%java_file_ascii%
echo  * distributed under the License is distributed on an "AS IS" BASIS,>>%java_file_ascii%
echo  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.>>%java_file_ascii%
echo  * See the License for the specific language governing permissions and>>%java_file_ascii%
echo  * limitations under the License.>>%java_file_ascii%
echo  */>>%java_file_ascii%

echo package %java_package_name%;>>%java_file_ascii%

if "%exe_mode%"=="plugin" (
	echo;>>%java_file_ascii%
	echo import java.util.ResourceBundle;>>%java_file_ascii%
)

echo;>>%java_file_ascii%
if not "%java_base_class_package%"=="%java_package_name%" (
	echo import %java_base_class_package%.%java_base_class_name%;>>%java_file_ascii%
)
echo;>>%java_file_ascii%
echo /**>>%java_file_ascii%
echo  * %java_class_name%.>>%java_file_ascii%
echo  * >>%java_file_ascii%
echo  * @author MessageGenerator>>%java_file_ascii%
echo  */>>%java_file_ascii%
echo public abstract class %java_class_name% extends %java_base_class_name% {>>%java_file_ascii%
echo;>>%java_file_ascii%

echo 	// 必ず定数定義より先に呼び出すこと.>>%java_file_ascii%
echo 	static {>>%java_file_ascii%
echo 		// このクラスを登録する.>>%java_file_ascii%
if "%exe_mode%"=="plugin" (
echo 		addResourceBundle(ResourceBundle.getBundle("%java_package_name%.messages"^)^);>>%java_file_ascii%
) else (
echo 		// このクラスを登録する.>>%java_file_ascii%
echo 		addResourceBundleString("%java_package_name%.messages"^);>>%java_file_ascii%
)
echo 	}>>%java_file_ascii%
echo;>>%java_file_ascii%

for /f "eol=/ tokens=1,2* delims==" %%i in (%outfile%) do ( 

  if "%%j"=="" (
    if not "%%i"=="" (
       echo 	// %%i>>%java_file_ascii%
    )
  ) else (
    if "%%k"=="" (
       echo 	/** %%i=%%j%%k. */>>%java_file_ascii%
    ) else (
       echo 	/** %%i=%%j=%%k. */>>%java_file_ascii%
    )
    echo 	public static final Message %%i = createMessage("%%i"^);>>%java_file_ascii%
    echo;>>%java_file_ascii%
  )
)

echo }>>%java_file_ascii%


"%JAVA_HOME%\bin\native2ascii" -encoding utf-8 -reverse %java_file_ascii% %java_file%
echo %java_file%を更新しました

del %java_file_ascii%

:end_error

:end

endlocal
rem pause
