package dev.swiftnote.probe.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class NoteFormatTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun sanitizeRemovesForbiddenKeepsTurkish() {
        assertEquals("Şimşek Ğ ü ö ç ı İ", NoteFormat.sanitizeFileName("Şimşek: Ğ ü ö ç ı İ"))
        assertEquals("a b c d e f g h i j k l m", NoteFormat.sanitizeFileName("a/b\\c:d*e?f\"g<h>i|j#k^l[m]"))
    }

    @Test fun sanitizeTrimsAndCollapses() {
        assertEquals("x y", NoteFormat.sanitizeFileName("  ..x \n\t y.. "))
        assertEquals("", NoteFormat.sanitizeFileName("///"))
    }

    @Test fun sanitizeCapsLengthByCodePoints() {
        assertEquals(NoteFormat.MAX_PART_CODE_POINTS, NoteFormat.sanitizeFileName("ş".repeat(300)).length)
        val emoji = NoteFormat.sanitizeFileName("😀".repeat(300))
        assertEquals(NoteFormat.MAX_PART_CODE_POINTS, emoji.codePointCount(0, emoji.length))
    }

    @Test fun fileName() {
        assertEquals("Şimşek Ğ ü ö ç — test — Kanal.md", NoteFormat.fileNameFor("Şimşek: Ğ ü ö ç — test", "Kanal"))
        assertEquals("Başlıksız — Kanalsız.md", NoteFormat.fileNameFor("", "///"))
    }

    @Test fun timeFormat() {
        assertEquals("00:00", NoteFormat.formatTime(0))
        assertEquals("12:34", NoteFormat.formatTime(754_900))
        assertEquals("1:02:03", NoteFormat.formatTime(3_723_000))
        assertEquals("00:00", NoteFormat.formatTime(-5))
    }

    @Test fun singleLineNote() {
        assertEquals("- **12:34** İlk not\n", NoteFormat.noteLine("12:34", "İlk not"))
    }

    @Test fun multiLineNoteIndented() {
        assertEquals("- **18:02** a\n  b\n  \n  c\n", NoteFormat.noteLine("18:02", "a\r\nb\n\nc\n\n"))
    }

    @Test fun initialContent() {
        assertEquals(
            "---\nkaynak: youtube\nkanal: Kanal\nolusturma: 2026-10-14\n---\n# Başlık\n\n",
            NoteFormat.initialContent("Başlık", "Kanal", "2026-10-14"),
        )
        assertTrue(NoteFormat.initialContent("T", "A: B", "d").contains("kanal: \"A: B\"\n"))
    }

    @Test fun clipboard() {
        assertEquals("12:34 — İlk not", NoteFormat.clipboardText("12:34", " İlk not\n"))
    }

    @Test fun writerCreatesThenAppendsOnly() {
        val w = NoteWriter(File(tmp.root, "Video Notları"))
        val f = w.append("Şimşek: Ğ", "Kanal", "00:05", "bir", "2026-10-14").getOrThrow()
        val first = f.readText()
        w.append("Şimşek: Ğ", "Kanal", "00:09", "iki\nsatır", "2026-10-15").getOrThrow()
        assertEquals(first + "- **00:09** iki\n  satır\n", f.readText())
        assertEquals(1, f.parentFile!!.listFiles()!!.size)
    }

    @Test fun writerAddsMissingTrailingNewline() {
        val dir = File(tmp.root, "d").apply { mkdirs() }
        val f = File(dir, NoteFormat.fileNameFor("T", "C")).apply { writeText("# x") }
        NoteWriter(dir).append("T", "C", "00:01", "n", "d").getOrThrow()
        assertEquals("# x\n- **00:01** n\n", f.readText())
    }

    @Test fun writerReportsFailureWithoutThrowing() {
        val blocker = File(tmp.root, "file").apply { writeText("x") }
        val r = NoteWriter(File(blocker, "sub")).append("T", "C", "00:01", "n", "d")
        assertTrue(r.isFailure)
        assertFalse(File(blocker, "sub").exists())
    }
}
