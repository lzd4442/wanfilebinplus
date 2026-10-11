# wanfilebin plus

> 万物皆可 .bin —— 丢什么文件都能转换成 `.bin` 格式

Android 文件转换工具，支持快速原样转换与 XOR 加密转换两种模式，采用 MVVM + Hilt + Room + Jetpack Compose (Material Design 3) 架构。

---

## ✨ 功能特性

- **快速转换（模式 A）**：原样提取字节流，无损输出 `.bin`
- **加密转换（模式 B）**：XOR 编码，安全隐藏文件内容，支持自定义密钥
- **双向还原**：`.bin` → 原文件，对称运算一键还原
- **批量转换**：一次选择多个文件，逐个转换
- **流式处理**：64KB 缓冲，支持超大文件不爆内存
- **转换历史**：Room 持久化记录，随时找回
- **Material You**：Android 12+ 跟随壁纸动态取色
- **暗色模式**：自动适配系统主题

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  HomeScreen · HistoryScreen · Settings  │
├─────────────────────────────────────────┤
│         ViewModel Layer (Hilt)          │
│     HomeViewModel · HistoryViewModel     │
├─────────────────────────────────────────┤
│         Repository Layer                │
│         ConvertRepository               │
├─────────────────────────────────────────┤
│          Data Layer                     │
│  Room (DAO) · BinConverter (Engine)     │
└─────────────────────────────────────────┘
```

### 核心技术栈

| 组件 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| 架构 | MVVM |
| 依赖注入 | Hilt |
| 本地数据库 | Room |
| 异步 | Coroutines + Flow |
| 文件选择 | Activity Result API |
| 后台任务 | WorkManager |

---

## 📁 项目结构

```
app/src/main/java/com/wanfile/binplus/
├── MainActivity.kt              # 入口 + 底部导航
├── WanFileBinPlusApp.kt         # Hilt Application
├── data/
│   ├── model/
│   │   └── ConvertRecord.kt     # 数据实体 + TypeConverters
│   ├── local/
│   │   ├── AppDatabase.kt       # Room Database
│   │   ├── ConvertRecordDao.kt  # DAO
│   │   └── ConvertRepository.kt # Repository
│   └── converter/
│       └── BinConverter.kt      # 转换引擎核心
├── di/
│   └── AppModule.kt             # Hilt DI 模块
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # MD3 配色
│   │   └── Theme.kt             # MaterialTheme
│   ├── home/
│   │   ├── HomeScreen.kt        # 首页 UI
│   │   └── HomeViewModel.kt     # 首页逻辑
│   ├── history/
│   │   ├── HistoryScreen.kt     # 历史页 UI
│   │   └── HistoryViewModel.kt  # 历史页逻辑
│   └── settings/
│       └── SettingsScreen.kt    # 设置页
└── util/
```

---

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog | 2023.1.1+
- JDK 17
- Android SDK 34
- 最低支持 Android 7.0 (API 24)

### 构建步骤

1. 克隆项目
2. 用 Android Studio 打开 `wanfilebinplus` 目录
3. 等待 Gradle 同步完成
4. 连接设备或启动模拟器
5. 点击 Run ▶️

### 手动构建

```bash
# 构建 Debug APK
./gradlew :app:assembleDebug

# 构建 Release APK（需配置签名）
./gradlew :app:assembleRelease
```

APK 输出路径：`app/build/outputs/apk/`

---

## 💡 核心逻辑说明

### 转换引擎（BinConverter.kt）

采用 **流式读写**，逐块处理文件字节，避免大文件一次性读入内存：

- **模式 A（RAW）**：直接字节复制，原样输出
- **模式 B（ENCRYPT）**：逐字节 XOR 编码，对称算法，再跑一遍即还原

```kotlin
// XOR 加密核心（同时也是解密核心）
val chunk = ByteArray(read) { i ->
    (buffer[i].toInt() xor key.toInt()).toByte()
}
```

### 密钥说明

- 默认值：`90`（0x5A）
- 范围：0 ~ 255
- 加密与还原必须使用**同一密钥**

---

## 📄 开源协议

MIT License
