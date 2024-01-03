
# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

# If you keep the line number information, uncomment this to
# hide the original source file name.

#-renamesourcefileattribute SourceFile
-dontwarn com.squareup.picasso.**
-keepattributes *Annotation*,Annotation,EnclosingMethod,SourceFile,LineNumberTable,Signature,Exception,InnerClasses

-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class app.inspiry.**$$serializer { *; }
-keepclassmembers class app.inspiry.** {
    *** Companion;
}
-keepclasseswithmembers class app.inspiry.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep public class com.android.installreferrer.** { *; }

-keep class com.adapty.** { *; }

-keep class com.android.billingclient.** { *; }

-keep public class io.ktor.client.** {
    public <methods>;
    private <methods>;
}

-keepnames class app.inspiry.views.group.InspGroupView
-keepnames class app.inspiry.views.text.InspTextView
-keepnames class app.inspiry.views.path.InspPathView
-keepnames class app.inspiry.views.template.InspTemplateView
-keepnames class app.inspiry.views.vector.InspVectorView
-keepnames class app.inspiry.views.InspView
-keepnames class app.inspiry.views.media.InspMediaView

-keep class com.un4seen.bass.** {*;}