package ru.oftendev.itsmyachievement.achievement

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import ru.oftendev.itsmyachievement.ItsMyAchievement
import ru.oftendev.itsmyachievement.util.fetchRecursiveConfigs

object Achievements: ConfigCategory("category", "categories") {
    /** Registered items. */
    private val registry = Registry<AchievementCategory>()

    @JvmStatic
    val categories
        get() = registry.values()

    @JvmStatic
    val enabledCategories: List<AchievementCategory>
        get() = categories.filter { it.enabled }

    @JvmStatic
    fun getById(id: String): AchievementCategory? {
        return categories.filter { it.enabled }.firstOrNull { it.id.equals(id, true) }
    }

    override fun beforeReload(plugin: LibreforgePlugin) {
        categories.forEach { it.categoryGui.unregisterCommands() }
    }

    override fun afterReload(plugin: LibreforgePlugin) {
        categories.forEach { it.categoryGui.unregisterCommands() }
        categories.forEach { it.categoryGui.registerCommands() }
        plugin.logger.info(
            "&fLoaded &3${enabledCategories.size} &fcategories with " +
                    "&3${enabledCategories.sumOf { it.achievements.size }} &ftotal achievements"
        )
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(
            AchievementCategory(id, config)
        )
    }

    override fun clear(plugin: LibreforgePlugin) {
        categories.forEach { it.categoryGui.unregisterCommands() }
        registry.clear()
    }
}