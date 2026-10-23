plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val edgeRuntimeBaseUrl = providers.gradleProperty("edgeRuntimeBaseUrl")
    .orElse("https://YOUR_PROJECT_REF.supabase.co/functions/v1")
    .get()
val useRemoteDeals = providers.gradleProperty("useRemoteDeals").orNull == "true"

android {
    namespace = "com.apps.tinyprice"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.apps.tinyprice"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "EDGE_RUNTIME_BASE_URL", "\"$edgeRuntimeBaseUrl\"")
        buildConfigField("boolean", "USE_REMOTE_DEALS", useRemoteDeals.toString())
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}