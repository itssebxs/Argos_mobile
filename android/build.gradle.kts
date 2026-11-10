buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.4")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}

// Fix for plugins that expect the 'flutter' property to be available (e.g. app_links)
// and ensuring compileSdkVersion is high enough to avoid 'lStar' resource errors.
subprojects {
    afterEvaluate {
        if (project.plugins.hasPlugin("com.android.library") || project.plugins.hasPlugin("com.android.application")) {
            val android = project.extensions.findByName("android") as? com.android.build.gradle.BaseExtension
            android?.apply {
                compileSdkVersion(36)
                defaultConfig {
                    targetSdkVersion(34)
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }

            // Alinea plugins antiguos (p. ej. flutter_vibrate) con el jvmTarget 17
            // que usa Kotlin desde Flutter 3.47.
            tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
                sourceCompatibility = JavaVersion.VERSION_17.toString()
                targetCompatibility = JavaVersion.VERSION_17.toString()
            }

            if (!project.hasProperty("flutter")) {
                project.extensions.extraProperties.set("flutter", mapOf(
                    "compileSdkVersion" to 36,
                    "targetSdkVersion" to 34,
                    "minSdkVersion" to 21
                ))
            }
        }
    }
}

// Alinea tambien el jvmTarget de Kotlin (no solo Java) a 17 en todos los
// submódulos. Algunos plugins antiguos (in_app_update, flutter_vibrate)
// dejan Kotlin en 1.8 mientras Java ya compila en 17, lo que rompe el build.
subprojects {
    afterEvaluate {
        tasks.configureEach {
            val task = this
            val className = task.javaClass.name
            if (className.contains("KotlinCompile") && !className.contains("KotlinCompile\$")) {
                runCatching {
                    val kotlinOptions = task.javaClass.methods
                        .firstOrNull { it.name == "getKotlinOptions" && it.parameterCount == 0 }
                        ?.invoke(task)
                    kotlinOptions?.javaClass?.methods
                        ?.firstOrNull { it.name == "setJvmTarget" && it.parameterCount == 1 }
                        ?.invoke(kotlinOptions, JavaVersion.VERSION_17.toString())
                }
            }
        }
    }
}

subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}