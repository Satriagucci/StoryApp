package com.example.mystorysubmission.helper

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.mystorysubmission.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String =
    SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

fun createCustomTempFile(context: Context): File = File.createTempFile(
    timeStamp,
    ".jpg",
    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
)

fun createFile(application: Application): File {
    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir

    return File(outputDirectory, "$timeStamp.jpg")
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val myFile = createCustomTempFile(context)
    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val orientation = getOrientation(contentResolver, selectedImg)
    val rotatedBitmap = rotateBitmap(inputStream, orientation)
    saveBitmapToFile(rotatedBitmap, myFile)

    inputStream.close()

    return myFile
}

private fun getOrientation(contentResolver: ContentResolver, uri: Uri): Int {
    val cursor = contentResolver.query(
        uri, arrayOf(MediaStore.Images.ImageColumns.ORIENTATION),
        null, null, null
    )
    cursor?.use {
        if (it.moveToFirst()) {
            return it.getInt(0)
        }
    }
    return 0
}

private fun rotateBitmap(inputStream: InputStream, orientation: Int): Bitmap {
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val matrix = Matrix()
    matrix.postRotate(orientation.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()
}

fun reduceFileImage(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int

    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)

        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > 1000000)

    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}