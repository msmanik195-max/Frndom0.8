package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

@Composable
fun ImageCropDialog(
    imageUri: Uri,
    onCropSuccess: (Uri) -> Unit,
    onDismiss: () -> Unit,
    isCircular: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    var isProcessing by remember { mutableStateOf(false) }
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingBitmap by remember { mutableStateOf(true) }

    // Load original bitmap
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                var stream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(stream, null, options)
                stream?.close()

                // Calculate inSampleSize to prevent OutOfMemory on huge camera pictures
                var sampleSize = 1
                while (options.outWidth / sampleSize > 2048 || options.outHeight / sampleSize > 2048) {
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
                .background(Color.Black)
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
                        text = "Crop Profile Picture",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            if (isProcessing) return@Button
                            isProcessing = true
                            scope.launch {
                                val croppedUri = cropAndSaveImage(
                                    context = context,
                                    sourceBitmap = sourceBitmap,
                                    sourceUri = imageUri,
                                    scale = scale,
                                    offsetX = offsetX,
                                    offsetY = offsetY,
                                    rotation = rotationAngle,
                                    isCircular = isCircular
                                )
                                isProcessing = false
                                if (croppedUri != null) {
                                    onCropSuccess(croppedUri)
                                } else {
                                    Toast.makeText(context, "Failed to crop image, using original", Toast.LENGTH_SHORT).show()
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
                    } else {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val viewportSize = min(constraints.maxWidth, constraints.maxHeight)
                            val cropRadiusPx = (viewportSize * 0.40f)

                            // Interactive Gestures Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(0.8f, 4.0f)
                                            offsetX += pan.x
                                            offsetY += pan.y
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Image with transformations
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            translationX = offsetX
                                            translationY = offsetY
                                            rotationZ = rotationAngle
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (sourceBitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = sourceBitmap!!.asImageBitmap(),
                                            contentDescription = "Crop target",
                                            modifier = Modifier.size(280.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        AsyncImage(
                                            model = imageUri,
                                            contentDescription = "Crop target",
                                            modifier = Modifier.size(280.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                // Dark Overlay with Circular Cutout
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    val centerX = canvasWidth / 2f
                                    val centerY = canvasHeight / 2f

                                    // Draw transparent circular viewport overlay
                                    // 1. Surrounding Dark Mask
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.60f),
                                        size = size
                                    )

                                    // 2. Clear circular viewport inside
                                    if (isCircular) {
                                        drawCircle(
                                            color = Color.Transparent,
                                            radius = cropRadiusPx,
                                            center = Offset(centerX, centerY),
                                            blendMode = BlendMode.Clear
                                        )
                                        // White circle border
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.85f),
                                            radius = cropRadiusPx,
                                            center = Offset(centerX, centerY),
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    } else {
                                        drawRect(
                                            color = Color.Transparent,
                                            topLeft = Offset(centerX - cropRadiusPx, centerY - cropRadiusPx),
                                            size = Size(cropRadiusPx * 2, cropRadiusPx * 2),
                                            blendMode = BlendMode.Clear
                                        )
                                        drawRect(
                                            color = Color.White.copy(alpha = 0.85f),
                                            topLeft = Offset(centerX - cropRadiusPx, centerY - cropRadiusPx),
                                            size = Size(cropRadiusPx * 2, cropRadiusPx * 2),
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }
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

private suspend fun cropAndSaveImage(
    context: Context,
    sourceBitmap: Bitmap?,
    sourceUri: Uri,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotation: Float,
    isCircular: Boolean
): Uri? = withContext(Dispatchers.IO) {
    try {
        val original = sourceBitmap ?: run {
            var stream = context.contentResolver.openInputStream(sourceUri)
            val bmp = BitmapFactory.decodeStream(stream)
            stream?.close()
            bmp
        } ?: return@withContext null

        val targetSize = 600
        val outputBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        val matrix = Matrix()
        // Center image
        val srcWidth = original.width.toFloat()
        val srcHeight = original.height.toFloat()
        
        matrix.postTranslate(-srcWidth / 2f, -srcHeight / 2f)
        matrix.postRotate(rotation)
        matrix.postScale(scale, scale)
        matrix.postTranslate(
            targetSize / 2f + (offsetX * (targetSize / 280f)),
            targetSize / 2f + (offsetY * (targetSize / 280f))
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(original, matrix, paint)

        // If circular, crop to circle
        val finalResult = if (isCircular) {
            val circleBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
            val circleCanvas = Canvas(circleBitmap)
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            circleCanvas.drawCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, circlePaint)
            circlePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            circleCanvas.drawBitmap(outputBitmap, 0f, 0f, circlePaint)
            circleBitmap
        } else {
            outputBitmap
        }

        val cacheFile = File(context.cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg")
        val outStream = FileOutputStream(cacheFile)
        finalResult.compress(Bitmap.CompressFormat.JPEG, 92, outStream)
        outStream.flush()
        outStream.close()

        Uri.fromFile(cacheFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
