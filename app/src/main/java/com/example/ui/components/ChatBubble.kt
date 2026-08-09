package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.data.model.ChatMessageEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private object NoPasteTextToolbar : TextToolbar {
    override val status: TextToolbarStatus = TextToolbarStatus.Hidden
    override fun hide() {}
    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        // Hide text toolbar menu to prevent pasting via context popup
    }
}

@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    personaDisplayName: String,
    onCopyText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    var selectionMenuRect by remember { mutableStateOf<Rect?>(null) }
    var onCopyCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var onSelectAllCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showSelectionMenu by remember { mutableStateOf(false) }

    val customSelectionTextToolbar = remember(message.content, isSpeaking) {
        object : TextToolbar {
            override val status: TextToolbarStatus
                get() = if (showSelectionMenu) TextToolbarStatus.Shown else TextToolbarStatus.Hidden

            override fun hide() {
                showSelectionMenu = false
            }

            override fun showMenu(
                rect: Rect,
                onCopyRequested: (() -> Unit)?,
                onPasteRequested: (() -> Unit)?,
                onCutRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ) {
                selectionMenuRect = rect
                onCopyCallback = onCopyRequested
                onSelectAllCallback = onSelectAllRequested
                showSelectionMenu = true
            }
        }
    }

    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.US
                ttsInstance?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }
                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                    }
                })
            }
        }
        ttsInstance = TextToSpeech(context, listener)
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val maxUserBubbleWidth = (LocalConfiguration.current.screenWidthDp * 0.72f).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User Message: Right-aligned bubble chip, max 72% width
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 4.dp
                    ),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .widthIn(max = maxUserBubbleWidth)
                        .testTag("user_chat_bubble")
                ) {
                    CompositionLocalProvider(LocalTextToolbar provides customSelectionTextToolbar) {
                        SelectionContainer {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                FormattedMessageText(
                                    content = message.content,
                                    isUser = true
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Assistant Message: Full width, clean ChatGPT style
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assistant_chat_bubble")
            ) {
                CompositionLocalProvider(LocalTextToolbar provides customSelectionTextToolbar) {
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            FormattedMessageText(
                                content = message.content,
                                isUser = false
                            )
                        }
                    }
                }
            }
        }

            // Custom Floating Menu on double-click / text selection (dragging)
            if (showSelectionMenu) {
                val rect = selectionMenuRect ?: Rect(0f, 0f, 100f, 100f)
                Popup(
                    popupPositionProvider = remember(rect) {
                        object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize
                            ): IntOffset {
                                val popupWidth = popupContentSize.width
                                val popupHeight = popupContentSize.height

                                // Normalize coordinates whether rect is in Window or Local space
                                val selectionLocalTop = if (rect.top >= anchorBounds.top) (rect.top - anchorBounds.top) else rect.top
                                val selectionLocalBottom = if (rect.bottom >= anchorBounds.top) (rect.bottom - anchorBounds.top) else rect.bottom
                                val selectionLocalLeft = if (rect.left >= anchorBounds.left) (rect.left - anchorBounds.left) else rect.left
                                val selectionLocalRight = if (rect.right >= anchorBounds.left) (rect.right - anchorBounds.left) else rect.right

                                val localCenterX = (selectionLocalLeft + selectionLocalRight) / 2f
                                val targetLocalX = localCenterX - popupWidth / 2f
                                val clampedLocalX = targetLocalX.coerceIn(8f, (anchorBounds.width - popupWidth - 8).toFloat().coerceAtLeast(8f))

                                // Position directly above selectionLocalTop
                                var targetLocalY = selectionLocalTop - popupHeight - 12f

                                // If target Y goes above anchor top, place below selectionLocalBottom
                                if (targetLocalY < 8f) {
                                    targetLocalY = selectionLocalBottom + 12f
                                }

                                return IntOffset(clampedLocalX.toInt(), targetLocalY.toInt())
                            }
                        }
                    },
                    onDismissRequest = { showSelectionMenu = false },
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Option 1: Copy
                            TextButton(
                                onClick = {
                                    if (onCopyCallback != null) {
                                        onCopyCallback?.invoke()
                                    } else {
                                        onCopyText(message.content)
                                    }
                                    Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
                                    showSelectionMenu = false
                                }
                            ) {
                                Text(
                                    text = "Copy",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .height(18.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )

                            // Option 2: Select All
                            TextButton(
                                onClick = {
                                    if (onSelectAllCallback != null) {
                                        onSelectAllCallback?.invoke()
                                    } else {
                                        onCopyText(message.content)
                                        Toast.makeText(context, "Selected all text", Toast.LENGTH_SHORT).show()
                                    }
                                    showSelectionMenu = false
                                }
                            ) {
                                Text(
                                    text = "Select All",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .height(18.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )

                            // Option 3: Read Aloud
                            TextButton(
                                onClick = {
                                    showSelectionMenu = false
                                    val nativeClipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                    if (onCopyCallback != null) {
                                        onCopyCallback?.invoke()
                                    }

                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        var textToRead = ""
                                        val clip = nativeClipboard?.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            val clipText = clip.getItemAt(0).text?.toString()
                                            if (!clipText.isNullOrBlank()) {
                                                textToRead = clipText
                                            }
                                        }
                                        if (textToRead.isBlank()) {
                                            textToRead = message.content
                                        }

                                        if (isSpeaking) {
                                            tts?.stop()
                                            isSpeaking = false
                                        } else {
                                            tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "read_aloud_selected")
                                            isSpeaking = true
                                            val previewSnippet = if (textToRead.length > 25) textToRead.take(25) + "..." else textToRead
                                            Toast.makeText(context, "Reading: \"$previewSnippet\"", Toast.LENGTH_SHORT).show()
                                        }
                                    }, 80L)
                                }
                            ) {
                                Text(
                                    text = if (isSpeaking) "Stop Reading" else "Read Aloud",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Assistant Action Row (Copy, Like, Dislike, Download, Share, More options)
            if (!isUser && message.content.isNotBlank()) {
                var isLiked by remember { mutableStateOf(false) }
                var isDisliked by remember { mutableStateOf(false) }
                var showMoreMenu by remember { mutableStateOf(false) }
                var showDownloadDialog by remember { mutableStateOf(false) }

                if (showDownloadDialog) {
                    var editedText by remember(message.content) { mutableStateOf(message.content) }

                    Dialog(
                        onDismissRequest = { showDownloadDialog = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Header Row with Back Button and Title
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { showDownloadDialog = false },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Edits & Download Response",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Instruction text
                                Text(
                                    text = "If you want to edit this context then edit first before downloading",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Editable input area (larger and paste protected)
                                CompositionLocalProvider(LocalTextToolbar provides NoPasteTextToolbar) {
                                    OutlinedTextField(
                                        value = editedText,
                                        onValueChange = { newText ->
                                            val insertedLength = newText.length - editedText.length
                                            if (insertedLength > 2) {
                                                Toast.makeText(
                                                    context,
                                                    "Direct clipboard paste is disabled. Please edit or type text directly.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                editedText = newText
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 220.dp, max = 340.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Two buttons horizontal alongside each other
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            showDownloadDialog = false
                                            saveAsTxt(context, editedText)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Download Text",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            showDownloadDialog = false
                                            saveAsPdf(context, editedText)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Download PDF",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // 1. Copy Icon
                    IconButton(
                        onClick = {
                            onCopyText(message.content)
                            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // 2. Like Button (Slidely hides when Disliked)
                    AnimatedVisibility(
                        visible = !isDisliked,
                        enter = slideInHorizontally { -it } + fadeIn(),
                        exit = slideOutHorizontally { -it } + fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                isLiked = !isLiked
                                if (isLiked) isDisliked = false
                                Toast.makeText(
                                    context,
                                    if (isLiked) "Thanks for your supporting" else "Removed feedback",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Like response",
                                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // 3. Dislike Button (Slidely hides when Liked)
                    AnimatedVisibility(
                        visible = !isLiked,
                        enter = slideInHorizontally { it } + fadeIn(),
                        exit = slideOutHorizontally { it } + fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                isDisliked = !isDisliked
                                if (isDisliked) isLiked = false
                                Toast.makeText(
                                    context,
                                    if (isDisliked) "Feedback submitted" else "Removed feedback",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                                contentDescription = "Dislike response",
                                tint = if (isDisliked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // 4. Download Icon
                    IconButton(
                        onClick = { showDownloadDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download response",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // 5. Share Icon
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message.content)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share response"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share response",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // 6. Three Vertical Dots (More options)
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
                        val timeString = remember(message.timestamp) {
                            val msgTime = if (message.timestamp > 0) message.timestamp else System.currentTimeMillis()
                            "Today, ${timeFormat.format(Date(msgTime))}"
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            // Header showing real-time timestamp like: Today, 2:00 PM
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = timeString,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))

                            // Option 1: Report Issue
                            DropdownMenuItem(
                                text = { Text("Report Issue") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    Toast.makeText(context, "Report submitted. Thank you!", Toast.LENGTH_SHORT).show()
                                }
                            )

                            // Option 2: Read Aloud
                            DropdownMenuItem(
                                text = { Text(if (isSpeaking) "Stop Reading" else "Read Aloud") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    if (isSpeaking) {
                                        tts?.stop()
                                        isSpeaking = false
                                    } else {
                                        tts?.speak(message.content, TextToSpeech.QUEUE_FLUSH, null, "msg_tts")
                                        isSpeaking = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun FormattedMessageText(
    content: String,
    isUser: Boolean
) {
    val textColor = MaterialTheme.colorScheme.onSurface

    if (content.contains("```")) {
        val parts = content.split("```")
        Column(modifier = if (isUser) Modifier else Modifier.fillMaxWidth()) {
            parts.forEachIndexed { index, part ->
                if (index % 2 == 1) {
                    // Code block -> Has internal padding inside dark code box
                    val lines = part.trim().lines()
                    val lang = if (lines.isNotEmpty() && lines[0].length < 15 && !lines[0].contains(" ")) lines[0] else "code"
                    val codeContent = if (lang == lines.firstOrNull()) lines.drop(1).joinToString("\n") else part.trim()

                    CodeBlockItem(codeContent = codeContent, language = lang)
                } else {
                    // Normal text block
                    if (part.isNotBlank()) {
                        NormalTextBlock(
                            text = part.trim(),
                            textColor = textColor,
                            isUser = isUser
                        )
                    }
                }
            }
        }
    } else {
        NormalTextBlock(
            text = content.trim(),
            textColor = textColor,
            isUser = isUser
        )
    }
}

@Composable
fun CodeBlockItem(
    codeContent: String,
    language: String
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(1.dp, Color(0xFF313244), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Color(0xFFCBA6F7)
                )
                IconButton(
                    onClick = {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Code Block", codeContent)
                        clipboardManager.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code snippet",
                        tint = Color(0xFFCDD6F4),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFF313244), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = codeContent,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp
                ),
                color = Color(0xFFCDD6F4),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun NormalTextBlock(
    text: String,
    textColor: Color,
    isUser: Boolean
) {
    val paragraphs = remember(text) { text.split(Regex("\n\n+")) }

    Column(
        modifier = (if (isUser) Modifier else Modifier.fillMaxWidth())
            .padding(vertical = 2.dp)
    ) {
        paragraphs.forEachIndexed { paraIndex, paragraph ->
            if (paraIndex > 0) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            val lines = paragraph.lines()
            val hasSpecialFormatting = lines.any { line ->
                val trimmed = line.trim()
                trimmed.startsWith("#") ||
                trimmed.startsWith("- ") ||
                trimmed.startsWith("* ") ||
                trimmed.startsWith("• ") ||
                trimmed.matches(Regex("""^\d+\.\s+.*"""))
            }

            if (hasSpecialFormatting) {
                lines.forEach { line ->
                    val trimmedLine = line.trim()
                    if (trimmedLine.isBlank()) return@forEach

                    when {
                        // Headers: # Header, ## Header, ### Header
                        trimmedLine.startsWith("#") -> {
                            val headerText = trimmedLine.replace(Regex("""^#+\s*"""), "")
                            Text(
                                text = parseInlineMarkdown(headerText, textColor),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    lineHeight = 24.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = textColor,
                                modifier = (if (isUser) Modifier else Modifier.fillMaxWidth())
                                    .padding(top = 6.dp, bottom = 4.dp)
                            )
                        }
                        // Bullet list items
                        trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") || trimmedLine.startsWith("• ") -> {
                            val itemText = trimmedLine.substring(2).trim()
                            Row(
                                modifier = (if (isUser) Modifier else Modifier.fillMaxWidth())
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = if (isUser) textColor else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp, top = 1.dp)
                                )
                                Text(
                                    text = parseInlineMarkdown(itemText, textColor),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 15.sp,
                                        lineHeight = 23.sp,
                                        letterSpacing = 0.15.sp
                                    ),
                                    color = textColor,
                                    modifier = if (isUser) Modifier else Modifier.fillMaxWidth()
                                )
                            }
                        }
                        // Numbered list items
                        trimmedLine.matches(Regex("""^\d+\.\s+.*""")) -> {
                            val match = Regex("""^(\d+)\.\s+(.*)""").find(trimmedLine)
                            val number = match?.groupValues?.get(1) ?: "1"
                            val itemText = match?.groupValues?.get(2) ?: trimmedLine

                            Row(
                                modifier = (if (isUser) Modifier else Modifier.fillMaxWidth())
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "$number.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    ),
                                    color = if (isUser) textColor else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp, top = 1.dp)
                                )
                                Text(
                                    text = parseInlineMarkdown(itemText, textColor),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 15.sp,
                                        lineHeight = 23.sp,
                                        letterSpacing = 0.15.sp
                                    ),
                                    color = textColor,
                                    modifier = if (isUser) Modifier else Modifier.fillMaxWidth()
                                )
                            }
                        }
                        // Normal paragraph line
                        else -> {
                            Text(
                                text = parseInlineMarkdown(trimmedLine, textColor),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 23.sp,
                                    letterSpacing = 0.15.sp
                                ),
                                color = textColor,
                                modifier = if (isUser) Modifier else Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = parseInlineMarkdown(paragraph, textColor),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                        letterSpacing = 0.15.sp
                    ),
                    color = textColor,
                    modifier = if (isUser) Modifier else Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun parseInlineMarkdown(
    text: String,
    textColor: Color
): AnnotatedString {
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBg = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surfaceVariant

    return remember(text, textColor, primaryColor, codeBg) {
        buildAnnotatedString {
            val regex = Regex("""(\*\*.*?\*\*|\*.*?\*|`.*?`)""")
            var currentIndex = 0

            regex.findAll(text).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val value = match.value

                if (start > currentIndex) {
                    append(text.substring(currentIndex, start))
                }

                when {
                    value.startsWith("**") && value.endsWith("**") && value.length >= 4 -> {
                        val inner = value.substring(2, value.length - 2)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                            append(inner)
                        }
                    }
                    value.startsWith("*") && value.endsWith("*") && value.length >= 2 -> {
                        val inner = value.substring(1, value.length - 1)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor)) {
                            append(inner)
                        }
                    }
                    value.startsWith("`") && value.endsWith("`") && value.length >= 2 -> {
                        val inner = value.substring(1, value.length - 1)
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.5.sp,
                                background = codeBg,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(" $inner ")
                        }
                    }
                    else -> {
                        append(value)
                    }
                }
                currentIndex = end
            }

            if (currentIndex < text.length) {
                append(text.substring(currentIndex))
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(
    personaDisplayName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thinking",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                DotAnimation()
            }
        }
    }
}

@Composable
fun DotAnimation() {
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            kotlinx.coroutines.delay(index * 150L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f + animatable.value * 0.7f)
                    )
            )
        }
    }
}

private fun saveAsPdf(context: Context, content: String) {
    try {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = android.text.TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val metaPaint = android.text.TextPaint().apply {
            color = android.graphics.Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }
        val bodyPaint = android.text.TextPaint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 12f
            isAntiAlias = true
        }

        canvas.drawText("AI Response Document", 40f, 50f, titlePaint)
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated: $dateStr", 40f, 68f, metaPaint)
        canvas.drawLine(40f, 78f, 555f, 78f, android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
        })

        val textWidth = 515
        val staticLayout = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.text.StaticLayout.Builder.obtain(content, 0, content.length, bodyPaint, textWidth)
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            android.text.StaticLayout(content, bodyPaint, textWidth, android.text.Layout.Alignment.ALIGN_NORMAL, 1.2f, 0f, false)
        }

        canvas.save()
        canvas.translate(40f, 90f)
        staticLayout.draw(canvas)
        canvas.restore()

        pdfDocument.finishPage(page)

        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val file = File(downloadsDir, "AI_Response_${System.currentTimeMillis()}.pdf")
        java.io.FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        Toast.makeText(context, "PDF saved: ${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun saveAsTxt(context: Context, content: String) {
    try {
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val file = File(downloadsDir, "AI_Response_${System.currentTimeMillis()}.txt")
        file.writeText(content)
        Toast.makeText(context, "TXT saved: ${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving TXT: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
