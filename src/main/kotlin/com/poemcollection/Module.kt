package com.poemcollection

import com.auth0.jwt.interfaces.JWTVerifier
import com.poemcollection.data.database.DatabaseProviderContract
import com.poemcollection.domain.interfaces.IUserDao
import com.poemcollection.modules.auth.*
import com.poemcollection.modules.categories.categoryRouting
import com.poemcollection.modules.poems.poemRouting
import com.poemcollection.modules.reviews.reviewRouting
import com.poemcollection.modules.users.userRouting
import com.poemcollection.statuspages.generalStatusPages
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
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
        gson()
    }
    install(StatusPages) {
        generalStatusPages()
    }
    install(Authentication) {
        jwt {
            setupAuthentication(config, jwtVerifier) {
                it.validateUser(databaseProvider, userDao)
            }
        }

        jwt(adminOnly) {
            setupAuthentication(config, jwtVerifier) {
                it.validateUserIsAdmin(databaseProvider, userDao)
            }
        }
    }

    install(Routing) {
        authRouting()
        route("api/v1/") {
            userRouting()
            categoryRouting()
            poemRouting()
            reviewRouting()
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