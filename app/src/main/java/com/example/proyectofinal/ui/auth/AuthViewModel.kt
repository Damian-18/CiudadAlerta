package com.example.proyectofinal.ui.auth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.data.repository.*
import com.example.proyectofinal.model.UserSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
data class AuthUiState(val session: UserSession? = null, val busy: Boolean = false, val message: String? = null)
class AuthViewModel(private val draftRepository: DraftRepository, private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()
    fun clearMessage() { _uiState.update { it.copy(message = null) } }
    private fun authenticate(block: suspend () -> Result<UserSession>) {
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val result = block()
            _uiState.update { it.copy(busy = false, session = result.getOrNull(), message = result.exceptionOrNull()?.message) }
        }
    }
    fun login(email: String, password: String) = authenticate { authRepository.login(email, password) }
    fun register(email: String, password: String, nombre: String) = authenticate { authRepository.register(email, password, nombre) }
    fun loginDemoCiudadano() = login("ciudadano@comunidad.mx", "Demo1234")
    fun loginDemoAdmin() = login("admin@ciudadalerta.mx", "Demo1234")
    fun recovery(email: String, password: String) {
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val result = authRepository.recovery(email, password)
            _uiState.update { it.copy(busy = false, message = result.getOrElse { e -> e.message ?: "No fue posible restablecer" }) }
        }
    }
    fun logout() {
        val session = _uiState.value.session ?: return
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = resultOf { draftRepository.clearUserDrafts(session.email) }
            _uiState.update { if (result.isSuccess) AuthUiState() else it.copy(busy = false, message = result.exceptionOrNull()?.message) }
        }
    }
}
