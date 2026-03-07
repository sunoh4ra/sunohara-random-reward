package com.sunohara.reward;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 奖励系统配置管理器
 */
public class RewardConfig {

    private final RandomRewardPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public RewardConfig(RandomRewardPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            try {
                plugin.saveResource("config.yml", false);
                plugin.getLogger().info("已创建默认配置文件");
            } catch (Exception e) {
                plugin.getLogger().warning("配置创建失败: " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        mergeWithDefaults();
    }

    private void mergeWithDefaults() {
        try (InputStream defaultStream = plugin.getResource("config.yml")) {
            if (defaultStream == null) return;
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            boolean modified = false;
            for (String key : defaultConfig.getKeys(true)) {
                if (defaultConfig.isConfigurationSection(key)) continue;
                if (!config.contains(key)) {
                    config.set(key, defaultConfig.get(key));
                    modified = true;
                }
            }
            if (modified) {
                config.save(configFile);
                plugin.getLogger().info("配置文件已更新，已添加新版本配置项");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("配置合并失败: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        try {
            config = YamlConfiguration.loadConfiguration(configFile);
            mergeWithDefaults();
        } catch (Exception e) {
            plugin.getLogger().warning("配置加载失败: " + e.getMessage());
        }
    }

    public RewardMode getRewardMode() {
        String mode = config.getString("reward-mode", "player");
        return RewardMode.fromConfigName(mode);
    }

    public long getPlayerIntervalMinutes() {
        return config.getLong("player-reward.interval-minutes", 30);
    }

    public long getPlayerCheckIntervalSeconds() {
        return Math.max(10, config.getLong("player-reward.check-interval-seconds", 30));
    }

    public boolean shouldShowProgress() {
        return config.getBoolean("player-reward.show-progress", true);
    }

    public List<Long> getNotificationMinutes() {
        List<?> list = config.getList("player-reward.notification-minutes");
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Number n) {
                result.add(n.longValue());
            }
        }
        return result;
    }

    public long getGlobalIntervalMinutes() {
        return config.getLong("global-reward.interval-minutes", 20);
    }

    public int getMinPlayers() {
        return config.getInt("global-reward.min-players", 1);
    }

    public boolean useAllMaterials() {
        return config.getBoolean("use-all-materials", false);
    }

    public int getMinAmount() {
        return Math.max(1, config.getInt("all-materials.min-amount", 1));
    }

    public int getMaxAmount() {
        return Math.min(64, config.getInt("all-materials.max-amount", 64));
    }

    public List<String> getExcludedMaterials() {
        return config.getStringList("all-materials.excluded-materials");
    }

    public List<RewardItem> getRewardItems() {
        List<RewardItem> rewards = new ArrayList<>();

        if (config.isList("rewards")) {
            for (int i = 0; i < config.getList("rewards").size(); i++) {
                String key = "rewards." + i;
                String materialName = config.getString(key + ".material", "DIAMOND");
                int amount = config.getInt(key + ".amount", 1);
                int weight = config.getInt(key + ".weight", 10);

                try {
                    Material material = Material.valueOf(materialName.toUpperCase());
                    rewards.add(new RewardItem(material, amount, weight));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的物品类型: " + materialName);
                }
            }
        }

        if (rewards.isEmpty()) {
            rewards.add(new RewardItem(Material.DIAMOND, 1, 10));
            rewards.add(new RewardItem(Material.EMERALD, 5, 15));
            rewards.add(new RewardItem(Material.GOLD_INGOT, 2, 20));
        }

        return rewards;
    }

    public String getPlayerRewardMessage(int amount, String material) {
        return config.getString("messages.player-reward", "§a你获得了奖励! §f获得了 {amount}x {material}")
                .replace("{amount}", String.valueOf(amount))
                .replace("{material}", material);
    }

    public String getGlobalRewardMessage() {
        return config.getString("messages.global-reward", "§6[系统] §a全服玩家获得了奖励!");
    }

    public String getPrefix() {
        return config.getString("messages.prefix", "§6[奖励系统]§r ");
    }

    public boolean isEnabled() {
        return config.getBoolean("features.enabled", true);
    }

    public boolean shouldPlaySound() {
        return config.getBoolean("features.play-sound", true);
    }

    public String getRewardSound() {
        return config.getString("features.reward-sound", "ENTITY_PLAYER_LEVELUP");
    }

    public boolean shouldLogRewards() {
        return config.getBoolean("logging.log-rewards", true);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("配置保存失败: " + e.getMessage());
        }
    }
}
