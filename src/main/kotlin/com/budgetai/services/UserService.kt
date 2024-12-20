package com.budgetai.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import com.budgetai.models.UserAuthenticationRequest
import com.budgetai.models.UserCreationRequest
import com.budgetai.models.UserDTO
import com.budgetai.plugins.TOKEN_EXPIRATION
import com.budgetai.repositories.UserRepository
import io.ktor.server.config.*
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserService(private val repository: UserRepository, private val config: ApplicationConfig) {
    private val logger = LoggerFactory.getLogger("UserService")

    private fun generateToken(id: String, email: String, role: String, config: ApplicationConfig): String {
        val jwtAudience = config.property("jwt.audience").getString()
        val jwtIssuer = config.property("jwt.issuer").getString()
        val jwtSecret = config.property("jwt.secret").getString()

        val issuedAt = Date()
        val expiresAt = Date(System.currentTimeMillis() + TOKEN_EXPIRATION * 1000)

        logger.info("Generating token with timestamps - issuedAt: $issuedAt, expiresAt: $expiresAt")

        return JWT.create().withAudience(jwtAudience).withIssuer(jwtIssuer).withClaim("id", id).withClaim("email", email)
            .withClaim("role", role).withExpiresAt(expiresAt).withIssuedAt(issuedAt).sign(HMAC256(jwtSecret))
    }

    // generate new token for user
    suspend fun authenticateUserWithToken(request: UserAuthenticationRequest): Pair<UserDTO, String>? {
        val user = authenticateUser(request) ?: return null
        val token = generateToken(
            id = user.id.toString(), email = user.email, role = user.role, config = config
        )
        return Pair(user, token)
    }

    // refresh token
    suspend fun refreshToken(id: Int): String? {
        val user = getUser(id) ?: return null
        return generateToken(
            id = user.id.toString(), email = user.email, role = user.role, config = config
        )
    }

    // Generates a random salt for password hashing
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    // Hashes a password with the provided salt
    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }

    // Validates email format
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return email.matches(emailRegex)
    }

    // Validates password strength
    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")
        return password.matches(passwordRegex)
    }

    // Validates that an email is unique
    private suspend fun validateEmailUnique(email: String, excludeId: Int? = null) {
        repository.findByEmail(email)?.let { existing ->
            if (excludeId == null || existing.id != excludeId) {
                throw IllegalArgumentException("Email already in use")
            }
        }
    }

    // Read Methods
    // find all users
    suspend fun getUsers(): List<UserDTO> {
        return repository.findAll()
    }

    // Retrieves a user by their ID
    suspend fun getUser(id: Int): UserDTO? {
        return repository.findById(id)
    }

    // Retrieves a user by their email
    suspend fun getUserByEmail(email: String): UserDTO? {
        return repository.findByEmail(email)
    }

    // Authenticates a user with email and password
    suspend fun authenticateUser(request: UserAuthenticationRequest): UserDTO? {
        val user = repository.findByEmail(request.email) ?: return null

        val storedHash = repository.findPasswordHash(user.id) ?: return null
        val (salt, hash) = storedHash.split(":")
        val saltBytes = Base64.getDecoder().decode(salt)
        val calculatedHash = hashPassword(request.password, saltBytes)

        return if (hash == calculatedHash) user else null
    }

    // Write Methods
    // Creates a new user and returns their ID
    suspend fun createUser(request: UserCreationRequest): Int {
        require(isValidEmail(request.email)) { "Invalid email format" }
        require(isValidPassword(request.password)) { "Password doesn't meet security requirements" }
        validateEmailUnique(request.email)

        val salt = generateSalt()
        val hashedPassword = hashPassword(request.password, salt)
        val encodedSalt = Base64.getEncoder().encodeToString(salt)
        val finalHash = "$encodedSalt:$hashedPassword"

        val userDTO = UserDTO(
            email = request.email, name = request.name, role = request.role
        )

        val userId = repository.create(userDTO)
        repository.updatePassword(userId, finalHash)

        return userId
    }

    // Updates user's basic information
    suspend fun updateUser(id: Int, user: UserDTO): Int {
        val existingUser = repository.findById(id) ?: throw IllegalArgumentException("User not found")

        if (user.email != existingUser.email) {
            require(isValidEmail(user.email)) { "Invalid email format" }
            validateEmailUnique(user.email, id)
        }

        return repository.update(id, user)
    }

    // Updates user's password
    suspend fun updatePassword(id: Int, newPassword: String) {
        require(isValidPassword(newPassword)) { "New password doesn't meet security requirements" }

        val newSalt = generateSalt()
        val newHashedPassword = hashPassword(newPassword, newSalt)
        val newEncodedSalt = Base64.getEncoder().encodeToString(newSalt)
        val finalHash = "$newEncodedSalt:$newHashedPassword"

        repository.updatePassword(id, finalHash)
    }

    // Deletes a user and all their data
    suspend fun deleteUser(id: Int) {
        repository.delete(id)
    }
}