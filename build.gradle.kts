// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("buildSmoke") {
    group = "verification"
    description = "Build debug, run unit tests, and lint -- both the full and foss flavors."
    dependsOn(
        ":app:assembleFullDebug",
        ":app:assembleFossDebug",
        ":app:testFullDebugUnitTest",
        ":app:testFossDebugUnitTest",
        ":app:lintFullDebug",
        ":app:lintFossDebug"
    )
}
