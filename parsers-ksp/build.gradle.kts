plugins {
	alias(libs.plugins.kotlin.jvm)
}

kotlin {
	jvmToolchain(27)
	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_26)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(26)
}

dependencies {
	implementation(libs.ksp.symbol.processing.api)
}
