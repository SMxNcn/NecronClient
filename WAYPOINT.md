# Waypoint GUI使用说明

## 命令
- `/nc create \<fileName>` 创建新的路径点文件：fileName.json
- `/nc wp` 打开Waypoint GUI

## GUI
### List
输入`/nc wp`后会打开Waypoint List界面

标题右侧的文件夹图标可以快速打开路径点存放文件夹

如果列表中存在路径点文件，鼠标悬停在上面会显示一个上箭头，点击即可加载这个路径点文件

加载文件后，列表中对应文件后会出现两个按钮。此时，标题旁会多出一个退格按钮，点击即可卸载（不是删除）当前已加载文件

左侧按钮由于重新加载当前路径点，右侧按钮用于打开[编辑](#EDIT)界面

如果路径点文件较多，可通过页面下方的箭头切换页面

### Edit
加载文件后点击编辑按钮就可以进入编辑模式

| 选项        | 说明                                                                                                                                          | 限制条件      |
|-----------|---------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| Type      | Router(默认):EtherwarpRouter<br>Normal:显示路径点名称<br>Farming:CropNuker                                                                           | 无         |
| XYZ坐标     | 点击输入坐标数据，按回车自动保存                                                                                                                            | 无         |
| Direction | 控制玩家到达路径点后的移动方向                                                                                                                             | 无         |
| Rotation  | 控制玩家到达路径点后的水平旋转方向                                                                                                                           | 无         |
| Island    | 输入岛屿[标识符](https://gitee.com/mixturedg/necron-client/blob/master/src/main/java/cn/boop/necron/utils/LocationUtils.java#L23-L40)  （例：THE_END） | 仅Normal模式 |
| Name      | 请输入文本                                                                                                                                       | 仅Normal模式 |
