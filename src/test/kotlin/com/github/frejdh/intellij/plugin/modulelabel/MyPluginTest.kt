package com.github.frejdh.intellij.plugin.modulelabel

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.PsiErrorElementUtil
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.File

@TestApplication
class MyPluginTest {
    private val projectFixture = codeInsightProjectFixture()
    private val myFixture by codeInsightFixture(projectFixture)

    @Test
    fun xmlFile() {
        runOnEdt {
            val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
            val xmlFile = assertInstanceOf(XmlFile::class.java, psiFile)

            assertFalse(PsiErrorElementUtil.hasErrors(myFixture.project, xmlFile.virtualFile))

            val rootTag = xmlFile.rootTag
            assertNotNull(rootTag)
            assertEquals("foo", rootTag!!.name)
            assertEquals("bar", rootTag.value.text)
        }
    }

    @Test
    fun rename() {
        runOnEdt {
            // Set here because @TestDataPath resolves against the IDE home, not this project.
            myFixture.testDataPath = File("src/test/testData/rename").absolutePath
            myFixture.testRename("foo.xml", "foo_after.xml", "a2")
        }
    }

    private fun runOnEdt(action: () -> Unit) {
        timeoutRunBlocking(context = Dispatchers.EDT) {
            writeIntentReadAction(action)
        }
    }
}
