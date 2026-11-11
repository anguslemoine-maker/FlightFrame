#!/bin/bash
# Builds Flight Frame (no Gradle needed). Needs: JDK 17, Android SDK with platforms;android-29 and build-tools;34.0.0
# Usage: ANDROID_SDK=/path/to/sdk ./build.sh
set -e
cd "$(dirname "$0")"
SDK=${ANDROID_SDK:-$ANDROID_HOME}
BT=$SDK/build-tools/34.0.0
JAR=$SDK/platforms/android-29/android.jar
OUT=build; rm -rf $OUT; mkdir -p $OUT/gen $OUT/classes $OUT/assets/www $OUT/assets/companion
# assets: the display and the phone app
cp ../display/index.html ../display/planes.js ../display/satellite.min.js ../display/settings.js ../display/schiphol.js $OUT/assets/www/
[ -f ../display/radarmap.js ] && cp ../display/radarmap.js ../display/radarmap.jpg $OUT/assets/www/ || true
cp -R ../companion/. $OUT/assets/companion/
$BT/aapt2 compile --dir res -o $OUT/res.zip
$BT/aapt2 link -o $OUT/unsigned.apk -I $JAR --manifest AndroidManifest.xml --java $OUT/gen -A $OUT/assets $OUT/res.zip --auto-add-overlay
javac -source 8 -target 8 -nowarn -Xlint:none -bootclasspath $JAR -classpath $JAR -d $OUT/classes $(find src $OUT/gen -name '*.java') 2>&1 | grep -v "^warning\|^Note:\|bootstrap class path\|^1 warning" || true
[ -f $OUT/classes/app/flightframe/MainActivity.class ] || { echo "compile failed"; exit 1; }
$BT/d8 --min-api 21 --lib $JAR --output $OUT $(find $OUT/classes -name '*.class')
cd $OUT && zip -q -j unsigned.apk classes.dex && cd ..
$BT/zipalign -f -p 4 $OUT/unsigned.apk $OUT/aligned.apk
KS=${KEYSTORE:-flightframe.keystore}
[ -f "$KS" ] || keytool -genkeypair -keystore "$KS" -storepass flightframe -keypass flightframe -alias flightframe -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Flight Frame" >/dev/null 2>&1
$BT/apksigner sign --ks "$KS" --ks-pass pass:flightframe --key-pass pass:flightframe --out FlightFrame.apk $OUT/aligned.apk
echo "Built $(pwd)/FlightFrame.apk ($(du -h FlightFrame.apk | cut -f1))"
