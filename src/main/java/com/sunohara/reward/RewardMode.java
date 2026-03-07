package com.sunohara.reward;

/**
 * 奖励模式枚举
 */
public enum RewardMode {
    PLAYER("玩家游玩模式", "player"),
    GLOBAL("全局模式", "global");

    private final String displayName;
    private final String configName;

    RewardMode(String displayName, String configName) {
        this.displayName = displayName;
        this.configName = configName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getConfigName() {
        return configName;
    }

    /**
     * 从配置名称获取模式
     */
    public static RewardMode fromConfigName(String name) {
        for (RewardMode mode : values()) {
            if (mode.configName.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return PLAYER;
    }
}
