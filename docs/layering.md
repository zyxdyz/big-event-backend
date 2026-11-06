# Controller / Service / Mapper 分层笔记

> 一句话：**Controller 接请求，Service 按规则办事，Mapper 存取数据。**

## 1. 依赖方向

```text
前端 / 浏览器
     │ HTTP
     ▼
Controller（Web 层）     路由、参数校验、包装 Result
     │ 调用
     ▼
Service（业务层）        业务规则、流程编排、事务
     │ 调用
     ▼
Mapper（持久层）         SQL、动态 SQL、结果映射
     │
     ▼
MySQL
```

原则：**Controller → Service → Mapper**，不要反向依赖。

## 2. 三层职责

| 层 | 负责 | 不应该做 | 本项目例子 |
|---|---|---|---|
| **Controller** | 路由映射、参数绑定/校验、读取请求头、调用 Service、返回 `Result` | 写 SQL、写业务规则、直接操作 Mapper | `UserController.login`、`UserController.updatePwd`、`ArticleController.list` |
| **Service** | 业务规则、流程编排、事务、Redis/JWT 等外部依赖、抛业务异常 | 关心 HTTP 请求/响应、写 SQL | `UserServiceImpl.login`、`UserServiceImpl.updatePwd`、`ArticleServiceImpl.list` |
| **Mapper** | SQL、CRUD、动态 SQL、结果映射 | 判断密码是否正确、判断用户是否存在、权限校验 | `UserMapper.findUserByName`、`ArticleMapper.xml` 的 `list` |

可以理解为：

- **Controller 是前台**：接待请求、检查材料是否齐全，然后把事情转给业务部门；
- **Service 是业务部门**：决定这件事怎么做、按什么规则做；
- **Mapper 是档案室**：只负责按指令存取数据，不判断该不该做。

## 3. 登录请求的完整链路

1. 前端 `POST /user/login`。
2. `WebConfig` 把 `/user/login` 排除在 `LoginInterceptor` 之外，登录不需要 token。
3. `UserController.login`：
   - `@Pattern` 校验用户名/密码格式；
   - 调用 `userService.login(username, password)`；
   - 成功时返回 `Result.success(token)`。
4. `UserServiceImpl.login`：
   - 调 `userMapper.findUserByName` 查用户；
   - 用户不存在 → 抛 `BusinessException("用户名不存在")`；
   - 对比 MD5 密码，不匹配 → 抛 `BusinessException("密码错误")`；
   - 生成 JWT，写入 Redis，返回 token。
5. `UserMapper.findUserByName`：执行 `SELECT * FROM user WHERE username = ?`。
6. `GlobalExceptionHandler`：捕获 `BusinessException`，返回 `Result.error(message)`。

### 出问题时先看哪一层

| 现象 | 优先排查 |
|---|---|
| 参数没收到、格式校验失败 | Controller 的参数绑定/校验 |
| 数据库有用户，但返回“用户名不存在” | Service 的查询逻辑，或 Mapper 的 SQL/字段映射 |
| 密码明明正确，却返回“密码错误” | Service 的 MD5 对比，或数据库里存的哈希 |
| 登录成功，但下一次请求 401 | Service 写 Redis 是否成功、Interceptor 校验 token |
| SQL 报错、字段不存在 | Mapper / XML |

## 4. 文章列表请求的完整链路

`ArticleController.list` 接收 `pageNum`、`pageSize`、`categoryId`、`state`，然后调用 `ArticleService.list`。

`ArticleServiceImpl.list` 做三件事：

1. `PageHelper.startPage(pageNum, pageSize)`：开启分页；
2. 从 `ThreadLocalUtil` 取当前用户 id；
3. 调 `articleMapper.list(userId, categoryId, state)`，再把结果包装成 `PageBean`。

`ArticleMapper.xml` 只负责拼 SQL：

```xml
<where>
    <if test="categoryId!=null">category_id=#{categoryId}</if>
    <if test="state!=null">and state=#{state}</if>
    and create_user=#{userId}
</where>
```

出问题时：

- 分页数量不对 → 看 Service 的 `PageHelper`；
- 分类/状态筛选不对 → 看 Mapper 动态 SQL；
- 用户看到了别人的文章 → 看 Service 取的 `userId`、Interceptor 设置的 ThreadLocal；
- SQL 语法错误 → 看 Mapper XML。

**分页属于业务流程编排，放在 Service；筛选 SQL 属于数据访问，放在 Mapper；参数接收属于 HTTP，放在 Controller。**

## 5. 分层的好处

### 5.1 Debug：缩小排查范围

每一层只做一类事，异常堆栈、日志、断点都能快速指向某一层：

