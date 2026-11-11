# Which frames work?

Flight Frame needs a frame that runs **full Android** and lets you connect with **ADB** (Android's developer connection). The frame's own photo app doesn't matter — Flight Frame replaces it.

## Tested

| Frame | Screen | Android | Notes |
|---|---|---|---|
| **Arktronic AK-W215B** (21.5", VPhoto app) | 1920×1080 IPS touch | 10, WebView 74 | Works fully. ADB over network was already on (port 5555). Root available, so screen-off uses the real sleep. The screen is locked to portrait by the firmware; Flight Frame rotates itself using the tilt sensor. |

## Probably works

- **Frameo frames** (Denver, Aeezo, and others): Settings → About → turn on *Beta program*, then *ADB access*. Many run Android 6 or 11 with an old WebView; if the display stays blank, update the WebView (search for "Frameo WebView update" — this is a riskier step). Expect a 1280×800 screen: set *Flights in the list* to 2 in the phone app.
- **Any Android tablet** (Android 5+). A cheap tablet in a picture frame works great.

## Won't work

- Nixplay, Aura, Meural, Google Nest Hub, Amazon Echo Show, Skylight: locked-down systems without ADB.

## Before you buy

1. Check the frame's settings for **Developer options**, **USB debugging**, **ADB**, or a **Beta program** switch.
2. Buy from a shop with easy returns.
3. After installing, open the phone app → **Frame → Status**. *Screen control* tells you how the frame can switch its screen off: `root` (best), `device admin` (good), or `black screen only` (backlight stays slightly on).
