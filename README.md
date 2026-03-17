# 居民信息管理系统

一个现代化的 Android 应用，用于管理居民信息，支持添加、编辑、删除、查询、统计和导出功能。

## 功能特性

- ✅ 居民信息管理（添加、编辑、删除、查询）
- ✅ 数据统计（按性别、年龄等维度）
- ✅ Excel 数据导出
- ✅ 生物识别登录
- ✅ 现代化 UI（Jetpack Compose）
- ✅ 数据加密存储

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **数据库**: Room
- **依赖注入**: Hilt
- **导航**: Navigation Compose
- **构建工具**: Gradle 8.2.2
- **Kotlin 版本**: 1.9.22

## 快速开始

### 推送到 GitHub

1. 打开 Git CMD
2. 进入项目目录：
   ```bash
   cd c:/Users/Administrator/WorkBuddy/20260312210322
   ```

3. 初始化 Git：
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   ```

4. 添加远程仓库：
   ```bash
   git remote add origin https://github.com/mmdjian/resident-management.git
   ```

5. 推送到 GitHub：
   ```bash
   git push -u origin main
   ```

   注意：推送时需要输入 GitHub 用户名和 Personal Access Token

### 构建 APK

推送成功后，GitHub Actions 会自动构建 APK：

1. 打开 GitHub 仓库页面
2. 点击 **Actions** 标签
3. 等待构建完成（约 5-10 分钟）
4. 构建成功后，可以在 Artifacts 中下载 APK

## 项目结构

```
app/
├── src/main/java/com/resident/app/
│   ├── data/
│   │   ├── database/      # 数据库相关
│   │   ├── entity/        # 数据实体
│   │   ├── repository/    # 数据仓库
│   │   ├── export/        # 导出功能
│   │   └── security/      # 安全认证
│   ├── ui/
│   │   ├── screens/       # 界面
│   │   ├── viewmodel/     # 视图模型
│   │   ├── navigation/    # 导航
│   │   └── theme/         # 主题
│   ├── MainActivity.kt
│   └── ResidentApp.kt
├── build.gradle.kts
└── ...
```

## 应用截图

- 居民列表
- 添加/编辑居民
- 数据统计

## 许可证

MIT License
