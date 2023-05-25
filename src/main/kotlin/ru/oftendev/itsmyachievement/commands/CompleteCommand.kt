package ru.oftendev.itsmyachievement.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import ru.oftendev.itsmyachievement.ItsMyAchievement
import ru.oftendev.itsmyachievement.achievement.Achievements
import ru.oftendev.itsmyachievement.achievement.completeAchievement
import ru.oftendev.itsmyachievement.achievement.hasComplete
import ru.oftendev.itsmyachievement.achievement.resetAchievement

class CompleteCommand(plugin: ItsMyAchievement): Subcommand(
    plugin,
    "complete",
    "itsmyachievement.complete.achievement",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        val categoryString = args.firstOrNull() ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("requires-category"))
            return
        }
        val category = Achievements.getById(categoryString) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-category"))
            return
        }
        val achievementString = args.getOrNull(1) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("requires-achievement"))
            return
        }
        val achievement = category.achievements
            .firstOrNull { it.id.equals(achievementString, true) } ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-achievement"))
            return
        }
        val playerString = args.getOrNull(2) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("requires-player"))
            return
        }
        val player = Bukkit.getPlayer(playerString) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        player.completeAchievement(achievement)

        sender.sendMessage(plugin.langYml.getFormattedString("messages.prefix") +
                plugin.langYml.getString("achievement-complete")
                    .replace("%achievement%", achievement.getFormattedName(player))
                    .replace("%category%", category.name)
                    .replace("%player%", player.name)
                    .formatEco(player, true)
        )
    }

    override fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return when(args.size) {
            1 -> StringUtil.copyPartialMatches(args.first(),
                Achievements.enabledCategories.map { it.id },
                mutableListOf()
            )
            2 -> {
                val category = Achievements.getById(args.first()) ?: return mutableListOf()
                return StringUtil.copyPartialMatches(args[1],
                    category.achievements.map { it.id },
                    mutableListOf()
                )
            }
            3 -> StringUtil.copyPartialMatches(args[2],
                Bukkit.getOnlinePlayers().map { it.name },
                mutableListOf()
            )
            else -> mutableListOf()
        }
    }
}