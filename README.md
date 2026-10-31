# Kinora Reforged

Kinora Reforged 是 `Mokels Bossfight: Kinora` 的非官方衍生作品，把原 Minecraft 1.20.1 Forge 版本迁移到 Minecraft 1.21.1 NeoForge。

An unofficial derivative work of *Mokels Bossfight: Kinora*, ported from Minecraft 1.20.1 Forge to Minecraft 1.21.1 NeoForge.

## 原作信息

- 原项目：Mokels Bossfight: Kinora（CurseForge 项目 ID 1007397）
- 原作者：Sir_Mokel
- 原许可：Academic Free License v3.0
- 原页面：https://www.curseforge.com/minecraft/mc-mods/mokels-bossfight-kinora

本项目与原作者无关，不代表原作者背书。

## 修改说明

本项目基于原作发布 JAR 的资源与行为制作，主要修改如下：

- 迁移到 Minecraft 1.21.1、NeoForge 21.1、Java 21，Mod ID 改为 `kinora_reforged`，资源与数据包改为 1.21 格式。
- 原 MCreator 生成的全局延时队列、命令和药水状态逻辑，改为实体自身持久化的状态与计时，保留原技能时间线、范围、粒子、音效与动画。
- 技能伤害改为按 Boss 基础攻击力乘技能倍率计算，并提供 Boss 属性配置（只应用一次，不覆盖后续调整）。
- 修复原版重复伤害、停止音乐作用于全服等问题。
- Boss 战表现重做：横扫粒子换成按原作配色绘制的弧形斩击、冲击环、花瓣与金色闪光，重击前有地面预警圈，重击带镜头震动（客户端配置 `screenShake` 可调或关闭）；原动画加入缓动、预备动作和身体/尾部跟随，判定时刻按动画实际落点微调。
- 新增（非原作）：棠郎古镰左键满蓄力挥出可穿透的横扫剑气；右键六段连斩，有冷却，每段伤害 = 基础伤害 + 玩家攻击力 × 比例（均可在通用配置中调整）。

## 构建

```
./gradlew build
```

依赖：NeoForge 21.1.x、GeckoLib 4.9.2 及以上。

## 许可

本项目以 Academic Free License v3.0 发布，完整文本见 `LICENSE`，原作署名与许可说明见 `ORIGINAL_LICENSE.md`。
