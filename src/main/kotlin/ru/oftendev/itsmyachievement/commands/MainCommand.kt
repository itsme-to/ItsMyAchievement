package ru.oftendev.itsmyachievement.commands

import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import ru.oftendev.itsmyachievement.ItsMyAchievement

class MainCommand(plugin: ItsMyAchievement): PluginCommand(
    plugin,
    "itsmyachievement",
    "itsmyachievement.use",
    false
) {
    init {
        this.addSubcommand(HelpCommand(plugin))
            .addSubcommand(InfoCommand(plugin))
            .addSubcommand(ResetCommand(plugin))
            .addSubcommand(ReloadCommand(plugin))
            .addSubcommand(CompleteCommand(plugin))
    }

    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        sender.sendMessage(plugin.langYml.getMessage("invalid-command"))
    }
}