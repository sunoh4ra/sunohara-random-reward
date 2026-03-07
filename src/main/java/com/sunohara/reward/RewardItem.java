package com.sunohara.reward;

import org.bukkit.Material;

/**
 * 奖励物品配置类
 */
public class RewardItem {
    private final Material material;
    private final int amount;
    private final int weight;

    public RewardItem(Material material, int amount, int weight) {
        this.material = material;
        this.amount = Math.max(1, Math.min(64, amount));
        this.weight = Math.max(1, weight);
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public int getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return material.toString();
    }

    @Override
    public String toString() {
        return amount + "x " + material.toString();
    }
}
