package com.example.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.mapper.toDomainFormat
import com.example.data.scanner.BarcodeAnalyzer
import com.example.presentation.scan.ScanUiEvent
import com.example.presentation.scan.ScanViewModel
import com.example.ui.components.ScanCountBadge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Trigger permission requests reactively on load
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Capture frames globally and dispatch standard scan events
    val analyzer = remember {
        BarcodeAnalyzer { barcodes ->
            val first = barcodes.firstOrNull() ?: return@BarcodeAnalyzer
            val rawValue = first.rawValue ?: return@BarcodeAnalyzer
            val domainFormat = first.format.toDomainFormat()
            viewModel.onEvent(ScanUiEvent.BarcodeDetected(rawValue, domainFormat))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E12))
    ) {
        if (hasCameraPermission) {
            // Live Frame Camera View Layer
            CameraPreview(analyzer = analyzer)

            // Scanning reticle guidance overlays representational frame
            ScanningReticle(isActive = state.isScanning)

            // Top Action Beacon and Counters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 50.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScanCountBadge(count = state.scanCount)

                // Quick toggle scan active laser beam
                IconButton(
                    onClick = { viewModel.onEvent(ScanUiEvent.ToggleScanState) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (state.isScanning) CyanPrimary else Color(0x99000000),
                            RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Active sweep laser diagnostic toggle",
                        tint = if (state.isScanning) Color(0xFF0D151D) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Amber warning duplicate flash alert notification screen overlay
            AnimatedVisibility(
                visible = state.isDuplicateFlash,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AmberWarning.copy(alpha = 0.2f))
                        .border(3.dp, AmberWarning)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color(0xE60A0E12), RoundedCornerShape(12.dp))
                            .border(1.dp, AmberWarning, RoundedCornerShape(12.dp))
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DUPLICATE DETECTED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AmberWarning,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This item has already been recorded inside this batch.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }
                }
            }

            // Success feedback overlays
            ScanSuccessOverlay(
                value = state.lastScannedValue ?: "",
                formatName = state.lastScannedFormat,
                visible = state.isSuccessFlash
            )

            // Live Items scrolling drawer bottom sheet
            ScannedListBottomSheet(
                codes = state.scannedCodes,
                exportState = state.exportState,
                onNewSession = { viewModel.onEvent(ScanUiEvent.StartNewSession) },
                onDeleteCode = { id -> viewModel.onEvent(ScanUiEvent.DeleteCode(id)) },
                onExport = { format, dest -> viewModel.onEvent(ScanUiEvent.ExportSession(format, dest)) },
                onClearExportState = { viewModel.onEvent(ScanUiEvent.ClearExportState) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

        } else {
            // Permission request placeholder UI block
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Required permissions placeholder icon",
                    tint = CyanPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "CAMERA ACCESS REQUIRED",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "We require camera stream permissions to drive the ML Kit continuous fast barcode scanning engine.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = Color(0xFF0D151D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun ScanningReticle(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Outer aiming rectangular bounding box
        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 220.dp)
                .border(
                    width = 2.dp,
                    color = if (isActive) CyanPrimary.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            if (isActive) {
                // Horizontal sweeping laser beam simulation element
                val infiniteTransition = rememberInfiniteTransition(label = "laser_pulse")
                val floatPos by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween<Float>(2000, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "laser_position"
                )
                
                val density = androidx.compose.ui.platform.LocalDensity.current
                val heightPx = with(density) { 220.dp.toPx() }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp)
                        .background(CyanPrimary)
                        .graphicsLayer {
                            translationY = heightPx * floatPos
                        }
                )
            }
        }
    }
}
