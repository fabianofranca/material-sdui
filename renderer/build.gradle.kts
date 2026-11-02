import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

group = "com.fabianofranca.sdui"
version = "0.1.0"

kotlin {
    android {
        namespace = "com.fabianofranca.sdui.renderer"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        // Capturas de tela na JVM (Robolectric + Roborazzi): ./gradlew :renderer:testAndroidHostTest
        withHostTest { isIncludeAndroidResources = true }
    }

    // O app iOS embute este framework (veja iosApp/).
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SduiRenderer"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":engine"))
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.compose.material3)
            implementation(libs.compose.material.icons.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.androidx.compose.ui.test.manifest)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}


// Grava as capturas em renderer/build/outputs/roborazzi (sem comparar com uma referência).
tasks.withType<Test>().configureEach {
    systemProperty("roborazzi.test.record", "true")
    systemProperty("sdui.examples", rootProject.file("docs/examples").absolutePath)
    // O Robolectric baixa o android-all do Maven Central na hora do teste; com um espelho:
    // ./gradlew :renderer:testAndroidHostTest -Probolectric.repo=https://meu.espelho/maven2
    providers.gradleProperty("robolectric.repo").orNull?.let { systemProperty("robolectric.dependency.repo.url", it) }
}
