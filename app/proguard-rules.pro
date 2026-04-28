-keep class com.otso.app.model.** { *; }
-keepnames class com.otso.app.viewmodel.**

# Gson-serialized data classes: field names must survive R8 renaming
-keep class com.otso.app.core.SessionData { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

-keepattributes RuntimeVisibleAnnotations, *Annotation*, Signature, EnclosingMethod, InnerClasses

-keepclassmembers class ** {
    @androidx.compose.runtime.Stable <fields>;
    @androidx.compose.runtime.Immutable <fields>;
}

-keepnames class kotlinx.coroutines.internal.DiagnosableCoroutineContext { *; }
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers class **$Serializer { *; }
-keepclassmembers class **$Companion { *; }

-keepclassmembers class **.R$* {
    public static <fields>;
}