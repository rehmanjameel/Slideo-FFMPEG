package org.codebase.slideo.models

class UserModel(
    val userName: String,
    val profileImageUri: String,
    val email: String,
    val gender: String,
    val videoUri: String

) : java.io.Serializable