package com.example.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Memory-bounded in-process cache for downsampled user profile photos.
 * Prevents repeated disk/ContentResolver decoding and avoids OutOfMemoryError on low-end devices.
 */
private object AvatarBitmapCache {
    private val cache = LruCache<String, ImageBitmap>(10)

    fun get(key: String): ImageBitmap? = cache.get(key)

    fun put(key: String, bitmap: ImageBitmap) {
        cache.put(key, bitmap)
    }
}

private data class AvatarPalette(
    val bgStart: Color,
    val bgEnd: Color,
    val skinOrFur: Color,
    val hairOrAccent: Color,
    val shirtColor: Color,
    val styleIndex: Int
)

private val PRESET_PALETTES = listOf(
    // 0: Alex - Warm Terracotta Explorer
    AvatarPalette(
        bgStart = Color(0xFFFFCCBC),
        bgEnd = Color(0xFFFF8A65),
        skinOrFur = Color(0xFFF5CBA7),
        hairOrAccent = Color(0xFF4E342E),
        shirtColor = Color(0xFFD84315),
        styleIndex = 0
    ),
    // 1: Felix - Teal Scholar with Glasses
    AvatarPalette(
        bgStart = Color(0xFFB2DFDB),
        bgEnd = Color(0xFF4DB6AC),
        skinOrFur = Color(0xFFFAD7A0),
        hairOrAccent = Color(0xFF263238),
        shirtColor = Color(0xFF00695C),
        styleIndex = 1
    ),
    // 2: Spike - Friendly Bot Reader
    AvatarPalette(
        bgStart = Color(0xFFD1C4E9),
        bgEnd = Color(0xFF9575CD),
        skinOrFur = Color(0xFFECEFF1),
        hairOrAccent = Color(0xFFFF6D00),
        shirtColor = Color(0xFF4527A0),
        styleIndex = 2
    ),
    // 3: Milo - Sunny Gold Adventurer
    AvatarPalette(
        bgStart = Color(0xFFFFF59D),
        bgEnd = Color(0xFFFFB74D),
        skinOrFur = Color(0xFFEDBB99),
        hairOrAccent = Color(0xFF6D4C41),
        shirtColor = Color(0xFFE65100),
        styleIndex = 0
    ),
    // 4: Zoe - Emerald Sage
    AvatarPalette(
        bgStart = Color(0xFFC8E6C9),
        bgEnd = Color(0xFF66BB6A),
        skinOrFur = Color(0xFFF5CBA7),
        hairOrAccent = Color(0xFF880E4F),
        shirtColor = Color(0xFF1B5E20),
        styleIndex = 1
    ),
    // 5: Luna - Night Sky Mystic
    AvatarPalette(
        bgStart = Color(0xFFC5CAE9),
        bgEnd = Color(0xFF5C6BC0),
        skinOrFur = Color(0xFFFDEBD0),
        hairOrAccent = Color(0xFF1A237E),
        shirtColor = Color(0xFF283593),
        styleIndex = 0
    ),
    // 6: Nova - Coral Flamekeeper
    AvatarPalette(
        bgStart = Color(0xFFFFCDD2),
        bgEnd = Color(0xFFEF5350),
        skinOrFur = Color(0xFFF5B7B1),
        hairOrAccent = Color(0xFF3E2723),
        shirtColor = Color(0xFFB71C1C),
        styleIndex = 1
    ),
    // 7: Leo - Sky Blue Captain
    AvatarPalette(
        bgStart = Color(0xFFBBDEFB),
        bgEnd = Color(0xFF42A5F5),
        skinOrFur = Color(0xFFE59866),
        hairOrAccent = Color(0xFF212121),
        shirtColor = Color(0xFF0D47A1),
        styleIndex = 2
    ),
    // 8: Cleo - Amber Crown Reader
    AvatarPalette(
        bgStart = Color(0xFFFFE082),
        bgEnd = Color(0xFFFFCA28),
        skinOrFur = Color(0xFFDC7633),
        hairOrAccent = Color(0xFF1B1B1B),
        shirtColor = Color(0xFFAB3500),
        styleIndex = 0
    )
)

/**
 * Lightweight, offline-first avatar renderer.
 * - Decodes local gallery/file URIs off the UI thread with downsampling (`inSampleSize`) and LruCache.
 * - Renders deterministic, zero-allocation vector/canvas character illustrations for preset/seed URLs
 *   with zero network requests and instant 60fps rendering.
 */
