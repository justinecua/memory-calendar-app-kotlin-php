package com.example.sample.com.example.sample.http

import android.util.Log
import com.example.sample.com.example.sample.utils.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object ApiService {

    fun register(username: String, email: String, password: String, callback: (String?) -> Unit) {
        val url = URL("${BASE_URL}register.php")
        val json = """
        {
            "username": "$username",
            "email": "$email",
            "password": "$password"
        }
    """.trimIndent()

        Thread {
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    outputStream.write(json.toByteArray(Charsets.UTF_8))

                    val responseCode = responseCode
                    val stream = if (responseCode in 200..299) inputStream else errorStream
                    val response = stream.bufferedReader().use { it.readText() }

                    callback(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback("Network error: ${e.localizedMessage}")
            }
        }.start()
    }



    fun login(email: String, password: String, callback: (String?) -> Unit) {
        val url = URL("${BASE_URL}login.php")
        val json = """
        {
            "email": "$email",
            "password": "$password"
        }
    """.trimIndent()

        Thread {
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    outputStream.write(json.toByteArray(Charsets.UTF_8))

                    val responseCode = responseCode
                    val stream = if (responseCode in 200..299) inputStream else errorStream
                    val response = stream.bufferedReader().use { it.readText() }
                    callback(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }

    fun uploadImage(
        userId: Int,
        imageBytes: ByteArray,
        imageFileName: String,
        memoryDate: String,
        description: String?,
        callback: (String?) -> Unit
    ) {
        val url = URL("${BASE_URL}upload.php")
        val boundary = "Boundary-${UUID.randomUUID()}"
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        Thread {
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    setRequestProperty("Connection", "Keep-Alive")
                    setRequestProperty("Cache-Control", "no-cache")
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                    val outputStream = DataOutputStream(outputStream)

                    //user_id
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"user_id\"$lineEnd$lineEnd")
                    outputStream.writeBytes(userId.toString() + lineEnd)

                    //memory_date
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"memory_date\"$lineEnd$lineEnd")
                    outputStream.writeBytes(memoryDate + lineEnd)

                    //description
                    description?.let {
                        outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                        outputStream.writeBytes("Content-Disposition: form-data; name=\"description\"$lineEnd$lineEnd")
                        outputStream.writeBytes(it + lineEnd)
                    }

                    //file
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                    outputStream.writeBytes(
                        "Content-Disposition: form-data; name=\"image\"; filename=\"$imageFileName\"$lineEnd"
                    )
                    outputStream.writeBytes("Content-Type: image/jpeg$lineEnd")
                    outputStream.writeBytes(lineEnd)

                    // Write image bytes
                    outputStream.write(imageBytes)
                    outputStream.writeBytes(lineEnd)

                    outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                    outputStream.flush()
                    outputStream.close()

                    val responseCode = responseCode
                    val inputStream = if (responseCode in 200..299) inputStream else errorStream
                    val response = inputStream.bufferedReader().use { it.readText() }

                    callback(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }


    fun fetchMemories(userId: Int, callback: (String?) -> Unit) {
        val url = URL("${BASE_URL}fetch_memories_by_id.php?userId=$userId")

        Thread {
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    val responseCode = responseCode
                    val stream = if (responseCode in 200..299) inputStream else errorStream
                    val response = stream.bufferedReader().use { it.readText() }
                    callback(response)
                    Log.d("Memories", "Memories: $response")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }



}


