package com.example.doesitusuario.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitusuario.data.repository.UserRepository
import com.example.doesitusuario.ui.theme.AppColors
import kotlinx.coroutines.launch

import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.doesitusuario.ui.screens.login.DarkTextField
import com.example.doesitusuario.ui.screens.login.ErrorBanner
import com.example.doesitusuario.ui.screens.login.SuccessBanner
import androidx.compose.animation.*
import com.example.doesitusuario.data.network.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: (String?) -> Unit,
    onBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToPhone: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository() }
    
    var pushEnabled by remember { mutableStateOf(PreferencesManager.pushEnabled) }
    var emailEnabled by remember { mutableStateOf(PreferencesManager.emailEnabled) }
    var smsEnabled by remember { mutableStateOf(PreferencesManager.smsEnabled) }
    var whatsappEnabled by remember { mutableStateOf(PreferencesManager.whatsappEnabled) }

    var showPasswordModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }
    
    var bannerError by remember { mutableStateOf("") }
    var bannerSuccess by remember { mutableStateOf("") }

    // Timers for banners
    LaunchedEffect(bannerError) {
        if (bannerError.isNotEmpty()) {
            delay(5000)
            bannerError = ""
        }
    }
    LaunchedEffect(bannerSuccess) {
        if (bannerSuccess.isNotEmpty()) {
            delay(5000)
            bannerSuccess = ""
        }
    }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            Column {
                Spacer(Modifier.height(40.dp)) // Safe area for banners
                CenterAlignedTopAppBar(
                    title = { Text("Configurações", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = AppColors.TextPrimary) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = AppColors.Surface) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Início") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Pedidos") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.PersonOutline, null) },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(AppColors.Background),
                contentPadding = PaddingValues(24.dp)
            ) {
                // ── DÚVIDAS ───────────────────────────────────────────────
                item { SettingSectionTitle("DÚVIDAS") }
                item {
                    SettingGroupCard {
                        SettingItem("Central de Ajuda", trailingIcon = Icons.Default.KeyboardArrowRight) { onNavigateToHelp() }
                        HorizontalDivider(color = AppColors.Border)
                        SettingItem("Ligar na Central de Atendimento", trailingIcon = Icons.Default.Phone) { onNavigateToPhone() }
                    }
                }

                // ── TERMOS ────────────────────────────────────────────────
                item { SettingSectionTitle("TERMOS E POLÍTICA") }
                item {
                    SettingGroupCard {
                        SettingItem("Política de privacidade", trailingIcon = Icons.Default.KeyboardArrowRight) { onNavigateToPrivacy() }
                        HorizontalDivider(color = AppColors.Border)
                        SettingItem("Termos de uso", trailingIcon = Icons.Default.KeyboardArrowRight) { onNavigateToTerms() }
                    }
                }

                // ── NOTIFICAÇÕES ──────────────────────────────────────────
                item { SettingSectionTitle("NOTIFICAÇÕES") }
                item {
                    SettingGroupCard {
                        SettingSwitchItem("Notificação Push", pushEnabled) { 
                            pushEnabled = it
                            PreferencesManager.pushEnabled = it
                        }
                        HorizontalDivider(color = AppColors.Border)
                        SettingSwitchItem("Email", emailEnabled) { 
                            emailEnabled = it
                            PreferencesManager.emailEnabled = it
                        }
                        HorizontalDivider(color = AppColors.Border)
                        SettingSwitchItem("SMS", smsEnabled) { 
                            smsEnabled = it
                            PreferencesManager.smsEnabled = it
                        }
                        HorizontalDivider(color = AppColors.Border)
                        SettingSwitchItem("WhatsApp", whatsappEnabled) { 
                            whatsappEnabled = it
                            PreferencesManager.whatsappEnabled = it
                        }
                    }
                }

                // ── MEUS DADOS ────────────────────────────────────────────
                item { SettingSectionTitle("MEUS DADOS") }
                item {
                    SettingGroupCard {
                        SettingItem("Alterar senha de acesso", trailingIcon = Icons.Default.Lock) { showPasswordModal = true }
                        HorizontalDivider(color = AppColors.Border)
                        SettingItem("Sair da conta", trailingIcon = Icons.AutoMirrored.Filled.ExitToApp, textColor = AppColors.TextSecondary) { onLogout(null) }
                        HorizontalDivider(color = AppColors.Border)
                        SettingItem("Excluir Conta DoesIt", trailingIcon = Icons.Default.Delete, textColor = AppColors.Error) { showDeleteModal = true }
                    }
                }
                item { Spacer(Modifier.height(56.dp)) }
            }

            // Error Banner
            AnimatedVisibility(
                visible = bannerError.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.padding(top = 40.dp).zIndex(1f)
            ) {
                ErrorBanner(message = bannerError)
            }

            // Success Banner
            AnimatedVisibility(
                visible = bannerSuccess.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.padding(top = 40.dp).zIndex(1f)
            ) {
                SuccessBanner(message = bannerSuccess)
            }
        }
    }

    if (showPasswordModal) {
        ChangePasswordModal(
            onDismiss = { showPasswordModal = false },
            onSuccess = {
                showPasswordModal = false
                bannerSuccess = "Senha alterada com sucesso!"
            },
            onShowError = { bannerError = it }
        )
    }

    if (showDeleteModal) {
        DeleteAccountModal(
            onDismiss = { showDeleteModal = false },
            onLogoutSuccess = { onLogout("Sua conta foi excluída com sucesso.") }
        )
    }

    // Overlay Banners to ensure they are on top of everything
    Box(Modifier.fillMaxSize()) {
        // Error Banner
        AnimatedVisibility(
            visible = bannerError.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.padding(top = 40.dp).zIndex(100f)
        ) {
            ErrorBanner(message = bannerError)
        }

        // Success Banner
        AnimatedVisibility(
            visible = bannerSuccess.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.padding(top = 40.dp).zIndex(100f)
        ) {
            SuccessBanner(message = bannerSuccess)
        }
    }
}

