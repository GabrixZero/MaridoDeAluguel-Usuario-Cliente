package com.example.doesitusuario.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitusuario.data.model.ServiceRequestDTO
import com.example.doesitusuario.data.repository.ServiceRepository
import com.example.doesitusuario.ui.theme.AppColors
import com.example.doesitusuario.ui.theme.getCategoryLogo
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { ServiceRepository() }
    val scope = rememberCoroutineScope()
    var pedidos by remember { mutableStateOf<List<ServiceRequestDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    var filtro by remember { mutableStateOf("TODOS") }
    val filtros = listOf("TODOS", "PENDENTE", "AGENDADO", "EM ANDAMENTO", "CONCLUIDO", "CANCELADO")
    val filtrosLabel = mapOf(
        "TODOS"        to "Todos",
        "PENDENTE"     to "Pendentes",
        "AGENDADO"     to "Agendados",
        "EM ANDAMENTO" to "Em Andamento",
        "CONCLUIDO"    to "Concluídos",
        "CANCELADO"    to "Cancelados"
    )

    fun load() {
        scope.launch {
            isLoading = true
            errorMessage = ""
            val statusParam = if (filtro == "TODOS") null else filtro
            repository.getHistory(statusParam).fold(
                onSuccess = { pedidos = it },
                onFailure = { errorMessage = "Erro ao carregar pedidos" }
            )
            isLoading = false
        }
    }
    
    LaunchedEffect(filtro) { load() }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pedidos", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.TextPrimary) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
            )
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
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) },
                    label = { Text("Pedidos") },
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
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(AppColors.Background)) {
            LazyRow(
                Modifier.padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { f ->
                    val isSelected = f == filtro
                    Surface(
                        modifier = Modifier.clickable { filtro = f },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) AppColors.TextPrimary else AppColors.SurfaceVariant,
                    ) {
                        Text(
                            filtrosLabel[f] ?: f,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else AppColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage, color = AppColors.Error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { load() }) {
                            Text("Tentar novamente")
                        }
                    }
                }
            } else if (pedidos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum pedido encontrado", color = AppColors.TextSecondary)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pedidos) { pedido ->
                        PedidoCardItem(
                            pedido = pedido,
                            onClick = { onNavigateToDetail(pedido.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoCardItem(
    pedido: ServiceRequestDTO,
    onClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(AppColors.SurfaceVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(getCategoryLogo(pedido.serviceName)),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(pedido.serviceName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                    val displayOtherName: String = pedido.otherPartyName ?: "Usuário"
                    Text(displayOtherName, color = AppColors.TextSecondary, fontSize = 14.sp)
                }
                Text(
                    "R$ ${String.format(Locale.getDefault(), "%.2f", pedido.value)}",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary
                )
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AppColors.Border)
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, Modifier.size(16.dp), tint = AppColors.TextSecondary)
                    Spacer(Modifier.width(8.dp))
                    val dateToDisplay = try {
                        formatIsoDate(pedido.date ?: "")
                    } catch (e: Exception) {
                        pedido.date ?: ""
                    }
                    Text(dateToDisplay, color = AppColors.TextSecondary, fontSize = 14.sp)
                }
                
                StatusBadge(pedido.status)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, txt, label) = when (status.uppercase()) {
        "PENDENTE" -> Triple(Color(0xFFFFF3E0), Color(0xFFFFA000), "PENDENTE")
        "AGENDADO" -> Triple(Color(0xFFE3F2FD), Color(0xFF1E88E5), "AGENDADO")
        "EM ANDAMENTO" -> Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), "EM ANDAMENTO")
        "CONCLUIDO" -> Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), "CONCLUÍDO")
        "CANCELADO" -> Triple(Color(0xFFFFEBEE), Color(0xFFF44336), "CANCELADO")
        else -> Triple(AppColors.SurfaceVariant, AppColors.TextSecondary, status)
    }
    Surface(color = bg, shape = RoundedCornerShape(4.dp)) {
        Text(
            label,
            color = txt,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun formatIsoDate(isoString: String): String {
    if (isoString.isBlank()) return ""
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val ldt = LocalDateTime.parse(isoString.replace(" ", "T"))
            ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        } else {
            isoString.substringBefore(".").replace("T", " ")
        }
    } catch (e: Exception) {
        isoString
    }
}
