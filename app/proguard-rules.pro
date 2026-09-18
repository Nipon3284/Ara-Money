# Proguard rules for Ara Money
-keepattributes *Annotation*
-dontwarn javax.annotation.**
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
