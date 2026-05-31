package com.example.doesitusuario.ui.screens.addresses

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.doesitusuario.data.model.AddressDTO
import com.example.doesitusuario.ui.screens.login.SuccessBanner
import com.example.doesitusuario.ui.screens.payments.dashedBorder
import com.example.doesitusuario.ui.theme.AppColors
import com.example.doesitusuario.ui.viewmodel.AddressViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    onNavigateToEditAddress: (Long) -> Unit,
    onBack: () -> Unit,
    successMessage: String? = null,
    viewModel: AddressViewModel = viewModel()
) {
    val addresses by viewModel.addresses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showSuccess by remember { mutableStateOf(successMessage != null) }
    var currentSuccessMessage by remember { mutableStateOf(successMessage ?: "") }

    LaunchedEffect(Unit) {
        viewModel.loadAddresses()
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null && successMessage.isNotEmpty()) {
            currentSuccessMessage = successMessage
            showSuccess = true
            delay(5000)
            showSuccess = false
        }
    }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meus endereços", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, null, tint = AppColors.TextPrimary)
                    } 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.Surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AppColors.Primary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(addresses, key = { it.id }) { addr ->
                        AddressItemCard(
                            address = addr,
                            onEditClick = { onNavigateToEditAddress(addr.id) }
                        )
                    }

                    // Botão Adicionar novo endereço
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .dashedBorder(color = AppColors.TextDisabled, cornerRadius = 12.dp)
                                .clickable { onNavigateToAddAddress() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AppColors.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Adicionar novo endereço",
                                    color = AppColors.TextSecondary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(56.dp)) }
                }
            }

            // Success Banner
            AnimatedVisibility(
                visible = showSuccess && currentSuccessMessage.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
            ) {
                SuccessBanner(message = currentSuccessMessage)
            }
        }
    }
}

@Composable
fun AddressItemCard(
    address: AddressDTO,
    onEditClick: () -> Unit
) {
    // Blindagem de campos nulos para evitar crash ao renderizar o card
    val safeTag = address.tag ?: "Endereço"
    val safeStreet = address.street ?: "Rua não informada"
    val safeNumber = address.number ?: "S/N"
    val safeNeighborhood = address.neighborhood ?: ""
    val safeCity = address.city ?: ""
    val safeState = address.state ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAddressIcon(safeTag),
                    contentDescription = null,
                    tint = AppColors.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = safeTag,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    if (address.isDefault) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = AppColors.Primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PRINCIPAL",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "$safeStreet, $safeNumber, $safeNeighborhood",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "$safeCity - $safeState",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
            
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = AppColors.TextDisabled,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun getAddressIcon(tag: String): ImageVector {
    return when (tag.lowercase()) {
        "casa" -> Icons.Default.Home
        "trabalho" -> Icons.Default.Work
        "escola", "faculdade" -> Icons.Default.School
        else -> Icons.Default.LocationOn
    }
}
