package com.watermelonkode.simpletemplate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform