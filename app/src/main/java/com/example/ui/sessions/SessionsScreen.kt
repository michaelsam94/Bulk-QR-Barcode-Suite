package com.example.ui.sessions

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.entity.ExportFormat
import com.example.presentation.scan.ExportState
import com.example.presentation.sessions.SessionsUiEvent
import com.example.presentation.sessions.SessionsViewModel
import com.example.ui.theme.CyanPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    viewModel: SessionsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var pendingExportSessionId by remember { mutableStateOf<String?>(null) }

    // SAF launchers registered for exporting respective formats safely
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        val sessionId = pendingExportSessionId ?: return@rememberLauncherForActivityResult
        uri?.let {
            viewModel.onEvent(SessionsUiEvent.ExportSession(sessionId, ExportFormat.CSV, it.toString()))
        }
    }

    val xlsxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        val sessionId = pendingExportSessionId ?: return@rememberLauncherForActivityResult
        uri?.let {
            viewModel.onEvent(SessionsUiEvent.ExportSession(sessionId, ExportFormat.XLSX, it.toString()))
        }
    }

    LaunchedEffect(state.exportState) {
        if (state.exportState is ExportState.Success) {
            android.widget.Toast.makeText(context, (state.exportState as ExportState.Success).message, android.widget.Toast.LENGTH_LONG).show()
            viewModel.onEvent(SessionsUiEvent.ClearExportState)
        } else if (state.exportState is ExportState.Error) {
            android.widget.Toast.makeText(context, (state.exportState as ExportState.Error).error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.onEvent(SessionsUiEvent.ClearExportState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HISTORICAL BATCHES",
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
        ) {
            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else if (state.sessions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty sessions placeholder",
                        tint = Color.Gray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "NO SESSIONS RECORDED",
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Batch scanning sessions will appear here once saved from the active scanning camera pane.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.sessions, key = { it.id }) { session ->
                        val isSelected = session.id == state.selectedSessionId
                        SessionCard(
                            session = session,
                            isExpanded = isSelected,
                            onToggleExpand = {
                                viewModel.onEvent(SessionsUiEvent.ToggleSessionSelection(session.id))
                            },
                            onExportCsv = {
                                pendingExportSessionId = session.id
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                val cleanName = session.name.replace(" ", "_").replace(",", "")
                                csvLauncher.launch("export_${cleanName}_$timeStamp.csv")
                            },
                            onExportXlsx = {
                                pendingExportSessionId = session.id
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                val cleanName = session.name.replace(" ", "_").replace(",", "")
                                xlsxLauncher.launch("export_${cleanName}_$timeStamp.xlsx")
                            },
                            onDelete = {
                                viewModel.onEvent(SessionsUiEvent.DeleteSession(session.id))
                            },
                            codes = if (isSelected) state.selectedSessionCodes else emptyList()
                        )
                    }
                }
            }
        }
    }
}
