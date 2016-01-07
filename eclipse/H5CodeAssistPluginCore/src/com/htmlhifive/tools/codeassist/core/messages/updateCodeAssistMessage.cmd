REM Copyright (C) 2012 NS Solutions Corporation
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
@echo off
setlocal
cd /d "%~dp0"

set exe_mode=plugin
set inputfile=messages.txt
set outfile=messages_ja.properties
set java_class_name=Messages
set java_package_name=com.htmlhifive.tools.codeassist.core.messages
set java_base_class_package=com.htmlhifive.tools.codeassist.core.messages
set java_base_class_name=MessagesBase
set message_class_file=MessageClass2.txt
set file_header_comment=Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.

set replace_str=%~dp0
set project_root=%replace_str:\src\com\htmlhifive\tools\codeassist\core\messages\=%

call "%project_root%\config\message\updateMessageHasParent.cmd"
endlocal
pause

