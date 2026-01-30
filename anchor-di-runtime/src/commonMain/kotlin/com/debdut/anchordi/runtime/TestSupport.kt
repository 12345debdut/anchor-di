package com.debdut.anchordi.runtime

/**
 * Utilities for testing with Anchor DI.
 *
 * ## Basic usage
 *
 * ```
 * @Before
 * fun setUp() {
 *     Anchor.reset()
 *     Anchor.init(AnchorGenerated)  // Or TestContributor with mocks
 * }
 *
 * @After
 * fun tearDown() {
 *     Anchor.reset()
 * }
 * ```
 *
 * ## With test doubles
 *
 * Create a [ComponentBindingContributor] that registers mock implementations:
 *
 * ```
 * object TestContributor : ComponentBindingContributor {
 *     override fun contribute(registry: BindingRegistry) {
 *         registry.register(Key("com.example.Api", null), Binding.Singleton(object : Factory<Any> {
 *             override fun create(container: AnchorContainer) = MockApi()
 *         }))
 *     }
 * }
 *
 * @Before
 * fun setUp() {
 *     Anchor.reset()
 *     Anchor.init(AnchorGenerated, TestContributor)  // TestContributor last = overrides
 * }
 * ```
 *
 * Note: Order matters—earlier contributors override later ones for the same key.
 */
// Marker for documentation; actual usage is via Anchor.reset() and Anchor.init()
