package com.example.listen_to_the_clouds.ui.activity.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listen_to_the_clouds.MainActivity
import com.example.listen_to_the_clouds.databinding.ActivityLoginBinding
import com.example.listen_to_the_clouds.utils.GlobalMessageBus

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewMode by viewModels()
    private var isRegisterMode = false
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        initObserve()
    }

    private fun initView() {
        binding.back.setOnClickListener{finish()}

        // 切换登录/注册模式
        binding.tvRegister.setOnClickListener {
            isRegisterMode = !isRegisterMode

            if (isRegisterMode) {
                // 显示注册字段
                binding.confirmPasswordCard.visibility = View.VISIBLE
                binding.sendEmailCode.visibility = View.VISIBLE
                binding.tvWelcome.text = "欢迎注册"
                binding.loginButton.text = "注册"
                binding.tvRegister.text = "已有账号？登录"
            } else {
                // 隐藏注册字段
                binding.confirmPasswordCard.visibility = View.GONE
                binding.sendEmailCode.visibility = View.GONE
                binding.tvWelcome.text = "欢迎登录"
                binding.loginButton.text = "登录"
                binding.tvRegister.text = "注册账号"
            }
        }

        // 登录/注册按钮
        binding.loginButton.setOnClickListener {
            val mailbox = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (isRegisterMode) {
                val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
                val code = binding.codeEditText.text.toString().trim()
                viewModel.register(mailbox, code, password, confirmPassword)
            } else {
                viewModel.login(mailbox, password)
            }
        }

        // 发送验证码按钮
        binding.btnSendCode.setOnClickListener {
            val mailbox = binding.usernameEditText.text.toString().trim()
            viewModel.sendEmailCode(mailbox)
        }
    }

    private fun initObserve() {
        // 登录成功
        viewModel.logInData.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                GlobalMessageBus.post("登录成功")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // 注册成功 -> 自动登录
        viewModel.registered.observe(this) { isRegistered ->
            if (isRegistered) {
                GlobalMessageBus.post("注册成功，正在自动登录…")
                val mailbox = binding.usernameEditText.text.toString().trim()
                val password = binding.passwordEditText.text.toString().trim()
                viewModel.login(mailbox, password)
            }
        }

        // 验证码发送成功
        viewModel.verificationCode.observe(this) { sent ->
            if (sent) {
                GlobalMessageBus.post("验证码已发送，请查收邮箱")
                startCountDown()
            }
        }
    }

    /**
     * 开始验证码倒计时（60秒）
     */
    private fun startCountDown() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(30000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.btnSendCode.isEnabled = false
                binding.btnSendCode.text = "${seconds}秒后重试"
            }

            override fun onFinish() {
                binding.btnSendCode.isEnabled = true
                binding.btnSendCode.text = "获取验证码"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
