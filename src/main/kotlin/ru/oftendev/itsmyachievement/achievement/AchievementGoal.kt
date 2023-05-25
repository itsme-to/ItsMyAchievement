package ru.oftendev.itsmyachievement.achievement

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.counters.Accumulator
import com.willfp.libreforge.counters.Counters
import com.willfp.libreforge.separatorAmbivalent
import org.bukkit.entity.Player
import ru.oftendev.itsmyachievement.ItsMyAchievement

class AchievementGoal(val parent: Achievement, config: Config, context: String): Accumulator {
    val workConfig = config
    val id = workConfig.getString("id")
    init {
        if (!workConfig.has("multiplier")) {
            workConfig.set("multiplier", 1.0)
        }
    }
    val goal = workConfig.getDouble("goal")
    private val counter = Counters.compile(config, ViolationContext(ItsMyAchievement.instance, context))
    private val countValue = workConfig.separatorAmbivalent().getDoubleOrNull("static-value")

    override fun accept(player: Player, count: Double) {
        if (count <= 0.0) return
        player.incrementGoal(parent, this, countValue ?: count)
    }

   fun bind() {
       counter?.bind(this)
   }

   fun unbind() {
       counter?.unbind()
   }
}