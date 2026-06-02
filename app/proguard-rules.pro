-keep class com.powergrid.exemployee.data.remote.model.** { *; }
-keep class com.powergrid.exemployee.domain.model.** { *; }
-keep interface com.powergrid.exemployee.data.remote.*Api { *; }
-keepattributes *Annotation*, Signature
-dontwarn okhttp3.**, retrofit2.**

# TensorFlow Lite
-keep class org.tensorflow.** { *; }
-keep interface org.tensorflow.** { *; }
-dontwarn org.tensorflow.**
