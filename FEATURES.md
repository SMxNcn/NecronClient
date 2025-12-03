# 功能使用说明

Mod配置界面可在OneConfig中的 "3rd Party" 分类中找到。

 主命令: `/necron`，命令别名: `/nc`

 调试命令: `/ncdebug`，命令别名: `/ncd`

## Auto Path
- 使用A*算法的寻路系统 **(WIP)**
- 使用 AOTV/AOTE 的**普通**传送技能进行寻路

## Auto Wardrobe
- 切换装备后自动关闭Wardrobe界面
- 阻止Unequip装备

## Chat Commands
- 在Hypixel中使用的组队命令。
- Hypixel语言需设置为英文！ `/lang english`

## Crop Nuker
**使用风险自负！！！**
- 带有简易的FailSafe **(WIP)** 检测。
- 农业路径点设置详见Waypoints部分。

## Etherwarp Router
- 左键点击时，自动瞄准并etherwarp至下一个路径点（从#1开始）。
- 循环模式可用于宝石挖掘等重复性任务。
- 带有预瞄准功能，可提高Router的速度。

## Fake Wipe
- 开启后，每次进入Hypixel服务器会显示SkyBlock Wipe Book。（别真被擦了.jpg）
- 开启Trigger GUI后，执行3次 `/ncd ban` 后，可以获得随机天数的封禁。
- `!ban` 聊天命令可以让dev进入limbo并断开连接（其他人可见虚假封禁消息）

## Loading Screen
- 类似于高版本Minecraft的加载界面。
- 配置中可选择黑色/红色背景。（需要重启游戏）
- 配置文件可在 Necron Settings 中快速打开。

## Main Menu
- 可加载自定义背景图，命名格式为 bg1.png，bg2.png……
- 图片存放路径: `./config/necron/backgrounds/` （也可在配置界面中快捷打开）
- 背景图轮换速度及显示时长可在Necron Settings中修改。
- 可通过主界面右上角的按钮与原版主菜单互相切换

## Random RNG
- 由Chat Commands中的 !roll 命令触发。
- 实际概率约为[游戏中](https://wiki.hypixel.net/Catacombs_Floor_VII#BedrockChest__)原概率的10倍。

## Reroll Protector
- 阻止你reroll地牢奖励箱中稀有物品
- 或者Send RNG to party！

## RNG Meter HUD
- 该功能来自[BloomModule](https://github.com/UnclaimedBloom6/BloomModule)
- 现支持地牢/Slayer RNG信息显示
- 地牢分数计算需要验证（TODO: 2025.11.27）

## Scoreboard
- 可控制背景颜色和圆角。
- 服务器IP行可使用自定义文本。

## Smooth Scrolling
- 为游戏中列表添加平滑滚动。 (TODO: 创造物品栏)
- 快捷栏平滑切换。（Smoothness设为0为禁用，10.0最慢）

## Title Manager
- Default Title: 显示为 Spongepowered v0.x.x 或自定义标题
- Custom Title: 可选择显示玩家名，当前位置，神秘提示以及自定义前缀

## Waypoint

详见[Waypoints使用说明](WAYPOINT.md)
