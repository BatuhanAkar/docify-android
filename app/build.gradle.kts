import com.android.build.gradle.internal.cxx.configure.defaultCmakeVersion
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation ("com.github.SmartToolFactory:Compose-Zoom:0.5.0")
    implementation ("com.github.nanihadesuka:LazyColumnScrollbar:2.2.0")
    implementation(project(":pdfium"))
    // implementation ("com.tom-roush:pdfbox-android:2.0.26.0")
    val room_version = "2.6.1"
   // implementation(files(("libs/pdfbox-app-3.0.3.jar")))
   // implementation ("org.apache.pdfbox:pdfbox-android:2.0.27")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)
   // implementation ("com.tom-roush:pdfbox-android:2.0.27.0")
   // implementation ("com.gemalto.jp2:jp2-android:1.0.3")
    // https://mvnrepository.com/artifact/com.github.Tgo1014/JP2ForAndroid
   // implementation("com.github.Tgo1014:JP2ForAndroid:1.0.4")

    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc10")
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
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}