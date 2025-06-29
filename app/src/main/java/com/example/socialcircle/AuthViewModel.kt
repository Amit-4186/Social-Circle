package com.example.socialcircle

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthenticationViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthResultState>(AuthResultState.Idle)
    val authState: StateFlow<AuthResultState> = _authState

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

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
                        _authState.value = AuthResultState.Error(task.exception?.message)
                    }
                }
        }
    }

    fun checkEmailVerification() {
        auth.currentUser?.reload()?.addOnCompleteListener {
            val verified = auth.currentUser?.isEmailVerified == true
            _authState.value = if (verified) AuthResultState.EmailVerified
            else AuthResultState.EmailNotVerified
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
                        AuthResultState.Error(task.exception?.message)
                    }
                }
        } else {
            _authState.value = AuthResultState.Error("No user to verify")
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (auth.currentUser?.isEmailVerified == true) {
                            _authState.value = AuthResultState.Success("Login Success")
                        } else {
                            _authState.value = AuthResultState.EmailNotVerified
                        }
                    } else {
                        _authState.value = AuthResultState.Error(it.exception?.message)
                    }
                }
        }
    }

    fun startPhoneNumberVerification(phoneNumber: String, activity: Activity) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(phoneAuthCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val phoneAuthCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthResultState.Error(e.message)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                resendToken = token
                _authState.value = AuthResultState.CodeSent
            }
        }

    fun verifyOtpCode(code: String) {
        val credential = storedVerificationId?.let {
            PhoneAuthProvider.getCredential(it, code)
        }
        credential?.let {
            signInWithPhoneCredential(it)
        } ?: run {
            _authState.value = AuthResultState.Error("Invalid verification ID")
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthResultState.Success("Phone Auth Success")
                } else {
                    _authState.value = AuthResultState.Error(task.exception?.message)
                }
            }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    _authState.value =
                        if (it.isSuccessful) AuthResultState.Success("Reset Link Sent")
                        else AuthResultState.Error(it.exception?.message)
                }
        }
    }

    fun changePassword(newPassword: String) {
        val user = auth.currentUser
        user?.let {
            it.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    _authState.value =
                        if (task.isSuccessful) AuthResultState.Success("Password Updated")
                        else AuthResultState.Error(task.exception?.message)
                }
        } ?: run {
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
    object CodeSent : AuthResultState()
}
