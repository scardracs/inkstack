plugins {
	`java-library`
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.ksp)
}

group = "dev.scardracs.inkstack"
version = "1.0"

tasks.test {
	useJUnitPlatform()
}

ksp {
	arg("summaryOutputDir", "${layout.buildDirectory.get()}/generated/parsers-summary")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	compilerOptions {
		// Align with app: Kotlin 2.4 tops out at JVM 26 while the toolchain is JDK 27.
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_26)
		freeCompilerArgs.addAll(
			"-opt-in=kotlin.RequiresOptIn",
			"-opt-in=kotlin.contracts.ExperimentalContracts",
			"-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
			"-opt-in=org.inkstack.parsers.InternalParsersApi",
		)
	}
}

kotlin {
	jvmToolchain(27)
	explicitApiWarning()
	sourceSets["main"].kotlin.srcDirs("build/generated/ksp/main/kotlin")
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(26)
}

dependencies {
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.okhttp)
	implementation(libs.okio)
	implementation(libs.json)
	implementation(libs.androidx.collection)
	api(libs.jsoup)

	ksp(project(":parsers-ksp"))

	testImplementation(libs.junit.jupiter.api)
	testImplementation(libs.junit.jupiter.engine)
	testImplementation(libs.junit.jupiter.params)
	testRuntimeOnly(libs.junit.platform.launcher)
	testImplementation(libs.kotlinx.coroutines.test)
	testImplementation(libs.quickjs)
}
