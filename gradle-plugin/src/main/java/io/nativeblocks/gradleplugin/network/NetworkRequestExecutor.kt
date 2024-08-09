package io.nativeblocks.gradleplugin.network

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class NetworkRequestExecutor {
    suspend fun <D : Operation.Data> requestExecutor(result: ApolloCall<D>): ResultModel<D?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = result.execute()
                when {
                    response.hasErrors() -> {
                        return@withContext errorHandler(response)
                    }

                    response.data != null -> {
                        return@withContext ResultModel.Success(response.data)
                    }

                    else -> return@withContext errorHandler(response)
                }
            } catch (e: Exception) {
                return@withContext ResultModel.Error(
                    ErrorModel("Please try again", GraphqlErrorTypes.UNKNOWN_ERROR.name)
                )
            }
        }
    }

    private fun <Q : Operation.Data> errorHandler(response: ApolloResponse<Q>): ResultModel.Error {
        val errorType = response.errors?.first()?.extensions?.get("classification").toString()
        return ResultModel.Error(
            ErrorModel(message = response.errors?.first()?.message, errorType = errorType)
        )
    }
}

internal enum class GraphqlErrorTypes {
    UNKNOWN_ERROR, BAD_REQUEST, UNAUTHORIZED, UNAUTHORIZED_SDK, NOT_FOUND, INTERNAL_ERROR
}