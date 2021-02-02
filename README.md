# Linkt ![](https://github.com/jeziellago/Linkt/workflows/DEPLOY/badge.svg?branch=release)
A light and simple kotlin library for deep link handling on Android.
## Setup
Add `Linkt` to your project `build.gradle`:
```
dependencies {
  implementation 'org.linkt:linkt:LATEST_VERSION'
}
```

1. Create your `DeepLinkModule` and register deep links:
```kotlin
class MyDeepLinkModule : DeepLinkModule {

    override fun load() {
        deepLinkOf(
            "linkt://sample",
            "linkt://sample/{userId}/{userName}"
        ) { context, bundle ->
            Intent(context, MainActivity::class.java)
                .apply { putExtras(bundle) }
        }
    }
}
```

> In multi-module projects you should have one or more DeepLinkModule`s.
>

2. Register your modules into `Application#onCreate`, with `DeepLinkLoader#setup`:
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DeepLinkLoader.setup(MyDeepLinkModule())
    }
}
```
3. Create the `DeepLinkActivity` (or use yours if already exists), and call `DeepLinkLoader#loadFrom`:
```kotlin
class DeepLinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // resolve deeplink
        DeepLinkLoader.loadFrom(this)
    }
}
```
Don't forget to configure `AndroidManifest.xml` (required for Android deep links):
```xml
<activity
    android:name="org.linkt.DeepLinkActivity"
    android:theme="@android:style/Theme.NoDisplay">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="mycompany" />
    </intent-filter>
</activity>
```
## How to get data from deeplink?
In your activity, you can get path parameters and query values as `String` from `intent.extras`:
### Path parameters
- Template `linkt://sample/{userId}/{userName}`
- Received: `linkt://sample/9999/Jose`
```kotlin
// get path parameters
val userId = intent.extras.getString("userId")
val userId = intent.extras.getString("userName")
```
### Query parameters
- Template: `linkt://sample`
- Received `linkt://sample?subject=Linkt&name=Sample`
```kotlin
// get query parameters
val subject = intent.extras.getString("subject")
val name = intent.extras.getString("name")
```
### Path + query parameters
- Template `linkt://sample/{userId}/{userName}`
- Received `linkt://sample/999/Jose?subject=Linkt&name=Sample`
```kotlin
// get path parameters
val userId = intent.extras.getString("userId")
val userId = intent.extras.getString("userName")
// get query parameters
val subject = intent.extras.getString("subject")
val name = intent.extras.getString("name")
```
## Licence
```
Copyright (c) 2021 Jeziel Lago

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```