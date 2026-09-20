import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val signingKeystorePath = System.getenv("KEYSTORE_PATH")
val signingKeystorePassword = System.getenv("KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() }
val signingKeyAlias = System.getenv("KEY_ALIAS")?.takeIf { it.isNotBlank() }
val signingKeyPassword = System.getenv("KEY_PASSWORD")?.takeIf { it.isNotBlank() }
val hasSigningConfig = signingKeystorePath != null &&
    signingKeystorePassword != null &&
    signingKeyAlias != null &&
    signingKeyPassword != null &&
    File(signingKeystorePath).exists()

android {
    namespace = "com.cocode.babakplayer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cocode.babakplayer"
        minSdk = 24
        targetSdk = 36

        // The version lives in gradle.properties, where the release workflow and
        // F-Droid's checkupdates both read it. See the comment there before bumping it.
        versionCode = providers.gradleProperty("VERSION_CODE").get().toInt()
        versionName = providers.gradleProperty("VERSION_NAME").get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Two builds of the same app, same applicationId: `full` keeps Google Cast exactly
    // as it always worked; `foss` drops every Google Play Services dependency so the
    // app can go on F-Droid. See src/full and src/foss, and cast/CastController for
    // the interface that keeps common code from knowing which flavor it's in.
    flavorDimensions += "distribution"
    productFlavors {
        create("full") {
            dimension = "distribution"
        }
        create("foss") {
            dimension = "distribution"
        }
    }

    signingConfigs {
        if (hasSigningConfig) {
            create("release") {
                storeFile = file(signingKeystorePath!!)
                storePassword = signingKeystorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            // R8 shrinks and optimises the release build. proguard-rules.pro explains why
            // no custom keep rules are needed (Cast's OptionsProvider is covered by its
            // library's own consumer rules).
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        jniLibs {
            // The only .so files in the APK are prebuilts from ExoPlayer/Cast AARs. AGP
            // strips them with whatever NDK it finds, so a rebuild without that exact NDK
            // produces different bytes -- F-Droid's builder has none unless its recipe
            // pins one. Keeping the symbols leaves the libraries exactly as their AARs
            // ship them, which rebuilds identically anywhere, and costs a few kB.
            keepDebugSymbols += "**/*.so"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    // AGP otherwise adds a "Dependency metadata" block to the APK signing block,
    // encrypted with a key only Google Play holds. F-Droid rejects APKs that carry
    // it, and it lands in the published release APK that F-Droid verifies against.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    // Cast is `full`-only: these are what a no-Google-Play-Services build must not
    // ship. See src/full and src/foss under cast/ and ui/screens/ for the split.
    // mediarouter itself carries no Google dependency, but nothing in src/foss uses
    // it any more once the Cast button is gone, so it stays with the flavor that does.
    "fullImplementation"(libs.androidx.media3.cast)
    "fullImplementation"(libs.google.play.services.cast.framework)
    "fullImplementation"(libs.androidx.mediarouter)
    // Only the full flavor serves media to a Chromecast; foss ships no HTTP server at all.
    "fullImplementation"(libs.nanohttpd)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
