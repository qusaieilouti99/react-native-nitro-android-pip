package com.margelo.nitro.nitroandroidpip

import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Rational
import androidx.annotation.RequiresApi
import com.margelo.nitro.NitroModules
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

object NitroAndroidPipManager : Application.ActivityLifecycleCallbacks {
    private const val TAG = "NitroAndroidPipManager"
    private const val ACTION_PIP_CONTROL_CLICKED = "com.margelo.nitro.nitroandroidpip.PIP_CONTROL_CLICKED"
    private const val EXTRA_CONTROL_ID = "control_id"
    private const val MIN_ASPECT_RATIO = 0.5f
    private const val MAX_ASPECT_RATIO = 2.39f

    enum class ActivityState {
        CREATED, STARTED, RESUMED, PAUSED, STOPPED, DESTROYED, UNKNOWN
    }

    // Stored state for the declarative API
    private var currentPipOptions: IPipOptions? = null
    private var currentPipActions: List<IPipAction> = emptyList()

    // Lifecycle and context properties
    private val context by lazy {
        NitroModules.applicationContext
            ?: throw IllegalStateException("NitroModules.applicationContext is null. Ensure Nitro is initialized.")
    }
    private var currentActivity: WeakReference<Activity>? = null
    private val activityState = AtomicReference(ActivityState.UNKNOWN)

    // Listener and state properties
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pipListener = AtomicReference<((Boolean) -> Unit)?>(null)
    private val isInPictureInPicture = AtomicReference(false)

    // Action-related properties
    private val actionCallbacks = ConcurrentHashMap<String, Func_void>()
    private val requestCodeGenerator = AtomicInteger(1000)
    private var isReceiverRegistered = false

