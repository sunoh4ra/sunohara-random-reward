# Sunohara Random Reward

基于 Paper 1.21.11 的 Minecraft 服务器随机奖励插件。

## 功能特性

- 🎁 **两种奖励模式**
  - **玩家模式** - 玩家游玩一定时间后自动获得奖励
  - **全局模式** - 服务器每隔一定时间为所有在线玩家生成奖励
- 🎲 **加权随机奖励** - 支持多种物品，权重可配置
- ⏱️ **时间可设置** - 灵活配置奖励间隔
- 🔊 **音效提示** - 获得奖励时播放声音
- 📊 **进度提示** - 实时显示距离下次奖励的时间
- 📝 **完整日志** - 记录所有奖励信息

## 安装

1. 确保服务器运行 Paper 1.21.11 或以上版本
2. 将编译后的 JAR 文件放入 `plugins/` 文件夹
3. 重启服务器
4. 查看服务器日志确认插件加载成功
5. 编辑 `plugins/SunoharaRandomReward/config.yml` 自定义配置

## 使用

### 命令

#### 查看帮助
```
/rhelp
```
查看插件帮助信息和系统说明

#### 重新加载配置
```
/reward reload
```
重新加载配置文件而无需重启服务器（需要管理员权限）

#### 查看状态
```
/reward status
```
查看系统当前状态、模式和玩家的奖励进度

### 奖励模式说明

#### 玩家模式（player）
- 玩家在服务器中游玩一定时间后，自动获得一个随机奖励
- 时间由 `player-reward.interval-minutes` 配置决定
- 默认30分钟获得一次奖励
- 每个玩家独立计时
- 检查间隔可配置：`player-reward.check-interval-seconds` 控制检查频率
- 建议在20-60秒之间调整，根据服务器玩家数量和配置选择合适的间隔
- ActionBar 通知时间可配置：`notification-minutes: [15,10,5,1]` 表示在还剩 15、10、5、1 分钟时各提示一次；留空则每次检查都显示

**特点：** 激励玩家长期在线、继续游玩

#### 全局模式（global）
- 服务器每隔一定时间为所有在线玩家生成奖励
- 时间由 `global-reward.interval-minutes` 配置决定
- 默认20分钟为全服生成一次奖励
- 需要达到最少在线玩家数才能生成奖励
- 全服广播显示奖励信息

**特点：** 制造期待感、增加服务器的互动性

## 权限系统

| 权限 | 说明 | 默认值 |
|------|------|--------|
| `sunohara.reward.admin` | 管理员权限（reload 等） | op |
| `sunohara.reward.help` | 查看帮助信息 | true |
| `sunohara.reward.*` | 所有奖励权限 | op |

## 配置文件

插件会自动在首次运行时创建 `plugins/SunoharaRandomReward/config.yml`

**完整配置示例：**

```yaml
# 奖励模式选择
reward-mode: "player"  # 或 "global"

# 玩家游玩模式设置
player-reward:
  interval-minutes: 30
  show-progress: true
  # ActionBar 通知时间点（分钟），如 [15,10,5,1] 表示在还剩 15、10、5、1 分钟时各提示一次
  # 留空 [] 则每次检查都显示剩余时间
  notification-minutes: [15, 10, 5, 1]
  # 检查间隔（秒）- 每隔多久检查一次所有玩家
  # 间隔越频繁，响应更快但服务器负担更重；间隔越长，负担更轻但反应时间更长
  # 建议在 20-60 秒之间根据服务器性能调整
  check-interval-seconds: 30

# 全局模式设置
global-reward:
  interval-minutes: 20
  min-players: 1

# 奖励物品配置（支持物品名称和数量权重）
rewards:
  - material: "DIAMOND"
    amount: 1
    weight: 10
  - material: "EMERALD"
    amount: 5
    weight: 15

# 消息设置
messages:
  player-reward: "§a你获得了奖励! §f获得了 {amount}x {material}"
  global-reward: "§6[系统] §a全服玩家获得了奖励!"

# 功能开关
features:
  enabled: true
  play-sound: true
  reward-sound: "ENTITY_PLAYER_LEVELUP"

# 日志设置
logging:
  log-rewards: true
  level: "INFO"
```

## 高级配置

### 奖励物品配置方式

支持两种奖励模式：

#### 模式 1：从配置的物品列表中选择（默认模式）

```yaml
use-all-materials: false

rewards:
  - material: "DIAMOND"
    amount: 1
    weight: 10
  - material: "EMERALD"
    amount: 5
    weight: 15
  - material: "GOLD_INGOT"
    amount: 2
    weight: 20
```

**特点：** 简单易配置，可精确控制奖励物品和概率，权重值决定获取概率

#### 模式 2：从全游戏物品中随机选择

