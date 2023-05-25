package ru.oftendev.itsmyachievement.achievement

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.gui.slot.SlotBuilder
import com.willfp.eco.core.placeholder.InjectablePlaceholder
import com.willfp.eco.core.placeholder.PlaceholderInjectable
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerStaticPlaceholder
import com.willfp.eco.core.placeholder.StaticPlaceholder
import com.willfp.eco.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.oftendev.itsmyachievement.ItsMyAchievement
import ru.oftendev.itsmyachievement.multilineComponent
import ru.oftendev.itsmyachievement.rewards.Rewards
import ru.oftendev.itsmyachievement.util.ConfiguredItem
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAccessor
import java.util.StringJoiner

class Achievement(config: Config, val category: AchievementCategory): PlaceholderInjectable {
    companion object {
        val completePercentCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(2))
            .build<Int, Double>()
    }

    private val injected = mutableListOf<InjectablePlaceholder>()

    val id = config.getString("id")
    val name = config.getString("name")
    val displayInGui = config.getBool("gui.display")
    private val guiItemLocked = ConfiguredItem(config.getSubsection("gui.locked"))
    private val guiItemUnlocked = ConfiguredItem(config.getSubsection("gui.unlocked"))
    val rewards = config.getStrings("rewards").mapNotNull { Rewards.getById(it) }
    val goals = config.getSubsections("goals")
        .mapIndexedNotNull {
                _, config -> AchievementGoal(this, config, "Achievement: $id, goal: ${config.getString("id")}")
        }
    val saveKey = PersistentDataKey(NamespacedKeyUtils.fromString("itsmyachievement:$id"),
        PersistentDataKeyType.BOOLEAN, false)
    val dateKey = PersistentDataKey(NamespacedKeyUtils.fromString("itsmyachievement:${id}_completed"),
        PersistentDataKeyType.STRING, "")

    init {
        for (goal in goals) {
            addInjectablePlaceholder(
                mutableListOf(
                    PlayerStaticPlaceholder("goal_${goal.id}") {
                        NumberUtils.format(goal.goal)
                    },
                    PlayerStaticPlaceholder("goal_${goal.id}_current") {
                        NumberUtils.format(it.getGoalValue(this, goal))
                    },
                    PlayerStaticPlaceholder("complete_date") {
                        it.getCompleteDateAsString(this)
                    },
                    PlayerStaticPlaceholder("your_complete_percent") {
                        NumberUtils.format(
                            (this.goals.sumOf {
                                    goal -> (it.getGoalValue(this, goal)/goal.goal)*100.0
                            }/((this.goals.size*100.0))*100.0)
                        )
                    },
                    StaticPlaceholder("complete_percent") {
                        NumberUtils.format(
                            completePercentCache.get(1) {
                                val offs = Bukkit.getOfflinePlayers()
                                val total = offs.size.toDouble()
                                val completers = offs.count { it.hasComplete(this) }.toDouble()
                                completers/total
                            }
                        )
                    }
                )
            )
        }
    }

    fun getLockedFor(player: Player): ItemStack {
        return guiItemLocked.getAsItemFor(player, injected)
    }

    fun getUnlockedFor(player: Player): ItemStack {
        return guiItemUnlocked.getAsItemFor(player, injected)
    }

    fun getLockedSlotFor(player: Player): SlotBuilder {
        return guiItemLocked.getAsSlotBuilderFor(player, injected)
    }

    fun getUnlockedSlotFor(player: Player): SlotBuilder {
        return guiItemUnlocked.getAsSlotBuilderFor(player, injected)
    }

    fun getSlotFor(player: Player): SlotBuilder {
        return if (player.hasComplete(this)) {
            getUnlockedSlotFor(player)
        } else getLockedSlotFor(player)
    }

    fun getItemFor(player: Player): ItemStack {
        return if (player.hasComplete(this)) {
            getUnlockedFor(player)
        } else getLockedFor(player)
    }

    /**
     * Clear injected placeholders.
     */
    override fun clearInjectedPlaceholders() {
        injected.clear()
    }

    override fun getPlaceholderInjections(): MutableList<InjectablePlaceholder> {
        return injected
    }

    override fun addInjectablePlaceholder(placeholders: MutableIterable<InjectablePlaceholder>) {
        injected.addAll(placeholders)
    }

    fun getGoalKey(goal: AchievementGoal): PersistentDataKey<Double> {
        return PersistentDataKey(NamespacedKeyUtils.fromString("itsmyachievement:${id}__${goal.id}"),
            PersistentDataKeyType.DOUBLE, 0.0)
    }

    fun isComplete(player: Player): Boolean {
        if (kotlin.runCatching { player.profile.read(this.saveKey) }.getOrElse { false }) return true
        if (this.goals.all { player.hasGoalComplete(this, it) }) {
            player.setHasComplete(this, true)
            rewardPlayer(player)
            return isComplete(player)
        }
        return false
    }

    fun rewardPlayer(player: Player) {
        category.unformattedCompleteMessage
            .forEach {
                player.sendMessage(
                    it.formatEco(player, true).toComponent()
                        .replaceText(
                            TextReplacementConfig.builder()
                                .match("%achievement%")
                                .replacement(
                                    this.getStupidName(player)
                                        .toComponent()
                                        .hoverEvent(
                                            multilineComponent(this
                                                .getItemFor(player).lore() ?: listOf())
                                        )
                                )
                                .build()
                        )
                )
            }
        this.rewards.forEach { it.give(player) }
    }

    fun getFormattedName(player: Player): String {
        return getItemFor(player).itemMeta?.displayName ?: ""
    }

    fun getStupidName(player: Player): String {
        return ChatColor.COLOR_CHAR.toString() +
                (if (player.hasComplete(this)) ChatColor.GREEN.char else ChatColor.RED.char) +
                (ChatColor.stripColor(getFormattedName(player)) ?: "")
    }
}

