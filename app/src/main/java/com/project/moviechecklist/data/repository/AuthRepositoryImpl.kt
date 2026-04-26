package com.project.moviechecklist.data.repository

import com.project.moviechecklist.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override fun login(email: String, password: String): Flow<Resource<FirebaseUser>> = callbackFlow {
        trySend(Resource.Loading())
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let {
                    trySend(Resource.Success(it))
                } ?: trySend(Resource.Error("User not found"))
            }
            .addOnFailureListener { exception ->
                trySend(Resource.Error(exception.message ?: "Login failed"))
            }
        awaitClose()
    }

    override fun register(email: String, password: String): Flow<Resource<FirebaseUser>> = callbackFlow {
        trySend(Resource.Loading())
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let {
                    trySend(Resource.Success(it))
                } ?: trySend(Resource.Error("Registration failed"))
            }
            .addOnFailureListener { exception ->
                trySend(Resource.Error(exception.message ?: "Registration failed"))
            }
        awaitClose()
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
