# Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontnote kotlin.internal.PlatformImplementationsKt
-dontnote kotlin.reflect.jvm.internal.**
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}

-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}

-keepclassmembers class **$Companion {
    public kotlinx.serialization.KSerializer serializer(...);
}

-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**


-verbose

-keepattributes Exceptions,InnerClasses,MethodParameters,*Annotation*,EnclosingMethod,Signature

-keepclassmembers class * { public <init>(...); }

-keep class kotlin.reflect.** { *; }

-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Ktor core
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# CIO / OkHttp / Android engine (залиши той, що використовуєш)
-keep class io.ktor.client.engine.** { *; }

# Content negotiation
-keep class io.ktor.serialization.** { *; }

# Зберегти RPC сервіси
-keep class kotlinx.rpc.** { *; }
-dontwarn kotlinx.rpc.**

# Зберегти всі інтерфейси RPC
-keep interface **RpcService
-keep interface **Service

-keep @interface kotlinx.rpc.*

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class androidx.** { *; }
-dontwarn androidx.**

-keep class android.** { *; }
-dontwarn android.**

-keep class kotlin.** { *; }
-dontwarn kotlin.**

-keep class kotlinx.** { *; }
-dontwarn kotlinx.**

-keep class ua.vald_zx.game.rat.race.card.shared.** { *; }
-dontwarn ua.vald_zx.game.rat.race.card.shared.**