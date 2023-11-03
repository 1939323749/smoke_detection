package app.smoke.data

import app.smoke.R
import java.io.File

object DataSource {

    val sourceOptions = listOf(
        Pair(R.string.source_file, 0),
        Pair(R.string.source_camera, 1),
    )
}

const val FILE=0
const val CAMARA=1