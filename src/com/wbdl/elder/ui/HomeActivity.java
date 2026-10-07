package com.wbdl.elder.ui;

/**
 * Wire-format declaration only, not a copy of the original Activity.
 * Java serializes an enum's binary class name and constant name. The receiving
 * app resolves these to ITS OWN HomeActivity$c enum. Never rename/obfuscate it.
 * This avoids loading or bundling the original APK's executable code.
 */
public final class HomeActivity {
    private HomeActivity() {}

    public enum c { Spells, Multiplayer, Devices, Profile }
}
