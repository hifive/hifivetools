@echo off
setlocal

rem 
rem プロパティファイル内の#によるコメント文に=が含まれると正常出力出来ません。
rem 

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
set java_file=%java_class_name%.java

rem native 2 ascii
"%JAVA_HOME%\bin\native2ascii" -encoding utf-8 %inputfile% %outfile%
echo %outfile%を更新しました

rem ascii 2 native
rem "%JAVA_HOME%\bin\native2ascii" -encoding utf-8 -reverse %outfile% %inputfile%


echo /*>%java_file_ascii%
echo  * %file_header_comment%>>%java_file_ascii%
echo  */>>%java_file_ascii%
echo package %java_package_name%;>>%java_file_ascii%

if "%exe_mode%"=="plugin" (
	echo;>>%java_file_ascii%
	echo import java.util.ResourceBundle;>>%java_file_ascii%
)

if not "%java_base_class_package%"=="%java_package_name%" (
	echo;>>%java_file_ascii%
	echo import %java_base_class_package%.%java_base_class_name%;>>%java_file_ascii%
)
echo;>>%java_file_ascii%
echo /**>>%java_file_ascii%
echo  * %java_class_name%.>>%java_file_ascii%
echo  * >>%java_file_ascii%
echo  * @author MessageGenerator>>%java_file_ascii%
echo  */>>%java_file_ascii%
echo public final class %java_class_name% extends %java_base_class_name% {>>%java_file_ascii%
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

echo 	/**>>%java_file_ascii%
echo 	 * デフォルトコンストラクタ.>>%java_file_ascii%
echo 	 */>>%java_file_ascii%
echo 	private %java_class_name%() {>>%java_file_ascii%
echo 		// no create>>%java_file_ascii%
echo 	}>>%java_file_ascii%

echo }>>%java_file_ascii%


"%JAVA_HOME%\bin\native2ascii" -encoding utf-8 -reverse %java_file_ascii% %java_file%
echo %java_file%を更新しました

del %java_file_ascii%

:end_error

:end

endlocal
rem pause
