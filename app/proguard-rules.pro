# ====================================================================
# Google Play R8 / ProGuard Optimization & Obfuscation
# App: Date Calculator (me.paco.datecalculator)
# ====================================================================

# 保留堆栈跟踪行号，方便在 Google Play Console 查看崩溃日志 (Android Vitals)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保留所有 Annotations (Jetpack Compose @Composable 需要)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 保留 Data 实体类中序列化所需字段
-keepclassmembers class me.paco.datecalculator.data.** { *; }

# 允许 R8 混淆、重命名与缩减 UI 与 ViewModel 类中的内部方法与字段
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }

# 保护 @Keep 注解修饰的类与成员
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# 保留所有 Enum 类的 values() 与 valueOf()
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
