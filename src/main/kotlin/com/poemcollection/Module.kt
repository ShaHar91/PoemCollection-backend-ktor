package com.poemcollection

import com.auth0.jwt.interfaces.JWTVerifier
import com.poemcollection.data.database.DatabaseProviderContract
import com.poemcollection.domain.interfaces.IUserDao
import com.poemcollection.modules.auth.setupAuthentication
import com.poemcollection.modules.auth.validateUser
import com.poemcollection.modules.auth.validateUserIsAdmin
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.slf4j.event.Level

fun Application.module() {

    val config by inject<ApplicationConfig>()
    val jwtVerifier by inject<JWTVerifier>()
    val userDao by inject<IUserDao>()
    val databaseProvider by inject<DatabaseProviderContract>()
    // Init database here
    databaseProvider.init()

    install(CallLogging) {
        level = Level.DEBUG
    }
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true // Will make sure that every field will be returned (as long as it has a default value)
            ignoreUnknownKeys = true // Will make sure that unsupported field that are in a request will not trigger an error and will just be ignored
        })

//        register(FormUrlEncoded, CustomFormUrlEncodedConverter)
    }
    install(StatusPages) {
        //TODO: still TBD what to use and how!
    }
    install(Authentication) {
        jwt {
            setupAuthentication(config, jwtVerifier) {
                it.validateUser(databaseProvider, userDao)
            }
        }

        jwt("admin") {
            setupAuthentication(config, jwtVerifier) {
                it.validateUserIsAdmin(databaseProvider, userDao)
            }
        }
    }
}

//object CustomFormUrlEncodedConverter : ContentConverter {
//    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any? {
//
//        val stringContent = content.toInputStream().reader(charset).readText()
//
//        println(stringContent)
//
//        return null
//    }
//}