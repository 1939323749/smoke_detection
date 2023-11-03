buildscript {
    extra.apply {
        set("lifecycle_version", "2.6.1")
    }
}
plugins {
    id("com.android.application") version "7.4.0" apply false
    id("com.android.library") version "7.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
}
