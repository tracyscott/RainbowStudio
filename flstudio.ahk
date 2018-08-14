#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
; #Warn  ; Enable warnings to assist with detecting common errors.
SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.
; SetTimer, CheckForExe, 5000 ;frequency
Loop
{
	Sleep 30000
	Process, Exist, FL64.exe
    if not Errorlevel {
        Run, C:\Program Files (x86)\Image-Line\FL Studio 20\FL64.exe "C:\Users\tracy\Documents\Image-Line\FL Studio\Projects\midimapped.flp", C:\Program Files (x86)\Image-Line\FL Studio 20
		Sleep 30000
	}
}