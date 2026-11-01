import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ind.finance.enersense"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ind.finance.enersense"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Reached via `adb reverse tcp:8080 tcp:8080`, which tunnels the device's own
        // localhost:8080 to the host over ADB. Avoids the host firewall entirely.
        // Host-loopback alternative, if the firewall allows inbound 8080: http://10.0.2.2:8080/
        buildConfigField("String", "BASE_URL", "\"http://127.0.0.1:8080/\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.mpandroidchart)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

// Keeps the ADB reverse tunnel alive so the app can reach the backend on the host.
//
// The emulator's `127.0.0.1:8080` is forwarded over the ADB channel to the host's
// 8080. Unlike `10.0.2.2`, this never crosses the host network stack, so Windows
// Firewall is not involved. The tunnel is dropped whenever the emulator restarts,
// hence running it on every install rather than relying on memory.
val adbReverse = tasks.register<Exec>("adbReverse") {
    group = "enersense"
    description = "Forwards the device's localhost:8080 to the backend on this machine."

    val exe = if (System.getProperty("os.name").startsWith("Windows", true)) "adb.exe" else "adb"
    val sdkDir = run {
        val propFile = File(rootProject.projectDir, "local.properties")
        if (propFile.exists()) {
            val props = Properties()
            propFile.inputStream().use { props.load(it) }
            props.getProperty("sdk.dir")
        } else {
            null
        }
    } ?: System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")

    val adb = if (sdkDir != null) {
        File(sdkDir, "platform-tools/$exe")
    } else {
        File(exe)
    }

    commandLine(adb.absolutePath, "reverse", "tcp:8080", "tcp:8080")

    // No device attached is a normal state (a plain `assemble`, CI, a unit-test run).
    // It must not fail the build.
    isIgnoreExitValue = true
    standardOutput = ByteArrayOutputStream()
    errorOutput = ByteArrayOutputStream()
}

// Runs after any install task, which is what Android Studio's Run button triggers.
tasks.matching { it.name.startsWith("install") }.configureEach {
    finalizedBy(adbReverse)
}
