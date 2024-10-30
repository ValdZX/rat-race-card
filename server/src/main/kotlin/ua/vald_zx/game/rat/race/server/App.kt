package ua.vald_zx.game.rat.race.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.ServiceOptions
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import mu.KotlinLogging


class App : HttpFunction {

    private val logger = KotlinLogging.logger {}
    private val db: Firestore

    init {
        val projectId: String = ServiceOptions.getDefaultProjectId()
        val firestoreOptions: FirestoreOptions =
            FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId(projectId)
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
        db = firestoreOptions.getService()
    }

    override fun service(request: HttpRequest, response: HttpResponse) {
        val boards = db.collection("boards")
        val document = boards.document("first")
        val count = document.get().get()?.getLong("counter") ?: 0L
        logger.info { "hello world $count" }
        document.set(mapOf("counter" to (count + 1L)))
        response.writer.write("FUNCTION COMPLETE  $count")
    }
}