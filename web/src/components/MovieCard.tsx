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
      <div className="relative aspect-[2/3] rounded-lg overflow-hidden bg-card transition-transform duration-300 group-hover:scale-105 group-hover:shadow-lg group-hover:shadow-accent/20">
        <Image
          src={getImageUrl(movie.poster_url)}
          alt={movie.name}
          fill
          className="object-cover"
          sizes="(max-width: 640px) 50vw, (max-width: 1024px) 25vw, 16vw"
        />
        {/* Badges */}
        <div className="absolute top-2 left-2 flex flex-col gap-1">
          {movie.quality && (
            <span className="bg-accent text-white text-xs font-bold px-2 py-0.5 rounded">
              {movie.quality}
            </span>
          )}
          {movie.lang && (
            <span className="bg-blue-600 text-white text-xs font-bold px-2 py-0.5 rounded">
              {movie.lang}
            </span>
          )}
        </div>
        {/* Episode badge */}
        {movie.episode_current && (
          <div className="absolute bottom-2 right-2">
            <span className="bg-black/70 text-white text-xs px-2 py-0.5 rounded">
              {movie.episode_current}
            </span>
          </div>
        )}
        {/* Gradient overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
      </div>
      <div className="mt-2 px-1">
        <h3 className="text-sm font-medium text-white truncate group-hover:text-accent transition-colors">
          {movie.name}
        </h3>
        <p className="text-xs text-gray-400 truncate">
          {movie.origin_name} {movie.year ? `(${movie.year})` : ""}
        </p>
      </div>
    </Link>
  );
}
