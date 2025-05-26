# 股票收藏功能说明

## 功能概述
该模块实现了股票收藏功能，用户可以收藏自己感兴趣的股票，查看收藏列表，以及取消收藏。系统同时使用Redis和数据库双重存储，保证数据的高效访问和持久化。

## 技术实现
- 数据持久化：MySQL数据库
- 缓存：Redis的ZSet(有序集合)结构，保证收藏顺序
- 自动填充：使用MyBatis-Plus的自动填充功能处理创建时间和更新时间
- 数据同步：应用启动时自动同步，也可手动触发同步

## API接口
1. 添加收藏
   - 请求: `POST /collect`
   - 参数: `{"ts_code": "000001.SZ"}`
   - 返回: `"股票收藏成功"` 或 `"该股票已收藏"`

2. 获取收藏列表
   - 请求: `GET /collect/all`
   - 返回: `["000001.SZ", "600000.SH", ...]`

3. 取消收藏
   - 请求: `DELETE /collect/{ts_code}`
   - 返回: `"取消收藏成功"` 或 `"该股票未收藏"`

4. 判断是否已收藏
   - 请求: `GET /collect/{ts_code}`
   - 返回: `true` 或 `false`

5. 手动同步数据
   - 请求: `POST /collect/sync`
   - 返回: `"同步完成"`

## 数据表结构
```sql
CREATE TABLE `collect` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ts_code` varchar(10) NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 使用说明
1. 首次使用需确保已创建`collect`表
2. 系统启动时会自动同步Redis和数据库数据
3. 如遇数据不一致，可调用同步接口手动同步 