package com.rodolfo.itaxcix.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import okio.IOException
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtils {
    /**
     * Convierte un archivo de imagen a string en formato Base64
     */
    fun File.toBase64String(): String {
        return try {
            val bytes = this.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: IOException) {
            throw IOException("Error al convertir imagen a Base64: ${e.localizedMessage}")
        }
    }

    /**
     * Comprime una imagen y la convierte a Base64
     * @param maxDimension Dimensión máxima (ancho o alto) en píxeles
     * @param quality Calidad de compresión JPEG (0-100)
     * @return String en formato Base64
     */
    fun File.compressAndConvertToBase64(maxDimension: Int = 500, quality: Int = 70): String {
        // Carga la imagen como bitmap (solo dimensiones)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(this.absolutePath, options)

        // Calcula factor de escala para reducir resolución
        var scaleFactor = 1
        if (options.outWidth > maxDimension || options.outHeight > maxDimension) {
            scaleFactor = Math.max(
                options.outWidth / maxDimension,
                options.outHeight / maxDimension
            )
        }

        // Configura opciones para cargar bitmap reducido
        options.apply {
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }

        // Carga el bitmap con resolución reducida
        val bitmap = BitmapFactory.decodeFile(this.absolutePath, options)

        // Comprime el bitmap a JPEG
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // Convierte a Base64
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Comprime una imagen y la convierte a formato "data:image/jpeg;base64,..."
     */
    fun File.compressToBase64DataUrl(maxDimension: Int = 500, quality: Int = 70): String {
        val base64 = compressAndConvertToBase64(maxDimension, quality)
        return "data:image/jpeg;base64,$base64"
    }
}