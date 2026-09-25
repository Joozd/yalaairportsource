package nl.joozd.airportsource.args

import nl.joozd.airportsource.args.DuplicateArgumentException
import nl.joozd.airportsource.args.UnknownArgumentNameException
import nl.joozd.airportsource.args.buildArgsFromCli
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ArgsTest {
    /**
     * Verifies that the output directory is parsed correctly.
     */
    @Test
    fun `test if output path is found correct`() {
        val outputDir = "/tmp/output"
        val testArgs = arrayOf("--output-dir", outputDir)

        val args = buildArgsFromCli(testArgs)

        assertEquals(outputDir, args.outputDir)
    }

    /**
     * Verifies that supplying the same argument twice is rejected.
     */
    @Test
    fun `test duplicate arguments are rejected`() {
        val testArgs = arrayOf(
            "--output-dir", "/tmp/output",
            "--output-dir", "/tmp/other"
        )

        assertThrows<DuplicateArgumentException> {
            buildArgsFromCli(testArgs)
        }
    }

    /**
     * Verifies that unknown argument names are rejected.
     */
    @Test
    fun `test unknown argument name is rejected`() {
        val testArgs = arrayOf("--does-not-exist")

        assertThrows<UnknownArgumentNameException> {
            buildArgsFromCli(testArgs)
        }
    }

    /**
     * Verifies that the help flag is parsed correctly.
     */
    @Test
    fun `test help argument is found correctly`() {
        val args = buildArgsFromCli(arrayOf("--help"))

        assertTrue(args.help)
    }

    /**
     * Verifies that the short output directory argument is parsed correctly.
     */
    @Test
    fun `test short output argument is found correctly`() {
        val outputDir = "/tmp/output"

        val args = buildArgsFromCli(arrayOf("-o", outputDir))

        assertEquals(outputDir, args.outputDir)
    }

    /**
     * Verifies that the short help argument is parsed correctly.
     */
    @Test
    fun `test short help argument is found correctly`() {
        val args = buildArgsFromCli(arrayOf("-h"))

        assertTrue(args.help)
    }

    /**
     * Verifies that short and long forms of the same argument are treated as duplicates.
     */
    @Test
    fun `test short and long output arguments are rejected as duplicates`() {
        val testArgs = arrayOf(
            "-o", "/tmp/output",
            "--output-dir", "/tmp/other"
        )

        assertThrows<DuplicateArgumentException> {
            buildArgsFromCli(testArgs)
        }
    }
}