package dev.watchbox.core.util

/**
 * On Web, proxy image URLs through Vercel rewrites to avoid CORS.
 * phimimg.com/... → /img/...
 * img.ophim.live/... → /ophim-img/...
 */
actual fun resolveImageUrl(url: String): String {
    return when {
        url.startsWith("https://phimimg.com/") ->
            url.replace("https://phimimg.com/", "/img/")
        url.startsWith("http://phimimg.com/") ->
            url.replace("http://phimimg.com/", "/img/")
        url.startsWith("https://img.ophim.live/") ->
            url.replace("https://img.ophim.live/", "/ophim-img/")
        url.startsWith("http://img.ophim.live/") ->
            url.replace("http://img.ophim.live/", "/ophim-img/")
        else -> url
    }
}
