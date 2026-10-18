    -optimizationpasses 8

-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn coil3.PlatformContext

-keep class org.inkstack.InkstackApp { *; }
-keep class org.inkstack.settings.about.changelog.ChangelogFragment

-keep class org.inkstack.core.exceptions.* { *; }
-keep class org.inkstack.parsers.** { *; }
-keep class org.inkstack.core.prefs.ScreenshotsPolicy { *; }
-keep class org.inkstack.backups.ui.periodical.PeriodicalBackupSettingsFragment { *; }
-keep class org.jsoup.parser.Tag
-keep class org.jsoup.internal.StringUtil

-keep class org.acra.security.NoKeyStoreFactory { *; }
-keep class org.acra.config.DefaultRetryPolicy { *; }
-keep class org.acra.attachment.DefaultAttachmentProvider { *; }
-keep class org.acra.sender.JobSenderService
# Loaded by ACRA through ServiceLoader (META-INF/services), so nothing references it directly.
-keep class org.inkstack.core.util.SentryReportSenderFactory { *; }
