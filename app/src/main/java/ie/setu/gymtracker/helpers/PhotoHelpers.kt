package ie.setu.gymtracker.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File

// Small helpers for the progress photos
object PhotoHelpers {

    // Makes a new empty file for the camera to save a photo into.
    // It lives in the app's private storage, in a "photos" folder
    fun newPhotoFile(context: Context): File {
        val folder = File(context.filesDir, "photos")
        folder.mkdirs()
        return File(folder, "workout_${System.currentTimeMillis()}.jpg")
    }

    // Turns a file into a content:// address the camera app is allowed to write to.
    // "fileprovider" must match android:authorities in the Manifest
    fun uriFor(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // Loads a photo, shrunk so its longest side is about maxSizePx.
    // A real camera photo can be 4000 pixels wide - far too big for a small box.
    // Returns null if the photo can't be read (e.g. it was deleted)
    fun loadBitmap(context: Context, uri: Uri, maxSizePx: Int): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val biggestSide = maxOf(info.size.width, info.size.height)
                if (biggestSide > maxSizePx) {
                    decoder.setTargetSampleSize(biggestSide / maxSizePx)   // e.g. 4 = a quarter of the size
                }
            }
        } catch (e: Exception) {
            Timber.w("Couldn't load photo $uri: ${e.message}")
            null
        }
    }

    // Shows a photo in an ImageView, or a camera icon if there's no photo
    fun showPhoto(view: ImageView, uri: String?, maxSizePx: Int = 600) {
        val bitmap = uri?.let { loadBitmap(view.context, Uri.parse(it), maxSizePx) }
        if (bitmap != null) {
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.setImageBitmap(bitmap)
        } else {
            view.scaleType = ImageView.ScaleType.CENTER
            view.setImageResource(android.R.drawable.ic_menu_camera)
        }
    }
}
