# TheLearningLock
An Android app that uses an anomaly detection (based on a Normal Distribution) to learn how the user enters their pattern and detects it there is an imposter

##### Setup
- Install JDK
- Install Android Studio + Android SDK
- Create / Get the `app/src/main/res/values/secrets.xml` file which contains API keys. The format should be
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="AWARENESS_API_KEY_DEBUG">{Google API Key for debug builds}</string>
    <string name="AWARENESS_API_KEY_RELEASE">{Google API Key for release builds}</string>
    <string name="FABRIC_API_KEY_DEBUG">{Crashlytics API Key for debug builds}</string>
    <string name="FABRIC_API_KEY_RELEASE">{Crashlytics API Key for release builds}</string>
</resources>
```
- Build and Run on Android Studio

##### Details 
Minimum API - Android 4.4 (Level 19) / Kitkat

##### Source Code package structure
- lockscreen: Contains code related to the Lock screen that is shown when the phone is locked and any related code, such as the notifications system
- main: The Activity that is actually launched when the app starts, and the Welcome and Settings fragments
- pattern: Shared files that are used for anything related to the Pattern unlock
- pin: Shared files that are used for anything related to the PIN unlock
- setup: Code related to the Setup flow (which is used by the stuff in main)
- shared: Code that is shared by the other packages

##### Credits
Created by [Alston Lin](https://github.com/AlstonLin) with contributions from [Ejaaz Merali](https://github.com/emerali), and [Tyler Wang](https://www.linkedin.com/in/tyler-yue-wang-81175ba3/)

Designed by [David Liu](https://github.com/davidlky)
