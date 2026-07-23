import Image from "next/image";
import Link from "next/link";
import { Movie } from "@/types/movie";
import { getImageUrl } from "@/lib/api";

interface HeroSectionProps {
  movie: Movie;
}

export default function HeroSection({ movie }: HeroSectionProps) {
  return (
    <section className="relative w-full h-[70vh] md:h-[80vh] overflow-hidden">
      {/* Background Image with Ken Burns */}
      <div className="absolute inset-0 animate-ken-burns">
        <Image
          src={getImageUrl(movie.thumb_url)}
          alt={movie.name}
          fill
          className="object-cover"
          priority
          sizes="100vw"
        />
      </div>

      {/* Gradient Overlays */}
      <div className="absolute inset-0 bg-gradient-to-t from-background via-background/40 to-background/20" />
      <div className="absolute inset-0 bg-gradient-to-r from-background/90 via-background/40 to-transparent" />
      <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-background to-transparent" />

      {/* Content */}
      <div className="relative h-full max-w-7xl mx-auto px-4 flex flex-col justify-end pb-20">
        {/* Category Pills */}
        {movie.category && movie.category.length > 0 && (
          <div className="flex items-center gap-2 mb-4 animate-fade-in opacity-0">
            {movie.category.slice(0, 3).map((cat) => (
              <span
                key={cat.id}
                className="text-xs font-medium text-gray-300 bg-white/10 backdrop-blur-sm px-3 py-1 rounded-full border border-white/10"
              >
                {cat.name}
              </span>
            ))}
          </div>
        )}

        {/* Title */}
        <h1 className="text-3xl md:text-5xl lg:text-6xl font-bold text-white mb-2 max-w-3xl animate-slide-up opacity-0 leading-tight">
          {movie.name}
        </h1>
        <p className="text-lg text-gray-300/80 mb-4 animate-slide-up opacity-0" style={{ animationDelay: "0.1s" }}>
          {movie.origin_name}
        </p>

        {/* Glass Badges */}
        <div className="flex items-center gap-3 mb-6 flex-wrap animate-slide-up opacity-0" style={{ animationDelay: "0.15s" }}>
          {movie.year && (
            <span className="glass text-sm text-gray-200 px-3 py-1.5 rounded-full">
              {movie.year}
            </span>
          )}
          {movie.quality && (
            <span className="text-sm text-white bg-accent/90 backdrop-blur-sm px-3 py-1.5 rounded-full font-medium border border-accent/30">
              {movie.quality}
            </span>
          )}
          {movie.lang && (
            <span className="glass text-sm text-blue-300 px-3 py-1.5 rounded-full font-medium">
              {movie.lang}
            </span>
          )}
          {movie.time && (
            <span className="glass text-sm text-gray-200 px-3 py-1.5 rounded-full">
              {movie.time}
            </span>
          )}
          {movie.episode_current && (
            <span className="glass text-sm text-gray-200 px-3 py-1.5 rounded-full">
              {movie.episode_current}
            </span>
          )}
        </div>

        {/* Buttons */}
        <div className="flex items-center gap-3 animate-slide-up opacity-0" style={{ animationDelay: "0.2s" }}>
          <Link
            href={`/phim/${movie.slug}`}
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
            Watch Now
          </Link>
          <Link
            href={`/phim/${movie.slug}`}
            className="inline-flex items-center gap-2 glass hover:bg-white/10 text-white font-medium px-7 py-3.5 rounded-xl transition-all duration-200"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={2}
              stroke="currentColor"
              className="w-5 h-5"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z"
              />
            </svg>
            Details
          </Link>
        </div>
      </div>
    </section>
  );
}
