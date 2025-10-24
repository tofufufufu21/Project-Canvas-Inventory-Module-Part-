import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {

    alias(libs.plugins.kotlinMultiplatform)

    alias(libs.plugins.androidApplication)

    alias(libs.plugins.composeMultiplatform)

    alias(libs.plugins.composeCompiler)

    kotlin("plugin.serialization") version "2.0.20"

}

kotlin {

    androidTarget {

        compilerOptions {

            jvmTarget.set(JvmTarget.JVM_11)

        }

    }
    sourceSets {
        val androidMain by getting {
            kotlin.srcDir("src/androidMain/kotlin")
        }
    }


    listOf(

        iosArm64(),

        iosSimulatorArm64()

    ).forEach { iosTarget ->

        iosTarget.binaries.framework {

            baseName = "ComposeApp"

            isStatic = true

        }

    }



    sourceSets {

        androidMain.dependencies {

            implementation(compose.preview)

            implementation(libs.androidx.activity.compose)



// Coroutines Android

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")



// Ktor Android engine - MOVED HERE

            implementation("io.ktor:ktor-client-android:3.0.1")


            implementation("androidx.compose.material:material-icons-extended:1.7.8")

            implementation(compose.runtime)

            implementation(compose.foundation)

            implementation(compose.material3)

            implementation(compose.ui)

            implementation(compose.components.resources)

            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(libs.androidx.lifecycle.runtimeCompose)



// Coroutines

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")



// Supabase - ADDED HERE IN COMMONMAIN

            implementation("io.github.jan-tennert.supabase:postgrest-kt:3.0.2")

            implementation("io.github.jan-tennert.supabase:storage-kt:3.0.2")

            implementation("io.github.jan-tennert.supabase:realtime-kt:3.0.2")



// Ktor Core

            implementation("io.ktor:ktor-client-core:3.0.1")

            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")

            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")



// Kotlinx Serialization

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

        }


        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

            // Supabase
            implementation("io.github.jan-tennert.supabase:postgrest-kt:3.0.2")
            implementation("io.github.jan-tennert.supabase:storage-kt:3.0.2")
            implementation("io.github.jan-tennert.supabase:realtime-kt:3.0.2")

            // Ktor Core
            implementation("io.ktor:ktor-client-core:3.0.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")

            // Kotlinx Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

            implementation("io.coil-kt:coil-compose:2.5.0")
        }



        iosMain.dependencies {

// Ktor iOS engine

            implementation("io.ktor:ktor-client-darwin:3.0.1")

        }



        commonTest.dependencies {

            implementation(libs.kotlin.test)

        }

    }

}



android {

    namespace = "com.example.inventory"

    compileSdk = libs.versions.android.compileSdk.get().toInt()



    defaultConfig {

        applicationId = "com.example.inventory"

        minSdk = libs.versions.android.minSdk.get().toInt()

        targetSdk = libs.versions.android.targetSdk.get().toInt()

        versionCode = 1

        versionName = "1.0"

    }

    packaging {

        resources {

            excludes += "/META-INF/{AL2.0,LGPL2.1}"

        }

    }

    buildTypes {

        getByName("release") {

            isMinifyEnabled = false

        }

    }

    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_11

        targetCompatibility = JavaVersion.VERSION_11

    }

}



dependencies {

    debugImplementation(compose.uiTooling)



// REMOVED ALL SUPABASE/KTOR FROM HERE - they belong in sourceSets above

}