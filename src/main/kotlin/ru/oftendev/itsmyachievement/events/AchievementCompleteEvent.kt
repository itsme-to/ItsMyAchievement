package ru.oftendev.itsmyachievement.events

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import ru.oftendev.itsmyachievement.achievement.Achievement

class AchievementCompleteEvent(who: Player, val achievement: Achievement): PlayerEvent(who) {
    override fun getHandlers(): HandlerList {
        TODO("Not yet implemented")
    }

}