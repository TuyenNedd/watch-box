import { Suspense } from "react";
import SearchBar from "@/components/SearchBar";
import MovieCard from "@/components/MovieCard";
import { Movie, SearchResponse } from "@/types/movie";

interface SearchPageProps {
  searchParams: Promise<{ q?: string }>;
}

async function searchMovies(query: string): Promise<Movie[]> {
  if (!query) return [];
  const res = await fetch(
    `https://phimapi.com/v1/api/tim-kiem?keyword=${encodeURIComponent(query)}&limit=20`,
    { next: { revalidate: 60 } }
  );
  const data: SearchResponse = await res.json();
  return data.data?.items || [];
}

async function SearchResults({ query }: { query: string }) {
  const movies = await searchMovies(query);

  if (!query) {
    return (
      <p className="text-center text-gray-400 mt-8">
        Nhập từ khóa để tìm kiếm phim
      </p>
    );
  }

  if (movies.length === 0) {
    return (
      <p className="text-center text-gray-400 mt-8">
        Không tìm thấy phim nào cho &ldquo;{query}&rdquo;
      </p>
    );
  }

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 mt-8">
      {movies.map((movie) => (
        <MovieCard key={movie._id} movie={movie} />
      ))}
    </div>
  );
}

export default async function SearchPage({ searchParams }: SearchPageProps) {
  const { q = "" } = await searchParams;

  return (
    <div className="pt-24 pb-12 px-4 max-w-7xl mx-auto">
      <h1 className="text-2xl md:text-3xl font-bold text-white mb-6 text-center">
        Tìm Kiếm Phim
      </h1>
      <Suspense fallback={null}>
        <SearchBar />
      </Suspense>
      <Suspense
        fallback={
          <div className="flex justify-center mt-12">
            <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin" />
          </div>
        }
      >
        <SearchResults query={q} />
      </Suspense>
    </div>
  );
}
