package com.example.doesitusuario.ui.screens.login

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doesitusuario.R
import com.example.doesitusuario.ui.theme.AppColors
import com.example.doesitusuario.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToCadastro: () -> Unit,
    initialSuccessMessage: String? = null,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val errorFromVM by viewModel.errorMessage.collectAsState()
    val successFromVM by viewModel.successMessage.collectAsState()
    
    var showForgotModal by remember { mutableStateOf(false) }
    
    // UI State for banners
    var showSuccess by remember { mutableStateOf(initialSuccessMessage != null) }
    var successMessage by remember { mutableStateOf(initialSuccessMessage ?: "") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val buttonEnabled = email.isNotBlank() && senha.isNotBlank() && !isLoading

    // Handle Error from ViewModel
    LaunchedEffect(errorFromVM) {
        if (errorFromVM.isNotEmpty()) {
            errorMessage = errorFromVM
            isError = true
            delay(5000)
            isError = false
            viewModel.clearMessages()
        }
    }

    // Handle Success from Initial Message or ViewModel
    LaunchedEffect(successMessage) {
        if (successMessage.isNotEmpty()) {
            showSuccess = true
            delay(5000)
            showSuccess = false
            successMessage = ""
        }
    }

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_doesit),
                        contentDescription = "DoesIt Logo",
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "DoesIt",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        "Seu marido de aluguel para qualquer situação.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Entrar", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.width(60.dp).height(2.dp).background(AppColors.Primary))
                    }
                    Column(
                        Modifier.weight(1f).clickable { onGoToCadastro() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Cadastrar", color = AppColors.TextSecondary, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.width(60.dp).height(2.dp).background(Color.Transparent))
                    }
                }

                Spacer(Modifier.height(32.dp))

                DarkTextField(
                    value = email,
                    onValueChange = { email = it; isError = false },
                    label = "E-mail",
                    placeholder = "joao.silva@email.com",
                    isError = isError,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
                )

                Spacer(Modifier.height(24.dp))

                DarkTextField(
                    value = senha,
                    onValueChange = { senha = it; isError = false },
                    label = "Senha",
                    placeholder = "••••••••",
                    isPassword = true,
                    isError = isError,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
                )

                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        "Esqueci minha senha",
                        color = AppColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showForgotModal = true }
                    )
                }

                Spacer(Modifier.height(48.dp))

                Button(
                    onClick = {
                        viewModel.login(email, senha) {
                            onLoginSuccess()
                        }
                    },
                    enabled = buttonEnabled,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (buttonEnabled) AppColors.Primary else AppColors.ButtonDisabled,
                        contentColor = if (buttonEnabled) Color.White else AppColors.TextButtonDisabled
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Acessar conta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp))
                        }
                    }
                }
                
                Spacer(Modifier.height(56.dp))
            }
        }

        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.padding(top = 40.dp)
        ) {
            ErrorBanner(message = errorMessage)
        }

        AnimatedVisibility(
            visible = showSuccess && successMessage.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.padding(top = 40.dp)
        ) {
            SuccessBanner(message = successMessage)
        }

        if (showForgotModal) {
            ForgotPasswordModal(
                onDismiss = { showForgotModal = false },
                onSuccess = { message ->
                    successMessage = message
                    showForgotModal = false
                }
            )
        }
    }
}
