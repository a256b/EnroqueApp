import java.util.Properties
import java.io.FileInputStream

fun getLocalProperty(key: String): String? {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use { fis ->
            properties.load(fis)
        }
        return properties.getProperty(key)
    }
    return null
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.apptorneosajedrez"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.apptorneosajedrez"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Leer la key manualmente y pasarla a manifestPlaceholders
        manifestPlaceholders["MAPS_API_KEY"] = getLocalProperty("MAPS_API_KEY") ?: ""
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
    }
}

dependencies {

implementation(platform(libs.firebase.bom))

implementation(libs.androidx.core.ktx)
implementation(libs.androidx.appcompat)
implementation(libs.androidx.activity)
implementation(libs.androidx.fragment.ktx)
implementation(libs.androidx.legacy.support.v4)
implementation(libs.androidx.annotation)
implementation(libs.material)
implementation(libs.androidx.constraintlayout)

implementation(libs.androidx.lifecycle.livedata.ktx)
implementation(libs.androidx.lifecycle.viewmodel.ktx)

implementation(libs.androidx.navigation.fragment.ktx)
implementation(libs.androidx.navigation.ui.ktx)

implementation(libs.firebase.firestore.ktx)
implementation(libs.firebase.auth)
implementation(libs.firebase.auth.ktx)

implementation(libs.play.services.maps)
implementation(libs.play.services.auth)

implementation(libs.androidx.credentials)
implementation(libs.androidx.credentials.play.services.auth)
implementation(libs.googleid)

testImplementation(libs.junit)
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)

}