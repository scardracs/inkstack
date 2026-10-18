import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlin.parcelize)
	alias(libs.plugins.hilt)
	alias(libs.plugins.room)
	alias(libs.plugins.kotlinx.serialization)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.sentry)
	// enable if needed
	// alias(libs.plugins.decoroutinator)
}

// Reads a credential from local.properties, falling back to an environment variable for CI.
//
// Properties.load only splits on the first '=', so a base64 token's '=' padding survives untouched
// and needs no quoting. Quotes would in fact become part of the value, so they are stripped here -
// along with stray whitespace - to make that paste mistake harmless instead of silently wrong.
fun readSecret(fileKey: String, envKey: String): String {
	val props = Properties()
	val file = rootProject.file("local.properties")
	if (file.exists()) {
		file.inputStream().use { props.load(it) }
	}
	var value = (props.getProperty(fileKey) ?: System.getenv(envKey) ?: "").trim()
	while (
		value.length > 1 &&
			(value.startsWith("'") || value.startsWith("\"")) &&
			value.first() == value.last()
	) {
		value = value.substring(1, value.length - 1).trim()
	}
	return value
}

android {
	compileSdk = 37
	buildToolsVersion = "36.0.0"
	namespace = "org.inkstack"

	dependenciesInfo {
		// Disables dependency metadata when building APKs.
		includeInApk = false
		// Disables dependency metadata when building Android App Bundles.
		includeInBundle = false
	}

	defaultConfig {
		applicationId = "org.inkstack"
		minSdk = 28
		targetSdk = 37
		versionCode = 1
		versionName = "0.1.0"
		ndk {
			// ARM only. x86 and x86_64 devices - Intel Chromebooks, emulators, BlueStacks - run these
			// through Android's ARM translation, so the x86 copies of every native library were
			// megabytes of download nobody needed. A device without that translation cannot install.
			abiFilters += listOf("armeabi-v7a", "arm64-v8a")
		}
		testInstrumentationRunner = "org.inkstack.HiltTestRunner"
		androidResources {
			// https://issuetracker.google.com/issues/408030127
			generateLocaleConfig = false
		}
		val localProperties = Properties()
		val localPropertiesFile = rootProject.file("local.properties")
		if (localPropertiesFile.exists()) {
			localPropertiesFile.inputStream().use { localProperties.load(it) }
		}
		resValue(
			"string",
			"tg_backup_bot_token",
			localProperties.getProperty("tg_backup_bot_token", ""),
		)
		// Crash reporting endpoint. Empty unless a `sentry_dsn` is set in local.properties (or the
		// SENTRY_DSN environment variable, for CI); an empty DSN disables reporting entirely, so a
		// plain clone still builds and simply never asks the user to send anything.
		val sentryDsn = readSecret("sentry_dsn", "SENTRY_DSN")
		buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")

		// Host that serves the shareable https manga links and the assetlinks.json that lets Android
		// verify them. Must match the deployed worker's hostname in web/wrangler.toml exactly, or the
		// links stop opening the app directly. Used by the manifest and by Manga.appUrl.
		val appLinkHost = "inkstack.app"
		manifestPlaceholders["appLinkHost"] = appLinkHost
		buildConfigField("String", "APP_LINK_HOST", "\"$appLinkHost\"")
	}

	signingConfigs {
		create("release") {
			// Resolve relative store paths from the repo root (local.properties lives there).
			val storePath = readSecret("RELEASE_STORE_FILE", "RELEASE_STORE_FILE")
			val storeFileCandidate = when {
				storePath.isEmpty() -> rootProject.file("keystore/inkstack-release.jks")
				File(storePath).isAbsolute -> file(storePath)
				else -> rootProject.file(storePath)
			}
			if (storeFileCandidate.isFile) {
				storeFile = storeFileCandidate
				storePassword = readSecret("RELEASE_STORE_PASSWORD", "RELEASE_STORE_PASSWORD")
				keyAlias = readSecret("RELEASE_KEY_ALIAS", "RELEASE_KEY_ALIAS").ifEmpty { "inkstack" }
				keyPassword = readSecret("RELEASE_KEY_PASSWORD", "RELEASE_KEY_PASSWORD")
					.ifEmpty { storePassword }
			}
		}
	}

	buildTypes {
		debug {
			applicationIdSuffix = ".debug"
		}
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
			signingConfigs.findByName("release")?.takeIf { it.storeFile?.isFile == true }?.let {
				signingConfig = it
			}
		}
		create("nightly") {
			initWith(getByName("release"))
			applicationIdSuffix = ".nightly"
		}
	}

	buildFeatures {
		viewBinding = true
		buildConfig = true
		compose = true
		resValues = true
	}

	packaging {
		resources {
			excludes += listOf(
				"META-INF/README.md",
				"META-INF/NOTICE.md",
			)
		}
		jniLibs {
			// Replaces android:extractNativeLibs="true" (forbidden in AGP 9+).
			useLegacyPackaging = true
		}
	}

	sourceSets {
		getByName("androidTest").assets.directories.add("$projectDir/schemas")
		getByName("main").kotlin.directories.add("src/main/kotlin/")
	}

	compileOptions {
		// Kotlin 2.4.x tops out at JVM 26; toolchain still runs on JDK 27.
		sourceCompatibility = JavaVersion.VERSION_26
		targetCompatibility = JavaVersion.VERSION_26
	}

	room {
		schemaDirectory("$projectDir/schemas")
	}

	lint {
		abortOnError = true
		disable += setOf(
			"MissingTranslation",
			"PrivateResource",
			"SetJavaScriptEnabled",
			"SimpleDateFormat",
		)
	}

	testOptions {
		unitTests.isIncludeAndroidResources = true
		unitTests.isReturnDefaultValues = false
	}
}

