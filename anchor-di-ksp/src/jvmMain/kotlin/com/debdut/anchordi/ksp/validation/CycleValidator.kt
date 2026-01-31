package com.debdut.anchordi.ksp.validation

/**
 * Validates that the dependency graph has no cycles.
 * [graph] maps each node to the set of nodes it depends on.
 */
object CycleValidator {

    fun validate(graph: Map<String, Set<String>>, reporter: ValidationReporter) {
        val visited = mutableSetOf<String>()
        val path = mutableSetOf<String>()
        val pathOrder = mutableListOf<String>()

        fun dfs(node: String): Boolean {
            if (path.contains(node)) {
                val cycleStart = pathOrder.indexOf(node)
                val cycle = pathOrder.drop(cycleStart) + node
                val suggestType = cycle.firstOrNull() ?: node
                reporter.error(
                    "[Anchor DI] Circular dependency: ${cycle.joinToString(" -> ")}. " +
                        "Break the cycle by using Lazy<$suggestType> or AnchorProvider<$suggestType> for one of the dependencies.",
                    null
                )
                return true
            }
            if (visited.contains(node)) return false
            visited.add(node)
            path.add(node)
            pathOrder.add(node)
            var hasCycle = false
            graph[node]?.forEach { dep ->
                if (dfs(dep)) hasCycle = true
            }
            path.remove(node)
            pathOrder.removeAt(pathOrder.lastIndex)
            return hasCycle
        }

        graph.keys.forEach { if (dfs(it)) return }
    }
}
