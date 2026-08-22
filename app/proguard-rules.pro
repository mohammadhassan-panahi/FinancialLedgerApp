# Financial Ledger App ProGuard Obfuscation & Security Keep Rules

# Room Database Keep Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# SQLCipher Encrypted Database Keep Rules
-keep class net.zetetic.database.sqlcipher.** { *; }
-keep class net.zetetic.database.sqlcipher.SupportOpenHelperFactory { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# Keep Financial Entities & Model Data Classes
-keep class com.example.data.local.** { *; }
-keepclassmembers class com.example.data.local.** { *; }
-keep class com.example.util.FinancialFormulas { *; }

# Biometrics
-keep class androidx.biometric.** { *; }

# Kotlin Coroutines & Serialization
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