@Composable
fun LocalAvatarImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val uriStr = model?.trim().orEmpty()
    val isLocalUri = uriStr.startsWith("content://") || uriStr.startsWith("file://")

    var decodedBitmap by remember(uriStr) {
        mutableStateOf(if (isLocalUri) AvatarBitmapCache.get(uriStr) else null)
    }

    LaunchedEffect(uriStr, isLocalUri) {
        if (isLocalUri && decodedBitmap == null) {
            val loaded = withContext(Dispatchers.IO) {
                decodeDownsampledBitmap(context, Uri.parse(uriStr), reqWidth = 256, reqHeight = 256)
            }
            if (loaded != null) {
                AvatarBitmapCache.put(uriStr, loaded)
                decodedBitmap = loaded
            }
        }
    }

    val currentBitmap = decodedBitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        val palette = remember(uriStr) { resolvePaletteForModel(uriStr) }
        val smilePath = remember { Path() }
        val semanticsModifier = if (contentDescription != null) {
            Modifier.semantics {
                this.contentDescription = contentDescription
                this.role = Role.Image
            }
        } else {
            Modifier
        }

        Box(
            modifier = modifier
                .then(semanticsModifier)
                .background(
                    Brush.linearGradient(
                        colors = listOf(palette.bgStart, palette.bgEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w * 0.5f
                val cy = h * 0.46f

                // Shoulders / Book Reader Torso
                drawArc(
                    color = palette.shirtColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.16f, h * 0.66f),
                    size = Size(w * 0.68f, h * 0.52f)
                )

                if (palette.styleIndex == 2) {
                    // Friendly Robot Reader Style
                    val headW = w * 0.46f
                    val headH = h * 0.38f
                    // Antenna
                    drawLine(
                        color = palette.hairOrAccent,
                        strokeWidth = w * 0.04f,
                        start = Offset(cx, cy - headH * 0.5f),
                        end = Offset(cx, cy - headH * 0.78f)
                    )
                    drawCircle(
                        color = palette.hairOrAccent,
                        radius = w * 0.055f,
                        center = Offset(cx, cy - headH * 0.8f)
                    )
                    // Robot Head
                    drawRoundRect(
                        color = palette.skinOrFur,
                        topLeft = Offset(cx - headW / 2f, cy - headH / 2f),
                        size = Size(headW, headH),
                        cornerRadius = CornerRadius(w * 0.1f, w * 0.1f)
                    )
                    // Visor
                    drawRoundRect(
                        color = palette.shirtColor,
                        topLeft = Offset(cx - headW * 0.36f, cy - headH * 0.18f),
                        size = Size(headW * 0.72f, headH * 0.28f),
                        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
                    )
                    // Glowing Eyes
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = w * 0.038f,
                        center = Offset(cx - headW * 0.16f, cy - headH * 0.04f)
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = w * 0.038f,
                        center = Offset(cx + headW * 0.16f, cy - headH * 0.04f)
                    )
                } else {
                    val headRadius = w * 0.23f
                    // Back Hair / Crown
                    drawCircle(
                        color = palette.hairOrAccent,
                        radius = headRadius * 1.12f,
                        center = Offset(cx, cy - headRadius * 0.14f)
                    )
                    // Face Circle
                    drawCircle(
                        color = palette.skinOrFur,
                        radius = headRadius,
                        center = Offset(cx, cy)
                    )
                    // Top Hair Swoop
                    drawArc(
                        color = palette.hairOrAccent,
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = true,
                        topLeft = Offset(cx - headRadius * 1.05f, cy - headRadius * 1.08f),
                        size = Size(headRadius * 2.1f, headRadius * 1.2f)
                    )
                    // Eyes
                    val eyeY = cy - headRadius * 0.05f
                    val eyeOffset = headRadius * 0.38f
                    drawCircle(
                        color = Color(0xFF1F1D1C),
                        radius = w * 0.028f,
                        center = Offset(cx - eyeOffset, eyeY)
                    )
                    drawCircle(
                        color = Color(0xFF1F1D1C),
                        radius = w * 0.028f,
                        center = Offset(cx + eyeOffset, eyeY)
                    )
                    // Optional Reader Glasses for styleIndex == 1
                    if (palette.styleIndex == 1) {
                        val stroke = Stroke(width = w * 0.022f)
                        drawCircle(
                            color = palette.hairOrAccent,
                            radius = w * 0.068f,
                            center = Offset(cx - eyeOffset, eyeY),
                            style = stroke
                        )
                        drawCircle(
                            color = palette.hairOrAccent,
                            radius = w * 0.068f,
                            center = Offset(cx + eyeOffset, eyeY),
                            style = stroke
                        )
                        drawLine(
                            color = palette.hairOrAccent,
                            start = Offset(cx - eyeOffset + w * 0.068f, eyeY),
                            end = Offset(cx + eyeOffset - w * 0.068f, eyeY),
                            strokeWidth = w * 0.02f
                        )
                    }
                    // Warm Smile
                    smilePath.reset()
                    smilePath.moveTo(cx - headRadius * 0.28f, cy + headRadius * 0.32f)
                    smilePath.quadraticTo(
                        cx,
                        cy + headRadius * 0.56f,
                        cx + headRadius * 0.28f,
                        cy + headRadius * 0.32f
                    )
                    drawPath(
                        path = smilePath,
                        color = Color(0xFF4E342E),
                        style = Stroke(width = w * 0.026f)
                    )
                }
            }
        }
    }
}

private fun resolvePaletteForModel(uriStr: String): AvatarPalette {
    val seed = when {
        uriStr.contains("seed=Alex", ignoreCase = true) -> 0
        uriStr.contains("seed=Felix", ignoreCase = true) -> 1
        uriStr.contains("seed=Spike", ignoreCase = true) -> 2
        uriStr.contains("seed=Milo", ignoreCase = true) -> 3
        uriStr.contains("seed=Zoe", ignoreCase = true) -> 4
        uriStr.contains("seed=Luna", ignoreCase = true) -> 5
        uriStr.contains("seed=Nova", ignoreCase = true) -> 6
        uriStr.contains("seed=Leo", ignoreCase = true) -> 7
        uriStr.contains("seed=Cleo", ignoreCase = true) -> 8
        uriStr.isNotEmpty() -> abs(uriStr.hashCode()) % PRESET_PALETTES.size
        else -> 0
    }
    return PRESET_PALETTES[seed]
}

private fun decodeDownsampledBitmap(
    context: Context,
    uri: Uri,
    reqWidth: Int,
    reqHeight: Int
): ImageBitmap? {
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
