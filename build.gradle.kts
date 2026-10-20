// Pin KGP/KSP above AGP's built-in Kotlin floor (2.2.10).
buildscript {
	dependencies {
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
		classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:${libs.versions.ksp.get()}")
	}
}

plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.parcelize) apply false
	alias(libs.plugins.hilt) apply false
	alias(libs.plugins.ksp) apply false
	alias(libs.plugins.room) apply false
	alias(libs.plugins.kotlinx.serialization) apply false
	alias(libs.plugins.compose.compiler) apply false
	// alias(libs.plugins.decoroutinator) apply false
}
