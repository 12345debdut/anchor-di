package com.debdut.simpletemplate.repository

/**
 * Repository contract for greeting text.
 * Bound to [GreetingRepositoryImpl] in [com.debdut.simpletemplate.di.RepositoryModule].
 */
interface GreetingRepository {
    /** Returns a platform-aware greeting string. */
    fun greet(): String
}
