@echo off

call gradlew.bat --offline --daemon --parallel assembleDebug
:: 刷新界面
::gradlew.bat --refresh-dependencies

set arg1=%1

if "%arg1%"=="" (
    echo "success"
    goto yes
) else (
    echo "failed"
    goto no
)

:no
echo 'no %arg1%'
call adb -s %1 install -r C:\StudioProjects\NativeAudio\app\build\outputs\apk\app-debug.apk
call adb -s %1 shell am start -n hello.leilei/.SplashActivity
goto secondApk

:yes
echo 'yes %arg1%'
call adb install -r C:\StudioProjects\NativeAudio\app\build\outputs\apk\app-debug.apk
call adb shell am start -n hello.leilei/.SplashActivity
goto secondApk

:secondApk
if "%arg2%"=="" (
    goto print
) else (
    echo "install apk on %arg2% ......"
    call adb -s %2 install -r C:\StudioProjects\NativeAudio\app\build\outputs\apk\app-debug.apk
    call adb -s %2 shell am start -n hello.leilei/.SplashActivity
)


:print
echo "install apk successfully!!!"
echo 'start time : %startTime% end time : %time%'