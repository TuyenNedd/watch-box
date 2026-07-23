import Image from "next/image";
import Link from "next/link";
import { Movie } from "@/types/movie";
import { getImageUrl } from "@/lib/api";

interface MovieCardProps {
  movie: Movie;
}

export default function MovieCard({ movie }: MovieCardProps) {
  return (
    <Link href={`/phim/${movie.slug}`} className="group block">
      <div className="relative aspect-[2/3] rounded-xl overflow-hidden bg-card transition-all duration-300 group-hover:scale-105 group-hover:shadow-xl group-hover:shadow-accent/10 group-hover:brightness-110">
        <Image
          src={getImageUrl(movie.poster_url)}
          alt={movie.name}
          fill
          className="object-cover"
          sizes="(max-width: 640px) 50vw, (max-width: 1024px) 25vw, 16vw"
        />

        {/* Glass Badges */}
        <div className="absolute top-2 left-2 flex flex-col gap-1.5">
          {movie.quality && (
            <span className="bg-accent/90 backdrop-blur-sm text-white text-xs font-bold px-2.5 py-1 rounded-lg border border-accent/30">
              {movie.quality}
            </span>
          )}
          {movie.lang && (
            <span className="bg-blue-600/80 backdrop-blur-sm text-white text-xs font-bold px-2.5 py-1 rounded-lg border border-blue-500/30">
              {movie.lang}
            </span>
          )}
        </div>

        {/* Episode badge */}
        {movie.episode_current && (
          <div className="absolute bottom-2 right-2">
            <span className="glass text-white text-xs px-2.5 py-1 rounded-lg">
              {movie.episode_current}
            </span>
          </div>
        )}

        {/* Hover overlay with rating/year and play icon */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-all duration-300 flex flex-col items-center justify-center">
          {/* Play icon */}
          <div className="w-12 h-12 rounded-full bg-accent/90 backdrop-blur-sm flex items-center justify-center transform scale-75 group-hover:scale-100 transition-transform duration-300 shadow-lg shadow-accent/30">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              className="w-5 h-5 text-white ml-0.5"
            >
              <path
                fillRule="evenodd"
                d="M4.5 5.653c0-1.427 1.529-2.33 2.779-1.643l11.54 6.347c1.295.712 1.295 2.573 0 3.286L7.28 19.99c-1.25.687-2.779-.217-2.779-1.643V5.653Z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          {/* Year/Type info on hover */}
          <div className="absolute bottom-3 left-3 right-3">
            <p className="text-white text-xs font-medium truncate">
              {movie.year ? movie.year : ""}{movie.year && movie.type ? " \u2022 " : ""}{movie.type === "single" ? "Movie" : movie.type === "series" ? "Series" : ""}
            </p>
          </div>
        </div>
      </div>
      <div className="mt-2.5 px-1">
        <h3 className="text-sm font-medium text-white truncate group-hover:text-accent transition-colors duration-200">
          {movie.name}
        </h3>
        <p className="text-xs text-gray-500 truncate mt-0.5">
          {movie.origin_name} {movie.year ? `(${movie.year})` : ""}
        </p>
      </div>
    </Link>
  );
}
