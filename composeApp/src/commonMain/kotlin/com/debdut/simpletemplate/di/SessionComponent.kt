package com.debdut.simpletemplate.di

import com.debdut.anchordi.Component

/**
 * Custom component scope for the sample app: one instance per app session.
 * Demonstrates [Anchor.scopedContainer][com.debdut.anchordi.runtime.Anchor.scopedContainer]
 * and custom [InstallIn] scopes. Bindings in [SessionModule] are created once per scope.
 */
@Component
interface SessionComponent
