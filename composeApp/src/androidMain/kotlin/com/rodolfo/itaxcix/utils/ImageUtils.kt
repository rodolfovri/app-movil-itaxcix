package com.rodolfo.itaxcix.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    // ✅ NUEVA FUNCIÓN: Maneja URIs directamente
    /**
     * Convierte un URI de contenido a archivo temporal
     * @param context Contexto de la aplicación
     * @param uri URI del archivo seleccionado
     * @return File temporal creado desde el URI
     */
    suspend fun createTempFileFromUri(context: Context, uri: Uri): File {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("No se puede abrir el archivo seleccionado")

            // Crear archivo temporal con extensión apropiada
            val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        }
    }

    // ✅ FUNCIÓN DE CONVENIENCIA: URI directo a Base64
    /**
     * Convierte un URI directamente a Base64 comprimido
     * @param context Contexto de la aplicación
     * @param uri URI del archivo
     * @param maxDimension Dimensión máxima en píxeles
     * @param quality Calidad de compresión (0-100)
     * @return String Base64
     */
    suspend fun uriToCompressedBase64(
        context: Context,
        uri: Uri,
        maxDimension: Int = 500,
        quality: Int = 70
    ): String {
        val tempFile = createTempFileFromUri(context, uri)
        return try {
            tempFile.compressAndConvertToBase64(maxDimension, quality)
        } finally {
            // Limpiar archivo temporal
            tempFile.delete()
        }
    }

    /**
     * Decodifica una cadena Base64 a un objeto Bitmap
     * @param base64Image Cadena en formato Base64 (puede incluir prefijo data:image)
     * @return Bitmap decodificado o null si ocurre un error
     */
    fun decodeBase64ToBitmap(base64Image: String): Bitmap? {
        return try {
            val imageData = if (base64Image.contains("base64,")) {
                base64Image.substringAfter("base64,")
            } else {
                base64Image
            }
            val imageBytes = Base64.decode(imageData, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}