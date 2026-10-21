package com.duoc.cappucare.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.duoc.cappucare.data.ProductEntity
import com.duoc.cappucare.ui.theme.BackgroundGray
import com.duoc.cappucare.ui.theme.TealDark
import com.duoc.cappucare.ui.theme.TealPrimary
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ScannerScreen"
private val ScanAccent = Color(0xFF5FC9D3)

/**
 * Pantalla Escanear código (mockups "Escanear" y "Escanear (Flash)").
 *
 * @param onLookup        busca un producto por código (la implementa el ViewModel)
 * @param onProductFound  navega al detalle del producto encontrado
 * @param onAddProduct    se llama si el código no existe y el usuario quiere registrarlo
 */
@Composable
fun ScannerScreen(
    onLookup: (code: String, onResult: (ProductEntity?) -> Unit) -> Unit,
    onProductFound: (sku: String) -> Unit,
    onAddProduct: (code: String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // ---------- Permiso de cámara ----------
    var hasPermission by remember { mutableStateOf(hasCameraPermission(context)) }
    var requestedOnce by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    // Al volver de Ajustes (o del diálogo de permiso) revisamos de nuevo
    LifecycleResumeEffect(Unit) {
        hasPermission = hasCameraPermission(context)
        onPauseOrDispose { }
    }
    // Pedimos el permiso automáticamente solo la primera vez
    LaunchedEffect(Unit) {
        if (!hasPermission && !requestedOnce) {
            requestedOnce = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ---------- Estado de la pantalla ----------
    var torchOn by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }           // evita procesar el mismo código muchas veces
    var showManualDialog by remember { mutableStateOf(false) }
    var notFoundCode by remember { mutableStateOf<String?>(null) }

    fun processCode(code: String) {
        if (busy) return
        busy = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onLookup(code) { product ->
            if (product != null) onProductFound(product.sku) else notFoundCode = code
        }
    }

    // Libera el escáner con una pausa corta, para que no vuelva a detectar el mismo código al instante
    fun releaseScanner() {
        notFoundCode = null
        scope.launch {
            delay(1500)
            busy = false
        }
    }

    // ---------- UI ----------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Escanear código", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = TealDark,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
        ) {
            if (hasPermission) {
                CameraPreview(
                    torchOn = torchOn,
                    onFlashAvailable = { hasFlash = it },
                    onCodeDetected = { processCode(it) },
                    modifier = Modifier.fillMaxSize()
                )

                HintLabel(
                    text = "Apunta al código de QR o de barras para registrar",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                )
                ScanFrame(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f)
                )
                HintLabel(
                    text = "Coloca el código QR o de barras dentro del recuadro",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 24.dp, end = 24.dp, bottom = 88.dp)
                )
            } else {
                PermissionPanel(
                    onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onOpenSettings = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 64.dp)
                )
            }

            // Botones inferiores: Linterna y Entrada manual
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (hasPermission) {
                    OverlayButton(
                        text = "Linterna",
                        icon = if (torchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        active = torchOn,
                        enabled = hasFlash,
                        onClick = { torchOn = !torchOn }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                OverlayButton(
                    text = "Entrada manual",
                    icon = Icons.Default.Keyboard,
                    onClick = { showManualDialog = true }
                )
            }
        }
    }

    // ---------- Diálogos ----------
    if (showManualDialog) {
        ManualEntryDialog(
            onDismiss = { showManualDialog = false },
            onConfirm = { code ->
                showManualDialog = false
                processCode(code)
            }
        )
    }

    notFoundCode?.let { code ->
        AlertDialog(
            onDismissRequest = { releaseScanner() },
            title = { Text("Producto no encontrado") },
            text = { Text("No existe ningún producto con el código $code. ¿Quieres registrarlo?") },
            confirmButton = {
                TextButton(onClick = {
                    releaseScanner()
                    onAddProduct(code)
                }) { Text("Agregar producto") }
            },
            dismissButton = {
                TextButton(onClick = { releaseScanner() }) { Text("Reintentar") }
            }
        )
    }
}

