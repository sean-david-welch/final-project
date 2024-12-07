package com.budgetai.lib

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import aws.smithy.kotlin.runtime.content.toByteArray
import io.ktor.server.config.*
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class S3Handler(config: ApplicationConfig) {
    private val bucketName: String = "budgetai.ie"
    private val s3Client = S3Client {
        region = "eu-west-1"
        credentialsProvider = StaticCredentialsProvider {
            accessKeyId = config.property("api-keys.aws_access").getString()
            secretAccessKey = config.property("api-keys.aws_secret").getString()
        }
    }

    // upload file to s3
    suspend fun uploadFile(file: File, key: String): Boolean {
        return try {
            val request = PutObjectRequest {
                bucket = bucketName
                this.key = key
                body = ByteStream.fromFile(file)
            }
            s3Client.putObject(request)
            true
        } catch (e: Exception) {
            println("Error uploading file: ${e.message}")
            false
        }
    }

    // upload byte array / image to s3
    suspend fun uploadBytes(bytes: ByteArray, key: String): Boolean {
        return try {
            val request = PutObjectRequest {
                bucket = bucketName
                this.key = key
                body = ByteStream.fromBytes(bytes)
            }
            s3Client.putObject(request)
            true
        } catch (e: Exception) {
            println("Error uploading bytes: ${e.message}")
            false
        }
    }

    // get file by key
    fun downloadFile(key: String): Flow<ByteArray> = flow {
        try {
            val request = GetObjectRequest {
                bucket = bucketName
                this.key = key
            }

            s3Client.getObject(request) { response ->
                response.body?.toByteArray()?.let { bytes ->
                    emit(bytes)
                }
            }
        } catch (e: Exception) {
            println("Error downloading file: ${e.message}")
        }
    }
}