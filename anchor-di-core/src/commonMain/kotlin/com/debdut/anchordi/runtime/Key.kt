package com.debdut.anchordi.runtime

/**
 * Identifies a unique binding in the container.
 *
 * A key combines the fully-qualified type name with an optional qualifier
 * (e.g. from [com.debdut.anchordi.Named]) to disambiguate
 * multiple bindings of the same type.
 */
data class Key(
    val typeName: String,
    val qualifier: String? = null,
) {
    override fun toString(): String =
        if (qualifier != null) {
            "$typeName[@$qualifier]"
        } else {
            typeName
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Key) return false
        return typeName == other.typeName && qualifier == other.qualifier
    }

    override fun hashCode(): Int = 31 * typeName.hashCode() + (qualifier?.hashCode() ?: 0)
}
