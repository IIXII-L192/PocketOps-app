package l192.aakarsh.pocketops.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import l192.aakarsh.pocketops.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun QrScannerDialog(
    onDismiss: () -> Unit,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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
        if (!isGranted) {
            android.widget.Toast.makeText(context, "Camera permission is required to scan QR codes", android.widget.Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                CameraPreviewView(
                    lifecycleOwner = lifecycleOwner,
                    onQrScanned = { raw ->
                        val parsedNumber = parseWhatsAppNumber(raw)
                        if (parsedNumber != null) {
                            onQrScanned(parsedNumber)
                        } else {
                            // Show toast or keep scanning
                            android.widget.Toast.makeText(context, "Invalid WhatsApp QR code", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                ScannerOverlayView(onClose = onDismiss)
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun CameraPreviewView(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var scanEnabled by remember { mutableStateOf(true) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(previewView, lifecycleOwner) {
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && scanEnabled) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                val scanner = BarcodeScanning.getClient()
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            val rawValue = barcode.rawValue
                                            if (!rawValue.isNullOrBlank()) {
                                                scanEnabled = false
                                                onQrScanned(rawValue)
                                                break
                                            }
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cameraExecutor.shutdown()
        }
    }
}

@Composable
fun ScannerOverlayView(onClose: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "laserLine")
    val laserYPercent by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPosition"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val boxSize = canvasWidth * 0.7f
            val left = (canvasWidth - boxSize) / 2
            val top = (canvasHeight - boxSize) / 2
            val right = left + boxSize
            val bottom = top + boxSize

            // 1. Draw dim background everywhere except the box
            val transparentPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(left, top, right, bottom),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                )
            }
            clipPath(transparentPath, clipOp = ClipOp.Difference) {
                drawRect(Color.Black.copy(alpha = 0.65f))
            }

            // 2. Draw active scanning box border (Material 3 style)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(left, top),
                size = size.copy(width = boxSize, height = boxSize),
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            // 3. Draw animated glowing laser line
            val laserY = top + (boxSize * laserYPercent)
            drawLine(
                color = Color(0xFF25D366), // WhatsApp Green
                start = Offset(left + 16.dp.toPx(), laserY),
                end = Offset(right - 16.dp.toPx(), laserY),
                strokeWidth = 4.dp.toPx()
            )
        }

        Surface(
            color = Color.Black.copy(alpha = 0.62f),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    modifier = Modifier.size(36.dp)
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_round),
                        contentDescription = "PocketOps logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = "PocketOps",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 24.dp)
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "Close",
                tint = Color.White
            )
        }

        // Instructions Text
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Align WhatsApp QR code within the frame",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private val CircleShape = RoundedCornerShape(50)

fun parseWhatsAppNumber(qrText: String): String? {
    val clean = qrText.trim()
    
    // 1. Check query parameter e.g. https://api.whatsapp.com/send?phone=1234567890
    val phoneRegex = Regex("[?&]phone=([+0-9]+)")
    val match = phoneRegex.find(clean)
    if (match != null) {
        return match.groupValues[1]
    }
    
    // 2. Check path e.g. wa.me/1234567890 or https://wa.me/1234567890
    if (clean.contains("wa.me/")) {
        val waMeRegex = Regex("wa\\.me/([+0-9]+)")
        val waMatch = waMeRegex.find(clean)
        if (waMatch != null) {
            return waMatch.groupValues[1]
        }
    }
    
    // 3. Plain phone number checks
    val digitsOnly = clean.replace(Regex("[^0-9]"), "")
    if (digitsOnly.length >= 10 && (clean.startsWith("+") || clean.all { it.isDigit() })) {
        return clean
    }
    
    return null
}
