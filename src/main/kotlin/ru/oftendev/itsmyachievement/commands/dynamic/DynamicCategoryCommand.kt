package ru.oftendev.itsmyachievement.commands.dynamic

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.oftendev.itsmyachievement.achievement.AchievementCategory
import ru.oftendev.itsmyachievement.achievement.Achievements

class DynamicCategoryCommand(plugin: EcoPlugin, command: String, val cat: String)
    : PluginCommand(
        plugin,
        command,
        "itsmyachievement.category.${cat.lowercase()}.$command",
    true
    ) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        val category = Achievements.getById(cat) ?: return
        val player = sender as? Player ?: return
        category.categoryGui.createGuiFor(player).open(player)
    }
}