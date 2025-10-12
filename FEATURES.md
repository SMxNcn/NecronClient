# 功能使用说明

Mod配置界面可在OneConfig中的 "3rd Party" 分类中找到。

 主命令: `/necron`，命令别名: `/nc`

 调试命令: `/ncdebug`，命令别名: `/ncd`

## Auto Path
使用A*算法的寻路系统 **(WIP)**

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
- 带有错误检测和断点续传。

## Fake Wipe
开启后，每次进入Hypixel服务器会显示SkyBlock Wipe Book。（别真被擦了.jpg）

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
- HUD使用OneConfig实现
- 目前实现尚不完善，无法重置非稀有物品的RNG。（LootProtector.java & LootEventHandler.java）

## Title Manager
- 可在"Title Text"中输入你想要的文字，并开启"Use your title text"选项以显示自定义标题。
- 自定义图标选项默认开启。如果不想使用自定义图标，需关闭Icon选项并重启游戏。

## Voidgloom
- 尚未完成

## Waypoint

> Waypoint文件存放路径: `./config/necron/waypoints/` （也可在配置界面中快捷打开）
> 
> 按住左Alt键时，右键单击一个方块则会在该方块处添加路径点，左键单击路径点所在方块可删除该路径点。

### 相关命令：

/nc create \<fileName>
- 在目录下新建一个路径点文件。

/nc wp
- 打开路径点GUI。  **(WIP)**

### 路径点GUI相关：
- 初步实现了路径点加载，快速编辑功能
- TODO: 添加对更多属性的编辑功能
