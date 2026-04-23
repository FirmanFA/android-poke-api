package code.id.poke.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.id.poke.data.local.SessionManager
import code.id.poke.domain.model.User
import code.id.poke.domain.usecase.LoginUseCase
import code.id.poke.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    object Registered : AuthState()
    data class Error(val message: String) : AuthState()
}

data class ProfileInfo(val userId: Int, val name: String, val email: String)

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _profileInfo = MutableStateFlow(loadProfileFromSession())
    val profileInfo: StateFlow<ProfileInfo?> = _profileInfo.asStateFlow()

    fun login(email: String, password: String) {
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            loginUseCase(email, password).fold(
                onSuccess = { user ->
                    sessionManager.saveLoginSession(user.email, user.name, user.id)
                    _profileInfo.value = ProfileInfo(user.id, user.name, user.email)
                    _authState.value = AuthState.Success(user)
                },
                onFailure = {
                    _authState.value = AuthState.Error(it.message ?: "Login failed")
                }
            )
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank()) {
            _authState.value = AuthState.Error("Name cannot be empty")
            return
        }
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            registerUseCase(name, email, password).fold(
                onSuccess = { _authState.value = AuthState.Registered },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _profileInfo.value = null
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun loadProfileFromSession(): ProfileInfo? =
        if (sessionManager.isLoggedIn()) {
            ProfileInfo(
                userId = sessionManager.getUserId(),
                name = sessionManager.getUserName() ?: "",
                email = sessionManager.getUserEmail() ?: ""
            )
        } else null
}
