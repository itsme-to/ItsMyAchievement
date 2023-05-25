package ru.oftendev.itsmyachievement.gui

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.gui.GUIComponent
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.menu.MenuLayer
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import org.bukkit.entity.Player
import ru.oftendev.itsmyachievement.ItsMyAchievement
import ru.oftendev.itsmyachievement.achievement.AchievementCategory
import ru.oftendev.itsmyachievement.commands.dynamic.DynamicCategoryCommand
import ru.oftendev.itsmyachievement.gui.component.GuiCloser
import kotlin.math.ceil

class CategoryGui(val category: AchievementCategory,
                  val settings: Config) {
    val commands = mutableListOf<DynamicCategoryCommand>()

    init {
        settings.getStrings("commands").map { it.replace(" ", "") }
            .forEach {
                commands.add(DynamicCategoryCommand(ItsMyAchievement.instance, it, category.id))
            }
    }

    fun createGuiFor(player: Player): Menu {
        val pattern = settings.getStrings("mask.pattern")
        return menu(pattern.size) {
            title = settings.getFormattedString("title")
            setMask(
                FillerMask(
                    MaskItems.fromItemNames(settings.getStrings("mask.items")),
                    *pattern.toTypedArray()
                )
            )

            val pane = ItemsScrollPane(settings, category)

            addComponent(
                settings.getInt("buttons.scroll-pane.row"),
                settings.getInt("buttons.scroll-pane.column"),
                pane
            )

            for (direction in PageChanger.Direction.values()) {
                val directionName = direction.name.lowercase()

                addComponent(
                    MenuLayer.TOP,
                    settings.getInt("buttons.page-change.$directionName.row"),
                    settings.getInt("buttons.page-change.$directionName.column"),
                    PageChanger(
                        Items.lookup(settings.getString("buttons.page-change.$directionName.item")).item,
                        direction
                    )
                )
            }

            addComponent(
                settings.getInt("buttons.close.row"),
                settings.getInt("buttons.close.column"),
                GuiCloser(
                    Items.lookup(settings.getString("buttons.close.item")).item
                )
            )

            maxPages {
                val items = category.achievements.filter { it.displayInGui }
                val total = items.size
                val perPage = pane.rows * pane.columns

                val pages = if (total == 0) {
                    0
                } else {
                    ceil(total.toDouble() / perPage).toInt()
                }
                pages
            }

            for (config in settings.getSubsections("buttons.custom-slots")) {
                setSlot(
                    config.getInt("row"),
                    config.getInt("column"),
                    ConfigSlot(config)
                )
            }
        }
    }

    class ItemsScrollPane(private val config: Config,
                          private val category: AchievementCategory): GUIComponent {
        override fun getRows() = config.getInt("buttons.scroll-pane.height")
        override fun getColumns() = config.getInt("buttons.scroll-pane.width")

        private val defaultSlot = slot(Items.lookup(config.getString("scroll-pane.empty-item")))

        override fun getSlotAt(row: Int, column: Int, player: Player, menu: Menu): Slot {
            val index = column + ((row - 1) * columns) - 1
            val page = menu.getPage(player)

            val items = category.achievements.filter { it.displayInGui }
            return items.getOrNull(index + ((page - 1) * (rows * columns)))
                ?.getSlotFor(player)?.build() ?: defaultSlot
        }
    }

    fun registerCommands() {
        commands.forEach { it.register() }
    }

    fun unregisterCommands() {
        commands.forEach { it.unregister() }
    }
}