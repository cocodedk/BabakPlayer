# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# R8 keep rules for the release build (isMinifyEnabled = true, isShrinkResources = true).
#
# No custom keep rules are needed here, checked one at a time:
# - Cast SDK: CastOptionsProvider (app/src/full/.../cast/CastOptionsProvider.kt) is
#   referenced from AndroidManifest.xml meta-data by fully-qualified name, but
#   play-services-cast-framework's own bundled consumer proguard.txt already has
#   `-keep public class * implements ...OptionsProvider { public <methods>; }`,
#   which AGP merges automatically. Verified: mapping.txt keeps the class name
#   unrenamed and seeds.txt lists it as an R8 root.
# - media3-cast, androidx-mediarouter: no reflection in our code or in the
#   libraries' own consumer rules (mediarouter's only bundled rule is an
#   -assumevalues on a debug flag); Cast's own auxiliary components
#   (ReconnectionService, MediaIntentReceiver, GoogleApiActivity) are declared in
#   the merged manifest, so aapt2 auto-generates their keep rules.
# - NanoHTTPD (LocalMediaServer): ships as a plain jar with no consumer rules, but
#   it is driven entirely through direct method overrides (serve()), never by
#   reflection or name-based lookup, so renaming LocalMediaServer is safe --
#   confirmed renamed (to a short R8 name) in mapping.txt, and nothing in the
#   codebase looks it up by name.
# - media3-exoplayer, media3-ui: each ships its own consumer proguard.txt covering
#   its internal reflection (DefaultRenderersFactory's optional decoder
#   constructors, PlayerView's SphericalGLSurfaceView / TrackSelectionDialogBuilder),
#   which AGP merges automatically; XML-inflated view classes (AspectRatioFrameLayout,
#   SubtitleView, ...) get aapt2-generated keep rules the same way.
# - No Gson/Moshi/Jackson/kotlinx.serialization, no Retrofit, no Room/Hilt/KSP, no
#   JavascriptInterface, no JNI, no java.io.Serializable / ObjectInputStream /
#   ObjectOutputStream, and no Class.forName/getDeclared* anywhere in app/src
#   (checked by grep).
#
# A clean `./gradlew assembleFullRelease assembleFossRelease` produced no R8
# "Missing class" warnings and no missing_rules.txt for either flavor, so no
# -dontwarn was added either.
