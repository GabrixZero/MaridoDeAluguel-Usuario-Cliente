package com.example.doesitusuario.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doesitusuario.data.model.NotificationDTO
import com.example.doesitusuario.ui.theme.AppColors
import com.example.doesitusuario.ui.viewmodel.NotificationViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Notificações", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background,
                    titleContentColor = AppColors.TextPrimary,
                    navigationIconContentColor = AppColors.TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.Surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { onBack() },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navegar para histórico */ },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Pedidos") },
                    label = { Text("Pedidos") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navegar para perfil */ },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AppColors.Primary)
            } else if (notifications.isEmpty()) {
                Text(
                    text = "Nenhuma notificação por enquanto",
                    modifier = Modifier.align(Alignment.Center),
                    color = AppColors.TextSecondary
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notifications) { notification ->
                        NotificationItemRow(notification)
                        HorizontalDivider(color = AppColors.Border.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(notification: NotificationDTO) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon Circle (Default for now)
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(AppColors.PrimaryLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary
                )
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = notification.message,
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = formatNotificationTime(notification.timestamp),
                fontSize = 12.sp,
                color = AppColors.TextDisabled
            )
        }
    }
}

fun formatNotificationTime(timestamp: String): String {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Remove espaços e garante formato ISO (substitui espaço por T se necessário)
            val isoTimestamp = timestamp.trim().replace(" ", "T")
            
            // Parse como LocalDateTime (supondo que a string não tenha o sufixo Z ou Offset)
            val ldt = LocalDateTime.parse(isoTimestamp)
            
            // Converte de UTC para o fuso horário local do celular
            val instant = ldt.atZone(ZoneId.of("UTC")).toInstant()
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            val now = LocalDateTime.now()
            
            val diff = Duration.between(localDateTime, now)
            val minutes = diff.toMinutes()
            val hours = diff.toHours()
            val days = diff.toDays()

            when {
                // Se a diferença for negativa (hora do servidor levemente à frente), mostra "Agora mesmo"
                minutes < 0 -> "Agora mesmo"
                minutes < 60 -> "$minutes min atrás"
                hours < 24 -> "$hours h atrás"
                days == 1L -> "Ontem"
                else -> localDateTime.format(DateTimeFormatter.ofPattern("dd MMM", Locale("pt", "BR")))
            }
        } else {
            timestamp.substringBefore(" ").ifEmpty { timestamp.substringBefore("T") }
        }
    } catch (e: Exception) {
        android.util.Log.e("NotificationTime", "Error parsing: $timestamp", e)
        timestamp
    }
}
