#!/bin/bash
# Flight Frame installer for macOS and Linux.
#   ./install.sh                 (finds the frame over USB, or asks for its wifi address)
#   ./install.sh 192.168.1.50    (frame's address, with wireless debugging/adb over network on)
# What it does: installs the Flight Frame app, lets it switch the screen off, makes it the
# frame's home screen so it starts on boot, and prints the address for the phone app.
set -e
cd "$(dirname "$0")"
say(){ printf "\n\033[1;33m%s\033[0m\n" "$*"; }

# 1. adb (Android's own tool for talking to devices). Downloaded next to this script if missing.
if command -v adb >/dev/null 2>&1; then ADB=adb
elif [ -x platform-tools/adb ]; then ADB=$PWD/platform-tools/adb
else
  say "Downloading Android platform-tools (adb)…"
  case "$(uname -s)" in Darwin) OS=darwin;; *) OS=linux;; esac
  curl -sL -o pt.zip "https://dl.google.com/android/repository/platform-tools-latest-$OS.zip"
  unzip -q -o pt.zip && rm pt.zip
  ADB=$PWD/platform-tools/adb
fi

# 2. connect
if [ -n "$1" ]; then
  T="$1"; [[ "$T" == *:* ]] || T="$T:5555"
  say "Connecting to $T… (accept the prompt on the frame if one appears)"
  $ADB connect "$T"
  export ANDROID_SERIAL="$T"
fi
$ADB wait-for-device
MODEL=$($ADB shell getprop ro.product.model | tr -d '\r')
SDK=$($ADB shell getprop ro.build.version.sdk | tr -d '\r')
WV=$($ADB shell dumpsys webviewupdate 2>/dev/null | grep -o 'Current WebView package.*' | grep -o '[0-9][0-9]*\.[0-9.]*' | head -1)
say "Found: $MODEL (Android API $SDK, WebView ${WV:-unknown})"
if [ "$SDK" -lt 21 ]; then echo "This frame's Android is too old (needs Android 5 or newer)."; exit 1; fi
if [ -n "$WV" ] && [ "${WV%%.*}" -lt 60 ]; then
  echo "Warning: this frame's WebView ($WV) is old. The display may not work until it is updated — see docs/HARDWARE.md."
fi

# 3. the app
APK=FlightFrame.apk
[ -f "$APK" ] || APK=../app/FlightFrame.apk
if [ ! -f "$APK" ]; then
  say "Downloading the latest Flight Frame app…"
  curl -sL -o FlightFrame.apk "https://github.com/anguslemoine-maker/FlightFrame/releases/latest/download/FlightFrame.apk"
  APK=FlightFrame.apk
fi
say "Installing the app…"
$ADB install -r "$APK"
$ADB shell pm grant app.flightframe android.permission.ACCESS_FINE_LOCATION 2>/dev/null || true
$ADB shell dpm set-active-admin app.flightframe/.AdminReceiver 2>/dev/null || echo "(couldn't enable screen-off; the schedule will use a black screen instead)"
$ADB shell cmd package set-home-activity app.flightframe/.MainActivity 2>/dev/null || true
$ADB shell settings put global stay_on_while_plugged_in 7 2>/dev/null || true

# 4. optional: your own map + settings
if [ -f radarmap.jpg ] && [ -f radarmap.js ]; then
  say "Copying your radar map…"
  D=/sdcard/Android/data/app.flightframe/files/www
  $ADB shell mkdir -p $D
  $ADB push radarmap.jpg radarmap.js $D/
fi

$ADB shell am start -n app.flightframe/.MainActivity >/dev/null
sleep 6
IP=$($ADB shell ip -4 addr show wlan0 2>/dev/null | grep -o 'inet [0-9.]*' | cut -d' ' -f2)
say "Done! Flight Frame is running."
echo "Phone app: open  http://${IP:-<frame-ip>}:8080  on your phone (same wifi), then 'Add to Home Screen'."
echo "Set your location there first (Location tab)."
echo
echo "Optional: hide the frame's original photo app so it can't take over the screen:"
echo "  $ADB shell pm list packages | grep -i -E 'frameo|photo|vphoto|nixplay'"
echo "  $ADB shell pm disable-user --user 0 <package>      (undo with: pm enable <package>)"
