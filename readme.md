Inspiry
==========

### IOS Branch

## iOS project setup before xcode launch

# preparing ide
1. install JDK (tested with jdk-15.0.2_osx-x64_bin.dmg)
2. install Android Studio and Android SDK
3. you can open and build project now (iosModule)
   note: if there are errors, try building the project for the first time in android studio
4. update signing

# external api setup 
1. place your photoroom api key into **RemoveBGProcessorImpl.swift** (find and replace **"your-photoroom-apiKey"**)
2. put your adapty api key into Adapty initializer (IosApp.swift)
3. put your amplitude api key into Amplitude initializer (IosApp.swift)
4. put your appsflyer api key into AppDelegate (replace "your-appsfler-key")
5. don't forget to register your app in firebase console and replace **GoogleService-Info.plist** in project with yours


* We haven't merged the ios_dev branch into the main branch, so the Android part is outdated here. (for android project use android branch)