@Composable
fun ChangePasswordModal(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onShowError: (String) -> Unit
) {
    var senhaAtual by remember { mutableStateOf("") }
    var novaSenha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    var errorSenhaAtual by remember { mutableStateOf(false) }
    var errorNovaSenha by remember { mutableStateOf(false) }
    var errorConfirmarSenha by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository() }

    val isButtonEnabled = senhaAtual.isNotEmpty() && novaSenha.isNotEmpty() && confirmarSenha.isNotEmpty() && !isLoading

    LaunchedEffect(localError) {
        if (localError.isNotEmpty()) {
            delay(5000)
            localError = ""
        }
    }

    fun validatePassword(password: String): Boolean {
        val pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$".toRegex()
        return pattern.matches(password)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) {},
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
                            "Nova senha",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null, tint = AppColors.Primary)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Crie uma nova senha de acesso para sua conta DoesIt.",
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    DarkTextField(
                        value = senhaAtual,
                        onValueChange = { senhaAtual = it; errorSenhaAtual = false },
                        label = "Senha atual",
                        placeholder = "••••••••",
                        isPassword = true,
                        isError = errorSenhaAtual
                    )

                    Spacer(Modifier.height(16.dp))

                    DarkTextField(
                        value = novaSenha,
                        onValueChange = { novaSenha = it; errorNovaSenha = false },
                        label = "Nova senha",
                        placeholder = "••••••••",
                        isPassword = true,
                        isError = errorNovaSenha
                    )

                    Spacer(Modifier.height(16.dp))

                    DarkTextField(
                        value = confirmarSenha,
                        onValueChange = { confirmarSenha = it; errorConfirmarSenha = false },
                        label = "Confirmar senha",
                        placeholder = "••••••••",
                        isPassword = true,
                        isError = errorConfirmarSenha
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            errorSenhaAtual = false
                            errorNovaSenha = false
                            errorConfirmarSenha = false

                            if (!validatePassword(novaSenha)) {
                                errorNovaSenha = true
                                localError = "Senha deve ter 8+ caracteres, com Maiúscula, Minúscula, Número e Símbolo"
                                return@Button
                            }
                            if (novaSenha != confirmarSenha) {
                                errorConfirmarSenha = true
                                localError = "As senhas não coincidem"
                                return@Button
                            }
                            if (novaSenha == senhaAtual) {
                                errorNovaSenha = true
                                localError = "A nova senha não pode ser igual à atual"
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                repository.changePassword(senhaAtual, novaSenha).fold(
                                    onSuccess = { onSuccess() },
                                    onFailure = { 
                                        errorSenhaAtual = true
                                        localError = it.message ?: "Erro ao alterar senha"
                                    }
                                )
                                isLoading = false
                            }
                        },
                        enabled = isButtonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isButtonEnabled) AppColors.Primary else AppColors.ButtonDisabled,
                            contentColor = if (isButtonEnabled) Color.White else AppColors.TextButtonDisabled
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

            // Error Banner Overlay inside Dialog Box
            AnimatedVisibility(
                visible = localError.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp).zIndex(100f)
            ) {
                ErrorBanner(message = localError)
            }
        }
    }
}

