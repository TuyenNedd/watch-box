# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class dev.watchbox.**$$serializer { *; }
-keepclassmembers class dev.watchbox.** { *** Companion; }
-keepclasseswithmembers class dev.watchbox.** { kotlinx.serialization.KSerializer serializer(...); }
