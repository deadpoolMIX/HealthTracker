package com.example.healthtracker.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.backup.DataBackupManager
import com.example.healthtracker.data.backup.ImportResult
import com.example.healthtracker.data.local.dao.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DataExportState(
    val isExporting: Boolean = false,
    val exportSuccess: Boolean? = null,
    val message: String? = null,
    val recordCounts: RecordCounts? = null
)

data class RecordCounts(
    val intakeCount: Int = 0,
    val bodyCount: Int = 0,
    val sleepCount: Int = 0,
    val foodCount: Int = 0
)

@HiltViewModel
class DataExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foodDao: FoodDao,
    private val intakeRecordDao: IntakeRecordDao,
    private val bodyRecordDao: BodyRecordDao,
    private val sleepRecordDao: SleepRecordDao,
    private val mealPlanDao: MealPlanDao,
    private val mealPlanItemDao: MealPlanItemDao,
    private val userSettingsDao: UserSettingsDao
) : ViewModel() {

    private val _state = MutableStateFlow(DataExportState())
    val state = _state.asStateFlow()

    private lateinit var backupManager: DataBackupManager

    init {
        backupManager = DataBackupManager(
            context = context,
            foodDao = foodDao,
            intakeRecordDao = intakeRecordDao,
            bodyRecordDao = bodyRecordDao,
            sleepRecordDao = sleepRecordDao,
            mealPlanDao = mealPlanDao,
            mealPlanItemDao = mealPlanItemDao,
            userSettingsDao = userSettingsDao
        )
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true, message = null)

            try {
                val success = backupManager.exportToFile(uri)
                if (success) {
                    val data = backupManager.exportData()
                    _state.value = _state.value.copy(
                        isExporting = false,
                        exportSuccess = true,
                        message = "导出成功！",
                        recordCounts = RecordCounts(
                            intakeCount = data.intakeRecords.size,
                            bodyCount = data.bodyRecords.size,
                            sleepCount = data.sleepRecords.size,
                            foodCount = data.customFoods.size
                        )
                    )
                } else {
                    _state.value = _state.value.copy(
                        isExporting = false,
                        exportSuccess = false,
                        message = "导出失败，请重试"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isExporting = false,
                    exportSuccess = false,
                    message = "导出失败: ${e.message}"
                )
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true, message = null)

            when (val result = backupManager.importFromFile(uri)) {
                is ImportResult.Success -> {
                    _state.value = _state.value.copy(
                        isExporting = false,
                        exportSuccess = true,
                        message = "导入成功！",
                        recordCounts = RecordCounts(
                            intakeCount = result.intakeCount,
                            bodyCount = result.bodyCount,
                            sleepCount = result.sleepCount,
                            foodCount = result.foodCount
                        )
                    )
                }
                is ImportResult.Error -> {
                    _state.value = _state.value.copy(
                        isExporting = false,
                        exportSuccess = false,
                        message = result.message
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, exportSuccess = null)
    }
}