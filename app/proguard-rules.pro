# ====================================================================
# Google Play R8 / ProGuard Full Mode Optimization & Obfuscation
# App: Date Calculator (me.paco.datecalculator)
# ====================================================================

# 开启 R8 多次极致代码优化与访问权限提升内联
-optimizationpasses 5
-allowaccessmodification

# 保留堆栈跟踪行号，方便在 Google Play Console 查看崩溃日志 (Android Vitals)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保留所有 Annotations (Jetpack Compose @Composable, @Preview 需要)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 保留 SharedPreferences 序列化数据模型 (防止混淆后数据读写失败)
-keep class me.paco.datecalculator.data.** { *; }
-keepclassmembers class me.paco.datecalculator.data.** { *; }

-keep class me.paco.datecalculator.ui.viewmodel.CustomEventItem { *; }
-keep class me.paco.datecalculator.ui.viewmodel.EventRepeatMode { *; }
-keep class me.paco.datecalculator.ui.viewmodel.CalcSubMode { *; }
-keep class me.paco.datecalculator.ui.viewmodel.RangeBreakdownResult { *; }
-keep class me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState { *; }

# 保留所有 Enum 类的 values() 与 valueOf()
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
