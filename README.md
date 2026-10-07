# Caster Launcher

A small, unofficial launcher that opens the **Devices** tab in the original
**Magic Caster 1.7.1**, avoiding its normal startup route when that route gets
stuck on **“Lost your way?”**

This is a separate app: it does **not** patch or replace Magic Caster, clear its
data, or restore missing content. No root, ADB, proxy, CA installation or Android
permissions are required by the launcher.

## What you need

- Android **9 or later**. Currently tested only on Android 12.
- The original, already-installed **Magic Caster 1.7.1 (version code 957)** in
  the same Android user/profile. Its version and signing certificate are checked.
- Existing app data/resources for the original app's offline features. This
  launcher does not create an account or supply missing caches or media.

**Keep your existing installation intact. Do not uninstall, downgrade, clear
storage/cache, log out or root your phone for this test.** An unsupported version
is a reason to stop, not to replace a potentially valuable surviving installation.

## Download and use

1. Download the signed **[v0.1.0 APK](https://github.com/uuyyyuu/magic-caster-launcher/releases/download/v0.1.0/MagicCaster-Devices-Launcher-0.1.0.apk)**
   from [GitHub Releases](https://github.com/uuyyyuu/magic-caster-launcher/releases).
2. Install it alongside Magic Caster. If Android asks, allow installation from
   the browser/file manager you used; you can revoke that permission afterward.
3. Open **Caster Launcher** and tap **Open Devices**.
4. If a residual **“Lost your way?”** overlay appears with the bottom tabs still
   visible, select **Devices**, then tap **Try again** once. That cleared it in
   an earlier test; it is not a universal error fix.

Afterward, navigate manually. **Profile → XP bar → Current Progress** reached
the cached spell list in an earlier test. Trying an individual spell is the next
step; actual casting through this launcher has not yet been verified.

## Tested, and what is not fixed

On **2026-10-07**, the release APK was tested with original 1.7.1 on a Redmi K30
running Android 12. Normal cold startup reproduced **“Lost your way?”**. Opening
the launcher and pressing its button reached **Devices** without manually
dismissing an overlay. A repeated launch also worked. The active connection had
no proxy/PAC, and the original APK hash remained unchanged.

The phone was rooted, but the launcher ran under its own ordinary app UID and
used no root API or command. ADB was used only to install, tap and observe during
testing; the launcher itself does not use ADB. A separate unrooted-device test,
other Android versions and different surviving account/cache states remain
untested.

- This restarts the original app's **screen stack**, not its storage. Normal
  actions inside the original app may still save state; it is not a forensic
  read-only environment.
- It does not restore servers, missing `extra` sounds/videos, login, or every
  offline feature. Other pages can still show errors.
- Starting Magic Caster normally later can still hit the startup error.
- Casting and media playback were **not** validated in this launcher test.

## How it works

The launcher calls the original app's exported
`com.wbdl.elder.ui.splash.RoutingActivity`, passing
`extra_skip_to_home_with_destination` as the serializable
`com.wbdl.elder.ui.HomeActivity$c.Devices` enum—not a string.

An independently declared minimal enum supplies the matching binary class name
and constant. Java enum serialization carries those names; the receiving app
resolves them to its own enum. `NEW_TASK | CLEAR_TASK` makes the routing activity
receive a fresh `onCreate` instead of reusing a stuck startup screen.

There are no shell commands, hidden Android APIs, dynamic code loading,
accessibility services, network calls or requested permissions. No original APK,
proprietary media, account data or signing private keys are included.

## Build

Requires JDK 17+, Android SDK Platform 35, Android Build-Tools, Bash, `zip`,
`openssl` and `shasum`. No Gradle dependencies or automatic downloads.

```sh
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
bash test.sh
MCW_BUILD_TOOLS="$ANDROID_HOME/build-tools/35.0.1" bash build.sh
```

Alternatively, set `MCW_ANDROID_JAR` to an SDK 35 `android.jar`. Output is
`dist/MagicCaster-Devices-Launcher-0.1.0.apk`. Build intermediates are ignored.

The first build creates your own signing key outside the project under
`${XDG_DATA_HOME:-$HOME/.local/share}/magic-caster-devices-launcher/signing`.
`MCW_SIGNING_DIR` can override this with another **private, non-served** directory.
Keep that key for future updates and never publish it. Your own build is signed
with your own key, so it cannot update the downloaded release in place. Do not
change or reinstall the original Magic Caster app to resolve a launcher-key mismatch.

## Release integrity

Release APKs are signed with the same separate launcher key. Its certificate
SHA-256 fingerprint is:

```text
e539452798587dd171921e9fc3b959a7792b28cd70c0cb42fe9066588bb6233d
```

Each release includes `SHA256SUMS` for the APK. This fingerprint belongs to the
launcher, **not** the original Magic Caster app.

Unofficial community research utility; not affiliated with or endorsed by
Warner Bros. Magic Caster and related trademarks belong to their respective owners.
