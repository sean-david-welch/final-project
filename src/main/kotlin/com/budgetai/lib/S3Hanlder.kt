import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import java.io.File
import aws.smithy.kotlin.runtime.content.ByteStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class S3FileHandler(
    private val s3Client: S3Client,
    private val bucketName: String
) {
    /**
     * Uploads a file to S3 bucket
     * @param file The file to upload
     * @param key The key (path) where the file will be stored in S3
     * @return Boolean indicating if upload was successful
     */
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

    /**
     * Uploads a ByteArray (like an image) to S3 bucket
     * @param bytes The ByteArray to upload
     * @param key The key (path) where the file will be stored in S3
     * @return Boolean indicating if upload was successful
     */
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

    /**
     * Downloads a file from S3 by its key
     * @param key The key (path) of the file in S3
     * @return Flow<ByteArray> of the file content
     */
    suspend fun downloadFile(key: String): Flow<ByteArray> = flow {
        try {
            val request = GetObjectRequest {
                bucket = bucketName
                this.key = key
            }

            val response = s3Client.getObject(request) { resp ->
                resp.body?.collect { chunk ->
                    emit(chunk)
                }
            }
        } catch (e: Exception) {
            println("Error downloading file: ${e.message}")
        }
    }
}