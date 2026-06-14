package com.project.pokekepo.presentation.auth

/**
 * Layar autentikasi — Login dan Register.
 *
 * Alur aplikasi:
 * 1. [LoginScreen]: email + password → AuthViewModel → Couchbase verify → sesi DataStore
 * 2. [RegisterScreen]: buat akun baru → hash password → simpan Couchbase → auto login
 * 3. Sukses → callback onLoginSuccess/onRegisterSuccess → NavHost ke Main
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.pokekepo.ui.theme.PokemonYellow
import org.koin.androidx.compose.koinViewModel

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedLabelColor = Color.Black,
    unfocusedLabelColor = Color.Black,
    cursorColor = Color.Black,
    focusedBorderColor = PokemonYellow,
    unfocusedBorderColor = Color(0xFFBDBDBD),
)

/**
 * Layar Login — titik masuk jika belum ada sesi.
 * Teks input berwarna hitam agar terbaca di background terang.
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successUser) {
        if (uiState.successUser != null) {
            onLoginSuccess()
            viewModel.clearMessages()
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Pokekepo",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Text(
            text = "Masuk untuk melihat daftar Pokemon",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = authTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Kata Sandi", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = authTextFieldColors(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { viewModel.login(email, password) },
            ),
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = PokemonYellow,
                contentColor = Color.Black,
            ),
        ) {
            Text(if (uiState.isLoading) "Memproses…" else "Masuk", color = Color.Black)
        }

        TextButton(
            onClick = onNavigateToRegister,
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Black),
        ) {
            Text("Belum pun akun? Daftar", color = Color.Black)
        }
    }
}

/** Layar registrasi akun baru — validasi konfirmasi password di ViewModel. */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successUser) {
        if (uiState.successUser != null) {
            onRegisterSuccess()
            viewModel.clearMessages()
        }
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Daftar Akun",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = authTextFieldColors(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = authTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Kata Sandi", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = authTextFieldColors(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("Konfirmasi Kata Sandi", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = authTextFieldColors(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Button(
            onClick = { viewModel.register(name, email, password, confirm) },
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = PokemonYellow,
                contentColor = Color.Black,
            ),
        ) {
            Text(if (uiState.isLoading) "Memproses…" else "Daftar", color = Color.Black)
        }

        TextButton(
            onClick = onNavigateToLogin,
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Black),
        ) {
            Text("Sudah pun akun? Masuk", color = Color.Black)
        }
    }
}