@Composable
fun DeleteAccountModal(
    onDismiss: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    var confirmarText by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var isErrorConfirmar by remember { mutableStateOf(false) }
    var isErrorSenha by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    var localError by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository() }

    val isButtonEnabled = confirmarText.isNotEmpty() && senha.isNotEmpty() && !isLoading

    LaunchedEffect(localError) {
        if (localError.isNotEmpty()) {
            delay(5000)
            localError = ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) {},
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
                            "Excluir conta",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.ErrorBanner
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null, tint = AppColors.Primary)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Esta ação é irreversível. Para apagar sua conta, digite \"Confirmar\" e insira sua senha.",
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    DarkTextField(
                        value = confirmarText,
                        onValueChange = { confirmarText = it; isErrorConfirmar = false },
                        label = "Digite \"Confirmar\"",
                        placeholder = "Confirmar",
                        isError = isErrorConfirmar
                    )

                    Spacer(Modifier.height(16.dp))

                    DarkTextField(
                        value = senha,
                        onValueChange = { senha = it; isErrorSenha = false },
                        label = "Sua senha",
                        placeholder = "••••••••",
                        isPassword = true,
                        isError = isErrorSenha
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (!confirmarText.equals("Confirmar", ignoreCase = true)) {
                                isErrorConfirmar = true
                                localError = "Digite 'Confirmar' corretamente"
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                repository.deleteAccount().fold(
                                    onSuccess = { onLogoutSuccess() },
                                    onFailure = { 
                                        isErrorSenha = true
                                        localError = it.message ?: "Erro ao excluir conta"
                                    }
                                )
                                isLoading = false
                            }
                        },
                        enabled = isButtonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isButtonEnabled) AppColors.ErrorBanner else AppColors.ButtonDisabled,
                            contentColor = if (isButtonEnabled) Color.White else AppColors.TextButtonDisabled
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Excluir conta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Error Banner Overlay inside Dialog Box
            AnimatedVisibility(
                visible = localError.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp).zIndex(100f)
            ) {
                ErrorBanner(message = localError)
            }
        }
    }
}

// ── Toggle sol / lua ─────────────────────────────────────────────────

@Composable fun SettingSectionTitle(title: String) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
}

@Composable fun SettingGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(content = content)
    }
}

@Composable fun SettingItem(text: String, textColor: Color = AppColors.TextPrimary,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        if (trailingIcon != null) Icon(trailingIcon, null, tint = AppColors.TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable fun SettingSwitchItem(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AppColors.White,
                checkedTrackColor = AppColors.Primary, uncheckedThumbColor = AppColors.TextSecondary,
                uncheckedTrackColor = AppColors.SurfaceVariant))
    }
}
