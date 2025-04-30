import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class MyCookieJar : CookieJar {
    private val cookieStore: MutableMap<String, MutableList<Cookie>> = mutableMapOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        for (cookie in cookies) {
            Log.d("COOKIE", "Saving cookie: ${cookie.name} = ${cookie.value}")
        }

        if (cookieStore[url.host] == null) {
            cookieStore[url.host] = mutableListOf()
        }
        cookieStore[url.host]?.addAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieStore[url.host] ?: listOf()
        for (cookie in cookies) {
            Log.d("COOKIE", "Sending cookie: ${cookie.name}=${cookie.value}")
        }
        return cookies
    }

}


