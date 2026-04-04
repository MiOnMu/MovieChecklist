package com.project.moviechecklist.data.repository

import com.project.moviechecklist.util.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun login(email: String, password: String): Flow<Resource<FirebaseUser>>
    fun register(email: String, password: String): Flow<Resource<FirebaseUser>>
    fun logout()
    fun isUserLoggedIn(): Boolean
}
