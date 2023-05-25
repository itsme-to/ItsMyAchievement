package ru.oftendev.itsmyachievement.rewards

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.drops.DropQueue
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.registry.Registrable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Reward(
    val id: String,
    private val commands: List<String>,
    private val playerCommands: List<String>,
    private val items: List<ItemStack>
): Registrable {
    constructor(config: Config) : this(
        config.getString("id"),
        config.getStrings("commands"),
        config.getStrings("player-commands"),
        config.getStrings("items").map { Items.lookup(it).item }
    )

    fun give(player: Player) {
        commands.forEach { command ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", player.name))

        }
        playerCommands.forEach { command ->
            Bukkit.dispatchCommand(player,
                command.replace("%player%", player.name))
        }
        DropQueue(player).addItems(items).forceTelekinesis().push()
    }

    /**
     * Get the ID of the element.
     *
     * @return The ID.
     */
    override fun getID(): String {
        return id
    }
}