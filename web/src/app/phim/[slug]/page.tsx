import Image from "next/image";
import Link from "next/link";
import { notFound } from "next/navigation";
import { MovieDetail } from "@/types/movie";
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

export default async function MoviePage({ params }: MoviePageProps) {
  const { slug } = await params;
  const data = await getMovie(slug);

  if (!data || !data.movie) {
    notFound();
  }

  const movie = data.movie;
  const episodes = data.episodes || [];

  return (
    <div className="pt-16">
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
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/60 to-transparent" />
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 -mt-32 relative z-10">
        <div className="flex flex-col md:flex-row gap-8">
          {/* Poster */}
          <div className="flex-shrink-0 w-48 md:w-64 mx-auto md:mx-0">
            <div className="relative aspect-[2/3] rounded-lg overflow-hidden shadow-2xl">
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
            <p className="text-lg text-gray-300 mb-4">{movie.origin_name}</p>

            {/* Metadata badges */}
            <div className="flex flex-wrap gap-2 mb-4">
              {movie.year && (
                <span className="bg-white/10 text-gray-300 px-3 py-1 rounded-full text-sm">
                  {movie.year}
                </span>
              )}
              {movie.quality && (
                <span className="bg-accent text-white px-3 py-1 rounded-full text-sm font-medium">
                  {movie.quality}
                </span>
              )}
              {movie.lang && (
                <span className="bg-blue-600 text-white px-3 py-1 rounded-full text-sm font-medium">
                  {movie.lang}
                </span>
              )}
              {movie.time && (
                <span className="bg-white/10 text-gray-300 px-3 py-1 rounded-full text-sm">
                  {movie.time}
                </span>
              )}
              {movie.episode_current && (
                <span className="bg-white/10 text-gray-300 px-3 py-1 rounded-full text-sm">
                  {movie.episode_current}
                </span>
              )}
            </div>

            {/* Categories */}
            {movie.category && movie.category.length > 0 && (
              <div className="flex flex-wrap gap-2 mb-4">
                {movie.category.map((cat) => (
                  <span
                    key={cat.id}
                    className="border border-white/20 text-gray-300 px-3 py-1 rounded-full text-sm"
                  >
                    {cat.name}
                  </span>
                ))}
              </div>
            )}

            {/* Actors */}
            {movie.actor && movie.actor.length > 0 && movie.actor[0] !== "" && (
              <div className="mb-4">
                <span className="text-gray-400 text-sm">Diễn viên: </span>
                <span className="text-gray-200 text-sm">
                  {movie.actor.slice(0, 5).join(", ")}
                </span>
              </div>
            )}

            {/* Directors */}
            {movie.director && movie.director.length > 0 && movie.director[0] !== "" && (
              <div className="mb-4">
                <span className="text-gray-400 text-sm">Đạo diễn: </span>
                <span className="text-gray-200 text-sm">
                  {movie.director.join(", ")}
                </span>
              </div>
            )}

            {/* Description */}
            {movie.content && (
              <div
                className="text-gray-300 text-sm leading-relaxed mb-6 max-w-3xl"
                dangerouslySetInnerHTML={{ __html: movie.content }}
              />
            )}

            {/* Watch button */}
            {episodes.length > 0 && episodes[0].server_data?.length > 0 && (
              <Link
                href={`/xem/${movie.slug}`}
                className="inline-flex items-center gap-2 bg-accent hover:bg-accent/90 text-white font-semibold px-6 py-3 rounded-lg transition-colors"
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
                Xem Phim
              </Link>
            )}
          </div>
        </div>

        {/* Episode list */}
        {episodes.length > 0 && (
          <div className="mt-12 mb-12">
            <h2 className="text-xl font-bold text-white mb-4">Danh Sách Tập</h2>
            {episodes.map((server, idx) => (
              <div key={idx} className="mb-6">
                <p className="text-sm text-gray-400 mb-2">{server.server_name}</p>
                <div className="flex flex-wrap gap-2">
                  {server.server_data.map((ep, epIdx) => (
                    <Link
                      key={epIdx}
                      href={`/xem/${movie.slug}?ep=${epIdx}&sv=${idx}`}
                      className="bg-card hover:bg-accent text-white px-4 py-2 rounded-lg text-sm transition-colors border border-white/10 hover:border-accent"
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
    </div>
  );
}
