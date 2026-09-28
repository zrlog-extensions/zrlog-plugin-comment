# zrlog-plugin-comment

ZrLog 评论插件。提供内置评论框配置，也可切换到畅言评论框；负责评论提交、样式配置、新评论邮件通知开关和畅言回推同步。

## 功能

- 配置内置评论框或畅言评论框
- 配置评论框主色、基础 URL 和自定义 CSS
- 接收站点评论提交
- 可开启新评论邮件通知
- 记录畅言回推同步状态

## 内置评论审核与 AI 建议

- 在配置参数中开启「先审后发」后，新内置评论进入待审队列，人工通过后才公开。默认关闭，保持升级前行为；畅言仍由畅言审核。
- 新增 30 秒提交间隔、10 分钟重复内容拦截、隐藏防机器人字段及长度校验。网站字段可留空。内存限流记录随插件进程重启清空。
- 待审队列通过插件配置协议持久化到 `comment_moderationQueue`，最多保留 100 条未处理记录；满额后提示稍后重试，不丢弃已有评论。
- 开启「AI 审核建议与回复草稿」后，站长可逐条点击 AI 分析，获取正常 / 疑似垃圾 / 需人工判断及理由，编辑和复制回复草稿。AI 不自动发布、删除评论或发送回复。
- 插件通过宿主 HTTP 转发调用 `POST /api/admin/internal/ai/comment/analyze`，只提交评论正文，由 plugin-core 在发往宿主精确路径的 POST 请求上注入 `X-Plugin-Token`，不附加昵称、邮箱、IP 或网站字段。评论正文自身包含的信息仍会参与分析。
- `zrlog-admin-ai` 负责请求校验、提示词、站点 AI 配置、AIService 调用及结构化结果校验；插件不读取 AI 密钥、不直接访问模型供应商。后台与 plugin-core 需同步升级到支持内部 token 调用的版本，并配置文本模型。AI 未配置、超时或返回无效结果时，可以继续人工审核。
- 审核、AI 分析和配置写入要求后台登录及页面令牌；AI 内部接口复用 refreshCache 的 `PluginTokenValidator` 验证 `X-Plugin-Token`，不依赖或转发用户 Cookie。接口不在插件公开路径中。
- 发布前先保存「发布中」状态。若保存或响应中断，记录保留为「发布结果待核对」，不会自动重发。请在站点评论管理中核对后移除待审记录；核对操作不删除已公开的评论。
- 通过后才触发已有的新评论邮件通知，并请求站点缓存刷新。

内部接口请求只包含 `content`，响应数据包含 `verdict`、`reason`、`reply`，由标准 `error` / `data` 响应包装。这是插件与后台之间的内部接口，要求 POST 和有效插件内部 token，不属于公开 API。

## 构建

```shell
export JAVA_HOME=${HOME}/dev/graalvm-jdk-latest
export PATH=${JAVA_HOME}/bin:$PATH
```

## 原生制品发布

Linux amd64/arm64 制品在上传前会调用 `zrlog-artifact-service`，通过与 `plugin-core`
相同的固定版本 `process-artifact` Action 完成压缩和 SHA-256、文件大小校验。
处理成功后才会生成最终制品的 MD5 并上传；处理失败会停止该平台的发布。
服务接收的版本号使用 `bin/build-info.sh` 生成的实际插件版本。

发布前需要配置 Actions Secret `ARTIFACT_SERVICE_TOKEN`，可在仓库中单独设置，
或授权该仓库使用同名组织 Secret。服务地址为 `https://webdav.zrlog.com/artifact`。
