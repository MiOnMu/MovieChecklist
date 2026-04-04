package com.project.moviechecklist.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.moviechecklist.data.repository.AuthRepository
import com.project.moviechecklist.util.Resource
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<FirebaseUser?>>(Resource.Success(null))
    val authState: StateFlow<Resource<FirebaseUser?>> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password).collect {
                _authState.value = it
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            authRepository.register(email, password).collect {
                _authState.value = it
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
    
    fun logout() {
        authRepository.logout()
        _authState.value = Resource.Success(null)
    }
    
    fun resetAuthState() {
        _authState.value = Resource.Success(null)
    }
}
