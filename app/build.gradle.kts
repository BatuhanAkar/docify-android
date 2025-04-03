import com.android.build.gradle.internal.cxx.configure.defaultCmakeVersion
import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.internal.declarativedsl.dom.resolution.resolutionContainer
//import org.jetbrains.kotlin.gradle.idea.proto.com.google.protobuf.compiler.version

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.batuscode.docunote"
    compileSdk = 35
    defaultConfig {

        applicationId = "com.batuscode.docunote"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // Specifies the ABI configurations of your native
            // libraries Gradle should build and package with your app.
            abiFilters += listOf("x86", "x86_64", "armeabi", "armeabi-v7a",
                "arm64-v8a")
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = VERSION_11
        targetCompatibility = VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    buildToolsVersion = "35.0.1"
    ndkVersion = "28.0.12433566 rc1"


}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //implementation ("com.github.SmartToolFactory:Compose-Extended-Gestures:4.0.0")
    implementation (libs.compose.zoom)
    implementation (libs.lazycolumnscrollbar)
    implementation(project(":pdfium"))
    // implementation ("com.tom-roush:pdfbox-android:2.0.26.0")
   // implementation(files(("libs/pdfbox-app-3.0.3.jar")))
   // implementation ("org.apache.pdfbox:pdfbox-android:2.0.27")
    implementation (libs.gson)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)
   // implementation ("com.tom-roush:pdfbox-android:2.0.27.0")
   // implementation ("com.gemalto.jp2:jp2-android:1.0.3")
    // https://mvnrepository.com/artifact/com.github.Tgo1014/JP2ForAndroid
   // implementation("com.github.Tgo1014:JP2ForAndroid:1.0.4")

    implementation (libs.androidx.material.icons.core)
    implementation (libs.androidx.material.icons.extended)
    implementation (libs.tasks.genai)
    implementation(libs.richeditor.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.compose.ui.geometry)
    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}