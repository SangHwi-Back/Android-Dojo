package com.example.moviceapp.repo

import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface MovieAppUserRepository {
    suspend fun getUser(token: String?): APIResult<UserEntity>
    suspend fun updateUser(token: String?, user: UserEntity): APIResult<UserEntity>
    suspend fun updateUserProfile(token: String?, profilePhoto: Bitmap): APIResult<UserEntity>
}

@Singleton
class MovieAppUserServiceImpl @Inject constructor(
    val service: MovieAppUserService
) : MovieAppUserRepository {
    override suspend fun getUser(token: String?): APIResult<UserEntity> =
        service.getUser(token).toAPIResult()

    override suspend fun updateUser(token: String?, user: UserEntity): APIResult<UserEntity> =
        service.updateUser(token, user).toAPIResult()

    override suspend fun updateUserProfile(token: String?, profilePhoto: Bitmap): APIResult<UserEntity> {
        val file = bitmapToFile(profilePhoto)
        val requestFile: RequestBody = file.asRequestBody("image/jpeg".toMediaType())
        val body: MultipartBody.Part = MultipartBody.Part.createFormData("image", file.name, requestFile)
        return service.updateUserProfile(token, body).toAPIResult()
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File.createTempFile("profile_", ".jpg")
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val fos = FileOutputStream(file)
        fos.write(stream.toByteArray())
        fos.flush()
        fos.close()

        return file
    }
}