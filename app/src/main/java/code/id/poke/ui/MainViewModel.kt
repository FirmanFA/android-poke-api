package code.id.poke.ui

import androidx.lifecycle.ViewModel
import code.id.poke.data.local.SessionManager

class MainViewModel(sessionManager: SessionManager) : ViewModel() {
    val isLoggedIn: Boolean = sessionManager.isLoggedIn()
}
