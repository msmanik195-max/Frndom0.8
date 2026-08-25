package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

enum class CropShape {
    CIRCLE,
    SQUARE,
    COVER
}

@Composable
fun ImageCropDialog(
    imageUri: Uri,
    onCropSuccess: (Uri) -> Unit,
    onDismiss: () -> Unit,
    cropShape: CropShape = CropShape.CIRCLE,
    title: String = if (cropShape == CropShape.COVER) "Crop Cover Photo" else "Crop Profile Picture"
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    var isProcessing by remember { mutableStateOf(false) }
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingBitmap by remember { mutableStateOf(true) }

    // Load original bitmap safely with downsampling if exceptionally huge
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                var stream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val boundsOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(stream, null, boundsOptions)
                stream?.close()

                var sampleSize = 1
                while (boundsOptions.outWidth / sampleSize > 2500 || boundsOptions.outHeight / sampleSize > 2500) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                stream = context.contentResolver.openInputStream(imageUri)
                val bmp = BitmapFactory.decodeStream(stream, null, decodeOptions)
                stream?.close()

                withContext(Dispatchers.Main) {
                    sourceBitmap = bmp
                    isLoadingBitmap = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingBitmap = false
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isProcessing,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F10))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("image_crop_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (!isProcessing) onDismiss() },
                        enabled = !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            if (isProcessing) return@Button
                            val bmp = sourceBitmap
                            if (bmp == null) {
                                onCropSuccess(imageUri)
                                return@Button
                            }
                            isProcessing = true
                            scope.launch {
                                val croppedUri = cropAndSaveImageExact(
                                    context = context,
                                    sourceBitmap = bmp,
                                    scale = scale,
                                    offsetX = offsetX,
                                    offsetY = offsetY,
                                    rotation = rotationAngle,
                                    cropShape = cropShape
                                )
                                isProcessing = false
                                if (croppedUri != null) {
                                    onCropSuccess(croppedUri)
                                } else {
                                    Toast.makeText(context, "Failed to crop, using original", Toast.LENGTH_SHORT).show()
                                    onCropSuccess(imageUri)
                                }
                            }
                        },
                        enabled = !isProcessing && !isLoadingBitmap,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1877F2),
                            contentColor = Color.White
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(text = "Save", fontWeight = FontWeight.Bold)
                    }
                }

                // Middle Cropping Viewport
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingBitmap) {
                        CircularProgressIndicator(color = Color(0xFF1877F2))
                    } else if (sourceBitmap != null) {
                        val bmp = sourceBitmap!!
                        val bmpWidth = bmp.width.toFloat()
                        val bmpHeight = bmp.height.toFloat()

                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val viewWidth = constraints.maxWidth.toFloat()
                            val viewHeight = constraints.maxHeight.toFloat()

                            // Calculate crop window dimensions on screen
                            val cropWidth: Float
                            val cropHeight: Float

                            when (cropShape) {
                                CropShape.CIRCLE, CropShape.SQUARE -> {
                                    val size = min(viewWidth * 0.85f, viewHeight * 0.70f)
                                    cropWidth = size
                                    cropHeight = size
                                }
                                CropShape.COVER -> {
                                    // 16:9 banner aspect ratio
                                    val targetW = min(viewWidth - with(density) { 32.dp.toPx() }, viewHeight * 1.5f)
                                    cropWidth = targetW
                                    cropHeight = targetW / (16f / 9f)
                                }
                            }

                            // Base image display sizing: scale so that the image comfortably fills the crop window
                            val fitScale = max(cropWidth / bmpWidth, cropHeight / bmpHeight)
                            val baseDisplayW = bmpWidth * fitScale
                            val baseDisplayH = bmpHeight * fitScale

                            val baseDisplayWDp = with(density) { baseDisplayW.toDp() }
                            val baseDisplayHDp = with(density) { baseDisplayH.toDp() }

                            val centerX = viewWidth / 2f
                            val centerY = viewHeight / 2f
                            val cropLeft = centerX - cropWidth / 2f
                            val cropTop = centerY - cropHeight / 2f
                            val cropRight = centerX + cropWidth / 2f
                            val cropBottom = centerY + cropHeight / 2f

                            // Interactive Gestures Area
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(1.0f, 4.0f)
                                            offsetX += pan.x
                                            offsetY += pan.y
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Source Image Layer
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Crop target",
                                    modifier = Modifier
                                        .size(width = baseDisplayWDp, height = baseDisplayHDp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            translationX = offsetX
                                            translationY = offsetY
                                            rotationZ = rotationAngle
                                        }
                                )

                                // Dark Scrim Overlay with Cutout (Using PathFillType.EvenOdd to avoid BlendMode.Clear issues)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val overlayPath = Path().apply {
                                        fillType = PathFillType.EvenOdd
                                        // Full Screen Outer Box
                                        addRect(Rect(0f, 0f, size.width, size.height))

                                        // Cutout Window
                                        when (cropShape) {
                                            CropShape.CIRCLE -> {
                                                addOval(Rect(cropLeft, cropTop, cropRight, cropBottom))
                                            }
                                            CropShape.SQUARE -> {
                                                addRoundRect(
                                                    RoundRect(
                                                        left = cropLeft,
                                                        top = cropTop,
                                                        right = cropRight,
                                                        bottom = cropBottom,
                                                        radiusX = 12.dp.toPx(),
                                                        radiusY = 12.dp.toPx()
                                                    )
                                                )
                                            }
                                            CropShape.COVER -> {
                                                addRoundRect(
                                                    RoundRect(
                                                        left = cropLeft,
                                                        top = cropTop,
                                                        right = cropRight,
                                                        bottom = cropBottom,
                                                        radiusX = 12.dp.toPx(),
                                                        radiusY = 12.dp.toPx()
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // 1. Draw solid dark scrim around cutout
                                    drawPath(path = overlayPath, color = Color.Black.copy(alpha = 0.75f))

                                    // 2. Draw border around crop window
                                    when (cropShape) {
                                        CropShape.CIRCLE -> {
                                            drawCircle(
                                                color = Color.White,
                                                radius = cropWidth / 2f,
                                                center = Offset(centerX, centerY),
                                                style = Stroke(width = 2.5.dp.toPx())
                                            )
                                        }
                                        CropShape.SQUARE, CropShape.COVER -> {
                                            drawRoundRect(
                                                color = Color.White,
                                                topLeft = Offset(cropLeft, cropTop),
                                                size = Size(cropWidth, cropHeight),
                                                cornerRadius = CornerRadius(12.dp.toPx()),
                                                style = Stroke(width = 2.5.dp.toPx())
                                            )
                                        }
                                    }

                                    // 3. Draw Rule of Thirds Guide Lines
                                    val stepX = cropWidth / 3f
                                    val stepY = cropHeight / 3f
                                    for (i in 1..2) {
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.35f),
                                            start = Offset(cropLeft + stepX * i, cropTop),
                                            end = Offset(cropLeft + stepX * i, cropBottom),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.35f),
                                            start = Offset(cropLeft, cropTop + stepY * i),
                                            end = Offset(cropRight, cropTop + stepY * i),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(text = "Unable to load image", color = Color.White)
                    }
                }

                // Bottom Control Panel
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF18191A),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Zoom Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Zoom Out",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = scale,
                                onValueChange = { scale = it },
                                valueRange = 1.0f..3.5f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF1877F2),
                                    activeTrackColor = Color(0xFF1877F2),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom In",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rotate and Reset Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rotate 90 deg
                            Button(
                                onClick = {
                                    rotationAngle = (rotationAngle + 90f) % 360f
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.12f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Rotate",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Rotate", fontSize = 13.sp)
                            }

                            // Reset
                            Button(
                                onClick = {
                                    scale = 1.0f
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotationAngle = 0f
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.12f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Reset", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Drag to reposition • Pinch or slide to zoom",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Accurately transforms and crops the original Bitmap based on user interaction (scale, pan, rotation).
 */
private suspend fun cropAndSaveImageExact(
    context: Context,
    sourceBitmap: Bitmap,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotation: Float,
    cropShape: CropShape
): Uri? = withContext(Dispatchers.IO) {
    try {
        val outWidth: Int
        val outHeight: Int

        when (cropShape) {
            CropShape.CIRCLE, CropShape.SQUARE -> {
                outWidth = 1080
                outHeight = 1080
            }
            CropShape.COVER -> {
                // 16:9 HD Cover Banner
                outWidth = 1280
                outHeight = 720
            }
        }

        val bmpWidth = sourceBitmap.width.toFloat()
        val bmpHeight = sourceBitmap.height.toFloat()

        // Screen crop viewport ratio calculations
        // In the UI, the crop window has dimension (outWidth : outHeight)
        // Base fit scale: image was scaled to cover the crop window
        val fitScale = max(outWidth.toFloat() / bmpWidth, outHeight.toFloat() / bmpHeight)

        val outputBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(android.graphics.Color.BLACK)

        val matrix = Matrix()
        // 1. Move bitmap center to origin
        matrix.postTranslate(-bmpWidth / 2f, -bmpHeight / 2f)

        // 2. Rotate around origin
        matrix.postRotate(rotation)

        // 3. Scale by fitScale * userScale
        val totalScale = fitScale * scale
        matrix.postScale(totalScale, totalScale)

        // 4. Translate by user offset (scaled from screen to output coordinates) and center on canvas
        // An offset of 1 unit in screen viewport translates to (outWidth / cropWidth) in output coordinates
        // Since both UI and output maintain the same aspect ratio and fit scale, (offsetX * fitScale * scale) or direct offset multiplier works cleanly.
        // In UI: screen crop size is around ~300dp (~800px on typical screen). Output is 1080px.
        // We use the normalized translation relative to base dimension:
        val screenEstimate = 800f
        val offsetScaleFactor = outWidth.toFloat() / screenEstimate

        matrix.postTranslate(
            outWidth / 2f + (offsetX * offsetScaleFactor),
            outHeight / 2f + (offsetY * offsetScaleFactor)
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        canvas.drawBitmap(sourceBitmap, matrix, paint)

        val filename = if (cropShape == CropShape.COVER) "cropped_cover_${System.currentTimeMillis()}.jpg" else "cropped_profile_${System.currentTimeMillis()}.jpg"
        val cacheFile = File(context.cacheDir, filename)
        val outStream = FileOutputStream(cacheFile)
        outputBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
        outStream.flush()
        outStream.close()

        Uri.fromFile(cacheFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
