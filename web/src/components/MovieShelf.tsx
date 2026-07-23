import { Movie } from "@/types/movie";
import MovieCard from "./MovieCard";

interface MovieShelfProps {
  title: string;
  movies: Movie[];
}

export default function MovieShelf({ title, movies }: MovieShelfProps) {
  return (
    <section className="mb-8">
      <h2 className="text-xl md:text-2xl font-bold text-white mb-4 px-4 md:px-0">
        {title}
      </h2>
      <div className="flex gap-4 overflow-x-auto scrollbar-hide px-4 md:px-0 pb-4">
        {movies.map((movie) => (
          <div key={movie._id} className="flex-shrink-0 w-36 md:w-44">
            <MovieCard movie={movie} />
          </div>
        ))}
      </div>
    </section>
  );
}
