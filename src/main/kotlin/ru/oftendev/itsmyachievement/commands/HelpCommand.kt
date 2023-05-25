package ru.oftendev.itsmyachievement.commands

import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender
import ru.oftendev.itsmyachievement.ItsMyAchievement

class HelpCommand(plugin: ItsMyAchievement): Subcommand(
    plugin,
    "help",
    "itsmyachievement.help",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        plugin.langYml.getFormattedStrings("help")
            .forEach {
                sender.sendMessage(it)
            }
    }
}