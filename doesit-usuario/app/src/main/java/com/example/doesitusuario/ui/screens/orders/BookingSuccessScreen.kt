package com.example.doesitusuario.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitusuario.ui.theme.AppColors

@Composable
fun BookingSuccessScreen(
    onViewOrders: () -> Unit,
    onGoHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Green Check Icon
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = Color(0xFFD1FAE5) // Light green background
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF10B981), // Solid green
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Agendamento\nSolicitado!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Seu pedido foi enviado ao profissional.\nVocê receberá uma notificação assim que\no agendamento for confirmado.",
            fontSize = 16.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onViewOrders,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
        ) {
            Text(
                text = "Ver Meus Pedidos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary.copy(alpha = 0.1f),
                contentColor = AppColors.Primary
            ),
            elevation = null
        ) {
            Text(
                text = "Voltar ao Início",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
