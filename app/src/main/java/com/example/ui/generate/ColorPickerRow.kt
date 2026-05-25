package com.example.ui.generate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
data class ProfessionalColor(
    val name: String,
    val color: Color,
    val hexValue: Int
)

val GeneratePalette = listOf(
    ProfessionalColor("Laser Cyan", CyanPrimary, 0xFF00E5FF.toInt()),
    ProfessionalColor("Total Black", Color(0xFF000000), 0xFF000000.toInt()),
    ProfessionalColor("Clean White", Color(0xFFFFFFFF), 0xFFFFFFFF.toInt()),
    ProfessionalColor("Business Blue", Color(0xFF1E88E5), 0xFF1E88E5.toInt()),
    ProfessionalColor("Ecology Green", Color(0xFF43A047), 0xFF43A047.toInt()),
    ProfessionalColor("Safety Orange", Color(0xFFFB8C00), 0xFFFB8C00.toInt()),
    ProfessionalColor("Royal Purple", Color(0xFF8E24AA), 0xFF8E24AA.toInt()),
    ProfessionalColor("Slate Grey", Color(0xFF546E7A), 0xFF546E7A.toInt())
)

@Composable
fun ColorPickerRow(
    selectedColorValue: Int,
    onColorSelected: (Int) -> Unit,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(GeneratePalette) { paletteColor ->
                val isSelected = paletteColor.hexValue == selectedColorValue
                
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(paletteColor.color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) CyanPrimary else Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(paletteColor.hexValue) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        // Tiny dot inside to represent validation
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (paletteColor.hexValue == 0xFFFFFFFF.toInt()) Color.Black else Color.White,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

