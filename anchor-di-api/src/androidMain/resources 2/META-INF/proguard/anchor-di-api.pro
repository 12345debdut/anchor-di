# Anchor DI API — ProGuard/R8 consumer rules
# Keep annotations with BINARY or RUNTIME retention so they survive R8.

-keep @interface com.debdut.anchordi.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
