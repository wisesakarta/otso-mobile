-keepnames class com.otso.app.model.** { *; }
-keepnames class com.otso.app.viewmodel.** { *; }

-keepattributes RuntimeVisibleAnnotations, *Annotation*, Signature

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

-keepclassmembers class **.R$* {
    public static <fields>;
}