package ru.oftendev.itsmyachievement

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.util.toNiceString
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import net.kyori.adventure.text.Component
import org.bukkit.event.Listener
import ru.oftendev.itsmyachievement.achievement.Achievements
import ru.oftendev.itsmyachievement.achievement.hasComplete
import ru.oftendev.itsmyachievement.commands.MainCommand
import ru.oftendev.itsmyachievement.config.RewardsYml
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ItsMyAchievement: LibreforgePlugin() {
    var rewardConfig: RewardsYml = RewardsYml(this)
    val currentDate: LocalDateTime
        get() = LocalDateTime.now(ZoneId.of(configYml.getString("time-zone")))
    val dateFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(configYml.getString("time-format"))

    init {
        instance = this
    }

    override fun handleEnable() {
        PlayerPlaceholder(
            this,
            "completed"
        ) {
            Achievements.enabledCategories.sumOf { cat -> cat.achievements.count { ach -> it.hasComplete(ach) } }
                .toNiceString()
        }.register()

        PlayerPlaceholder(
            this,
            "remaining"
        ) {
            Achievements.enabledCategories.sumOf { cat -> cat.achievements.count { ach -> !it.hasComplete(ach) } }
                .toNiceString()
        }.register()

        PlayerlessPlaceholder(
            this,
            "total"
        ) {
            Achievements.enabledCategories.sumOf { cat -> cat.achievements.size }
                .toNiceString()
        }.register()
    }

    override fun handleReload() {
        PlayerPlaceholder(
            this,
            "completed"
        ) {
            Achievements.enabledCategories.sumOf { cat -> cat.achievements.count { ach -> it.hasComplete(ach) } }
                .toNiceString()
        }.register()

        PlayerPlaceholder(
            this,
            "remaining"
        ) {
            Achievements.enabledCategories.sumOf { cat -> cat.achievements.count { ach -> !it.hasComplete(ach) } }
                .toNiceString()
        }.register()

        PlayerlessPlaceholder(
            this,
            "total"
        ) {
            Achievements.enabledCategories.sumOf { cat -> cat.achievements.size }
                .toNiceString()
        }.register()
    }

    override fun loadConfigCategories(): List<ConfigCategory> {
        return mutableListOf(
            Achievements
        )
    }


    override fun loadListeners(): MutableList<Listener> {
        return mutableListOf()
    }

    override fun loadPluginCommands(): MutableList<PluginCommand> {
        return mutableListOf(MainCommand(this))
    }

    companion object {
        @JvmStatic
        lateinit var instance: ItsMyAchievement
    }
}

fun multilineComponent(components: List<Component>): Component {
    var result = Component.empty()
    for (i in 0 until components.size-1) {
        result = result.append(components[i])
        result = result.append(Component.newline())
    }
    result = result.append(components.last())
    return result
}