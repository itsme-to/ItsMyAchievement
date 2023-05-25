package ru.oftendev.itsmyachievement.gui.component

import com.willfp.eco.core.gui.GUIComponent
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot.Slot
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GuiCloser(
    private val stack: ItemStack
): GUIComponent {
    /**
     * Get the amount of rows in the component.
     *
     * @return The rows.
     */
    override fun getRows(): Int {
        return 1
    }

    /**
     * Get the amount of columns in the component.
     *
     * @return The columns.
     */
    override fun getColumns(): Int {
        return 1
    }

    override fun getSlotAt(row: Int, column: Int, player: Player, menu: Menu): Slot? {
        return Slot.builder(stack)
            .onLeftClick { event, _, _ -> event.whoClicked.closeInventory() }
            .build()
    }
}