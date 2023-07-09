package com.poemcollection.modules.users

import com.poemcollection.data.dto.requests.user.InsertNewUser
import com.poemcollection.data.dto.requests.user.UpdatePassword
import com.poemcollection.data.dto.requests.user.UpdateUser
import com.poemcollection.data.dto.requests.user.UserDto
import com.poemcollection.domain.interfaces.IUserDao
import com.poemcollection.domain.models.user.toDto
import com.poemcollection.modules.BaseController
import com.poemcollection.utils.PasswordManagerContract
import com.poemcollection.utils.TBDException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserControllerImpl : BaseController(), UserController, KoinComponent {

    private val userDao by inject<IUserDao>()
    private val passwordEncryption by inject<PasswordManagerContract>()

    override suspend fun postUser(insertNewUser: InsertNewUser): UserDto = dbQuery {
        if (!insertNewUser.isValid) throw TBDException

        val userUnique = userDao.userUnique(insertNewUser.email)
        if (!userUnique || !insertNewUser.email.contains("@")) throw TBDException

        if (!insertNewUser.isPasswordSame) throw TBDException

        if (!insertNewUser.isPasswordStrong) throw TBDException

        val encryptedPassword = passwordEncryption.encryptPassword(insertNewUser.password)

        userDao.insertUser(insertNewUser.copy(password = encryptedPassword, repeatPassword = null))?.toDto() ?: throw TBDException
    }

    override suspend fun getUserById(userId: Int): UserDto = dbQuery {
        userDao.getUser(userId)?.toDto() ?: throw TBDException
    }

    override suspend fun updateUserById(userId: Int, updateUser: UpdateUser): UserDto = dbQuery {
        if (updateUser.firstName == null && updateUser.lastName == null) throw TBDException

        userDao.updateUser(userId, updateUser)?.toDto() ?: throw TBDException
    }

    override suspend fun updateUserPasswordById(userId: Int, updatePassword: UpdatePassword): UserDto = dbQuery {
        val userHashable = userDao.getUserHashableById(userId) ?: throw TBDException

        if (listOf(updatePassword.password, updatePassword.repeatPassword).any { it == updatePassword.oldPassword }) throw TBDException

        val isValidPassword = passwordEncryption.validatePassword(updatePassword.oldPassword, userHashable.password ?: "")
        if (!isValidPassword) throw  TBDException

        if (!updatePassword.isPasswordSame) throw TBDException

        if (!updatePassword.isPasswordStrong) throw TBDException

        val encryptedPassword = passwordEncryption.encryptPassword(updatePassword.password)

        userDao.updateUserPassword(userId, encryptedPassword)?.toDto() ?: throw TBDException
    }

    override suspend fun deleteUserById(userId: Int) {
        return dbQuery { userDao.deleteUser(userId) }
    }
}

interface UserController {
    suspend fun postUser(insertNewUser: InsertNewUser): UserDto
    suspend fun getUserById(userId: Int): UserDto
    suspend fun updateUserById(userId: Int, updateUser: UpdateUser): UserDto
    suspend fun updateUserPasswordById(userId: Int, updatePassword: UpdatePassword): UserDto
    suspend fun deleteUserById(userId: Int)
}