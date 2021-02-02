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

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf

/**
 * Core loader to solves deeplink`s
 * */
object DeepLinkLoader {

    internal val registries = mutableMapOf<Uri, DeepLinkAction>()

    /**
     * @param modules, list of [DeepLinkModule], where must be registered the deeplink`s
     *
     * This method must be called into onCreate of [Application]
     * */
    fun setup(vararg modules: DeepLinkModule) {
        modules.forEach { deepLinkRegister -> deepLinkRegister.load() }
    }

    /**
     * @param activity, yours activity configured to open deeplink
     *
     * Example:
     * class DeepLinkActivity : AppCompatActivity() {
     *     override fun onCreate(savedInstanceState: Bundle?) {
     *          super.onCreate(savedInstanceState)
     *          DeepLinkLoader.loadFrom(this)
     *     }
     * */
    fun loadFrom(activity: AppCompatActivity) {
        require(registries.isNotEmpty()) {
            "DeepLinkLoader must be configured in Application with DeepLinkModule`s."
        }
        val uri: Uri? = activity.intent.data
        val templateUri = matches(checkNotNull(uri))
        if (templateUri != null) {
            with(activity) {
                val intent = registries[templateUri]?.invoke(this, createBundle(templateUri, uri))
                startActivity(intent)
            }
        }
        activity.finish()
    }

    /**
     * @param uri, target uri received from external deeplink
     *
     * @return [Uri], returns null if uri doesn't matches,
     *  otherwise source Uri registered into app
     * */
    fun matches(uri: Uri): Uri? {
        fun matchesUri(source: Uri, target: Uri): Boolean {
            if (source == target) return true
            if (source.authority != target.authority ||
                source.pathSegments.size != target.pathSegments.size
            ) {
                return false
            }

            source.pathSegments.forEachIndexed { index, s ->
                val t = target.pathSegments[index]
                if (s != t && !(s.startsWith("{") && s.endsWith("}"))) {
                    return false
                }
            }
            return true
        }

        registries.keys.forEach { source ->
            if (matchesUri(source, uri)) return source
        }

        return null
    }

    /**
     * @param source, template Uri
     * @param target, uri from external deeplink
     *
     * @return [Map] with params from deeplink
     * */
    fun getParamsFrom(source: Uri, target: Uri): Map<String, String> {
        val params = mutableMapOf<String, String>()
        source.pathSegments.forEachIndexed { index, s ->
            val t = target.pathSegments[index]
            if (s != t && (s.startsWith("{") && s.endsWith("}"))) {
                val path = s
                    .replace("{", "")
                    .replace("}", "")
                params[path] = t
            }
        }

        target.queryParameterNames.forEach { paramName ->
            target.getQueryParameter(paramName)?.let { param ->
                params[paramName] = param
            }
        }
        return params
    }

    /**
     * Clear all registries from [DeepLinkModule]`s
     * */
    internal fun clear() {
        registries.clear()
    }

    /**
     * @param templateUri, source from app, e.g.: myapp://example/{param1}/{param2}
     * @param uri, target uri received from external deeplink
     *
     * @return [Bundle]
     * */
    private fun createBundle(templateUri: Uri, uri: Uri): Bundle {
        return bundleOf().apply {
            getParamsFrom(templateUri, uri).forEach { (key, value) ->
                putString(key, value)
            }
        }
    }
}