package com.debdut.anchordi.ksp.validation

import com.debdut.anchordi.ksp.model.BindingDescriptor
import com.debdut.anchordi.ksp.model.ComponentDescriptor
import com.debdut.anchordi.ksp.model.DependencyRequirement
import com.debdut.anchordi.ksp.model.ModuleDescriptor

/**
 * All inputs required for model-level validation. Built after the DI model is constructed;
 * passed to each [ValidationPhase.MODEL] pass so validators stay single-responsibility.
 */
data class ValidationModelContext(
    val bindings: List<BindingDescriptor>,
    val injectClassDescriptors: List<InjectClassDescriptor>,
    val moduleDescriptors: List<ModuleDescriptor>,
    val components: Map<String, ComponentDescriptor>,
    val providedKeys: Set<String>,
    val requirements: List<DependencyRequirement>,
    val dependencyGraph: Map<String, Set<String>>
)
