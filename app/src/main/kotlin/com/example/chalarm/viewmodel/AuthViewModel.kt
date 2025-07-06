package com.example.chalarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<Boolean?>(null)
    val authState = _authState.asStateFlow()

    fun register(email: String, password: String) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    _authState.value = task.isSuccessful
                    Log.d("AuthViewModel", "Register success: ${task.isSuccessful}, error: ${task.exception?.message}")
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    _authState.value = task.isSuccessful
                    Log.d("AuthViewModel", "Login success: ${task.isSuccessful}, error: ${task.exception?.message}")
                }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = false
    }

    fun checkUserLoggedIn() {
        _authState.value = auth.currentUser != null
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserEmail(): String {
        return auth.currentUser?.email ?: "Unknown user"
    }
}
