import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.moviceapp"
    buildFeatures {
        // dataBinding = true already implies viewBinding, so viewBinding is not needed separately
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    compileSdk = 36

    val properties = Properties()
    rootProject.file("local.properties").inputStream().use { properties.load(it) }

    defaultConfig {
        applicationId = "com.example.moviceapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["IP_API_SERVER"] = properties.getValue(
            "network.ip.local").toString()
        manifestPlaceholders["PORT_API_SERVER"] = properties.getValue(
            "network.port.local").toString()

        buildConfigField("String", "IP_API_SERVER", "\""+properties.getValue(
            "network.ip.local").toString()+"\"")
        buildConfigField("String", "PORT_API_SERVER", "\""+properties.getValue(
            "network.port.local").toString()+"\"")
        resValue("string", "IP_API_SERVER", properties.getValue(
            "network.ip.local").toString())
        resValue("string", "PORT_API_SERVER", properties.getValue(
            "network.port.local").toString())
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
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    // ViewPager2
    implementation(libs.androidx.viewpager2)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.okhttp.logging.interceptor)
    // Views/Fragments integration
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    // Feature module support for Fragments (not currently used — commented out)
    // implementation(libs.androidx.navigation.dynamic.features.fragment)
    implementation(libs.androidx.recyclerview)
    // Testing Navigation
    androidTestImplementation(libs.androidx.navigation.testing)
    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)
    // Coil Image Loading
    implementation(libs.coil)
    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}