# TokenManager 使用说明

## 全局静态方法

### 1. 检查用户是否登录

```kotlin
// 无需传入 Context，直接调用
if (TokenManager.isUserLoggedIn()) {
    // 用户已登录
    println("用户已登录")
} else {
    // 用户未登录
    println("用户未登录，请先登录")
}
```

### 2. 获取当前用户的 Token

```kotlin
// 无需传入 Context，直接调用
val token = TokenManager.getCurrentToken()
if (token != null) {
    println("当前 Token: $token")
} else {
    println("未获取到 Token")
}
```

### 3. 退出登录

```kotlin
// 无需传入 Context，直接调用
TokenManager.logout()
println("已退出登录")
```

## 原有方法（需要传入 Context）

### 1. 保存 Token

```kotlin
TokenManager.saveToken(context, "your_token_here")
```

### 2. 获取 Token

```kotlin
val token = TokenManager.getToken(context)
```

### 3. 清除 Token

```kotlin
TokenManager.clearToken(context)
```

### 4. 检查是否登录

```kotlin
val isLoggedIn = TokenManager.isLoggedIn(context)
```

## 使用场景示例

### 在 Activity 中使用

```kotlin
class SomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检查登录状态
        if (TokenManager.isUserLoggedIn()) {
            // 已登录，显示用户内容
            loadUserData()
        } else {
            // 未登录，跳转到登录页
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
```

### 在 Fragment 中使用

```kotlin
class MyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.logoutButton.setOnClickListener {
            // 退出登录
            TokenManager.logout()
            // 跳转到登录页
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
}
```

### 在网络请求拦截器中使用

```kotlin
private val authInterceptor = Interceptor { chain ->
    val original = chain.request()
    
    // 使用全局方法获取 Token
    val token = TokenManager.getCurrentToken()
    
    val request = if (!token.isNullOrEmpty()) {
        original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    } else {
        original
    }
    
    chain.proceed(request)
}
```

### 在任何地方判断登录状态

```kotlin
// 在工具类中
object Utils {
    fun doSomething() {
        if (TokenManager.isUserLoggedIn()) {
            // 执行需要登录的操作
        } else {
            // 提示用户登录
        }
    }
}

// 在数据仓库中
class SomeRepository {
    suspend fun getData(): Result<Data> {
        if (!TokenManager.isUserLoggedIn()) {
            return Result.failure(Exception("请先登录"))
        }
        // 执行数据获取
    }
}
```

## 优势

1. **无需传入 Context**：使用全局 Application 实例，任何地方都可以调用
2. **类型安全**：使用 `@JvmStatic` 注解，支持 Java 调用
3. **异常处理**：内置异常捕获，避免崩溃
4. **简洁易用**：API 简单明了，一行代码即可判断登录状态
