# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Apps\Dev\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard/index.html

# Add any project-specific rules here

# Baseline Coroutines & Compose Rules
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class kotlinx.coroutines.** { *; }
-keep class androidx.compose.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
