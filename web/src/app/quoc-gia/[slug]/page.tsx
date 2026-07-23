import MovieCard from "@/components/MovieCard";
import Pagination from "@/components/Pagination";
import Footer from "@/components/Footer";
import { Movie } from "@/types/movie";

interface PageProps {
  params: Promise<{ slug: string }>;
  searchParams: Promise<{ page?: string }>;
}

async function getMoviesByCountry(slug: string, page: number) {
  const res = await fetch(
    `https://phimapi.com/v1/api/quoc-gia/${slug}?page=${page}`,
    { next: { revalidate: 300 } }
  );
  if (!res.ok) return null;
  return res.json();
}

export default async function CountryMoviesPage({ params, searchParams }: PageProps) {
  const { slug } = await params;
  const { page: pageParam } = await searchParams;
  const page = parseInt(pageParam || "1", 10) || 1;

  const data = await getMoviesByCountry(slug, page);

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
  const titlePage = data.data.titlePage || slug;
  const pagination = data.data.params?.pagination;
  const totalPages = pagination?.totalPages || 1;
  const currentPage = pagination?.currentPage || page;

  return (
    <div className="pt-16 min-h-screen">
      <div className="max-w-7xl mx-auto px-4 py-10">
        <h1 className="text-2xl md:text-3xl font-bold text-white mb-8">
          {titlePage}
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
              basePath={`/quoc-gia/${slug}`}
            />
          </>
        )}
      </div>
      <Footer />
    </div>
  );
}
