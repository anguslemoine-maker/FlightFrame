# Flight Frame installer for Windows (PowerShell).
#   powershell -ExecutionPolicy Bypass -File install.ps1 [frame-ip]
param([string]$Frame = "")
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
function Say($t) { Write-Host "`n$t" -ForegroundColor Yellow }

$adb = (Get-Command adb -ErrorAction SilentlyContinue).Source
if (-not $adb) {
  if (-not (Test-Path "platform-tools\adb.exe")) {
    Say "Downloading Android platform-tools (adb)..."
    Invoke-WebRequest "https://dl.google.com/android/repository/platform-tools-latest-windows.zip" -OutFile pt.zip
    Expand-Archive pt.zip -DestinationPath . -Force; Remove-Item pt.zip
  }
  $adb = "$PSScriptRoot\platform-tools\adb.exe"
}
if ($Frame) {
  if ($Frame -notmatch ":") { $Frame = "$Frame`:5555" }
  Say "Connecting to $Frame (accept the prompt on the frame if one appears)"
  & $adb connect $Frame
  $env:ANDROID_SERIAL = $Frame
}
& $adb wait-for-device
$model = (& $adb shell getprop ro.product.model).Trim()
Say "Found: $model"

$apk = "FlightFrame.apk"
if (-not (Test-Path $apk)) { $apk = "..\app\FlightFrame.apk" }
if (-not (Test-Path $apk)) {
  Say "Downloading the latest Flight Frame app..."
  Invoke-WebRequest "https://github.com/anguslemoine-maker/FlightFrame/releases/latest/download/FlightFrame.apk" -OutFile FlightFrame.apk
  $apk = "FlightFrame.apk"
}
Say "Installing the app..."
& $adb install -r $apk
& $adb shell pm grant app.flightframe android.permission.ACCESS_FINE_LOCATION 2>$null
& $adb shell dpm set-active-admin app.flightframe/.AdminReceiver 2>$null
& $adb shell cmd package set-home-activity app.flightframe/.MainActivity 2>$null
& $adb shell settings put global stay_on_while_plugged_in 7 2>$null
if ((Test-Path radarmap.jpg) -and (Test-Path radarmap.js)) {
  Say "Copying your radar map..."
  & $adb shell mkdir -p /sdcard/Android/data/app.flightframe/files/www
  & $adb push radarmap.jpg radarmap.js /sdcard/Android/data/app.flightframe/files/www/
}
& $adb shell am start -n app.flightframe/.MainActivity | Out-Null
Start-Sleep 6
$ip = ((& $adb shell ip -4 addr show wlan0) | Select-String 'inet ([0-9.]+)').Matches.Groups[1].Value
Say "Done! Flight Frame is running."
Write-Host "Phone app: open http://$ip`:8080 on your phone (same wifi), then 'Add to Home Screen'."