ksp {
	arg("room.generateKotlin", "true")
}

kotlin {
	jvmToolchain(27)
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_26)
		freeCompilerArgs.addAll(
			"-opt-in=kotlin.ExperimentalStdlibApi",
			"-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
			"-opt-in=kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi",
			"-opt-in=kotlinx.coroutines.InternalForInheritanceCoroutinesApi",
			"-opt-in=kotlinx.coroutines.FlowPreview",
			"-opt-in=kotlin.contracts.ExperimentalContracts",
			"-opt-in=coil3.annotation.ExperimentalCoilApi",
			"-opt-in=coil3.annotation.InternalCoilApi",
			"-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
			"-opt-in=org.inkstack.parsers.InternalParsersApi",
			"-Xjspecify-annotations=strict",
			"-Xannotation-default-target=first-only",
			"-Xtype-enhancement-improvements-strict-mode",
		)
	}
}

extensions.configure<ApplicationAndroidComponentsExtension>("androidComponents") {
	onVariants(selector().withBuildType("nightly")) { variant ->
		val now = LocalDateTime.now()
		val code = now.format(DateTimeFormatter.ofPattern("yyMMdd")).toInt()
		val name = "N" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
		variant.outputs.forEach { output ->
			output.versionCode.set(code)
			output.versionName.set(name)
		}
	}
}

