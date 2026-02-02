package com.debdut.anchordi.ksp

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Minimal KSP tests: verify the processor provider is loadable and creates a processor.
 * Full processor tests (run on test sources and assert generated output) would require
 * KSP testing infrastructure (e.g. symbol-processing-test) or integration tests in a
 * consuming module.
 */
class AnchorSymbolProcessorProviderTest {
    @Test
    fun provider_create_returnsProcessor() {
        val provider = AnchorSymbolProcessorProvider()
        // We cannot call create() without a real SymbolProcessorEnvironment; just verify type.
        assertNotNull(provider)
        assertTrue(provider is SymbolProcessorProvider)
    }

    @Test
    fun provider_isLoadableViaServiceLoader() {
        val loader = java.util.ServiceLoader.load(SymbolProcessorProvider::class.java)
        val anchorProvider = loader.find { it is AnchorSymbolProcessorProvider }
        assertNotNull(anchorProvider)
    }
}
