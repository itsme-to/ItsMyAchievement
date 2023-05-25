package ru.oftendev.itsmyachievement.util

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.gui.slot.SlotBuilder
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.placeholder.InjectablePlaceholder
import com.willfp.eco.core.placeholder.PlayerStaticPlaceholder
import com.willfp.eco.core.placeholder.StaticPlaceholder
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ConfiguredItem(config: Config) {
    private val item = Items.lookup(config.getString("item"))
    private val name = config.getString("name")
    private val lore: MutableList<String> = config.getStrings("lore")

    fun getAsItemFor(player: Player, injections: MutableList<InjectablePlaceholder> = mutableListOf()): ItemStack {
        var theName = name
        val theLore = mutableListOf(*lore.toTypedArray())
        injections.forEach {
            if (it is StaticPlaceholder) {
                theName = theName.replace("%${it.identifier}%", it.value)
                theLore.replaceAll { str ->
                    str.replace("%${it.identifier}%", it.value)
                }
            } else if (it is PlayerStaticPlaceholder) {
                theName = theName.replace("%${it.identifier}%", it.getValue(player))
                theLore.replaceAll { str ->
                    str.replace("%${it.identifier}%", it.getValue(player))
                }
            }
        }
        return ItemStackBuilder(item)
            .setDisplayName(theName)
            .addLoreLines(theLore)
            .build()
    }

    fun getAsSlotBuilderFor(player: Player,
                            injections: MutableList<InjectablePlaceholder> = mutableListOf()): SlotBuilder {
        return Slot.builder(getAsItemFor(player, injections))
    }
}