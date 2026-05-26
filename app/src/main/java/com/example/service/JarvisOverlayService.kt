package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class JarvisOverlayService : Service(), LifecycleOwner {

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private lateinit var params: WindowManager.LayoutParams
    
    // Core custom Lifecycle Management for running Compose in Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@JarvisOverlayService)
            
            val store = ViewModelStore()
            setViewTreeViewModelStoreOwner(object : androidx.lifecycle.ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = store
            })

            setViewTreeSavedStateRegistryOwner(MySavedStateRegistryOwner(this@JarvisOverlayService))
            
            setContent {
                JarvisOverlayHUD()
            }
        }

        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    fun JarvisOverlayHUD() {
        var isExpanded by remember { mutableStateOf(false) }
        var offsetX by remember { mutableStateOf(100f) }
        var offsetY by remember { mutableStateOf(200f) }

        val infiniteTransition = rememberInfiniteTransition(label = "reactor")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        Box(
            modifier = Modifier
                .wrapContentSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            params.x = offsetX.roundToInt()
                            params.y = offsetY.roundToInt()
                            try {
                                windowManager.updateViewLayout(composeView, params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            params.x = offsetX.roundToInt()
                            params.y = offsetY.roundToInt()
                            try {
                                windowManager.updateViewLayout(composeView, params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.wrapContentSize()
            ) {
                // Expanding panel with diagnostic links
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xEC0D1117)
                        ),
                        modifier = Modifier
                            .width(240.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AUXILIARY HUD v3.5",
                                    fontSize = 11.sp,
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { isExpanded = false },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White.copy(0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(0.05f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "STARK MAIN LINK: ON",
                                        fontSize = 10.sp,
                                        color = Color(0xFF00FF88),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "COGNITIVE MODULES: ARMED",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(0.8f),
                                        fontWeight = FontWeight.Light
                                    )
                                    Text(
                                        text = "TIME: " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                                        fontSize = 10.sp,
                                        color = Color.White.copy(0.5f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        val launchIntent = this@JarvisOverlayService.packageManager
                                            .getLaunchIntentForPackage(this@JarvisOverlayService.packageName)
                                        launchIntent?.let {
                                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            this@JarvisOverlayService.startActivity(it)
                                        }
                                        isExpanded = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x3300E5FF)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = "Open App",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("OPEN", fontSize = 9.sp, color = Color(0xFF00E5FF))
                                }

                                Button(
                                    onClick = {
                                        this@JarvisOverlayService.stopSelf()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x33FF3D00)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Kill HUD",
                                        tint = Color(0xFFFF3D00),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SHUT", fontSize = 9.sp, color = Color(0xFFFF3D00))
                                }
                            }
                        }
                    }
                }

                // Core Draggable Orb Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x3A00E5FF), Color(0xBC001222))
                            )
                        )
                        .clickable { isExpanded = !isExpanded }
                        .padding(4.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotation)
                    ) {
                        val width = size.width
                        val height = size.height
                        val strokeAmt = Stroke(width = 2.dp.toPx())

                        drawCircle(
                            color = Color(0xFF00E5FF),
                            style = strokeAmt
                        )

                        for (i in 0..360 step 60) {
                            val xAngle = Math.cos(Math.toRadians(i.toDouble())).toFloat()
                            val yAngle = Math.sin(Math.toRadians(i.toDouble())).toFloat()
                            drawLine(
                                color = Color(0xFF00E5FF).copy(0.6f),
                                start = Offset(width/2, height/2),
                                end = Offset(width/2 + xAngle*(width/2.5f), height/2 + yAngle*(height/2.5f)),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }

                        drawCircle(
                            color = Color(0xFF00E5FF),
                            radius = width / 4.5f,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.White, Color(0xFF00E5FF))
                                )
                            )
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onDestroy()
    }
}
