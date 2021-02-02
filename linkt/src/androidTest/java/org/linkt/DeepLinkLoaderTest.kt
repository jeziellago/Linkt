/* MIT License
*
* Copyright (c) 2021 Jeziel Lago
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package org.linkt

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkLoaderTest {

    @Test
    fun shouldMatchesUris() {
        val expectedUri = Uri.parse("test://test/{id}/b/{name}/c")

        deepLinkOf("test://tester/{id}/b/{name}/c") { _, _ -> Intent() }
        deepLinkOf("test://test/{id}/b/{name}/c") { _, _ -> Intent() }
        deepLinkOf("test://test/{id}/b/c") { _, _ -> Intent() }

        val result = DeepLinkLoader.matches(
            Uri.parse("test://test/123/b/jeziel/c?value=myValue")
        )

        assertEquals(expectedUri, result)
    }

    @Test
    fun shouldNotMatchWithDifferentAuthority() {

        deepLinkOf("test://tester/{id}/b/{name}/c") { _, _ -> Intent() }

        val result = DeepLinkLoader.matches(Uri.parse("test://test/123/b/jeziel/c"))

        assertNull(result)
    }

    @Test
    fun shouldMatchesWithoutParams() {
        val expected = Uri.parse("test://test/a/b/c")

        deepLinkOf("test://tester/a/b/c") { _, _ -> Intent() }
        deepLinkOf("test://test/a/b/c") { _, _ -> Intent() }
        deepLinkOf("test://test/b/c") { _, _ -> Intent() }

        val result = DeepLinkLoader.matches(Uri.parse("test://test/a/b/c"))

        assertEquals(expected, result)
    }

    @Test
    fun shouldParseParamsWhenReceivedUriContainsData() {
        val r = DeepLinkLoader.getParamsFrom(
            Uri.parse("test://test/{id}/b/{name}/c"),
            Uri.parse("test://test/123/b/jeziel/c?value1=myValue1&value2=MyValue2")
        )

        val params = mutableMapOf(
            "id" to "123",
            "name" to "jeziel",
            "value1" to "myValue1",
            "value2" to "MyValue2",
        )

        assertEquals(params, r)
    }

    @Test
    fun shouldParseParamsWithUriWithoutQuery() {
        val r = DeepLinkLoader.getParamsFrom(
            Uri.parse("test://test/{id}/b/{name}/c"),
            Uri.parse("test://test/123/b/jeziel/c")
        )

        val params = mutableMapOf(
            "id" to "123",
            "name" to "jeziel"
        )

        assertEquals(params, r)
    }

    @After
    fun tearDown() {
        DeepLinkLoader.clear()
    }

}