package br.ufc.geonotas.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import java.io.FileNotFoundException
import kotlin.math.min

class Image {
    companion object{

        @Throws(IllegalArgumentException::class)
        fun cropToSquare(bitmap: Bitmap): Bitmap {
            val width = bitmap.width
            val height = bitmap.height
            val newWidth = min(height, width)
            val newHeight = if(height > width) height -(height - width) else height
            var cropWidth = (width - height) / 2
            cropWidth = if(cropWidth < 0) 0 else cropWidth
            var cropHeight = (height - width) / 2
            cropHeight = if(cropHeight < 0) 0 else cropHeight

            return Bitmap.createBitmap(bitmap, cropWidth, cropHeight, newWidth, newHeight)
        }


        @Throws(FileNotFoundException::class)
        fun fromUri(context: Context, uri: Uri): Bitmap{
            val inputStream = context.contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(inputStream)

        }

    }
}