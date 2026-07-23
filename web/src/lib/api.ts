import { MovieListResponse, MovieDetail, SearchResponse } from "@/types/movie";

const BASE_URL = typeof window !== "undefined" ? "" : "http://localhost:3000";

export async function getMovieList(page: number = 1): Promise<MovieListResponse> {
  const res = await fetch(`${BASE_URL}/api/phim/list?page=${page}`, {
    next: { revalidate: 300 },
  });
  if (!res.ok) throw new Error("Failed to fetch movie list");
  return res.json();
}

export async function getMovieDetail(slug: string): Promise<MovieDetail> {
  const res = await fetch(`${BASE_URL}/api/phim/${slug}`, {
    next: { revalidate: 300 },
  });
  if (!res.ok) throw new Error("Failed to fetch movie detail");
  return res.json();
}

export async function searchMovies(query: string): Promise<SearchResponse> {
  const res = await fetch(`${BASE_URL}/api/phim/search?q=${encodeURIComponent(query)}`, {
    next: { revalidate: 60 },
  });
  if (!res.ok) throw new Error("Failed to search movies");
  return res.json();
}

export function getImageUrl(path: string): string {
  if (!path) return "/placeholder.png";
  if (path.startsWith("http")) return path;
  return `https://phimimg.com/${path}`;
}
