package com.debdut.anchordi.ksp.validation

/**
 * Formats validation messages for readability. Use [formatError] and [formatWarn] so consumers
 * see a short summary plus optional detail and fix on separate lines (IDEs and logs that support
 * multi-line will display them clearly; single-line display still has clear sentence boundaries).
 */
object ValidationMessageFormat {
    private const val PREFIX = "[Anchor DI]"

    /**
     * Builds an error message with optional detail and fix.
     * Output format:
     *   [Anchor DI] &lt;summary&gt;
     *     Detail: &lt;detail&gt;   (if present)
     *     Fix: &lt;fix&gt;         (if present)
     */
    fun formatError(
        summary: String,
        detail: String? = null,
        fix: String? = null,
    ): String = buildMessage(PREFIX, summary, detail, fix)

    /**
     * Builds a warning message with optional detail and fix.
     */
    fun formatWarn(
        summary: String,
        detail: String? = null,
        fix: String? = null,
    ): String = buildMessage(PREFIX, summary, detail, fix)

    private fun buildMessage(
        prefix: String,
        summary: String,
        detail: String?,
        fix: String?,
    ): String {
        val b = StringBuilder().append(prefix).append(" ").append(summary.trim())
        if (!detail.isNullOrBlank()) b.append("\n  Detail: ").append(detail.trim())
        if (!fix.isNullOrBlank()) b.append("\n  Fix: ").append(fix.trim())
        return b.toString()
    }
}
