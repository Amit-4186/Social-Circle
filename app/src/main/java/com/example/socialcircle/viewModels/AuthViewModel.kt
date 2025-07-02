package com.example.socialcircle.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthResultState>(AuthResultState.Idle)
    val authState: StateFlow<AuthResultState> = _authState

    fun signupWithEmail(email: String, password: String) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    _authState.value = AuthResultState.VerificationEmailSent
                                } else {
                                    _authState.value =
                                        AuthResultState.Error(emailTask.exception?.message)
                                }
                            }
                    } else {
                        val exception = task.exception
                        val message = when (exception) {
                            is FirebaseAuthWeakPasswordException -> "Weak Password"
                            is FirebaseAuthInvalidCredentialsException -> "Invalid Email"
                            is FirebaseAuthUserCollisionException -> "Email already in use. Please Login!"
                            else -> exception?.localizedMessage ?: "Unknown Error"
                        }
                        _authState.value = AuthResultState.Error(message)
                    }
                }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (auth.currentUser?.isEmailVerified == true) {
                            _authState.value = AuthResultState.Success("Login Successful")
                        } else {
                            resendVerificationEmail()
                        }
                    } else {
                        val exception = task.exception
                        val message = when (exception) {
                            is FirebaseAuthInvalidUserException -> "No account found with this email"
                            is FirebaseAuthInvalidCredentialsException -> "Incorrect password"
                            else -> exception?.localizedMessage ?: "Login failed"
                        }
                        _authState.value = AuthResultState.Error(message)
                    }
                }
        }
    }

    fun resendVerificationEmail() {
        val user = auth.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    _authState.value = if (task.isSuccessful) {
                        AuthResultState.VerificationEmailSent
                    } else {
                        val exception = task.exception
                        val message = exception?.localizedMessage ?: "Failed to resend verification email"
                        AuthResultState.Error(message)
                    }
                }
        } else {
            _authState.value = AuthResultState.Error("No user to verify")
        }
    }

    fun checkEmailVerification() {
        auth.currentUser?.reload()?.addOnCompleteListener {
            val verified = auth.currentUser?.isEmailVerified == true
            _authState.value = if (verified) AuthResultState.EmailVerified
            else AuthResultState.EmailNotVerified
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    _authState.value = if (task.isSuccessful) {
                        AuthResultState.Success("Reset Link Sent")
                    } else {
                        val exception = task.exception
                        val message = when (exception) {
                            is FirebaseAuthInvalidUserException -> "No account found with this email"
                            else -> exception?.localizedMessage ?: "Failed to send reset email"
                        }
                        AuthResultState.Error(message)
                    }
                }
        }
    }

    fun changePassword(newPassword: String) {
        val user = auth.currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    _authState.value = if (task.isSuccessful) {
                        AuthResultState.Success("Password Updated")
                    } else {
                        val exception = task.exception
                        val message = when (exception) {
                            is FirebaseAuthRecentLoginRequiredException -> "Please re-authenticate before changing password"
                            else -> exception?.localizedMessage ?: "Failed to update password"
                        }
                        AuthResultState.Error(message)
                    }
                }
        } else {
            _authState.value = AuthResultState.Error("No user logged in")
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthResultState.Success("Logged Out")
    }

}

sealed class AuthResultState {
    object Idle : AuthResultState()
    data class Success(val message: String) : AuthResultState()
    data class Error(val error: String?) : AuthResultState()
    object VerificationEmailSent : AuthResultState()
    object EmailVerified: AuthResultState()
    object EmailNotVerified : AuthResultState()
}
