package com.example.apptorneosajedrez.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.apptorneosajedrez.MainActivity
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.databinding.ActivityLoginBinding
import com.example.apptorneosajedrez.ui.register.RegisterActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)
    }

    // Google / Credential Manager
    private lateinit var credentialManager: CredentialManager
    private lateinit var googleIdOption: GetGoogleIdOption

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        setupObservers()
        setupListeners()
    }

    private fun setupGoogleSignIn() {
        credentialManager = CredentialManager.create(this)

        googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
    }

    private fun setupObservers() {
        loginViewModel.uiState.observe(this, Observer { state ->
            binding.loading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            state.errorMessage?.let { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }

            state.loggedInUser?.let {
                // Navegar a la pantalla principal
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        })
    }

    private fun setupListeners() = with(binding) {
        btnLogin.setOnClickListener {
            performEmailLogin()
        }

        username.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                password.requestFocus()
                true
            } else {
                false
            }
        }

        password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performEmailLogin()
                true
            } else {
                false
            }
        }

        btnGoogleSignIn.setOnClickListener {
            launchGoogleSignIn()
        }

        registerNow.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }

    private fun performEmailLogin() {
        val email = binding.username.text.toString().trim()
        val password = binding.password.text.toString()

        loginViewModel.loginWithEmail(email, password)
    }

    private fun launchGoogleSignIn() {
        val clientId = getString(R.string.default_web_client_id)
        Log.d("Auth", "=== Iniciando Google Sign-In ===")
        Log.d("Auth", "Client ID: $clientId")
        Log.d("Auth", "Package Name: ${packageName}")

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                Log.d("Auth", "Solicitando credencial...")
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )

                Log.d("Auth", "Credencial obtenida, tipo: ${result.credential.type}")

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    Log.d("Auth", "ID Token obtenido exitosamente")
                    loginViewModel.loginWithGoogle(idToken)
                } else {
                    Log.e("Auth", "Tipo de credencial inesperado: ${credential.type}")
                    Toast.makeText(
                        this@LoginActivity,
                        "Credencial de Google no válida",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (_: GetCredentialCancellationException) {
                Log.d("Auth", "Usuario canceló el login")

            } catch (e: NoCredentialException) {
                Log.e("Auth", "NoCredentialException: ${e.message}", e)
                Toast.makeText(
                    this@LoginActivity,
                    "No hay cuentas de Google disponibles",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: GetCredentialException) {
                Log.e("Auth", "GetCredentialException: ${e::class.java.simpleName}")
                Log.e("Auth", "Mensaje: ${e.message}", e)
                Log.e("Auth", "Causa: ${e.cause?.message}")

                // Mensaje más específico
                val errorMsg = when {
                    e.message?.contains("SIGN_IN_REQUIRED") == true ->
                        "Se requiere autenticación. Verifica tu configuración de OAuth."

                    e.message?.contains("DEVELOPER_ERROR") == true ->
                        "Error de configuración. Verifica SHA-1 y Client ID."

                    else -> "Error: ${e.message}"
                }

                Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                Log.e("Auth", "Error inesperado: ${e::class.java.name}")
                Log.e("Auth", "Mensaje: ${e.message}", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}