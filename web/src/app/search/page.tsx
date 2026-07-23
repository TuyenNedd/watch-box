import { Suspense } from "react";
import SearchBar from "@/components/SearchBar";
import MovieCard from "@/components/MovieCard";
import Footer from "@/components/Footer";
import { SkeletonGrid } from "@/components/Skeleton";
import { Movie, SearchResponse } from "@/types/movie";

interface SearchPageProps {
  searchParams: Promise<{ q?: string }>;
}

async function searchMovies(query: string): Promise<Movie[]> {
  if (!query) return [];
  const res = await fetch(
    `https://phimapi.com/v1/api/tim-kiem?keyword=${encodeURIComponent(query)}&limit=24`,
    { next: { revalidate: 60 } }
  );
  const data: SearchResponse = await res.json();
  return data.data?.items || [];
}

async function SearchResults({ query }: { query: string }) {
  const movies = await searchMovies(query);

  if (!query) {
    return (
      <div className="text-center mt-16">
        <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-white/5 flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-7 h-7 text-gray-500">
            <path strokeLinecap="round" strokeLinejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z" />
          </svg>
        </div>
        <p className="text-gray-400 text-lg">
          Enter keywords to search for movies
        </p>
        <p className="text-gray-600 text-sm mt-1">
          Search by title, actor, or genre
        </p>
      </div>
    );
  }

  if (movies.length === 0) {
    return (
      <div className="text-center mt-16">
        <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-white/5 flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-7 h-7 text-gray-500">
            <path strokeLinecap="round" strokeLinejoin="round" d="M15.182 16.318A4.486 4.486 0 0 0 12.016 15a4.486 4.486 0 0 0-3.198 1.318M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0ZM9.75 9.75c0 .414-.168.75-.375.75S9 10.164 9 9.75 9.168 9 9.375 9s.375.336.375.75Zm-.375 0h.008v.015h-.008V9.75Zm5.625 0c0 .414-.168.75-.375.75s-.375-.336-.375-.75.168-.75.375-.75.375.336.375.75Zm-.375 0h.008v.015h-.008V9.75Z" />
          </svg>
        </div>
        <p className="text-gray-400 text-lg">
          No movies found for &ldquo;{query}&rdquo;
        </p>
        <p className="text-gray-600 text-sm mt-1">
          Try different keywords or browse our categories
        </p>
      </div>
    );
  }

  return (
    <div className="mt-8">
      <p className="text-gray-500 text-sm mb-4">
        Found {movies.length} result{movies.length !== 1 ? "s" : ""} for &ldquo;{query}&rdquo;
      </p>
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
        {movies.map((movie) => (
          <MovieCard key={movie._id} movie={movie} />
        ))}
      </div>
    </div>
  );
}

export default async function SearchPage({ searchParams }: SearchPageProps) {
  const { q = "" } = await searchParams;

  return (
    <div className="pt-24 pb-12 min-h-screen">
      <div className="max-w-7xl mx-auto px-4">
        <h1 className="text-3xl md:text-4xl font-bold text-white mb-2 text-center">
          Search Movies
        </h1>
        <p className="text-gray-500 text-center mb-8">
          Discover your next favorite movie or series
        </p>
        <Suspense fallback={null}>
          <SearchBar />
        </Suspense>
        <Suspense fallback={<SkeletonGrid />}>
          <SearchResults query={q} />
        </Suspense>
      </div>
      <Footer />
    </div>
  );
}
