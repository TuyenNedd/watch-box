import HeroSection from "@/components/HeroSection";
import MovieShelf from "@/components/MovieShelf";
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

export default async function Home() {
  const [data1, data2] = await Promise.all([getMovies(), getMoviesPage2()]);

  const allMovies = data1.items || [];
  const moreMovies = data2.items || [];

  const heroMovie = allMovies[0];
  const newMovies = allMovies.slice(1, 13);
  const trendingMovies = allMovies.slice(13, 25);
  const moreToWatch = moreMovies.slice(0, 12);

  return (
    <div className="pt-16">
      {heroMovie && <HeroSection movie={heroMovie} />}
      <div className="max-w-7xl mx-auto py-8">
        <MovieShelf title="Phim Mới Cập Nhật" movies={newMovies} />
        <MovieShelf title="Đang Thịnh Hành" movies={trendingMovies} />
        <MovieShelf title="Có Thể Bạn Thích" movies={moreToWatch} />
      </div>
    </div>
  );
}
