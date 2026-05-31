package com.example.doesitusuario.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doesitusuario.ui.theme.AppColors
import com.example.doesitusuario.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSolicitarServico: () -> Unit,
    onNavigateToPagamentos: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToEnderecos: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val primeiroNome = remember(userName) {
        try {
            if (userName.isNullOrBlank()) "Usuário"
            else userName.trim().split("\\s+".toRegex()).firstOrNull() ?: "Usuário"
        } catch (e: Exception) {
            "Usuário"
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.Surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.Primary,
                        selectedTextColor = AppColors.Primary,
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Pedidos") },
                    label = { Text("Pedidos") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.PersonOutline, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(AppColors.Background)
            ) {
                // Header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(AppColors.Primary)
                            .padding(horizontal = 24.dp, vertical = 40.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = AppColors.White.copy(alpha = 0.2f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.padding(12.dp),
                                    tint = AppColors.White
                                )
                            }
                            
                            Spacer(Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bem vindo,",
                                    color = AppColors.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "$primeiroNome!",
                                    color = AppColors.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HeaderActionButton(
                                    icon = Icons.Default.Notifications,
                                    onClick = onNavigateToNotifications
                                )
                                HeaderActionButton(
                                    icon = Icons.Default.Settings,
                                    onClick = onNavigateToSettings
                                )
                            }
                        }
                    }
                }

                // Main Service Card
                item {
                    Card(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Precisa de um marido de aluguel hoje?",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 26.sp,
                                        color = AppColors.TextPrimary
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "Eletricistas, encanadores e faz-tudo prontos para ajudar.",
                                        fontSize = 14.sp,
                                        color = AppColors.TextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(AppColors.PrimaryLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Build,
                                        contentDescription = null,
                                        tint = AppColors.Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            Button(
                                onClick = onNavigateToSolicitarServico,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Solicitar serviço",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Access Section
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Acesso rápido",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            QuickAccessCard(
                                label = "Pagamentos",
                                icon = Icons.Default.CreditCard,
                                onClick = onNavigateToPagamentos,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(16.dp))
                            QuickAccessCard(
                                label = "Pedidos",
                                icon = Icons.AutoMirrored.Filled.Assignment,
                                onClick = onNavigateToHistory,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            QuickAccessCard(
                                label = "Meus endereços",
                                icon = Icons.Default.LocationOn,
                                onClick = onNavigateToEnderecos,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.weight(1f))
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(40.dp))
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            }
        }
    }
}

@Composable
fun HeaderActionButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.White.copy(alpha = 0.15f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun QuickAccessCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AppColors.PrimaryLight, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
        }
    }
}
