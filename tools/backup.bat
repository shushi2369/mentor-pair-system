@echo off
chcp 65001 >nul
REM ============================================
REM  师生导师双选系统 · 数据库一键备份
REM  使用前请核对下面三项：
REM    1) MYSQL_BIN ：mysqldump.exe 所在目录
REM    2) DB_USER   ：数据库账号
REM    3) DB_PWD    ：数据库密码
REM  备份文件会保存到桌面，文件名带日期。
REM ============================================
set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin"
set "DB_USER=root"
set "DB_PWD=123456"

set "YYYY=%date:~0,4%"
set "MM=%date:~5,2%"
set "DD=%date:~8,2%"
set "OUT=%USERPROFILE%\Desktop\mentor_pair_backup_%YYYY%%MM%%DD%.sql"

echo 正在备份数据库 mentor_pair …
"%MYSQL_BIN%\mysqldump.exe" -u%DB_USER% -p%DB_PWD% --default-character-set=utf8mb4 mentor_pair > "%OUT%"
if %errorlevel%==0 (
    echo.
    echo 备份成功：%OUT%
) else (
    echo.
    echo 备份失败：请检查 MYSQL_BIN 路径、账号密码是否正确。
)
pause
