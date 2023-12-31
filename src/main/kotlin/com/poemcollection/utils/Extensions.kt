package com.poemcollection.utils

import com.poemcollection.ParamConstants
import com.poemcollection.domain.models.user.User
import com.poemcollection.statuspages.ApiException
import com.poemcollection.statuspages.ErrorInvalidUUID
import com.poemcollection.statuspages.InternalServerException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

val ApplicationCall.authenticatedUser get() = authentication.principal<User>()!!

suspend inline fun <reified T> ApplicationCall.receiveOrRespondWithError(): T {
    return try {
        runCatching { receiveNullable<T>() }.getOrNull() ?: run {
            // TODO: I think this happened when the incoming data could not be parsed!
            throw TBDException
        }
    } catch (e: Exception) {
        // This happens when no "body" was added to the network call
        throw InternalServerException()
    }
}

fun ApplicationCall.getUserId(): Int = parameters[ParamConstants.USER_ID_KEY]?.toIntOrNull() ?: throw ErrorInvalidUUID
fun ApplicationCall.getCategoryIdNullable(): Int? = parameters[ParamConstants.CATEGORY_ID_KEY]?.toIntOrNull()
fun ApplicationCall.getCategoryId(): Int = getCategoryIdNullable() ?: throw ErrorInvalidUUID
fun ApplicationCall.getPoemId(): Int = parameters[ParamConstants.POEM_ID_KEY]?.toIntOrNull() ?: throw ErrorInvalidUUID
fun ApplicationCall.getReviewId(): Int = parameters[ParamConstants.REVIEW_ID_KEY]?.toIntOrNull() ?: throw ErrorInvalidUUID

suspend fun PipelineContext<Unit, ApplicationCall>.sendOk() {
    call.respond(HttpStatusCode.OK)
}

object TBDException : ApiException("TBD_error", "An error, but still under development", HttpStatusCode.InternalServerError)