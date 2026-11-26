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

    fun recovery(email: String) {
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val result = authRepository.recovery(email)
            _uiState.update { it.copy(busy = false, message = result.getOrElse { e -> e.message ?: "No fue posible restablecer" }) }
        }
    }
    
    fun restoreSession() {
        val session = authRepository.getCurrentSession()
        if (session != null) {
            _uiState.update { it.copy(session = session) }
        }
    }

    fun logout() {
        val session = _uiState.value.session ?: return
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            authRepository.logout()
            val result = resultOf { draftRepository.clearUserDrafts(session.email) }
            _uiState.update { if (result.isSuccess) AuthUiState() else it.copy(busy = false, message = result.exceptionOrNull()?.message) }
        }
    }
}
