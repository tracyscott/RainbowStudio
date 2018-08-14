#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
; #Warn  ; Enable warnings to assist with detecting common errors.
SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.
; SetTimer, CheckForExe, 5000 ;frequency
Loop
{
    ;Process, Exist, RainbowStudio
	;IfWinExist RainbowStudio
	Process, Exist, java.exe
    if not Errorlevel {
        Run, C:\Users\tracy\Documents\Processing\RainbowStudio\rainbowstudio.bat, C:\Users\tracy\Documents\Processing\RainbowStudio
		Sleep 40000
	}
	Sleep 20000
}