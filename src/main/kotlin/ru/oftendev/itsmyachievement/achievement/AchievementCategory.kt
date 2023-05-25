package ru.oftendev.itsmyachievement.achievement

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.registry.Registrable
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.toNiceString
import ru.oftendev.itsmyachievement.ItsMyAchievement
import ru.oftendev.itsmyachievement.gui.CategoryGui

class AchievementCategory(val id: String,
                          config: Config): Registrable {
    val enabled = config.getBool("enabled")
    val name = config.getFormattedString("name")
    val achievements = config.getSubsections("achievements").mapNotNull { Achievement(it, this) }
    val categoryGui = CategoryGui(this, config.getSubsection("gui"))
    val unformattedCompleteMessage = config.getStrings("achievement-completed")

    init {
        PlayerPlaceholder(
            ItsMyAchievement.instance,
            "${id}_completed"
        ) {
            achievements.filter { ach -> it.hasComplete(ach) }.size.toNiceString()
        }.register()

        PlayerlessPlaceholder(
            ItsMyAchievement.instance,
            "${id}_total"
        ) {
            achievements.size.toNiceString()
        }.register()

        PlayerPlaceholder(
            ItsMyAchievement.instance,
            "${id}_complete"
        ) {
            achievements.count { ach -> it.hasComplete(ach) }.toNiceString()
        }.register()

        PlayerPlaceholder(
            ItsMyAchievement.instance,
            "${id}_remaining"
        ) {
            achievements.count { ach -> !it.hasComplete(ach) }.toNiceString()
        }.register()

        achievements.forEach { ach ->
            PlayerPlaceholder(
                ItsMyAchievement.instance,
                "${id}_${ach.id}_completed"
            ) {
                if (it.hasComplete(ach)) "true" else "false"
            }.register()

            PlayerPlaceholder(
                ItsMyAchievement.instance,
                "${id}_${ach.id}_name"
            ) {
                ach.getFormattedName(it)
            }.register()

            PlayerPlaceholder(
                ItsMyAchievement.instance,
                "${id}_${ach.id}_complete_date"
            ) {
                it.getCompleteDateAsString(ach)
            }.register()

            PlayerPlaceholder(
                ItsMyAchievement.instance,
                "${id}_${ach.id}_complete_percent"
            ) {
                NumberUtils.format(
                    (ach.goals.sumOf {
                        goal -> (it.getGoalValue(ach, goal)/goal.goal)*100.0
                    }/((ach.goals.size*100.0))*100.0)
                )
            }.register()

            ach.goals.forEach {
                goal ->
                    PlayerlessPlaceholder(
                        ItsMyAchievement.instance,
                        "${id}_${ach.id}_${goal.id}_goal"
                    ) {
                        goal.goal.toNiceString()
                    }.register()

                    PlayerPlaceholder(
                        ItsMyAchievement.instance,
                        "${id}_${ach.id}_${goal.id}_score"
                    ) {
                        it.getGoalValue(ach, goal).toNiceString()
                    }.register()

                    PlayerPlaceholder(
                        ItsMyAchievement.instance,
                        "${id}_${ach.id}_${goal.id}_complete_percent"
                    ) {
                        NumberUtils.format(
                            (it.getGoalValue(ach, goal)/goal.goal)*100.0
                        )
                    }.register()
            }
        }
    }

    override fun onRegister() {
        this.achievements.forEach { it.goals.forEach { goal -> goal.bind() } }
    }

    override fun onRemove() {
        this.achievements.forEach { it.goals.forEach { goal -> goal.unbind() } }
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