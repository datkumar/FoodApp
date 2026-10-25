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
    namespace = "com.example.tech.foodapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.tech.foodapp"
        minSdk = 24
        targetSdk = 37

        val buildNum = generateBuildNumber("01")
        versionCode = buildNum.toInt()
        versionName = "1.0.$buildNum"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

//    sourceSets {
//        getByName("debug") {
//            res.directories.add("src/debug/res")
//            assets.directories.add("src/debug/assets")
//            java.directories.add("src/debug/java")
//        }
//    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Core UI and Material Design
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)

    // Android Integration
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.i18n)

    // Testing & Debugging
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.converter.serialization)
    // Pagination
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Image Loading
    implementation(libs.coil.compose)

    // Persistent Data storage
    implementation(libs.androidx.datastore.preferences)
    // Room SQLite DB
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Location access
    implementation(libs.google.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)

    // Camera libs bundle
    implementation(libs.bundles.camerax)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Local Unit Tests (src/test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test) // For runTest and MainDispatcherRule
    testImplementation(libs.mockk.core)              // For mocking repositories in ViewModels
    testImplementation(libs.cashapp.turbine)         // For testing UI state Flows easily
    testImplementation(libs.okhttp.mockwebserver)    // For feeding your debug JSONs to Retrofit

    // Instrumented Tests (src/androidTest)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)           // Instrumented MockK variant
    androidTestImplementation(libs.androidx.paging.testing) // For validating paginated list states
    androidTestImplementation(libs.room.testing)            // Room DB testing
    // Debug rules for layout checking
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}


fun generateBuildNumber(count: String = "01"): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val timeStamp = now.format(formatter)
    val buildNum = "$timeStamp$count"
    println("CURRENT BUILD NUMBER: $buildNum")
    return buildNum
}