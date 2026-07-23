import Image from "next/image";
import Link from "next/link";
import { notFound } from "next/navigation";
import { MovieDetail } from "@/types/movie";
import Footer from "@/components/Footer";
import { getImageUrl } from "@/lib/api";

interface MoviePageProps {
  params: Promise<{ slug: string }>;
}

async function getMovie(slug: string): Promise<MovieDetail | null> {
  try {
    const res = await fetch(`https://phimapi.com/phim/${slug}`, {
      next: { revalidate: 300 },
    });
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

function extractYouTubeId(url: string): string | null {
  if (!url) return null;
  const match = url.match(/(?:youtube\.com\/(?:watch\?v=|embed\/)|youtu\.be\/)([\w-]+)/);
  return match ? match[1] : null;
}

export default async function MoviePage({ params }: MoviePageProps) {
  const { slug } = await params;
  const data = await getMovie(slug);

  if (!data || !data.movie) {
    notFound();
  }

  const movie = data.movie;
  const episodes = data.episodes || [];
  const youtubeId = extractYouTubeId(movie.trailer_url);

  return (
    <div className="pt-16 min-h-screen">
      {/* Backdrop */}
      <div className="relative w-full h-[50vh] md:h-[60vh]">
        <Image
          src={getImageUrl(movie.thumb_url)}
          alt={movie.name}
          fill
          className="object-cover"
          priority
          sizes="100vw"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/60 to-background/20" />
        <div className="absolute inset-0 bg-gradient-to-r from-background/70 to-transparent" />
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 -mt-36 relative z-10">
        {/* Breadcrumb */}
        <nav className="flex items-center gap-2 text-sm mb-6">
          <Link href="/" className="text-gray-400 hover:text-white transition-colors">Home</Link>
          <span className="text-gray-600">/</span>
          <span className="text-gray-300 truncate max-w-[200px]">{movie.name}</span>
        </nav>

        <div className="flex flex-col md:flex-row gap-8">
          {/* Poster */}
          <div className="flex-shrink-0 w-48 md:w-64 mx-auto md:mx-0">
            <div className="relative aspect-[2/3] rounded-xl overflow-hidden shadow-2xl shadow-black/50 ring-1 ring-white/10">
              <Image
                src={getImageUrl(movie.poster_url)}
                alt={movie.name}
                fill
                className="object-cover"
                sizes="256px"
              />
            </div>
          </div>

          {/* Info */}
          <div className="flex-1">
            <h1 className="text-2xl md:text-4xl font-bold text-white mb-1">
              {movie.name}
            </h1>
            <p className="text-lg text-gray-400 mb-4">{movie.origin_name}</p>

            {/* Metadata badges */}
            <div className="flex flex-wrap gap-2 mb-5">
              {movie.year && (
                <span className="glass text-gray-200 px-3 py-1.5 rounded-full text-sm">
                  {movie.year}
                </span>
              )}
              {movie.quality && (
                <span className="bg-accent/90 backdrop-blur-sm text-white px-3 py-1.5 rounded-full text-sm font-medium border border-accent/30">
                  {movie.quality}
                </span>
              )}
              {movie.lang && (
                <span className="glass text-blue-300 px-3 py-1.5 rounded-full text-sm font-medium">
                  {movie.lang}
                </span>
              )}
              {movie.time && (
                <span className="glass text-gray-200 px-3 py-1.5 rounded-full text-sm">
                  {movie.time}
                </span>
              )}
              {movie.episode_current && (
                <span className="glass text-gray-200 px-3 py-1.5 rounded-full text-sm">
                  {movie.episode_current}
                </span>
              )}
            </div>

            {/* Categories */}
            {movie.category && movie.category.length > 0 && (
              <div className="flex flex-wrap gap-2 mb-5">
                {movie.category.map((cat) => (
                  <span
                    key={cat.id}
                    className="border border-white/10 bg-white/5 text-gray-300 px-3 py-1.5 rounded-full text-sm hover:border-accent/30 hover:text-accent transition-colors"
                  >
                    {cat.name}
                  </span>
                ))}
              </div>
            )}

            {/* Actors */}
            {movie.actor && movie.actor.length > 0 && movie.actor[0] !== "" && (
              <div className="mb-3">
                <span className="text-gray-500 text-sm">Cast: </span>
                <span className="text-gray-300 text-sm">
                  {movie.actor.slice(0, 5).join(", ")}
                </span>
              </div>
            )}

            {/* Directors */}
            {movie.director && movie.director.length > 0 && movie.director[0] !== "" && (
              <div className="mb-5">
                <span className="text-gray-500 text-sm">Director: </span>
                <span className="text-gray-300 text-sm">
                  {movie.director.join(", ")}
                </span>
              </div>
            )}

            {/* Description */}
            {movie.content && (
              <div
                className="text-gray-400 text-sm leading-relaxed mb-6 max-w-3xl"
                dangerouslySetInnerHTML={{ __html: movie.content }}
              />
            )}

            {/* Action Buttons */}
            <div className="flex items-center gap-3 flex-wrap">
              {episodes.length > 0 && episodes[0].server_data?.length > 0 && (
                <Link
                  href={`/xem/${movie.slug}`}
                  className="inline-flex items-center gap-2 bg-accent hover:bg-accent-hover text-white font-semibold px-7 py-3.5 rounded-xl transition-all duration-200 hover:scale-105 hover:shadow-lg hover:shadow-accent/25"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    className="w-5 h-5"
                  >
                    <path
                      fillRule="evenodd"
                      d="M4.5 5.653c0-1.427 1.529-2.33 2.779-1.643l11.54 6.347c1.295.712 1.295 2.573 0 3.286L7.28 19.99c-1.25.687-2.779-.217-2.779-1.643V5.653Z"
                      clipRule="evenodd"
                    />
                  </svg>
                  Watch
                </Link>
              )}
              {/* Share button */}
              <button
                className="inline-flex items-center gap-2 glass hover:bg-white/10 text-white font-medium px-5 py-3.5 rounded-xl transition-all duration-200"
                title="Share"
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-5 h-5">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M7.217 10.907a2.25 2.25 0 1 0 0 2.186m0-2.186c.18.324.283.696.283 1.093s-.103.77-.283 1.093m0-2.186 9.566-5.314m-9.566 7.5 9.566 5.314m0 0a2.25 2.25 0 1 0 3.935 2.186 2.25 2.25 0 0 0-3.935-2.186Zm0-12.814a2.25 2.25 0 1 0 3.935-2.186 2.25 2.25 0 0 0-3.935 2.186Z" />
                </svg>
                Share
              </button>
            </div>
          </div>
        </div>

        {/* Trailer */}
        {youtubeId && (
          <div className="mt-12">
            <h2 className="text-xl font-bold text-white mb-4">Trailer</h2>
            <div className="relative w-full max-w-3xl aspect-video rounded-xl overflow-hidden ring-1 ring-white/10">
              <iframe
                src={`https://www.youtube.com/embed/${youtubeId}`}
                title="Trailer"
                className="absolute inset-0 w-full h-full"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            </div>
          </div>
        )}

        {/* Episode list */}
        {episodes.length > 0 && (
          <div className="mt-12 mb-12">
            <h2 className="text-xl font-bold text-white mb-4">Episodes</h2>
            {episodes.map((server, idx) => (
              <div key={idx} className="mb-6">
                <p className="text-sm text-gray-500 mb-3 font-medium">{server.server_name}</p>
                <div className="flex flex-wrap gap-2 max-h-64 overflow-y-auto pr-2">
                  {server.server_data.map((ep, epIdx) => (
                    <Link
                      key={epIdx}
                      href={`/xem/${movie.slug}?ep=${epIdx}&sv=${idx}`}
                      className="bg-white/5 hover:bg-accent hover:border-accent text-white px-4 py-2.5 rounded-lg text-sm transition-all duration-200 border border-white/10 hover:scale-105"
                    >
                      {ep.name}
                    </Link>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
      <Footer />
    </div>
  );
}
