package com.example.healthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.healthtracker.data.repository.UserSettingsRepository
import com.example.healthtracker.ui.navigation.HealthTrackerNavGraph
import com.example.healthtracker.ui.theme.HealthTrackerTheme
import com.example.healthtracker.util.SelectedDateManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // 隐藏 ActionBar
        actionBar?.hide()

        super.onCreate(savedInstanceState)

        // 应用启动时重置选中日期为今天
        SelectedDateManager.resetToToday()

        setContent {
            val settings by userSettingsRepository.getSettingsFlow().collectAsState(initial = null)
            val systemDarkTheme = isSystemInDarkTheme()

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
                    HealthTrackerNavGraph()
                }
            }
        }
    }
}