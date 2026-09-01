package com.example.gallery

import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.example.model.VideoRecording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class GalleryRepository(private val context: Context) {

    suspend fun getRecordedVideos(): List<VideoRecording> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoRecording>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val widthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)

                var count = 0
                while (cursor.moveToNext() && count < 100) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Video_$id.mp4"
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateColumn)
                    val width = if (widthColumn >= 0) cursor.getInt(widthColumn) else 0
                    val height = if (heightColumn >= 0) cursor.getInt(heightColumn) else 0

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    videos.add(
                        VideoRecording(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAddedSec = dateAdded,
                            width = width,
                            height = height
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        videos
    }

    suspend fun deleteVideo(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun scanMediaFile(path: String, onScanned: ((Uri?) -> Unit)? = null) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(path),
            arrayOf("video/mp4")
        ) { _, uri ->
            onScanned?.invoke(uri)
        }
    }

    fun getAvailableStorageMb(): Long {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val availableBlocks = stat.availableBlocksLong
            val blockSize = stat.blockSizeLong
            (availableBlocks * blockSize) / (1024 * 1024)
        } catch (e: Exception) {
            1024L
        }
    }
}
