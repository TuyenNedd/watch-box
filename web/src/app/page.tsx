import HeroSection from "@/components/HeroSection";
import MovieShelf from "@/components/MovieShelf";
import Footer from "@/components/Footer";
import { Movie, MovieListResponse } from "@/types/movie";
import { NguonCItem, NguonCListResponse } from "@/lib/api";

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

interface V1ApiResponse {
  data?: {
    items?: Movie[];
  };
  items?: Movie[];
}

async function getSingleMovies(): Promise<V1ApiResponse> {
  const res = await fetch(
    "https://phimapi.com/v1/api/danh-sach/phim-le?page=1",
    { next: { revalidate: 300 } }
  );
  return res.json();
}

async function getSeriesMovies(): Promise<V1ApiResponse> {
  const res = await fetch(
    "https://phimapi.com/v1/api/danh-sach/phim-bo?page=1",
    { next: { revalidate: 300 } }
  );
  return res.json();
}

async function getAnimationMovies(): Promise<V1ApiResponse> {
  const res = await fetch(
    "https://phimapi.com/v1/api/danh-sach/hoat-hinh?page=1",
    { next: { revalidate: 300 } }
  );
  return res.json();
}

async function getNguonCMovies(): Promise<NguonCListResponse> {
  const BASE_URL = "http://localhost:3000";
  const res = await fetch(`${BASE_URL}/api/nguonc/films/phim-moi-cap-nhat?page=1`, {
    next: { revalidate: 300 },
  });
  return res.json();
}

function extractItems(data: V1ApiResponse): Movie[] {
  return data?.data?.items || data?.items || [];
}

function nguoncToMovie(item: NguonCItem): Movie {
  return {
    _id: item.slug,
    name: item.name,
    slug: item.slug,
    origin_name: item.original_name || "",
    type: "",
    poster_url: item.poster_url || "",
    thumb_url: item.thumb_url || "",
    sub_docquyen: false,
    chipiloeng: false,
    time: "",
    episode_current: item.current_episode || "",
    quality: item.quality || "",
    lang: item.language || "",
    year: item.year || 0,
    category: (item.category || []).map((c) => ({ id: c.slug, name: c.name, slug: c.slug })),
    country: (item.country || []).map((c) => ({ id: c.slug, name: c.name, slug: c.slug })),
  };
}

export default async function Home() {
  const [data1, data2, singleData, seriesData, animationData, nguoncData] = await Promise.all([
    getMovies(),
    getMoviesPage2(),
    getSingleMovies().catch(() => ({} as V1ApiResponse)),
    getSeriesMovies().catch(() => ({} as V1ApiResponse)),
    getAnimationMovies().catch(() => ({} as V1ApiResponse)),
    getNguonCMovies().catch(() => ({ status: "error", paginate: null, items: [] } as NguonCListResponse)),
  ]);

  const allMovies = data1.items || [];
  const moreMovies = data2.items || [];

  const singleMovies = extractItems(singleData).slice(0, 12);
  const seriesMovies = extractItems(seriesData).slice(0, 12);
  const animationMovies = extractItems(animationData).slice(0, 12);

  const nguoncMovies: Movie[] =
    nguoncData.status === "success" && nguoncData.items?.length > 0
      ? nguoncData.items.slice(0, 12).map(nguoncToMovie)
      : [];

  const heroMovie = allMovies[0];
  const newMovies = allMovies.slice(1, 13);
  const trendingMovies = allMovies.slice(13, 25);
  const moreToWatch = moreMovies.slice(0, 12);

  return (
    <div className="pt-16">
      {heroMovie && <HeroSection movie={heroMovie} />}
      <div className="max-w-7xl mx-auto py-10">
        <MovieShelf title="New Releases" movies={newMovies} seeAllHref="/danh-sach/phim-le" />
        <MovieShelf title="Trending Now" movies={trendingMovies} seeAllHref="/danh-sach/phim-bo" />
        {singleMovies.length > 0 && (
          <MovieShelf title="Movies" movies={singleMovies} seeAllHref="/danh-sach/phim-le" />
        )}
        {seriesMovies.length > 0 && (
          <MovieShelf title="Series" movies={seriesMovies} seeAllHref="/danh-sach/phim-bo" />
        )}
        {animationMovies.length > 0 && (
          <MovieShelf title="Animation" movies={animationMovies} seeAllHref="/danh-sach/hoat-hinh" />
        )}
        {nguoncMovies.length > 0 && (
          <MovieShelf title="More Movies" movies={nguoncMovies} seeAllHref="/danh-sach/phim-le" />
        )}
        <MovieShelf title="You May Like" movies={moreToWatch} seeAllHref="/danh-sach/tv-shows" />
      </div>
      <Footer />
    </div>
  );
}
