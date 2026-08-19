package com.aistra.hail.app

internal fun normalizeTagOrder(
    current: List<Pair<String, Int>>,
    ordered: List<Pair<String, Int>>
): List<Pair<String, Int>> {
    if (current.size != ordered.size || ordered.firstOrNull()?.second != 0) return current
    val currentIds = current.map { it.second }
    val orderedIds = ordered.map { it.second }
    if (orderedIds.distinct().size != orderedIds.size || orderedIds.toSet() != currentIds.toSet()) return current
    return ordered.toList()
}
