package com.sunohara.reward;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 随机奖励插件主类
 */
public class RandomRewardPlugin extends JavaPlugin implements Listener {

    private RewardConfig rewardConfig;
    private RewardManager rewardManager;
    private int playerCheckTaskId = -1;

    @Override
    public void onEnable() {
        // 初始化配置管理器
        rewardConfig = new RewardConfig(this);
        rewardConfig.loadConfig();

        // 初始化奖励管理器
        rewardManager = new RewardManager(this, rewardConfig);
        rewardManager.initialize();

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);

        // 注册命令
        RewardCommand commandExecutor = new RewardCommand(this);
        getCommand("reward").setExecutor(commandExecutor);
        getCommand("reward").setTabCompleter(commandExecutor);
        getCommand("rhelp").setExecutor(commandExecutor);

        if (rewardConfig.getRewardMode() == RewardMode.PLAYER) {
            startPlayerCheckTask();
        }

        getLogger().info("================================");
        getLogger().info("  Sunohara Random Reward（随机奖励）");
        getLogger().info("  版本: " + getDescription().getVersion());
        getLogger().info("  模式: " + rewardConfig.getRewardMode().getDisplayName());
        getLogger().info("  已启用");
        getLogger().info("================================");
    }

    @Override
    public void onDisable() {
        if (playerCheckTaskId != -1) {
            Bukkit.getScheduler().cancelTask(playerCheckTaskId);
        }
        rewardManager.shutdown();
        getLogger().info("Sunohara Random Reward 已禁用");
    }

    private void startPlayerCheckTask() {
        long checkIntervalSeconds = rewardConfig.getPlayerCheckIntervalSeconds();
        long checkIntervalTicks = checkIntervalSeconds * 20;

        playerCheckTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this,
                () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        rewardManager.checkAndRewardPlayer(player);
                    }
                },
                checkIntervalTicks,
                checkIntervalTicks
        );
        getLogger().info("玩家奖励检查任务已启动（检查间隔：" + checkIntervalSeconds + "秒）");
    }

    /**
     * 玩家退出事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        rewardManager.removePlayerData(player.getName());
    }

    /**
     * 获取奖励配置
     */
    public RewardConfig getRewardConfig() {
        return rewardConfig;
    }

    /**
     * 获取奖励管理器
     */
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    /**
     * 获取日志前缀
     */
    public String getLogPrefix() {
        return "§6[" + this.getName() + "§6]§r ";
    }
}
