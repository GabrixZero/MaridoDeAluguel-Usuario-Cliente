package com.example.doesitusuario.ui.screens.login

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.doesitusuario.data.repository.UserRepository
import com.example.doesitusuario.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class ForgotStep {
    ENTER_EMAIL,
    VERIFY_CODE,
    NEW_PASSWORD
}

@Composable
fun ForgotPasswordModal(
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var step by remember { mutableStateOf(ForgotStep.ENTER_EMAIL) }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf(List(6) { "" }) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository() }

    // Timer logic for Resend Code
    var timeLeft by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    var codeSentTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(step) {
        if (step == ForgotStep.VERIFY_CODE) {
            timeLeft = 60
            canResend = false
            codeSentTime = System.currentTimeMillis()
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            canResend = true
        }
    }

    // Reset error after 5s
    LaunchedEffect(isError) {
        if (isError) {
            delay(5000)
            isError = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            // Error Banner in Modal
            AnimatedVisibility(
                visible = isError && errorMessage.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
            ) {
                ErrorBanner(message = errorMessage)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (step) {
                                ForgotStep.ENTER_EMAIL -> "Esqueci minha senha"
                                ForgotStep.VERIFY_CODE -> "Esqueci minha senha"
                                ForgotStep.NEW_PASSWORD -> "Nova senha"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(AppColors.PrimaryLight, RoundedCornerShape(8.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = AppColors.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = when (step) {
                            ForgotStep.ENTER_EMAIL -> "Informe o seu e-mail abaixo para receber as instruções de recuperação de senha."
                            ForgotStep.VERIFY_CODE -> "O código foi enviado ao seu e-mail e será válido por 15 minutos."
                            ForgotStep.NEW_PASSWORD -> "Crie uma nova senha de acesso para sua conta DoesIt."
                        },
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    // Steps Content
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                        },
                        label = "StepTransition"
                    ) { currentStep ->
                        Column {
                            when (currentStep) {
                                ForgotStep.ENTER_EMAIL -> {
                                    EmailStep(
                                        email = email,
                                        onEmailChange = { email = it; isError = false },
                                        isError = isError,
                                        isLoading = isLoading,
                                        onConfirm = {
                                            scope.launch {
                                                isLoading = true
                                                // Always proceed to next step to mislead actors
                                                repository.forgotPassword(email)
                                                step = ForgotStep.VERIFY_CODE
                                                isLoading = false
                                            }
                                        }
                                    )
                                }
                                ForgotStep.VERIFY_CODE -> {
                                    CodeStep(
                                        code = code,
                                        onCodeChange = { index, value ->
                                            val newList = code.toMutableList()
                                            newList[index] = value
                                            code = newList
                                            isError = false
                                        },
                                        isError = isError,
                                        isLoading = isLoading,
                                        canResend = canResend,
                                        timeLeft = timeLeft,
                                        onConfirm = {
                                            val inputCode = code.joinToString("")
                                            
                                            // Check 15min expiration
                                            val isExpired = System.currentTimeMillis() - codeSentTime > 15 * 60 * 1000
                                            if (isExpired) {
                                                errorMessage = "Código expirado. Solicite um novo."
                                                isError = true
                                                return@CodeStep
                                            }

                                            scope.launch {
                                                isLoading = true
                                                repository.verifyCode(email, inputCode).fold(
                                                    onSuccess = { step = ForgotStep.NEW_PASSWORD },
                                                    onFailure = {
                                                        errorMessage = it.message ?: "Código incorreto"
                                                        isError = true
                                                    }
                                                )
                                                isLoading = false
                                            }
                                        },
                                        onResend = {
                                            scope.launch {
                                                repository.forgotPassword(email)
                                                timeLeft = 60
                                                canResend = false
                                                codeSentTime = System.currentTimeMillis()
                                                while (timeLeft > 0) {
                                                    delay(1000)
                                                    timeLeft--
                                                }
                                                canResend = true
                                            }
                                        }
                                    )
                                }
                                ForgotStep.NEW_PASSWORD -> {
                                    PasswordStep(
                                        password = newPassword,
                                        confirmPassword = confirmPassword,
                                        onPasswordChange = { newPassword = it; isError = false },
                                        onConfirmPasswordChange = { confirmPassword = it; isError = false },
                                        isError = isError,
                                        isLoading = isLoading,
                                        onConfirm = {
                                            if (newPassword != confirmPassword) {
                                                errorMessage = "As senhas não coincidem"
                                                isError = true
                                            } else if (!isPasswordStrong(newPassword)) {
                                                errorMessage = "Senha muito fraca. Use maiúsculas, minúsculas, números e símbolos."
                                                isError = true
                                            } else if (newPassword.length < 8) {
                                                errorMessage = "A senha deve conter no mínimo 8 dígitos"
                                                isError = true
                                            } else {
                                                scope.launch {
                                                    isLoading = true
                                                    repository.resetPassword(email, code.joinToString(""), newPassword).fold(
                                                        onSuccess = { onSuccess("Senha alterada com sucesso!") },
                                                        onFailure = {
                                                            errorMessage = it.message ?: "Erro ao resetar senha"
                                                            isError = true
                                                        }
                                                    )
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isPasswordStrong(password: String): Boolean {
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }
    return hasUpper && hasLower && hasDigit && hasSymbol
}

@Composable
fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    isError: Boolean,
    isLoading: Boolean,
    onConfirm: () -> Unit
) {
    val buttonEnabled = email.isNotBlank() && !isLoading

    Column {
        DarkTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "E-mail cadastrado",
            placeholder = "joao.silva@email.com",
            isError = isError
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onConfirm,
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
                Text("Confirmar e-mail", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CodeStep(
    code: List<String>,
    onCodeChange: (Int, String) -> Unit,
    isError: Boolean,
    isLoading: Boolean,
    canResend: Boolean,
    timeLeft: Int,
    onConfirm: () -> Unit,
    onResend: () -> Unit
) {
    val buttonEnabled = code.all { it.isNotBlank() } && !isLoading
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            code.forEachIndexed { index, value ->
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        if (it.length <= 1) {
                            onCodeChange(index, it)
                            if (it.isNotBlank()) {
                                if (index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                } else {
                                    // Last digit entered: dismiss keyboard and clear focus
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .width(44.dp) // Maintain fixed width
                        .height(56.dp) // Increase height vertically
                        .focusRequester(focusRequesters[index])
                        .onKeyEvent { keyEvent ->
                            // Corrected Backspace logic: when empty, move to previous
                            if (keyEvent.type == KeyEventType.KeyDown && 
                                keyEvent.key == Key.Backspace) {
                                if (value.isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                    true
                                } else if (value.isNotEmpty()) {
                                    // Let the onValueChange handle clearing the current box
                                    false
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                    shape = RoundedCornerShape(8.dp),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp, // Proportional reduction
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.InputBackground,
                        unfocusedContainerColor = AppColors.InputBackground,
                        focusedBorderColor = if (isError) AppColors.ErrorBanner else AppColors.Primary,
                        unfocusedBorderColor = if (isError) AppColors.ErrorBanner else AppColors.Border,
                        cursorColor = Color.Transparent // No blinking line
                    ),
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onConfirm,
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
                Text("Confirmar código", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onResend,
            enabled = canResend,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canResend) AppColors.PrimaryLight else AppColors.ButtonDisabled,
                contentColor = if (canResend) AppColors.Primary else AppColors.TextButtonDisabled
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = null
        ) {
            val timeFmt = String.format(Locale.getDefault(), "%02d:%02d", timeLeft / 60, timeLeft % 60)
            Text(
                if (canResend) "Reenviar código" else "Reenviar código ($timeFmt)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PasswordStep(
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    isError: Boolean,
    isLoading: Boolean,
    onConfirm: () -> Unit
) {
    val buttonEnabled = password.isNotBlank() && confirmPassword.isNotBlank() && !isLoading

    Column {
        DarkTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Nova senha",
            placeholder = "••••••••",
            isPassword = true,
            isError = isError
        )

        Spacer(Modifier.height(24.dp))

        DarkTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirmar senha",
            placeholder = "••••••••",
            isPassword = true,
            isError = isError
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onConfirm,
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
                Text("Confirmar nova senha", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
