package com.sunohara.reward;

/**
 * 玩家奖励数据追踪
 */
public class PlayerRewardData {
    private final String playerName;
    private long lastRewardTime;
    private long lastShownNotificationMinutes = -1;

    public PlayerRewardData(String playerName) {
        this.playerName = playerName;
        this.lastRewardTime = System.currentTimeMillis();
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getLastRewardTime() {
        return lastRewardTime;
    }

    public void setLastRewardTime(long time) {
        this.lastRewardTime = time;
    }

    public long getMinutesSinceLastReward() {
        return (System.currentTimeMillis() - lastRewardTime) / 60000;
    }

    public boolean isRewardReady(long intervalMinutes) {
        return getMinutesSinceLastReward() >= intervalMinutes;
    }

    public long getRemainingMinutes(long intervalMinutes) {
        long elapsed = getMinutesSinceLastReward();
        return Math.max(0, intervalMinutes - elapsed);
    }

    public long getLastShownNotificationMinutes() {
        return lastShownNotificationMinutes;
    }

    public void setLastShownNotificationMinutes(long minutes) {
        this.lastShownNotificationMinutes = minutes;
    }

    public void resetNotificationState() {
        this.lastShownNotificationMinutes = -1;
    }
}