private fun hasCameraPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

// =====================================================================
//  Cámara (CameraX) + lectura de códigos (ML Kit)
// =====================================================================

@Composable
private fun CameraPreview(
    torchOn: Boolean,
    onFlashAvailable: (Boolean) -> Unit,
    onCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    var camera by remember { mutableStateOf<Camera?>(null) }

    // El análisis se crea una sola vez, así que usamos las versiones "actualizadas" de los callbacks
    val currentOnCode by rememberUpdatedState(onCodeDetected)
    val currentOnFlash by rememberUpdatedState(onFlashAvailable)

    DisposableEffect(lifecycleOwner) {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39
                )
                .build()
        )
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var disposed = false

        providerFuture.addListener({
            if (disposed) return@addListener
            try {
                val provider = providerFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    analyzeFrame(imageProxy, scanner) { code -> currentOnCode(code) }
                }

                provider.unbindAll()
                val bound = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
                camera = bound
                currentOnFlash(bound.cameraInfo.hasFlashUnit())
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo iniciar la cámara", e)
            }
        }, mainExecutor)

        onDispose {
            // Importante: al salir de la pantalla la Activity sigue viva, así que hay que soltar la cámara a mano
            disposed = true
            if (providerFuture.isDone) {
                runCatching { providerFuture.get().unbindAll() }
            }
            analysisExecutor.shutdown()
            scanner.close()
        }
    }

    // Enciende o apaga la linterna cuando cambia el estado
    LaunchedEffect(camera, torchOn) {
        camera?.cameraControl?.enableTorch(torchOn)
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

/** Procesa un frame: si ML Kit encuentra un código, lo entrega. Siempre cierra el frame. */
@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun analyzeFrame(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstNotNullOfOrNull { it.rawValue }?.let(onResult)
        }
        .addOnCompleteListener { imageProxy.close() }
}

// =====================================================================
//  Componentes visuales
// =====================================================================

/** Recuadro con esquinas y línea de escaneo animada (Guía 12: animaciones). */
@Composable
private fun ScanFrame(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scanLine")
    val progress by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineProgress"
    )

    Canvas(modifier = modifier) {
        val stroke = 6.dp.toPx()
        val half = stroke / 2
        val len = size.minDimension * 0.2f
        val w = size.width
        val h = size.height

        fun corner(x: Float, y: Float, dx: Float, dy: Float) {
            drawLine(ScanAccent, Offset(x, y), Offset(x + dx * len, y), stroke, StrokeCap.Round)
            drawLine(ScanAccent, Offset(x, y), Offset(x, y + dy * len), stroke, StrokeCap.Round)
        }
        corner(half, half, 1f, 1f)
        corner(w - half, half, -1f, 1f)
        corner(half, h - half, 1f, -1f)
        corner(w - half, h - half, -1f, -1f)

        val y = h * progress
        drawLine(
            color = ScanAccent.copy(alpha = 0.85f),
            start = Offset(stroke, y),
            end = Offset(w - stroke, y),
            strokeWidth = 3.dp.toPx()
        )
    }
}

@Composable
private fun HintLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 15.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun OverlayButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    active: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) TealPrimary else Color.Black.copy(alpha = 0.55f),
            contentColor = Color.White,
            disabledContainerColor = Color.Black.copy(alpha = 0.3f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 15.sp)
    }
}

@Composable
private fun PermissionPanel(
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Necesitamos acceso a la cámara para escanear códigos QR y de barras.",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRequest,
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) { Text("Permitir cámara") }
        TextButton(onClick = onOpenSettings) {
            Text("Abrir ajustes", color = ScanAccent)
        }
    }
}

@Composable
private fun ManualEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Entrada manual") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Código SKU o de barras") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank()) {
                Text("Buscar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}