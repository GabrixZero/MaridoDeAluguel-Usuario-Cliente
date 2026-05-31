package com.example.doesitusuario.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitusuario.ui.theme.AppColors

@Composable
fun SuccessBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .background(AppColors.SuccessBanner, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .background(AppColors.ErrorBanner, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = AppColors.TextDisabled) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else visualTransformation,
            trailingIcon = if (isPassword) {
                {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = AppColors.TextSecondary)
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.InputBackground,
                unfocusedContainerColor = AppColors.InputBackground,
                focusedBorderColor = if (isError) AppColors.ErrorBanner else AppColors.Primary,
                unfocusedBorderColor = if (isError) AppColors.ErrorBanner else Color.Transparent,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                cursorColor = AppColors.Primary
            ),
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }
}
