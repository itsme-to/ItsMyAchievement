package ru.oftendev.itsmyachievement.util

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.config.ConfigType
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.config.readConfig
import java.io.File

fun fetchRecursiveConfigs(plugin: EcoPlugin, directory: String): Map<String, Config> {
    val files = mutableMapOf<String, Config>()
    val file = File(plugin.dataFolder, directory)
    for (potentialConfig in file.walk()) {
        if (potentialConfig.isFile && potentialConfig.name.endsWith(".yml")
            && !potentialConfig.path.contains("disabled", true)) {
            files[potentialConfig.nameWithoutExtension] = potentialConfig.readConfig(ConfigType.YAML)
        }
    }
    return files
}