- `NullPointerException` 在 Controller → 参数绑定/请求头问题；
- `BusinessException` 在 Service → 业务规则拦截；
- `SQLException` / MyBatis 异常 → Mapper/XML。

### 5.2 测试：可以隔离依赖

- 测 Service：mock `UserMapper` 和 `StringRedisTemplate`，不需要启动 Spring、连 MySQL/Redis；
- 测 Controller：用 MockMvc + mock Service，不需要数据库；
- 测 Mapper：用 H2/测试库验证 SQL 和动态条件。

`UserServiceImpl.updatePwd(username, oldPwd, newPwd, rePwd)` 现在不依赖 HTTP 请求，传入用户名和三个密码即可测试；`UserMapper` 可以 mock。如果把 `ThreadLocalUtil` 也去掉，测试会更纯粹。

当前项目的 `AppTest` 只是占位测试，分层做好之后，后续才有地方加真正有价值的测试。

### 5.3 维护：修改影响范围小

- 改接口路径、参数名 → 只动 Controller；
- 改密码规则、权限规则 → 只动 Service；
- 改表结构、SQL → 只动 Mapper/XML。

不会出现“改一个字段，Controller、Service、SQL 三处都要跟着改”的情况。

### 5.4 复用：同一业务逻辑可被多个入口调用

Service 不依赖 HTTP。以后如果有定时任务、命令行工具、其他 Controller 也要登录/改密，可以直接调 Service，而不是复制一遍控制器逻辑。

### 5.5 协作：接口就是契约

`UserService`、`ArticleService` 这些接口定义了“能做什么”。一个人改实现，另一个人按接口写 Controller，冲突更少。

### 5.6 事务：Service 是天然的事务边界

如果以后“新增文章”还要同时写标签、写日志，应该把多个 Mapper 调用放在 Service 的一个方法里，加 `@Transactional`。Controller 和 Mapper 都不是合适的事务边界。

### 5.7 安全：规则集中，减少绕过

- 参数校验放在 Controller 边界；
- 权限/归属判断放在 Service，所有入口都会经过；
- SQL 参数绑定放在 Mapper，避免 SQL 注入。

## 6. 本项目还可以改进的分层点

1. **`UserController.register` 仍在做业务判断**

   ```java
   User user = userService.findUserByName(username);
   if (user != null) return Result.error("此用户名已被使用");
   ```

   “用户名是否重复”是业务规则，应该放到 `UserService.register` 里，Controller 只负责调用和返回。

2. **`ArticleServiceImpl` 和 `CategoryServiceImpl` 直接使用 `ThreadLocalUtil`**

   Service 依赖了 Web 请求上下文，单元测试时必须先设置 ThreadLocal。更好的做法是 Controller 取出用户 id 后传给 Service，就像 `updatePwd` 现在这样。

3. **`ArticleServiceImpl.delete` 没有校验文章归属**

   目前直接 `articleMapper.delete(id)`。任何登录用户只要知道文章 id，就能删除别人的文章。这种权限规则应该放在 Service：先查文章，确认 `create_user` 是当前用户，再删除。

4. **Service 实现类里残留 Web 注解 import**

   `ArticleServiceImpl` 里有未使用的 `RequestMapping`，`CategoryServiceImpl` 里有未使用的 `RestController`/`RequestMapping`。虽然不影响运行，但会让层边界变模糊。

5. **事务还没出现，但要有意识**

   现在多数操作是单条 SQL，暂时不需要 `@Transactional`；以后出现“多条写操作必须同时成功/失败”时，事务应该加在 Service 方法上。

## 7. 判断代码该放哪一层

- 看到 `@GetMapping`、`@RequestParam`、`HttpServletRequest`、`Result` → **Controller**
- 看到密码校验、权限判断、多个 Mapper 调用、Redis/JWT、事务 → **Service**
- 看到 `SELECT`、`UPDATE`、表名、字段名、动态 SQL → **Mapper**

如果一段逻辑“即使没有 HTTP 请求也应该成立”，它就不属于 Controller，而属于 Service。

## 8. 自查清单

- [ ] Controller 里还有 SQL 或业务规则吗？
- [ ] Service 里依赖 `HttpServletRequest` / `ThreadLocalUtil` 吗？
- [ ] Mapper 里做业务判断了吗？
- [ ] 删除/修改操作有归属或权限校验吗？
- [ ] 多步写操作有事务吗？
- [ ] Service 能脱离 Web 环境单独测试吗？
- [ ] Controller 的 import 里还有 `Mapper` / `JdbcTemplate` 吗？

---

**核心记忆：每层只因为一个原因变化。**
Controller 因为接口变，Service 因为规则变，Mapper 因为数据访问变。
