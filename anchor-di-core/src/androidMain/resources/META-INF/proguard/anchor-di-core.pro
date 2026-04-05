# Anchor DI Core — ProGuard/R8 consumer rules
# These rules are bundled with the library and applied automatically to consumer projects.

# Keep the runtime container and public API
-keep class com.debdut.anchordi.runtime.Anchor { *; }
-keep class com.debdut.anchordi.runtime.AnchorContainer { *; }
-keep class com.debdut.anchordi.runtime.Key { *; }
-keep class com.debdut.anchordi.runtime.Binding { *; }
-keep class com.debdut.anchordi.runtime.Binding$* { *; }
-keep class com.debdut.anchordi.runtime.Factory { *; }
-keep class com.debdut.anchordi.runtime.AnchorProvider { *; }
-keep class com.debdut.anchordi.runtime.BindingRegistry { *; }

# Keep all generated binding contributors (KSP-generated code)
-keep class * implements com.debdut.anchordi.runtime.ComponentBindingContributor { *; }
-keep class com.debdut.anchordi.generated.** { *; }
