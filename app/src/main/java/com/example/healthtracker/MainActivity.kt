package com.example.healthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.healthtracker.data.repository.UserSettingsRepository
import com.example.healthtracker.ui.navigation.HealthTrackerNavGraph
import com.example.healthtracker.ui.theme.HealthTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // 安装 Splash Screen，在主题加载完成前显示
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 设置 Splash Screen 保持显示的条件
        // 当 settings 为 null 时（即还在加载中），保持显示
        var settingsLoaded = false
        splashScreen.setKeepOnScreenCondition { !settingsLoaded }

        setContent {
            val settings by userSettingsRepository.getSettingsFlow().collectAsState(initial = null)
            val systemDarkTheme = isSystemInDarkTheme()

            // 当设置加载完成时，更新标志
            if (settings != null) {
                settingsLoaded = true
            }

            // 计算实际深色模式
            val darkTheme = when (settings?.themeMode) {
                1 -> false  // 浅色
                2 -> true   // 深色
                else -> systemDarkTheme  // 跟随系统
            }

            HealthTrackerTheme(
                darkTheme = darkTheme,
                themeColorIndex = settings?.themeColor ?: 0
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 只有设置加载完成后才显示内容
                    if (settings != null) {
                        HealthTrackerNavGraph()
                    }
                }
            }
        }
    }
}