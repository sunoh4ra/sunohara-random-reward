package com.sunohara.reward;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 奖励管理器
 */
public class RewardManager {

    private final RandomRewardPlugin plugin;
    private final RewardConfig config;
    private final Map<String, PlayerRewardData> playerRewardData = new HashMap<>();
    private final Random random = new Random();
    private int globalRewardTaskId = -1;

    public RewardManager(RandomRewardPlugin plugin, RewardConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void initialize() {
        if (!config.isEnabled()) {
            plugin.getLogger().info("奖励系统已禁用");
            return;
        }

        RewardMode mode = config.getRewardMode();

        if (mode == RewardMode.GLOBAL) {
            startGlobalRewardTask();
        }

        plugin.getLogger().info("奖励系统启动: " + mode.getDisplayName());
    }

    public void shutdown() {
        if (globalRewardTaskId != -1) {
            Bukkit.getScheduler().cancelTask(globalRewardTaskId);
        }
        playerRewardData.clear();
    }

    private void startGlobalRewardTask() {
        long intervalMinutes = config.getGlobalIntervalMinutes();
        long intervalTicks = intervalMinutes * 60 * 20;

        globalRewardTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin,
                this::distributeGlobalRewards,
                intervalTicks,
                intervalTicks
        );

        plugin.getLogger().info("全局奖励任务已启动: 每 " + intervalMinutes + " 分钟");
    }

    private void distributeGlobalRewards() {
        List<? extends Player> players = Bukkit.getOnlinePlayers().stream().toList();

        if (players.size() < config.getMinPlayers()) {
            return;
        }

        for (Player player : players) {
            giveRandomReward(player, true);
        }

        String message = config.getGlobalRewardMessage();
        Bukkit.broadcastMessage(config.getPrefix() + message);
    }

    public void checkAndRewardPlayer(Player player) {
        if (!config.isEnabled()) {
            return;
        }

        if (config.getRewardMode() != RewardMode.PLAYER) {
            return;
        }

        String playerName = player.getName();
        PlayerRewardData data = playerRewardData.computeIfAbsent(playerName, PlayerRewardData::new);

        long intervalMinutes = config.getPlayerIntervalMinutes();

        if (data.isRewardReady(intervalMinutes)) {
            giveRandomReward(player, false);
            data.setLastRewardTime(System.currentTimeMillis());
            data.resetNotificationState();
        } else if (config.shouldShowProgress()) {
            long remaining = data.getRemainingMinutes(intervalMinutes);
            List<Long> notificationMinutes = config.getNotificationMinutes();
            boolean shouldNotify = notificationMinutes.isEmpty()
                    || (notificationMinutes.contains(remaining) && remaining != data.getLastShownNotificationMinutes());
            if (shouldNotify) {
                player.sendActionBar("§7距离下个奖励还需 " + remaining + " 分钟");
                if (!notificationMinutes.isEmpty()) {
                    data.setLastShownNotificationMinutes(remaining);
                }
            }
        }
    }

    private void giveRandomReward(Player player, boolean isGlobal) {
        RewardItem selectedReward = null;

        if (config.useAllMaterials()) {
            selectedReward = selectRandomMaterial();
        } else {
            List<RewardItem> rewards = config.getRewardItems();
            if (rewards.isEmpty()) {
                plugin.getLogger().warning("没有配置任何奖励物品");
                return;
            }
            selectedReward = selectRandomReward(rewards);
        }

        if (selectedReward != null) {
            ItemStack item = new ItemStack(selectedReward.getMaterial(), selectedReward.getAmount());

            Map<Integer, ItemStack> excess = player.getInventory().addItem(item);

            if (!excess.isEmpty()) {
                excess.values().forEach(excessItem ->
                        player.getWorld().dropItem(player.getLocation(), excessItem)
                );
            }

            if (!isGlobal) {
                String message = config.getPlayerRewardMessage(
                        selectedReward.getAmount(),
                        selectedReward.getDisplayName()
                );
                player.sendMessage(config.getPrefix() + message);
            }

            if (config.shouldPlaySound()) {
                try {
                    Sound sound = Sound.valueOf(config.getRewardSound());
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的声音: " + config.getRewardSound());
                }
            }

            if (config.shouldLogRewards()) {
                plugin.getLogger().info(player.getName() + " 获得了奖励: " + selectedReward);
            }
        }
    }

    private RewardItem selectRandomMaterial() {
        Material[] materials = Material.values();
        List<String> excluded = config.getExcludedMaterials();
        int minAmount = config.getMinAmount();
        int maxAmount = config.getMaxAmount();

        Material selectedMaterial = null;
        int attempts = 0;

        while (selectedMaterial == null && attempts < 100) {
            Material material = materials[random.nextInt(materials.length)];

            if (!excluded.contains(material.name()) && material.isItem()) {
                selectedMaterial = material;
            }
            attempts++;
        }

        if (selectedMaterial == null) {
            return new RewardItem(Material.DIAMOND, minAmount, 1);
        }

        int amount = minAmount + (maxAmount > minAmount ? random.nextInt(maxAmount - minAmount + 1) : 0);
        return new RewardItem(selectedMaterial, Math.max(1, amount), 1);
    }

    private RewardItem selectRandomReward(List<RewardItem> rewards) {
        int totalWeight = rewards.stream().mapToInt(RewardItem::getWeight).sum();
        int selectedWeight = random.nextInt(totalWeight);

        int currentWeight = 0;
        for (RewardItem reward : rewards) {
            currentWeight += reward.getWeight();
            if (selectedWeight < currentWeight) {
                return reward;
            }
        }

        return rewards.isEmpty() ? null : rewards.get(0);
    }

    public PlayerRewardData getPlayerRewardData(String playerName) {
        return playerRewardData.get(playerName);
    }

    public void removePlayerData(String playerName) {
        playerRewardData.remove(playerName);
    }

    public Map<String, PlayerRewardData> getAllPlayerData() {
        return new HashMap<>(playerRewardData);
    }
}
