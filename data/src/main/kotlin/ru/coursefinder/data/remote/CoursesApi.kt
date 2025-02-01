package ru.coursefinder.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendEncodedPathSegments
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.coursefinder.data.model.CoursesResponse
import ru.coursefinder.data.model.UsersResponse

internal interface CoursesApi {
    suspend fun getCoursesFromPage(page: Int, pageSize: Int): CoursesResponse

    suspend fun getCourseById(id: Long): CoursesResponse

    suspend fun getUserById(id: Long): UsersResponse

    suspend fun getCourseRating(courseId: Long): Double?
}


private class CoursesApiImpl : CoursesApi {
    private companion object {
        const val API_URL = "https://stepik.org/api/"

        const val TAG = "CoursesApi"

        val json by lazy {
            Json { ignoreUnknownKeys = true }
        }
    }


    override suspend fun getCoursesFromPage(page: Int, pageSize: Int): CoursesResponse {
        try {
            HttpClient(Android).use { client ->
                return client.get(API_URL) {
                    url {
                        appendEncodedPathSegments("courses")
                        parameters.append(name = "page", value = page.toString())
                        /*parameters.append(name = "page_size", value = pageSize.toString())*/
                    }
                }.bodyAsText().let(json::decodeFromString)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, e.message, e)
            throw SerializationException(e.message, e)
        } catch (e: SerializationException) {
            Log.e(TAG, e.message, e)
            throw SerializationException(e.message, e)
        }
    }

    override suspend fun getCourseById(id: Long): CoursesResponse {
        HttpClient(Android).use { client ->
            return client.get(API_URL) {
                url {
                    appendEncodedPathSegments("courses", id.toString())
                }
            }.bodyAsText().let(json::decodeFromString)
        }
    }

    override suspend fun getUserById(id: Long): UsersResponse {
        HttpClient(Android).use { client ->
            return client.get(API_URL) {
                url {
                    appendEncodedPathSegments("users", id.toString())
                }
            }.bodyAsText().let(json::decodeFromString)
        }
    }

    override suspend fun getCourseRating(courseId: Long): Double? = try {
        HttpClient(Android).use { client ->
            val response = client.get(API_URL) {
                url {
                    appendEncodedPathSegments("course-review-summaries")
                    parameters.append(name = "course", value = courseId.toString())
                }
            }.bodyAsText().let(json::parseToJsonElement) as JsonObject

            val reviewSummaries = response["course-review-summaries"]!!
                .let { json.decodeFromJsonElement<List<JsonObject>>(it) }[0]

            val reviewCount: Int = reviewSummaries["count"]!!.let(json::decodeFromJsonElement)
            return reviewSummaries["average"]
                ?.takeIf { reviewCount != 0 }
                ?.let(json::decodeFromJsonElement)
        }
    } catch (e: IllegalArgumentException) {
        Log.e(TAG, e.message, e)
        null
    } catch (e: SerializationException) {
        Log.e(TAG, e.message, e)
        null
    }
}



internal val coursesApiModule = module {
    singleOf<CoursesApi>(::CoursesApiImpl)
}