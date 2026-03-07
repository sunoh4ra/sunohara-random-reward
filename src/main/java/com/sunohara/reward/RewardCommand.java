package com.sunohara.reward;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 奖励命令执行器
 */
public class RewardCommand implements CommandExecutor, TabCompleter {

    private final RandomRewardPlugin plugin;
    private final RewardConfig config;
    private final RewardManager manager;

    public RewardCommand(RandomRewardPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getRewardConfig();
        this.manager = plugin.getRewardManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "reward" -> {
                return handleReward(sender, args);
            }
            case "rhelp" -> {
                return handleHelp(sender);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean handleReward(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e用法: /reward <reload|status>");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "reload" -> {
                return handleReload(sender);
            }
            case "status" -> {
                return handleStatus(sender);
            }
            default -> {
                sender.sendMessage("§c未知的子命令: " + subcommand);
                return true;
            }
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("sunohara.reward.admin")) {
            sender.sendMessage("§c你没有权限！");
            return true;
        }

        try {
            config.reloadConfig();
            sender.sendMessage("§a配置已重新加载！");
            plugin.getLogger().info("配置已重新加载");
        } catch (Exception e) {
            sender.sendMessage("§c配置加载失败: " + e.getMessage());
            plugin.getLogger().warning("配置加载失败: " + e.getMessage());
        }

        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        sender.sendMessage("§6==================== 奖励系统状态 ====================");
        sender.sendMessage("§e状态: " + (config.isEnabled() ? "§a已启用" : "§c已禁用"));
        sender.sendMessage("§e模式: §f" + config.getRewardMode().getDisplayName());

        if (config.getRewardMode() == RewardMode.PLAYER) {
            sender.sendMessage("§e间隔时间: §f" + config.getPlayerIntervalMinutes() + " 分钟");
            sender.sendMessage("§e进度显示: " + (config.shouldShowProgress() ? "§a启用" : "§c禁用"));

            if (sender instanceof Player player) {
                PlayerRewardData data = manager.getPlayerRewardData(player.getName());
                if (data != null) {
                    long remaining = data.getRemainingMinutes(config.getPlayerIntervalMinutes());
                    sender.sendMessage("§e你的奖励距离: §f" + remaining + " 分钟");
                }
            }
        } else {
            sender.sendMessage("§e间隔时间: §f" + config.getGlobalIntervalMinutes() + " 分钟");
            sender.sendMessage("§e最少玩家数: §f" + config.getMinPlayers());
        }

        sender.sendMessage("§e奖励物品数量: §f" + config.getRewardItems().size());
        sender.sendMessage("§e音效: " + (config.shouldPlaySound() ? "§a启用" : "§c禁用"));
        sender.sendMessage("§6====================================================");

        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage("§6==================== 奖励系统帮助 ====================");
        sender.sendMessage("§e命令：");
        sender.sendMessage("  §a/reward reload §f- 重新加载配置文件");
        sender.sendMessage("  §a/reward status §f- 查看系统状态");
        sender.sendMessage("  §a/rhelp §f- 查看此帮助信息");
        sender.sendMessage("");
        sender.sendMessage("§e功能说明：");
        sender.sendMessage("  §f这个插件支持两种奖励模式：");
        sender.sendMessage("  §a• 玩家模式 §f- 玩家游玩一定时间后获得奖励");
        sender.sendMessage("  §a• 全局模式 §f- 服务器每隔一定时间为所有在线玩家生成奖励");
        sender.sendMessage("");
        sender.sendMessage("§e当前模式: §f" + plugin.getRewardConfig().getRewardMode().getDisplayName());
        sender.sendMessage("§6====================================================");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("reward")) {
            return List.of();
        }
        if (args.length == 1 && sender.hasPermission("sunohara.reward.admin")) {
            return filterCompletions(Arrays.asList("reload", "status"), args[0]);
        }
        return List.of();
    }

    private List<String> filterCompletions(List<String> options, String prefix) {
        if (prefix.isEmpty()) return options;
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
