plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt.android) // Hilt Gradle plugin
    // alias(libs.plugins.compose.compiler) // This plugin is usually applied differently or implicitly by Kotlin plugin
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.moviechecklist" // Changed
        minSdk = 26 // Changed to fix adaptive icon issue and for modern practices
        targetSdk = 35
        versionCode = 1 // Reset for the new app ID
        versionName = "1.0.0" // Reset for the new app ID

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // Keep this for Hilt tests later
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    namespace = "com.example.moviechecklist" // Changed

    // Product Flavors for API Key - this is a good practice
    flavorDimensions += "version"
    productFlavors {
        create("develop") {
            dimension = "version"
            // Make sure this API key is for TMDB and you have the rights to use it.
            // It's better to fetch this from a secure place or local.properties for production.
            buildConfigField("String", "TMDB_API_KEY", "\"226ba52e9a63d646b27622f629e2433f\"") // Renamed for clarity
            // Example: buildConfigField("String", "TMDB_API_KEY", "\"59cd6896d8432f9c69aed9b86b9c2931\"")
        }
        // You can create a production flavor that doesn't hardcode the key
        // create("production") {
        //     dimension = "version"
        //     // buildConfigField("String", "TMDB_API_KEY", "\"GET_FROM_SECURE_CONFIG\"")
        // }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false // Set to true for production releases with proper Proguard rules
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true // Needed for productFlavors.buildConfigField
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.extension.get() // Explicitly set
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    // Packaging options might be needed for duplicate classes if any (e.g. from logging interceptor)
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // BOM
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Test BOM
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Additional libraries from your original list
    implementation(libs.androidx.constraintlayout.compose) // If you use it
    implementation(libs.androidx.paging.compose) // If you use Paging 3
    implementation(libs.androidx.core.splashscreen)
    // implementation(libs.androidx.multidex) // Generally not needed for minSdk 26

    // XML Material components (can be removed if your app is 100% Compose and doesn't need it for anything)
    implementation(libs.material)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor) // Be mindful of logging in release builds
    implementation(libs.gson)

    // Image Loading (Landscapist with Coil)
    implementation(libs.landscapist.coil)
    implementation(libs.landscapist.placeholder)
    implementation(libs.landscapist.animation)
    implementation("io.coil-kt:coil-compose:2.4.0")
    // Or if you prefer direct Coil:
    // implementation(libs.coil.compose)


    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // Hilt compiler
    implementation(libs.androidx.hilt.navigation.compose)


    // Logger
    implementation(libs.timber)

    // Room database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler) // Use KSP for Room
    // annotationProcessor(libs.room.compiler) // Remove: Redundant if KSP is used for Room
}