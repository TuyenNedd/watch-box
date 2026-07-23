import { notFound } from "next/navigation";
import MovieCard from "@/components/MovieCard";
import Pagination from "@/components/Pagination";
import Footer from "@/components/Footer";
import { Movie } from "@/types/movie";

const TYPE_LABELS: Record<string, string> = {
  "phim-le": "Movies",
  "phim-bo": "Series",
  "hoat-hinh": "Animation",
  "tv-shows": "TV Shows",
};

interface PageProps {
  params: Promise<{ type: string }>;
  searchParams: Promise<{ page?: string }>;
}

async function getMoviesByType(type: string, page: number) {
  const res = await fetch(
    `https://phimapi.com/v1/api/danh-sach/${type}?page=${page}`,
    { next: { revalidate: 300 } }
  );
  if (!res.ok) return null;
  return res.json();
}

export default async function MovieListPage({ params, searchParams }: PageProps) {
  const { type } = await params;
  const { page: pageParam } = await searchParams;

  if (!TYPE_LABELS[type]) {
    notFound();
  }

  const page = parseInt(pageParam || "1", 10) || 1;
  const data = await getMoviesByType(type, page);

  if (!data || !data.data) {
    return (
      <div className="pt-16 min-h-screen">
        <div className="max-w-7xl mx-auto px-4 py-12 text-center">
          <p className="text-gray-400">No movies found</p>
        </div>
        <Footer />
      </div>
    );
  }

  const movies: Movie[] = data.data.items || [];
  const titlePage = data.data.titlePage || TYPE_LABELS[type];
  const pagination = data.data.params?.pagination;
  const totalPages = pagination?.totalPages || 1;
  const currentPage = pagination?.currentPage || page;

  return (
    <div className="pt-16 min-h-screen">
      <div className="max-w-7xl mx-auto px-4 py-10">
        <h1 className="text-2xl md:text-3xl font-bold text-white mb-8">
          {TYPE_LABELS[type] || titlePage}
        </h1>

        {movies.length === 0 ? (
          <p className="text-gray-400 text-center py-20">No movies found</p>
        ) : (
          <>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
              {movies.map((movie: Movie) => (
                <MovieCard key={movie._id || movie.slug} movie={movie} />
              ))}
            </div>
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              basePath={`/danh-sach/${type}`}
            />
          </>
        )}
      </div>
      <Footer />
    </div>
  );
}
