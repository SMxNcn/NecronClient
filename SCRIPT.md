# Necron 脚本语法使用文档

## 基本语法结构
脚本文件遵循特定的语法结构，必须以 `*Start` 开始，以 `*End` 结束。

以#开头的行表示注释

```
*Start([脚本名称][是否启用])
# 脚本内容
*End
```

## 脚本命令

### 脚本级别指令
- -TriggerKey: {KEY_CODE}
```
-TriggerKey:KEY_G
-TriggerKey:60  # 使用LWJGL按键或键码
```
- -ScDelay: {DELAY}
```
-Delay:500  # 毫秒
-Delay:10t  # 游戏刻（10 ticks = 500ms）
```

### 动作指令
动作指令需要包装在 `-Action:[` 和 `]` 块中执行。

格式：
```
-Action:[
# 动作指令列表
]
```

-SendChat: {message}

- 发送聊天消息
```
-Action:[
-SendChat: Hello World!
]
```

-SendCmd: {cmd}

- 发送命令
```
-Action:[
-SendChat: help
]
```

-ClickSlot: {slotId}

- 点击物品栏内指定槽位
```
-Action:[
-ClickSlot: 44
]
```

-UseKey: {KEYCODE}

- 按一次指定按键
```
-Action:[
-UseKey: KEY_V
]
```

-Delay: {DELAY} 

- 动作间延迟（在动作块内使用，控制动作间的间隔）
```
-Action:[
-Delay: 500
]
```

-SendClient: {message}

- 发送聊天栏消息
```
-Action:[
-SendClient: §7Wosh§a?!
]
```

## 脚本示例
```
# 按下G键后，自动切换到衣柜的#9号装备，并自动关闭衣柜

*Start([WardrobeSwitch][true])
-TriggerKey:KEY_G
-ScDelay:400
-Action:[
-SendCmd: wardrobe
-Delay: 400
-ClickSlot: 44
-Delay: 150
-ClickSlot: 49
]
*End
```
