package com.budgetai.services

import com.budgetai.models.UserDTO
import com.budgetai.repositories.UserRepository
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64

class UserService(private val repository: UserRepository) {

    data class UserCreationRequest(
        val email: String,
        val name: String,
        val password: String
    )

    data class UserAuthenticationRequest(
        val email: String,
        val password: String
    )

    suspend fun createUser(request: UserCreationRequest): Int {
        // Validate email format
        require(isValidEmail(request.email)) { "Invalid email format" }

        // Check if email already exists
        repository.findByEmail(request.email)?.let {
            throw IllegalArgumentException("Email already registered")
        }

        // Validate password strength
        require(isValidPassword(request.password)) { "Password doesn't meet security requirements" }

        // Generate salt and hash password
        val salt = generateSalt()
        val hashedPassword = hashPassword(request.password, salt)
        val encodedSalt = Base64.getEncoder().encodeToString(salt)
        val finalHash = "$encodedSalt:$hashedPassword"

        // Create user
        val userDTO = UserDTO(
            email = request.email,
            name = request.name
        )

        val userId = repository.create(userDTO)
        repository.updatePassword(userId, finalHash)

        return userId
    }

    suspend fun authenticateUser(request: UserAuthenticationRequest): UserDTO? {
        val user = repository.findByEmail(request.email) ?: return null

        // Verify password
        val storedHash = repository.findPasswordHash(user.id) ?: return null
        val (salt, hash) = storedHash.split(":")
        val saltBytes = Base64.getDecoder().decode(salt)
        val calculatedHash = hashPassword(request.password, saltBytes)

        return if (hash == calculatedHash) user else null
    }

    suspend fun getUser(id: Int): UserDTO? {
        return repository.findById(id)
    }

    suspend fun getUserByEmail(email: String): UserDTO? {
        return repository.findByEmail(email)
    }

    suspend fun updateUser(id: Int, user: UserDTO) {
        // Validate email if it's being changed
        val existingUser = repository.findById(id) ?: throw IllegalArgumentException("User not found")

        if (user.email != existingUser.email) {
            require(isValidEmail(user.email)) { "Invalid email format" }
            repository.findByEmail(user.email)?.let {
                throw IllegalArgumentException("Email already in use")
            }
        }

        repository.update(id, user)
    }

    suspend fun updatePassword(id: Int, currentPassword: String, newPassword: String) {
        // Verify current password
        val user = repository.findById(id) ?: throw IllegalArgumentException("User not found")
        val storedHash = repository.findPasswordHash(id) ?: throw IllegalStateException("Password hash not found")

        val (salt, hash) = storedHash.split(":")
        val saltBytes = Base64.getDecoder().decode(salt)
        val calculatedHash = hashPassword(currentPassword, saltBytes)

        require(hash == calculatedHash) { "Current password is incorrect" }
        require(isValidPassword(newPassword)) { "New password doesn't meet security requirements" }

        // Generate new salt and hash for the new password
        val newSalt = generateSalt()
        val newHashedPassword = hashPassword(newPassword, newSalt)
        val newEncodedSalt = Base64.getEncoder().encodeToString(newSalt)
        val finalHash = "$newEncodedSalt:$newHashedPassword"

        repository.updatePassword(id, finalHash)
    }

    suspend fun deleteUser(id: Int) {
        repository.delete(id)
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return email.matches(emailRegex)
    }

    private fun isValidPassword(password: String): Boolean {
        // At least 8 characters, 1 uppercase, 1 lowercase, 1 number
        val passwordRegex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")
        return password.matches(passwordRegex)
    }
}