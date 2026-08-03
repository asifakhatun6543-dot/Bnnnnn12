package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonaEntity
import com.example.data.model.ProviderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    allPersonas: List<PersonaEntity>,
    editingPersona: PersonaEntity?,
    onOpenEditor: (PersonaEntity?) -> Unit,
    onCloseEditor: () -> Unit,
    onSavePersona: (PersonaEntity) -> Unit,
    onDeletePersona: (Long) -> Unit,
    onCloseAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Admin Control Center",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Configure API Keys, Display Names & Model Versioning",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCloseAdmin) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { onOpenEditor(null) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_persona_button"),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Persona")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Notice Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "User UI Protection Active: Users will ONLY see your Custom Display Names (e.g., 'Smart Code Assistant'). Provider API keys and vendor names remain hidden.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = "CONFIGURED ASSISTANTS & MODELS (${allPersonas.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Personas List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allPersonas, key = { it.id }) { persona ->
                    PersonaAdminCard(
                        persona = persona,
                        onEdit = { onOpenEditor(persona) },
                        onDelete = { onDeletePersona(persona.id) }
                    )
                }
            }
        }
    }

    // Persona Editor Modal Dialog
    if (editingPersona != null) {
        PersonaEditorDialog(
            persona = editingPersona,
            onDismiss = onCloseEditor,
            onSave = onSavePersona
        )
    }
}

@Composable
fun PersonaAdminCard(
    persona: PersonaEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = persona.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (persona.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "DEFAULT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Internal Provider Tag (For Admin eyes only)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Provider: ${persona.providerType}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Model Versioning Tag
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        ) {
                            Text(
                                text = "Ver: ${persona.modelVersion.ifBlank { "default" }}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Key Status Indicator
                    Icon(
                        imageVector = if (persona.apiKey.isNotBlank()) Icons.Default.Key else Icons.Default.KeyOff,
                        contentDescription = null,
                        tint = if (persona.apiKey.isNotBlank()) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit persona")
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete persona",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // System Prompt Preview
            Text(
                text = "System Prompt: \"${persona.systemPrompt}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Temp: ${persona.temperature} | MaxTokens: ${persona.maxTokens}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = if (persona.isActive) "● Active" else "○ Disabled",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (persona.isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaEditorDialog(
    persona: PersonaEntity,
    onDismiss: () -> Unit,
    onSave: (PersonaEntity) -> Unit
) {
    var displayName by remember { mutableStateOf(persona.displayName) }
    var selectedProvider by remember { mutableStateOf(persona.providerType) }
    var apiKey by remember { mutableStateOf(persona.apiKey) }
    var modelVersion by remember { mutableStateOf(persona.modelVersion) }
    var baseUrl by remember { mutableStateOf(persona.baseUrl) }
    var systemPrompt by remember { mutableStateOf(persona.systemPrompt) }
    var temperature by remember { mutableFloatStateOf(persona.temperature) }
    var maxTokens by remember { mutableStateOf(persona.maxTokens.toString()) }
    var badgeText by remember { mutableStateOf(persona.badgeText) }
    var isActive by remember { mutableStateOf(persona.isActive) }
    var isDefault by remember { mutableStateOf(persona.isDefault) }

    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (persona.id == 0L) "Add New AI Persona" else "Edit Persona & Model Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Preset Quick Templates
                Text(
                    text = "QUICK PRESETS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedProvider = ProviderType.CLAUDE.name
                            if (modelVersion.isBlank()) modelVersion = "claude-3-5-sonnet-20241022"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Claude", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedProvider = ProviderType.GEMINI.name
                            if (modelVersion.isBlank()) modelVersion = "gemini-2.5-flash"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Gemini", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedProvider = ProviderType.MISTRAL.name
                            if (modelVersion.isBlank()) modelVersion = "mistral-large-latest"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mistral", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedProvider = ProviderType.OPENAI.name
                            if (modelVersion.isBlank()) modelVersion = "gpt-4o-mini"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("OpenAI", fontSize = 11.sp)
                    }
                }

                HorizontalDivider()

                // User Display Name (CRITICAL: Name shown to end user)
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("User Display Name (Visible to Users)") },
                    placeholder = { Text("e.g. Smart Code Assistant Pro") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("persona_display_name_input")
                )

                // Provider Engine
                Text(
                    text = "Provider Backend Engine",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Column {
                    ProviderType.values().forEach { provider ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedProvider = provider.name }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedProvider == provider.name,
                                onClick = { selectedProvider = provider.name }
                            )
                            Text(
                                text = provider.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // API Key Input
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Provider API Key") },
                    placeholder = { Text("Paste API Key here...") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle key visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Model Versioning Setting
                OutlinedTextField(
                    value = modelVersion,
                    onValueChange = { modelVersion = it },
                    label = { Text("Model Version Identifier") },
                    placeholder = { Text("e.g. gemini-2.5-flash, claude-3-5-sonnet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Custom Base URL
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Custom API Base URL (Optional)") },
                    placeholder = { Text("https://api.custom-endpoint.com/v1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // System Prompt
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("Customizable System Prompt") },
                    placeholder = { Text("Define persona behavior...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                // Temperature Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Temperature (Creativity)")
                        Text(text = String.format("%.2f", temperature))
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0.0f..1.0f
                    )
                }

                // Max Tokens & Badge Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = maxTokens,
                        onValueChange = { maxTokens = it },
                        label = { Text("Max Tokens") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = badgeText,
                        onValueChange = { badgeText = it },
                        label = { Text("Badge Label") },
                        placeholder = { Text("e.g. Pro") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Active & Available to Users")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Set as Default Assistant")
                    Switch(checked = isDefault, onCheckedChange = { isDefault = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tokensInt = maxTokens.toIntOrNull() ?: 2048
                    val updated = persona.copy(
                        displayName = displayName.ifBlank { "Custom Assistant" },
                        providerType = selectedProvider,
                        apiKey = apiKey.trim(),
                        modelVersion = modelVersion.trim(),
                        baseUrl = baseUrl.trim(),
                        systemPrompt = systemPrompt.ifBlank { "You are a helpful assistant." },
                        temperature = temperature,
                        maxTokens = tokensInt,
                        badgeText = badgeText.trim(),
                        isActive = isActive,
                        isDefault = isDefault
                    )
                    onSave(updated)
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
