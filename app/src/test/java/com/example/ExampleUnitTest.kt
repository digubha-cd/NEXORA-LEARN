package com.example

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Unit test to verify project and Firebase configurations.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testGoogleServicesJsonConfig() {
    val file = File("src/main/google-services.json").let {
      if (it.exists()) it else File("google-services.json")
    }
    assertTrue("google-services.json must exist", file.exists())
    val content = file.readText()
    assertTrue("Project ID must be nexora-learn-2f4a4", content.contains("\"project_id\": \"nexora-learn-2f4a4\""))
    assertTrue("Package name must be com.aistudio.nexoralearn.qjvpk", content.contains("\"package_name\": \"com.aistudio.nexoralearn.qjvpk\""))
    assertTrue("API key must be valid non-placeholder", content.contains("AIzaSyB7P4-cKw6XupCjGraGu7qr-hXwhgNeP3k"))
    assertFalse("Must not contain synthetic placeholder key", content.contains("AIzaSyNexoraLearnAppKey2026StudioProduction"))
  }
}
