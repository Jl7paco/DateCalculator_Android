# ====================================================================
# Google Play R8 / ProGuard Optimization & Obfuscation
# App: Date Calculator (me.paco.datecalculator)
# ====================================================================

# 保留堆栈跟踪行号，方便在 Google Play Console 查看崩溃日志 (Android Vitals)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保留所有 Annotations (Jetpack Compose @Composable 需要)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 保留所有 ViewModel 与 UI 状态组件，彻底解决 R8 字节码校验崩溃 (VerifyError)
-keep class me.paco.datecalculator.ui.** { *; }
-keepclassmembers class me.paco.datecalculator.ui.** { *; }

-keep class me.paco.datecalculator.data.** { *; }
-keepclassmembers class me.paco.datecalculator.data.** { *; }

-keep class me.paco.datecalculator.ui.viewmodel.** { *; }
-keepclassmembers class me.paco.datecalculator.ui.viewmodel.** { *; }

# Jetpack Compose 框架保护规则
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# 保留所有 Enum 类的 values() 与 valueOf()
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
