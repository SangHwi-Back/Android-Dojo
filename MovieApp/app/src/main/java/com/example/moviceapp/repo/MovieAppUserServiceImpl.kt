package com.example.moviceapp.repo

import android.graphics.Bitmap
import com.example.moviceapp.AppException.AuthException
import com.example.moviceapp.myinfo.getAuthToken
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
    suspend fun getUser(): APIResult<UserEntity>
    suspend fun updateUser(user: UserEntity): APIResult<UserEntity>
    suspend fun updateUserProfile(profilePhoto: Bitmap): APIResult<UserEntity>
}

@Singleton
class MovieAppUserServiceImpl @Inject constructor(
    val service: MovieAppUserService
) : MovieAppUserRepository {
    override suspend fun getUser(): APIResult<UserEntity> {
        val token = getAuthToken() ?: return APIResult.Failure(AuthException.NotAuthenticated())
        return service.getUser(token).toAPIResult()
    }

    override suspend fun updateUser(user: UserEntity): APIResult<UserEntity> {
        val token = getAuthToken() ?: return APIResult.Failure(AuthException.NotAuthenticated())
        return service.updateUser(token, user).toAPIResult()
    }

    override suspend fun updateUserProfile(profilePhoto: Bitmap): APIResult<UserEntity> {
        val token = getAuthToken() ?: return APIResult.Failure(AuthException.NotAuthenticated())
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