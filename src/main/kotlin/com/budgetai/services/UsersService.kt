package com.budgetai.services

import com.budgetai.models.User
import com.budgetai.repositories.UserRepository

class UserService(private val repository: UserRepository) {
    suspend fun createUser(user: User): Int {
        return repository.create(user)
    }

    suspend fun getUser(id: Int): User? {
        return repository.findById(id)
    }

    suspend fun updateUser(id: Int, user: User) {
        // Add validation logic
        repository.update(id, user)
    }

    suspend fun deleteUser(id: Int) {
        repository.delete(id)
    }
}