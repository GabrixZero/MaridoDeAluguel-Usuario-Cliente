package com.example.doesitusuario.ui.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitusuario.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAddCard: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pagamentos", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Balance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Primary)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Saldo disponível",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "R$ 1.250,00",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Cards Section
            item {
                Column {
                    Text(
                        text = "Meus cartões",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StaticCardItem(
                            bankName = "Nubank",
                            brand = "Mastercard",
                            last4 = "4829"
                        )
                        StaticCardItem(
                            bankName = "Inter",
                            brand = "Visa",
                            last4 = "9921"
                        )
                    }
                }
            }

            // Add Card Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .dashedBorder(color = AppColors.TextDisabled, cornerRadius = 12.dp),
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
                            text = "Cadastrar novo cartão",
                            color = AppColors.TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StaticCardItem(bankName: String, brand: String, last4: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = AppColors.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bankName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "$brand •••• $last4",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.TextDisabled,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun Modifier.dashedBorder(color: Color, cornerRadius: Dp): Modifier = this.drawBehind {
    val strokeWidth = 1.dp.toPx()
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth, pathEffect = dashPathEffect),
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    PaymentScreen(
        onNavigateToHome = {},
        onNavigateToHistory = {},
        onNavigateToProfile = {},
        onNavigateToAddCard = {},
        onBack = {}
    )
}
