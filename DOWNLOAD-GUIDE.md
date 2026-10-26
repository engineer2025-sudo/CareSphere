# CareSphere apps: download and run

This bundle contains source code, not signed installers. It includes the iPhone, Android, and web projects, their required local assets, and build configuration. Build tools download platform dependencies when needed. The artwork is bundled locally; there are no remotely hosted image or font assets.

## Get the source

After creating an empty GitHub repository, clone it and enter the project folder:

```bash
git clone https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
cd YOUR-REPOSITORY/CareSphere
```

Replace the URL and folder name with yours.

## Web

Install Node.js 20.19+ or 22.12+, then from the project root run:

```bash
npm ci
npm run dev
```

For a production build, run `npm run build`. The deployable static files are written to `dist/`; host that folder on an HTTPS static web host.

## iPhone

On a Mac with Xcode 16+ (Xcode 26 recommended), open `ios/CareSphere.xcodeproj`. Select the **CareSphere** scheme and a connected iPhone, set your Apple team under **Signing & Capabilities**, then press **Run**. This bundle does not include a signed IPA or TestFlight release.

## Android

Install JDK 17 and the Android SDK. From the project root run:

```bash
cd android
./gradlew assembleDebug
```

The debug APK is created at `app/build/outputs/apk/debug/app-debug.apk`. To install it on a connected Android phone with USB debugging enabled, run:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

This is a debug build for testing, not a Play Store release.
