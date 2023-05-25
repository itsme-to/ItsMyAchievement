package ru.oftendev.itsmyachievement.commands

import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender
import ru.oftendev.itsmyachievement.ItsMyAchievement

class ReloadCommand(plugin: ItsMyAchievement): Subcommand(
    plugin,
    "reload",
    "itsmyachievement.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        this.plugin.reload()
        sender.sendMessage(plugin.langYml.getMessage("reloaded"))
    }
}