```yaml
use-all-materials: true

all-materials:
  # 奖励物品的数量范围
  min-amount: 1
  max-amount: 64
  
  # 排除的物品列表（这些物品不会被随机选中）
  excluded-materials:
    - "AIR"
    - "VOID_AIR"
    - "CAVE_AIR"
    - "COMMAND_BLOCK"
    - "BARRIER"
    - "LIGHT"
```

**特点：** 完全随机，增加游戏的惊喜感和趣味性，每次获得的物品数量在 min-amount 和 max-amount 之间随机

### 物品参数说明

| 参数 | 说明 | 示例 |
|------|------|------|
| `material` | 物品类型（大写） | `DIAMOND`, `EMERALD`, `GOLD_INGOT` |
| `amount` | 物品数量（1-64） | 1, 5, 10 |
| `weight` | 随机权重 | 10, 15, 20（仅在配置模式1中生效，数字越大越容易获得） |

### 支持的物品类型

所有有效的 Minecraft 物品均可使用，包括但不限于：

`DIAMOND`, `EMERALD`, `GOLD_INGOT`, `IRON_INGOT`, `COPPER_INGOT`, `LAPIS_LAZULI`, 
`REDSTONE`, `COAL`, `STICK`, `OBSIDIAN`, `NETHERITE_INGOT`, `AMETHYST_SHARD`, `GLOWSTONE`、
`HONEY_BLOCK`, `SLIME_BLOCK` 等所有物品

### 声音列表

常用声音：
- `ENTITY_PLAYER_LEVELUP` - 升级音效（默认）
- `BLOCK_NOTE_BLOCK_PLING` - 音符盒声
- `ENTITY_EXPERIENCE_ORB_PICKUP` - 经验球拾取
- `ITEM_PICKUP` - 物品拾取

## 项目结构

```
sunohara-random-reward/
├── src/main/java/com/sunohara/reward/
│   ├── RandomRewardPlugin.java   # 主插件类
│   ├── RewardCommand.java        # 命令处理
│   ├── RewardManager.java        # 奖励管理
│   ├── RewardConfig.java         # 配置管理
│   ├── RewardMode.java           # 奖励模式枚举
│   ├── RewardItem.java           # 奖励物品
│   └── PlayerRewardData.java     # 玩家数据
├── src/main/resources/
│   ├── plugin.yml
│   └── config.yml
├── pom.xml
└── README.md
```

## 项目架构

- **RandomRewardPlugin** - 主插件类，事件监听与初始化
- **RewardCommand** - 命令处理
- **RewardManager** - 奖励逻辑与分配
- **RewardConfig** - 配置加载与保存
- **RewardMode** - 奖励模式枚举（PLAYER、GLOBAL）
- **RewardItem** - 奖励物品配置
- **PlayerRewardData** - 玩家奖励数据

## 构建

```bash
mvn clean package
```

编译后的 JAR 文件将生成在 `target/` 目录下。

## 开发指南

### 扩展开发

可以根据需要添加以下功能：

1. **多重奖励** - 支持更多的物品类型和概率分布
2. **奖励历史** - 记录和查看奖励历史
3. **玩家排行榜** - 显示获得奖励最多的玩家
4. **特殊事件** - 在特殊情况下（如玩家升级）进行奖励
5. **自定义消息** - 支持更灵活的公告消息格式

## 使用场景举例

### 场景 1：鼓励长期游玩

配置玩家模式，30分钟获得一次奖励：

```yaml
reward-mode: "player"
player-reward:
  interval-minutes: 30
  show-progress: true
rewards:
  - material: "DIAMOND"
    amount: 1
    weight: 10
  - material: "EMERALD"
    amount: 3
    weight: 15
```

### 场景 2：制造期待感

配置全局模式，20分钟全服随机奖励：

```yaml
reward-mode: "global"
global-reward:
  interval-minutes: 20
  min-players: 2
rewards:
  - material: "NETHERITE_INGOT"
    amount: 1
    weight: 5
  - material: "DIAMOND"
    amount: 5
    weight: 20
```

### 场景 3：完全随机的宝箱惊喜

使用全游戏物品随机模式，给玩家带来完全的惊喜：

```yaml
reward-mode: "player"
use-all-materials: true
all-materials:
  min-amount: 1
  max-amount: 64
  excluded-materials:
    - "AIR"
    - "VOID_AIR"
    - "CAVE_AIR"
    - "COMMAND_BLOCK"
    - "BARRIER"
```

这种模式下，每台玩家获得的奖励都是完全随机的，可能是珍贵的矿物，也可能是普通的方块，增加游戏的趣味性。

## 更新日志

### v1.0.0
- **新增：** 初始发布
- **新增：** 两种奖励模式（玩家模式、全局模式）
- **新增：** 加权随机奖励选择
- **新增：** 时间和物品可配置
- **新增：** 音效和进度提示
- **新增：** 完整的命令系统和权限管理

## 许可证

MIT License

## 支持

如有问题或建议，欢迎提出 Issue 或 Pull Request。
