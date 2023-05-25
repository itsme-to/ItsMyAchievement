package ru.oftendev.itsmyachievement.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.toComponent
import com.willfp.eco.util.toNiceString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.TextReplacementConfig
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.util.StringUtil
import ru.oftendev.itsmyachievement.ItsMyAchievement
import ru.oftendev.itsmyachievement.achievement.Achievements
import ru.oftendev.itsmyachievement.achievement.getCompleteDateAsString
import ru.oftendev.itsmyachievement.achievement.hasComplete

class InfoCommand(plugin: ItsMyAchievement): Subcommand(
    plugin,
    "info",
    "itsmyachievement.info",
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

        val player = Bukkit.getPlayer(args.getOrNull(1) ?: "") ?: sender as? Player ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }
        val current = category.achievements.count { player.hasComplete(it) }
        plugin.langYml.getStrings("category-info")
            .map {
                it.replace("%category%", category.name)
                    .replace("%current%", current.toNiceString())
                    .replace("%total%", category.achievements.size.toNiceString())
                    .replace("%player%", player.name)
                    .formatEco(player, true)
            }.forEach {
                if (it.contains("%achievement%", true)) {
                    category.achievements.forEach { ach ->
                        sender.sendMessage(
                            it.toComponent()
                                .replaceText(
                                    TextReplacementConfig.builder()
                                        .match("%achievement%")
                                        .replacement(
                                            ach.getStupidName(player)
                                                .toComponent()
                                                .hoverEvent(
                                                    multilineComponent(ach
                                                        .getItemFor(player).lore() ?: listOf())
                                                )
                                        )
                                        .build()
                                )

                        )
                    }
                } else {
                    sender.sendMessage(it)
                }
            }
    }

    override fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return when(args.size) {
            1 -> StringUtil.copyPartialMatches(args.first(),
                Achievements.enabledCategories.map { it.id },
                mutableListOf()
            )
            2 -> StringUtil.copyPartialMatches(args[1],
                Bukkit.getOnlinePlayers().map { it.name },
                mutableListOf()
            )
            else -> mutableListOf()
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
}