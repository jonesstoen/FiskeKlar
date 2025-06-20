import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    id("com.google.devtools.ksp") version "2.1.20-1.0.31" // ksp plugin
}

android {
    namespace = "no.uio.ifi.in2000.team46"
    compileSdk = 35

    defaultConfig {
        applicationId = "no.uio.ifi.in2000.team46"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "MAPTILER_API_KEY", "\"${localProperties["MAPTILER_API_KEY"]}\"")
        buildConfigField("String", "MET_USER_AGENT_EMAIL", "\"${localProperties["MET_USER_AGENT_EMAIL"]}\"")
        buildConfigField("String", "MET_USER_AGENT_NAME", "\"${localProperties["MET_USER_AGENT_NAME"]}\"")
        buildConfigField("String", "BW_CLIENT_ID", "\"${localProperties["BW_CLIENT_ID"]}\"")
        buildConfigField("String", "BW_CLIENT_SECRET", "\"${localProperties["BW_CLIENT_SECRET"]}\"")



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
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.datetime)
    //maplibre
    implementation(libs.maplibre.android)
    //location tracking and permissions
    implementation(libs.play.services.location)
    //corutine
    implementation(libs.kotlinx.coroutines.play.services)
    //retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.runtime.livedata)

    //icons
    implementation(libs.androidx.material.icons.extended)

    // OkHttp and logging interceptor
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)


    // For grib
    implementation(libs.cdmcore)
    implementation(libs.grib)
    //for theme
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.media3.common.ktx)

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.compiler)

    // room database

    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    //coil for picture handling
    implementation("io.coil-kt:coil-compose:2.4.0")

    //For datastoring theme settings
    implementation (libs.androidx.datastore.preferences)

    // For testing
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.junit)
    testImplementation (libs.junit.jupiter.api)
    testImplementation(libs.slf4j.simple)

    tasks.withType<Test> {
        // Supress Byte-Buddy-warning
        jvmArgs = (jvmArgs ?: emptyList()) + "-XX:+EnableDynamicAgentLoading"

    }
    testImplementation(kotlin("test"))
}