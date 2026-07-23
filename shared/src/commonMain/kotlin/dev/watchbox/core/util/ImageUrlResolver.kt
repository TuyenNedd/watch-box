package dev.watchbox.core.util

/**
 * Resolves image URLs for the current platform.
 * On Web, external image CDNs (phimimg.com, img.ophim.live) block CORS,
 * so we proxy them through Vercel rewrites.
 * On Android, URLs pass through unchanged.
 */
expect fun resolveImageUrl(url: String): String
