import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import com.android.build.gradle.internal.cxx.configure.defaultCmakeVersion
import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.internal.declarativedsl.dom.resolution.resolutionContainer
//import org.jetbrains.kotlin.gradle.idea.proto.com.google.protobuf.compiler.version

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")

}

android {

    namespace = "com.batuscode.docunote"
    compileSdk = 35
    defaultConfig {

        applicationId = "com.batuscode.docunote"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 15
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // Specifies the ABI configurations of your native
            // libraries Gradle should build and package with your app.
            abiFilters += listOf("x86", "x86_64", "armeabi", "armeabi-v7a",
                "arm64-v8a")
        }

    }
    bundle {
        abi { enableSplit = true }
        texture { enableSplit = true }
        language { enableSplit = true }
    }

    buildTypes {
        debug {
            isDebuggable = true  // ← BU MUTLAKA TRUE OLMALI
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1" ,
                "META-INF/LICENSE",
                "META-INF/LICENSE.md" ,
                "META-INF/NOTICE.md" ,
                "META-INF/mimetypes.default" ,
                "META-INF/mailcap.default" ,

            )
        }
    }
    buildToolsVersion = "35.0.1"
    ndkVersion = "28.0.12433566 rc1"


}

dependencies {


    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation (libs.compose.zoom)
    implementation (libs.lazycolumnscrollbar)
    implementation(project(":pdfium"))
    implementation (libs.gson)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)
    implementation(libs.commons.io)
    implementation(libs.asset.delivery.ktx)
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
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("androidx.compose.material:material:1.8.0")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    val billing_version = "7.0.0"

    implementation("com.android.billingclient:billing-ktx:$billing_version")
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