// Uploads the R8 mapping file so release and nightly crash reports arrive deobfuscated. Without it
// every production stack trace in Sentry is unreadable, since both build types are minified.
//
// Needs sentry_org / sentry_project in local.properties (or SENTRY_ORG / SENTRY_PROJECT env vars) and
// a SENTRY_AUTH_TOKEN. With no token the build still works - the mapping is bundled and tagged with a
// uuid, just not uploaded, so it can be uploaded later with sentry-cli.
sentry {
	val sentryOrg = readSecret("sentry_org", "SENTRY_ORG")
	val sentryProject = readSecret("sentry_project", "SENTRY_PROJECT")
	val sentryToken = readSecret("sentry_auth_token", "SENTRY_AUTH_TOKEN")

	org.set(sentryOrg)
	projectName.set(sentryProject)
	authToken.set(sentryToken)

	// This org lives in Sentry's EU region. sentry-cli defaults to sentry.io (US), where the org does
	// not exist, so the mapping upload fails there rather than anywhere useful. The DSN says the same
	// thing - it points at ingest.de.sentry.io. Override via local.properties / SENTRY_URL if the org
	// is ever migrated to another region.
	val sentryUrl = readSecret("sentry_url", "SENTRY_URL")
	url.set(sentryUrl.ifEmpty { "https://de.sentry.io" })

	includeProguardMapping.set(true)
	autoUploadProguardMapping.set(
		sentryToken.isNotEmpty() && sentryOrg.isNotEmpty() && sentryProject.isNotEmpty(),
	)
	// Debug builds are not minified, so there is nothing to map.
	ignoredBuildTypes.set(listOf("debug"))

	// The SDK dependency is pinned by hand in the version catalog; let the plugin leave it alone.
	autoInstallation {
		enabled.set(false)
	}
	// Performance tracing is not used, and its bytecode instrumentation would add overhead for data
	// nothing consumes.
	tracingInstrumentation {
		enabled.set(false)
	}
	uploadNativeSymbols.set(false)
	includeSourceContext.set(false)
}

dependencies {
	implementation(project(":parsers")) {
		exclude(group = "org.json", module = "json")
	}

	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlinx.coroutines.android)
	implementation(libs.kotlinx.coroutines.guava)

	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.runtime)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.activity)

	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.core)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.fragment)
	implementation(libs.androidx.transition)
	implementation(libs.androidx.collection)
	implementation(libs.lifecycle.viewmodel)
	implementation(libs.lifecycle.service)
	implementation(libs.lifecycle.process)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.documentfile)
	implementation(libs.androidx.swiperefreshlayout)
	implementation(libs.androidx.recyclerview)
	implementation(libs.androidx.viewpager2)
	implementation(libs.androidx.preference)
	implementation(libs.androidx.biometric)
	implementation(libs.material)
	implementation(libs.androidx.lifecycle.common.java8)
	implementation(libs.androidx.webkit)

	implementation(libs.androidx.work.runtime)
	implementation(libs.guava)

	// Foldable/Window layout
	implementation(libs.androidx.window)

	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.room.ktx)
	ksp(libs.androidx.room.compiler)

	implementation(libs.okhttp)
	implementation(libs.okhttp.tls)
	implementation(libs.okhttp.dnsoverhttps)
	implementation(libs.okio)
	implementation(libs.kotlinx.serialization.json)

	implementation(libs.adapterdelegates)
	implementation(libs.adapterdelegates.viewbinding) {
		// Conflicts with kotlin-parcelize-runtime (duplicate kotlinx.android.parcel.*).
		exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
	}

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)
	implementation(libs.androidx.hilt.work)
	ksp(libs.androidx.hilt.compiler)

	implementation(libs.coil.core)
	implementation(libs.coil.network)
	implementation(libs.coil.gif)
	implementation(libs.coil.svg)
	implementation(libs.avif.decoder)
	implementation(libs.jxl.coder)
	implementation(libs.ssiv)
	implementation(libs.disk.lru.cache)
	implementation(libs.markwon)
	implementation(libs.kizzyrpc)

	implementation(libs.acra.dialog)
	implementation(libs.sentry.android)

	"debugImplementation"(libs.leakcanary.android)
	"nightlyImplementation"(libs.leakcanary.android)
	"debugImplementation"(libs.workinspector)

	testImplementation(libs.junit)
	testImplementation(libs.json)
	testImplementation(libs.kotlinx.coroutines.test)

	androidTestImplementation(libs.androidx.runner)
	androidTestImplementation(libs.androidx.rules)
	androidTestImplementation(libs.androidx.test.core)
	androidTestImplementation(libs.androidx.junit)

	androidTestImplementation(libs.kotlinx.coroutines.test)

	androidTestImplementation(libs.androidx.room.testing)
	androidTestImplementation(libs.moshi.kotlin)

	androidTestImplementation(libs.hilt.android.testing)
	kspAndroidTest(libs.hilt.android.compiler)
}
