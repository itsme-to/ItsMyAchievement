package ru.oftendev.itsmyachievement.config

import com.willfp.eco.core.config.BaseConfig
import com.willfp.eco.core.config.ConfigType
import ru.oftendev.itsmyachievement.ItsMyAchievement

class RewardsYml(plugin: ItsMyAchievement): BaseConfig(
    "rewards",
    plugin,
    false,
    ConfigType.YAML
)
