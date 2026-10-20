    -optimizationpasses 8

-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn coil3.PlatformContext

-keep class org.inkstack.InkstackApp { *; }
-keep class org.inkstack.core.exceptions.* { *; }
-keep class org.inkstack.parsers.** { *; }
-keep class org.inkstack.core.prefs.ScreenshotsPolicy { *; }
-keep class org.jsoup.parser.Tag
-keep class org.jsoup.internal.StringUtil
