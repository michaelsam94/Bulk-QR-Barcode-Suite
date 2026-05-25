package com.example.ui.generate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.generate.GenerateUiEvent
import com.example.presentation.generate.GenerateViewModel
import com.example.ui.theme.CyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BRANDED QR GENERATOR",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0E12)
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E12))
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Content Input text block
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "QR CODE CONTENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = state.content,
                    onValueChange = { viewModel.onEvent(GenerateUiEvent.ContentChanged(it)) },
                    placeholder = { Text("Enter URL, standard text, or business payload...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CyanPrimary,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = CyanPrimary
                    )
                )
            }

            // Foreground Color Selection Row
            ColorPickerRow(
                selectedColorValue = state.foregroundColor,
                onColorSelected = { viewModel.onEvent(GenerateUiEvent.FgColorChanged(it)) },
                title = "FOREGROUND DESIGN PIXEL COLOR"
            )

            // Background Color Selection Row
            ColorPickerRow(
                selectedColorValue = state.backgroundColor,
                onColorSelected = { viewModel.onEvent(GenerateUiEvent.BgColorChanged(it)) },
                title = "BACKGROUND DESIGN PLATES COLOR"
            )

            // Branded select logo Section Component
            LogoPickerSection(
                logoUri = state.logoUri,
                onLogoPicked = { viewModel.onEvent(GenerateUiEvent.LogoPicked(it)) },
                onRemoveLogo = { viewModel.onEvent(GenerateUiEvent.RemoveLogo) }
            )

            // Submit generation Action Trigger
            Button(
                onClick = { viewModel.onEvent(GenerateUiEvent.Generate) },
                enabled = state.content.isNotBlank() && !state.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = Color(0xFF0D151D)
                )
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF0D151D)
                    )
                } else {
                    Text(
                        text = "Generate Branded QR Code",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }

            // Error summary display panel
            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 16.sp
                )
            }

            // Output generator preview attachment
            if (state.generatedQrUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                QrPreview(generatedPath = state.generatedQrUri ?: "")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
