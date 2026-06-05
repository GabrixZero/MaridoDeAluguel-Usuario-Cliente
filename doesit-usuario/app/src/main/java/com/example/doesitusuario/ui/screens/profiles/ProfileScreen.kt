package com.example.doesitusuario.ui.screens.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.doesitusuario.data.network.SessionManager
import com.example.doesitusuario.data.repository.UserRepository
import com.example.doesitusuario.ui.screens.login.DarkTextField
import com.example.doesitusuario.ui.screens.login.ErrorBanner
import com.example.doesitusuario.ui.screens.login.SuccessBanner
import com.example.doesitusuario.ui.theme.AppColors
import com.example.doesitusuario.ui.theme.formatDateBR
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository() }

    var nome by remember { mutableStateOf(SessionManager.userName) }
    var celular by remember { mutableStateOf(SessionManager.userPhone) }
    var email by remember { mutableStateOf(SessionManager.userEmail) }

    var baseNome by remember { mutableStateOf(SessionManager.userName) }
    var baseCelular by remember { mutableStateOf(SessionManager.userPhone) }
    var baseEmail by remember { mutableStateOf(SessionManager.userEmail) }

    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val dataNascimentoExibida = remember(SessionManager.userBirthDate) {
        formatDateBR(SessionManager.userBirthDate).ifBlank { "—" }
    }

    val cpfMascarado = remember(SessionManager.userCpf) {
        maskCpf(SessionManager.userCpf)
    }

    val isPhoneValid = celular.length == 11
    val hasChanges = (nome != baseNome) || (celular != baseCelular) || (email != baseEmail)
    val canSave = hasChanges && nome.isNotBlank() && isPhoneValid && email.isNotBlank() && !isSaving

    LaunchedEffect(Unit) {
        isLoading = true
        repository.getCurrentUser().fold(
            onSuccess = {
                nome = SessionManager.userName
                celular = SessionManager.userPhone
                email = SessionManager.userEmail
                baseNome = SessionManager.userName
                baseCelular = SessionManager.userPhone
                baseEmail = SessionManager.userEmail
                isLoading = false
            },
            onFailure = {
                message = "Erro ao atualizar dados do servidor"
                showError = true
                isLoading = false
            }
        )
    }

    LaunchedEffect(showSuccess, showError) {
        if (showSuccess || showError) {
            delay(5000)
            showSuccess = false
            showError = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = AppColors.Background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Meu Perfil",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AppColors.TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = AppColors.Surface, tonalElevation = 0.dp) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToHome,
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Início") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = AppColors.TextDisabled,
                            unselectedTextColor = AppColors.TextDisabled,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToHistory,
                        icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) },
                        label = { Text("Pedidos") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = AppColors.TextDisabled,
                            unselectedTextColor = AppColors.TextDisabled,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Perfil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.Primary,
                            selectedTextColor = AppColors.Primary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                Modifier.size(120.dp).clip(CircleShape).background(AppColors.SurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person, null,
                                    modifier = Modifier.size(60.dp),
                                    tint = AppColors.TextDisabled
                                )
                            }
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Primary)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Informações Pessoais", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)

                        DarkTextField(
                            value = nome,
                            onValueChange = { nome = it },
                            label = "Nome completo",
                            placeholder = "Digite seu nome"
                        )

                        ReadOnlyField(label = "CPF (Inalterável)", value = cpfMascarado)

                        ReadOnlyField(label = "Data de Nascimento (Inalterável)", value = dataNascimentoExibida)
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Contato", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)

                        DarkTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "E-mail",
                            placeholder = "seu@email.com"
                        )

                        DarkTextField(
                            value = celular,
                            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 11) celular = it },
                            label = "Celular",
                            placeholder = "11912341234",
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            )
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AppColors.Primary)
                        }
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    isSaving = true
                                    message = ""
                                    repository.updateProfile(
                                        mapOf("nome" to nome, "email" to email, "telefone" to celular)
                                    ).fold(
                                        onSuccess = {
                                            message = "Perfil atualizado com sucesso!"
                                            showSuccess = true
                                            isSaving = false
                                            baseNome = nome
                                            baseEmail = email
                                            baseCelular = celular
                                        },
                                        onFailure = {
                                            message = it.message ?: "Erro ao atualizar perfil"
                                            showError = true
                                            isSaving = false
                                        }
                                    )
                                }
                            },
                            enabled = canSave,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canSave) AppColors.Primary else AppColors.ButtonDisabled,
                                contentColor = if (canSave) Color.White else AppColors.TextButtonDisabled
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Salvar Alterações", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(56.dp))
                }
            }
        }

        if (showSuccess) {
            Box(Modifier.padding(top = 40.dp).align(Alignment.TopCenter).zIndex(10f)) {
                SuccessBanner(message)
            }
        }
        if (showError) {
            Box(Modifier.padding(top = 40.dp).align(Alignment.TopCenter).zIndex(10f)) {
                ErrorBanner(message)
            }
        }
    }
}

@Composable
fun ReadOnlyField(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(AppColors.InputBackground, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(value, color = AppColors.TextDisabled, fontSize = 16.sp)
        }
    }
}

private fun maskCpf(cpf: String): String {
    val digits = cpf.filter { it.isDigit() }
    if (digits.length < 11) return cpf
    return "***.${digits.substring(3, 6)}.${digits.substring(6, 9)}-**"
}

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 11) text.text.substring(0, 11) else text.text
        var out = ""
        for (i in trimmed.indices) {
            if (i == 0) out += "("
            out += trimmed[i]
            if (i == 1) out += ")"
            if (i == 6) out += "-"
        }

        val phoneOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset + 1
                if (offset <= 6) return offset + 2
                if (offset <= 11) return offset + 3
                return 14
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return (offset - 1).coerceAtLeast(0)
                if (offset <= 8) return (offset - 2).coerceAtLeast(0)
                if (offset <= 14) return (offset - 3).coerceAtLeast(0)
                return 11
            }
        }

        return TransformedText(AnnotatedString(out), phoneOffsetTranslator)
    }
}