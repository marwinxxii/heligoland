package h8d.interpreter.testharness

import com.diffplug.selfie.kotest.SelfieExtension
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension

// accessed by Kotest in runtime
@Suppress("unused")
internal class KotestConfig : AbstractProjectConfig() {
    override val extensions: List<Extension> =
        listOf(
            SelfieExtension(this),
        )
}
