import Image from "next/image";
import Link from "next/link";
import { Movie } from "@/types/movie";
import { getImageUrl } from "@/lib/api";

interface HeroSectionProps {
  movie: Movie;
}

export default function HeroSection({ movie }: HeroSectionProps) {
  return (
    <section className="relative w-full h-[60vh] md:h-[70vh] overflow-hidden">
      {/* Background Image */}
      <div className="absolute inset-0">
        <Image
          src={getImageUrl(movie.thumb_url)}
          alt={movie.name}
          fill
          className="object-cover"
          priority
          sizes="100vw"
        />
        {/* Gradients */}
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/50 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-r from-background/80 to-transparent" />
      </div>

      {/* Content */}
      <div className="relative h-full max-w-7xl mx-auto px-4 flex flex-col justify-end pb-16">
        <h1 className="text-3xl md:text-5xl font-bold text-white mb-2 max-w-2xl">
          {movie.name}
        </h1>
        <p className="text-lg text-gray-300 mb-2">{movie.origin_name}</p>
        <div className="flex items-center gap-3 mb-4 flex-wrap">
          {movie.year && (
            <span className="text-sm text-gray-300 bg-white/10 px-3 py-1 rounded-full">
              {movie.year}
            </span>
          )}
          {movie.quality && (
            <span className="text-sm text-white bg-accent px-3 py-1 rounded-full font-medium">
              {movie.quality}
            </span>
          )}
          {movie.lang && (
            <span className="text-sm text-white bg-blue-600 px-3 py-1 rounded-full font-medium">
              {movie.lang}
            </span>
          )}
          {movie.time && (
            <span className="text-sm text-gray-300 bg-white/10 px-3 py-1 rounded-full">
              {movie.time}
            </span>
          )}
        </div>
        <Link
          href={`/phim/${movie.slug}`}
          className="inline-flex items-center gap-2 bg-accent hover:bg-accent/90 text-white font-semibold px-6 py-3 rounded-lg transition-colors w-fit"
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
          Xem Ngay
        </Link>
      </div>
    </section>
  );
}
