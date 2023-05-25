package ru.oftendev.itsmyachievement.target

import com.willfp.eco.core.integrations.IntegrationLoader

enum class TargetType(val id: String, var enabled: Boolean = false) {
    PLAYER("player", true),
    BENTO_BOX("bentobox"),
    FUUID("factionsuuid"),
    GLOBAL("global", true),
    HUSKTOWNS("husktowns"),
    SS2("superiorskyblock2");

    fun getById(id: String): TargetType? {
        return values().filter { it.enabled }.firstOrNull { it.id.equals(id, true) }
    }

    fun enable() {
        this.enabled = true
    }

    override fun toString(): String {
        return this.id
    }

    companion object {
        @JvmStatic
        fun getIntegrationLoaders(): List<IntegrationLoader> {
            return listOf(
                IntegrationLoader("BentoBox") { BENTO_BOX.enable() },
                IntegrationLoader("FactionsUUID") { FUUID.enable() },
                IntegrationLoader("HuskTowns") { HUSKTOWNS.enable() },
                IntegrationLoader("SuperiorSkyblock2") { SS2.enable() },
            )
        }
    }
}