package code.id.poke.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.id.poke.data.local.SessionManager
import code.id.poke.data.local.UserEntity
import code.id.poke.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user


    fun getUserByEmail(email: String) {
        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email)
            _user.value = user
        }
    }

    fun login(email: String, password: String) {
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            userRepository.login(email, password)
                .onSuccess { user ->
                    sessionManager.saveLoginSession(user.email, user.name, user.id)
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Login failed")
                }
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
            userRepository.register(UserEntity(name = name, email = email, password = password))
                .onSuccess {
                    _authState.value = AuthState.Registered
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        sessionManager.clearSession()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    object Registered : AuthState()
    data class Error(val message: String) : AuthState()
}
