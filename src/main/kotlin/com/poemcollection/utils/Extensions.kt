package com.poemcollection.utils

import com.poemcollection.routes.ParamConstants
import com.poemcollection.security.security.token.TokenClaim
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun ApplicationCall.getUserId(): Int? = principal<JWTPrincipal>()?.getClaim(TokenClaim.TOKEN_CLAIM_USER_ID_KEY, String::class)?.toIntOrNull()

fun ApplicationCall.getPoemId(): Int? = parameters[ParamConstants.POEM_ID_KEY]?.toIntOrNull()

fun ApplicationCall.getReviewId(): Int? = parameters[ParamConstants.REVIEW_ID_KEY]?.toIntOrNull()