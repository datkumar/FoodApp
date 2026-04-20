import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.phoenix.tech.foodapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.phoenix.tech.foodapp"
        minSdk = 24
        targetSdk = 37

        val buildNum = generateBuildNumber("01")
        versionCode = buildNum.toInt()
        versionName = "1.0.$buildNum"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("debug") {
            res.directories.add("src/debug/res")
            assets.directories.add("src/debug/assets")
            java.directories.add("src/debug/java")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

secrets {
    propertiesFileName = "secrets.properties"
    // Optional: If you want to keep some keys in local.properties too,
    // the plugin will merge them.
    defaultPropertiesFileName = "local.properties"
}


dependencies {
    // The Version Manager (Compose BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)

    // Core UI and Material Design
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)

    // Android Integration
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.core.i18n)

    // Testing & Debugging
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.converter.serialization)

    // Image Loading
    implementation(libs.coil.compose)

    // Persistent Data storage
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.rxjava3)
    // Room SQLite DB
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Location value access
    implementation(libs.google.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)

    // Pagination
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)
}


fun generateBuildNumber(count: String = "01"): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val timeStamp = now.format(formatter)
    val buildNum = "$timeStamp$count"
    println("CURRENT BUILD NUMBER: $buildNum")
    return buildNum
}