    private val pipControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_PIP_CONTROL_CLICKED) {
                val controlId = intent.getStringExtra(EXTRA_CONTROL_ID)
                controlId?.let { id ->
                    try {
                        actionCallbacks[id]?.invoke()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error invoking action callback for $id", e)
                    }
                }
            }
        }
    }

    /**
     * One-time initialization. Call this from your Application.onCreate().
     */
    fun initialize(application: Application) {
        try {
            application.unregisterActivityLifecycleCallbacks(this) // Prevent double registration
            application.registerActivityLifecycleCallbacks(this)
            Log.d(TAG, "PiP Manager initialized and listening for Activity lifecycle events.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize PiP Manager", e)
            // Depending on severity, you might want to rethrow or disable PiP features here.
        }
    }

    // --- Public API ---

    fun setPipOptions(options: IPipOptions?, actions: List<IPipAction>) {
        try {
            currentPipOptions = options
            currentPipActions = actions
            Log.d(TAG, "PiP options have been set. Auto-enter: ${options?.autoEnterEnabled}")

            val activity = currentActivity?.get() ?: run {
                Log.w(TAG, "No current activity found to set PiP options. Options stored for later.")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // PiP is supported from Android O.
                val params = buildPipParams(currentPipOptions, currentPipActions)
                activity.setPictureInPictureParams(params)
                Log.d(TAG, "PiP params ${if (isPipActive()) "updated" else "primed"} for Android O+.")
            } else {
                Log.w(TAG, "Device does not support PiP (API < 26). Ignoring setPipOptions.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set PiP options due to an unexpected error.", e)
        }
    }

    fun startPip() {
        try {
            val activity = currentActivity?.get() ?: run {
                Log.e(TAG, "No current activity found to start PiP.")
                return
            }

            if (isPipActive()) {
                Log.w(TAG, "PiP is already active, ignoring startPip call.")
                return
            }
            if (!canEnterPip(activity)) {
                Log.e(TAG, "Activity must be in RESUMED state to enter picture-in-picture. Current state: ${activityState.get()}")
                return
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Log.e(TAG, "Device does not support PiP (API < 26). Cannot start PiP.")
                return
            }

            registerReceiver()
            val params = buildPipParams(currentPipOptions, currentPipActions)
            val success = activity.enterPictureInPictureMode(params)
            if (!success) {
                Log.e(TAG, "Failed to enter Picture-in-Picture mode.")
            } else {
                Log.d(TAG, "Successfully requested Picture-in-Picture mode.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred while trying to start PiP.", e)
        }
    }

    fun stopPip() {
        try {
            val activity = currentActivity?.get() ?: return
            if (isActivityAlive(activity)) {
                activity.moveTaskToFront()
                Log.d(TAG, "Moved activity to front to stop PiP.")
            } else {
                Log.d(TAG, "Activity not alive to move to front for stopping PiP.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop PiP/move activity to front.", e)
        }
    }

    fun isPipSupported(): Boolean {
        return try {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if PiP is supported.", e)
            false
        }
    }

    fun isPipActive(): Boolean {
        return isInPictureInPicture.get()
    }

    fun setListener(listener: ((Boolean) -> Unit)?) {
        pipListener.set(listener)
        listener?.let {
            try {
                mainHandler.post { it(isPipActive()) }
            } catch (e: Exception) {
                Log.e(TAG, "Error posting initial PiP state to listener.", e)
            }
        }
    }

    fun clearListener() {
        pipListener.set(null)
    }

    // --- Event Handlers (called from MainActivity) ---

    fun onUserLeaveHint() {
        Log.d(TAG, "User leave hint received. Current autoEnterEnabled state is: ${currentPipOptions?.autoEnterEnabled}")
        val wantsAutoEnter = currentPipOptions?.autoEnterEnabled ?: false
        if (wantsAutoEnter) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                Log.d(TAG, "Triggering auto-PiP for older Android version (pre-S).")
                if (!isPipActive()) {
                    startPip() // startPip has its own error handling
                }
            } else {
                // For S+ devices, auto-enter is handled by setPictureInPictureParams(setAutoEnterEnabled(true))
                Log.d(TAG, "Auto-PiP for Android S+ managed by PictureInPictureParams. No explicit startPip.")
            }
        }
    }

    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        val oldState = this.isInPictureInPicture.getAndSet(isInPictureInPictureMode)
        if (oldState != isInPictureInPictureMode) {
            Log.d(TAG, "PiP state changed: $isInPictureInPictureMode")
            try {
                pipListener.get()?.invoke(isInPictureInPictureMode)
            } catch (e: Exception) {
                Log.e(TAG, "Error invoking PiP listener callback.", e)
            }
        }
    }

    // --- ActivityLifecycleCallbacks Implementation ---
    // These are generally safe as they are framework callbacks, but wrapping state changes is harmless.

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = WeakReference(activity)
        onActivityStateChanged(ActivityState.CREATED)
    }

    override fun onActivityStarted(activity: Activity) {
        onActivityStateChanged(ActivityState.STARTED)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = WeakReference(activity)
        onActivityStateChanged(ActivityState.RESUMED)
    }

    override fun onActivityPaused(activity: Activity) {
        onActivityStateChanged(ActivityState.PAUSED)
    }

    override fun onActivityStopped(activity: Activity) {
        onActivityStateChanged(ActivityState.STOPPED)
    }

    override fun onActivityDestroyed(activity: Activity) {
        onActivityStateChanged(ActivityState.DESTROYED)
        if (currentActivity?.get() == activity) {
            currentActivity?.clear()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        /* Not needed */
    }

    // --- Private Helpers ---

    private fun onActivityStateChanged(state: ActivityState) {
        activityState.set(state)
        Log.v(TAG, "Activity state changed to: $state")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildPipParams(options: IPipOptions?, actions: List<IPipAction>): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder()

        try {
            options?.aspectRatio?.let {
                try {
                    validateAspectRatio(it.width, it.height)
                    builder.setAspectRatio(Rational(it.width.toInt(), it.height.toInt()))
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Invalid aspect ratio provided: ${it.width}:${it.height}. Omitting aspect ratio.", e)
                    // Continue without setting aspect ratio
                }
            }

            options?.sourceRectHint?.let {
                val rect = Rect(it.left.toInt(), it.top.toInt(), it.right.toInt(), it.bottom.toInt())
                builder.setSourceRectHint(rect)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val autoEnter = options?.autoEnterEnabled ?: false
                builder.setAutoEnterEnabled(autoEnter)
                builder.setSeamlessResizeEnabled(true)
            }

            if (actions.isNotEmpty()) {
                val remoteActions = buildRemoteActions(actions)
                if (remoteActions.isNotEmpty()) {
                    builder.setActions(remoteActions)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error building PictureInPictureParams. Returning default params.", e)
            // Fallback to a minimal params builder if something goes wrong during construction.
            // This might prevent a crash but PiP might not behave as expected.
            return PictureInPictureParams.Builder().build()
        }

        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildRemoteActions(actions: List<IPipAction>): List<RemoteAction> {
        val activity = currentActivity?.get() ?: return emptyList()
        val maxActions = activity.maxNumPictureInPictureActions

        if (actions.size > maxActions) {
            Log.w(TAG, "Too many actions provided (${actions.size}), using first $maxActions")
        }

        actionCallbacks.clear()

        return actions.take(maxActions).mapNotNull { action ->
            try {
                val iconResourceId = getDrawableResourceId(action.iconResourceName)
                if (iconResourceId == 0) {
                    Log.e(TAG, "Icon resource '${action.iconResourceName}' not found for action ${action.id}. Skipping.")
                    null
                } else {
                    actionCallbacks[action.id] = action.onPress

                    val intent = Intent(ACTION_PIP_CONTROL_CLICKED).apply {
                        setPackage(context.packageName)
                        putExtra(EXTRA_CONTROL_ID, action.id)
                    }

                    val requestCode = requestCodeGenerator.getAndIncrement()
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, requestCode, intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val icon = Icon.createWithResource(context, iconResourceId)
                    RemoteAction(icon, action.title, action.contentDescription, pendingIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating remote action for ${action.id}", e)
                null // Skip this problematic action
            }
        }
    }

    private fun validateAspectRatio(width: Double, height: Double) {
        if (width <= 0 || height <= 0) throw IllegalArgumentException("Aspect ratio dimensions must be positive (got $width:$height)")
        val ratio = width / height
        if (ratio < MIN_ASPECT_RATIO || ratio > MAX_ASPECT_RATIO) {
            throw IllegalArgumentException("Aspect ratio $ratio is outside allowed range ($MIN_ASPECT_RATIO - $MAX_ASPECT_RATIO) for dimensions $width:$height")
        }
    }

    private fun getDrawableResourceId(name: String): Int = try {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    } catch (e: Exception) {
        Log.e(TAG, "Error getting drawable resource ID for '$name'.", e)
        0
    }

    private fun canEnterPip(activity: Activity): Boolean = activityState.get() == ActivityState.RESUMED && isActivityAlive(activity)
    private fun isActivityAlive(activity: Activity): Boolean = !activity.isFinishing && !activity.isDestroyed

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            try {
                val intentFilter = IntentFilter(ACTION_PIP_CONTROL_CLICKED)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(pipControlReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    context.registerReceiver(pipControlReceiver, intentFilter)
                }
                isReceiverRegistered = true
                Log.d(TAG, "PiP control receiver registered.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register PiP control receiver.", e)
            }
        }
    }

    private fun Activity.moveTaskToFront() {
        try {
            val intent = Intent(context, this::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to move activity to front: ${this::class.java.simpleName}", e)
        }
    }
}