fun OfflinePlayer.hasComplete(achievement: Achievement): Boolean {
    return kotlin.runCatching { this.profile.read(achievement.saveKey) }.getOrElse { false }
}

fun OfflinePlayer.hasGoalComplete(achievement: Achievement, goal: AchievementGoal): Boolean {
    return this.profile.read(achievement.getGoalKey(goal)) >= goal.goal
}

fun OfflinePlayer.setHasComplete(achievement: Achievement, complete: Boolean) {
    this.profile.write(achievement.saveKey, complete)
    this.profile.write(achievement.dateKey, ItsMyAchievement.instance.dateFormatter.format(
        ItsMyAchievement.instance.currentDate
    ))
}

fun OfflinePlayer.getCompleteDate(achievement: Achievement): TemporalAccessor? {
    return if (this.hasComplete(achievement)) {
        ItsMyAchievement.instance.dateFormatter.parse(
            this.profile.read(achievement.dateKey)
        )
    } else null
}

fun OfflinePlayer.getCompleteDateAsString(achievement: Achievement): String {
    return ItsMyAchievement.instance.dateFormatter.format(this.getCompleteDate(achievement) ?: return "")
}

fun OfflinePlayer.getGoalValue(achievement: Achievement, goal: AchievementGoal): Double {
    return this.profile.read(achievement.getGoalKey(goal))
}

fun Player.setGoalValue(achievement: Achievement, goal: AchievementGoal, value: Double) {
    this.profile.write(achievement.getGoalKey(goal), value)
    achievement.isComplete(this)
}

fun Player.incrementGoal(achievement: Achievement, goal: AchievementGoal, increment: Double) {
    val current = this.getGoalValue(achievement, goal)
    this.setGoalValue(achievement, goal, current+increment)
}

fun Player.resetAchievement(achievement: Achievement) {
    achievement.goals.forEach {
        this.setGoalValue(achievement, it, 0.0)
    }
    this.setHasComplete(achievement, false)
}

fun Player.completeAchievement(achievement: Achievement) {
    achievement.goals.forEach {
        this.setGoalValue(achievement, it, it.goal)
    }
    this.setHasComplete(achievement, true)
}