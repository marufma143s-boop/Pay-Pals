package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    private const val TAG = "ImageUtils"

    fun uriToBase64(context: Context, uri: Uri, maxDimension: Int = 400, quality: Int = 75): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return null

            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                if (ratio > 1) {
                    val targetW = maxDimension
                    val targetH = (maxDimension / ratio).toInt()
                    Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
                } else {
                    val targetH = maxDimension
                    val targetW = (maxDimension * ratio).toInt()
                    Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
                }
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scale.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "uriToBase64 error: ${e.message}", e)
            null
        }
    }

    fun base64ToBitmap(base64Str: String): Bitmap? {
        if (base64Str.isBlank()) return null
        return try {
            val cleanBase64 = if (base64Str.contains(",")) {
                base64Str.substringAfter(",")
            } else {
                base64Str
            }
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "base64ToBitmap error: ${e.message}")
            null
        }
    }
}

@Composable
fun Base64OrResourceImage(
    base64Str: String?,
    placeholderRes: Int = R.drawable.img_avatar_maruf_1787554123074,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap = remember(base64Str) {
        if (!base64Str.isNullOrBlank()) {
            ImageUtils.base64ToBitmap(base64Str)
        } else null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Image(
            painter = painterResource(id = placeholderRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

object AudioRecordingManager {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTimeMillis: Long = 0L

    val isRecording: Boolean get() = mediaRecorder != null

    fun startRecording(context: Context): Boolean {
        return try {
            cancelRecording()
            val tempDir = context.cacheDir
            val audioFile = File.createTempFile("voice_${System.currentTimeMillis()}_", ".m4a", tempDir)
            currentOutputFile = audioFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            startTimeMillis = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "startRecording error: ${e.message}", e)
            cleanup()
            false
        }
    }

    fun stopRecording(): Pair<String, Int>? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            val durationSeconds = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt().coerceAtLeast(1)
            val file = currentOutputFile
            if (file != null && file.exists() && file.length() > 0) {
                val bytes = file.readBytes()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                Pair(base64, durationSeconds)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "stopRecording error: ${e.message}", e)
            null
        } finally {
            cleanup()
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "cancelRecording error: ${e.message}")
        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        mediaRecorder = null
        try {
            currentOutputFile?.delete()
        } catch (e: Exception) {
            // ignore
        }
        currentOutputFile = null
    }
}

object AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlayingMessageId: String? = null

    fun playBase64Audio(
        context: Context,
        messageId: String,
        base64Audio: String,
        onCompletion: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        stop()
        try {
            val bytes = Base64.decode(base64Audio, Base64.DEFAULT)
            val tempFile = File.createTempFile("play_${messageId}_", ".m4a", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(bytes) }

            val player = MediaPlayer()
            player.setDataSource(tempFile.absolutePath)
            player.prepare()
            player.setOnCompletionListener {
                currentlyPlayingMessageId = null
                tempFile.delete()
                onCompletion()
            }
            player.setOnErrorListener { _, _, _ ->
                currentlyPlayingMessageId = null
                tempFile.delete()
                onError()
                true
            }
            player.start()
            mediaPlayer = player
            currentlyPlayingMessageId = messageId
        } catch (e: Exception) {
            Log.e("AudioPlayer", "playBase64Audio error: ${e.message}", e)
            currentlyPlayingMessageId = null
            onError()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "stop error: ${e.message}")
        } finally {
            mediaPlayer = null
            currentlyPlayingMessageId = null
        }
    }

    fun isPlaying(messageId: String): Boolean {
        return currentlyPlayingMessageId == messageId && mediaPlayer?.isPlaying == true
    }

    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
