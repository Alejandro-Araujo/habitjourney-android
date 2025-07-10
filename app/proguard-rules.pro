# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# WorkManager con Hilt
-keep class androidx.hilt.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}
-keep class com.alejandro.habitjourney.features.task.data.worker.** { *; }


-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}


# Keep all members of Google Play Services Auth related classes
-keep class com.google.android.gms.auth.** { *; }
-keep interface com.google.android.gms.auth.** { *; }

# Keep all members of Google Identity Services related classes (especially GoogleIdTokenCredential)
-keep class com.google.android.libraries.identity.** { *; }
-keep interface com.google.android.libraries.identity.** { *; }

# General rules for Credential Manager and Play Services adapter
-keep class androidx.credentials.** { *; }
-keep interface androidx.credentials.** { *; }
-keep class androidx.credentials.playservices.** { *; }

# If GoogleIdTokenCredential is still not recognized, explicitly keep it
-keep class com.google.android.libraries.identity.googleid.GoogleIdTokenCredential { *; }
-keep class com.google.android.libraries.identity.googleid.GoogleIdTokenCredential$Builder { *; }