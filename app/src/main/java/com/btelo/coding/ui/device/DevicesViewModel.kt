package com.btelo.coding.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btelo.coding.domain.model.Device
import com.btelo.coding.domain.repository.DeviceRepository
import com.btelo.coding.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DevicesUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val editingDeviceId: String? = null,
    val editingName: String = ""
)

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    private fun loadDevices() {
        viewModelScope.launch {
            deviceRepository.getAllDevices().collect { devices ->
                _uiState.value = _uiState.value.copy(devices = devices)
            }
        }
    }

    fun startEditName(device: Device) {
        _uiState.value = _uiState.value.copy(
            editingDeviceId = device.id,
            editingName = device.name
        )
    }

    fun updateEditName(name: String) {
        _uiState.value = _uiState.value.copy(editingName = name)
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(editingDeviceId = null, editingName = "")
    }

    fun saveDeviceName() {
        val name = _uiState.value.editingName.trim()
        if (name.isBlank()) {
            cancelEdit()
            return
        }
        // TODO: update device name via repository when implemented
        Logger.i("DevicesVM", "Rename device ${_uiState.value.editingDeviceId} to $name")
        cancelEdit()
    }

    fun removeDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                deviceRepository.removeDevice(deviceId)
                Logger.i("DevicesVM", "Removed device: $deviceId")
            } catch (e: Exception) {
                Logger.e("DevicesVM", "Failed to remove device: ${e.message}")
            }
        }
    }
}
