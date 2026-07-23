import HeroSection from "@/components/HeroSection";
import MovieShelf from "@/components/MovieShelf";
import Footer from "@/components/Footer";
import { Movie, MovieListResponse } from "@/types/movie";

async function getMovies(): Promise<MovieListResponse> {
  const res = await fetch(
    "https://phimapi.com/danh-sach/phim-moi-cap-nhat?page=1",
    { next: { revalidate: 300 } }
  );
  return res.json();
}

async function getMoviesPage2(): Promise<MovieListResponse> {
  const res = await fetch(
    "https://phimapi.com/danh-sach/phim-moi-cap-nhat?page=2",
    { next: { revalidate: 300 } }
  );
  return res.json();
}

async function getSingleMovies(): Promise<MovieListResponse> {
  const res = await fetch(
    "https://phimapi.com/v1/api/danh-sach/phim-le?page=1&limit=12",
    { next: { revalidate: 300 } }
  );
  return res.json();
}

async function getSeriesMovies(): Promise<MovieListResponse> {
  const res = await fetch(
    "https://phimapi.com/v1/api/danh-sach/phim-bo?page=1&limit=12",
    { next: { revalidate: 300 } }
  );
  return res.json();
}

export default async function Home() {
  const [data1, data2, singleData, seriesData] = await Promise.all([
    getMovies(),
    getMoviesPage2(),
    getSingleMovies().catch(() => ({ items: [], status: "", pagination: { totalItems: 0, totalItemsPerPage: 0, currentPage: 0, totalPages: 0 } })),
    getSeriesMovies().catch(() => ({ items: [], status: "", pagination: { totalItems: 0, totalItemsPerPage: 0, currentPage: 0, totalPages: 0 } })),
  ]);

  const allMovies = data1.items || [];
  const moreMovies = data2.items || [];

  // Try to get items from v1 API format (data.items) or direct items
  const singleMovies: Movie[] = (singleData as unknown as { data?: { items?: Movie[] } })?.data?.items || singleData.items || [];
  const seriesMovies: Movie[] = (seriesData as unknown as { data?: { items?: Movie[] } })?.data?.items || seriesData.items || [];

  const heroMovie = allMovies[0];
  const newMovies = allMovies.slice(1, 13);
  const trendingMovies = allMovies.slice(13, 25);
  const moreToWatch = moreMovies.slice(0, 12);

  return (
    <div className="pt-16">
      {heroMovie && <HeroSection movie={heroMovie} />}
      <div className="max-w-7xl mx-auto py-10">
        <MovieShelf title="New Releases" movies={newMovies} seeAllHref="/search?q=new" />
        <MovieShelf title="Trending Now" movies={trendingMovies} seeAllHref="/search?q=trending" />
        {singleMovies.length > 0 && (
          <MovieShelf title="Latest Movies" movies={singleMovies} seeAllHref="/search?q=phim-le" />
        )}
        {seriesMovies.length > 0 && (
          <MovieShelf title="Ongoing Series" movies={seriesMovies} seeAllHref="/search?q=phim-bo" />
        )}
        <MovieShelf title="You May Like" movies={moreToWatch} seeAllHref="/search?q=recommended" />
      </div>
      <Footer />
    </div>
  );
}
