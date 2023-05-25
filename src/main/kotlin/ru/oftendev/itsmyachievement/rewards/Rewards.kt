package ru.oftendev.itsmyachievement.rewards

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory

object Rewards: ConfigCategory("category", "categories") {
    /** Registered items. */
    private val registry = Registry<Reward>()

    @JvmStatic
    val rewards = mutableListOf<Reward>()

    override fun afterReload(plugin: LibreforgePlugin) {
        plugin.logger.info(
            "&fLoaded &3${rewards.size} &frewards"
        )
    }

    @JvmStatic
    fun getById(id: String) = rewards.firstOrNull { it.id.equals(id, true) }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(Reward(config))
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }
}