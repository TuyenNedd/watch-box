"use client";

import { useEffect, useState } from "react";
import { useSource } from "@/lib/source-context";
import HeroSection from "@/components/HeroSection";
import MovieShelf from "@/components/MovieShelf";
import Footer from "@/components/Footer";
import { Movie } from "@/types/movie";

interface HomeData {
  newMovies: Movie[];
  trendingMovies: Movie[];
  singleMovies: Movie[];
  seriesMovies: Movie[];
  animationMovies: Movie[];
  nguoncMovies: Movie[];
  moreToWatch: Movie[];
  heroMovie: Movie | null;
}

function tagMovies(movies: Movie[], source: "phimapi" | "ophim" | "nguonc"): Movie[] {
  return movies.map((m) => ({ ...m, source }));
}

export default function HomeContent({ serverData }: { serverData: HomeData }) {
  const { source } = useSource();
  const [clientData, setClientData] = useState<HomeData | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (source === "all") {
      setClientData(null);
      return;
    }

    setLoading(true);
    const fetchSourceData = async () => {
      try {
        if (source === "phimapi") {
          const [res1, res2] = await Promise.all([
            fetch("/api/phim/list?page=1"),
            fetch("/api/phim/list?page=2"),
          ]);
          const data1 = await res1.json();
          const data2 = await res2.json();
          const movies1 = tagMovies(data1.items || [], "phimapi");
          const movies2 = tagMovies(data2.items || [], "phimapi");
          setClientData({
            heroMovie: movies1[0] || null,
            newMovies: movies1.slice(1, 13),
            trendingMovies: movies1.slice(13, 25),
            singleMovies: [],
            seriesMovies: [],
            animationMovies: [],
            nguoncMovies: [],
            moreToWatch: movies2.slice(0, 12),
          });
        } else if (source === "ophim") {
          const res = await fetch("/api/ophim/danh-sach/phim-moi-cap-nhat?page=1");
          const data = await res.json();
          const items = data.items || data.data?.items || [];
          const movies = tagMovies(items.map((item: any) => ({
            _id: item._id || item.slug,
            name: item.name,
            slug: item.slug,
            origin_name: item.origin_name || "",
            type: item.type || "",
            poster_url: item.poster_url || item.thumb_url || "",
            thumb_url: item.thumb_url || item.poster_url || "",
            sub_docquyen: false,
            chipiloeng: false,
            time: "",
            episode_current: item.episode_current || "",
            quality: item.quality || "",
            lang: item.lang || "",
            year: item.year || 0,
            category: item.category || [],
            country: item.country || [],
          })), "ophim");
          setClientData({
            heroMovie: movies[0] || null,
            newMovies: movies.slice(1, 13),
            trendingMovies: movies.slice(13, 25),
            singleMovies: [],
            seriesMovies: [],
            animationMovies: [],
            nguoncMovies: [],
            moreToWatch: [],
          });
        } else if (source === "nguonc") {
          const res = await fetch("/api/nguonc/films/phim-moi-cap-nhat?page=1");
          const data = await res.json();
          const items = data.items || [];
          const movies = tagMovies(items.map((item: any) => ({
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
            category: (item.category || []).map((c: any) => ({ id: c.slug, name: c.name, slug: c.slug })),
            country: (item.country || []).map((c: any) => ({ id: c.slug, name: c.name, slug: c.slug })),
          })), "nguonc");
          setClientData({
            heroMovie: movies[0] || null,
            newMovies: movies.slice(1, 13),
            trendingMovies: movies.slice(13, 25),
            singleMovies: [],
            seriesMovies: [],
            animationMovies: [],
            nguoncMovies: [],
            moreToWatch: [],
          });
        }
      } catch {
        setClientData(null);
      } finally {
        setLoading(false);
      }
    };
    fetchSourceData();
  }, [source]);

  const data = source === "all" ? serverData : (clientData || serverData);

  if (loading) {
    return (
      <div className="pt-16">
        <div className="flex items-center justify-center min-h-[60vh]">
          <div className="w-10 h-10 border-2 border-accent border-t-transparent rounded-full animate-spin" />
        </div>
      </div>
    );
  }

  return (
    <div className="pt-16">
      {data.heroMovie && <HeroSection movie={data.heroMovie} />}
      <div className="max-w-7xl mx-auto py-10">
        {data.newMovies.length > 0 && (
          <MovieShelf title="New Releases" movies={data.newMovies} seeAllHref="/danh-sach/phim-le" />
        )}
        {data.trendingMovies.length > 0 && (
          <MovieShelf title="Trending Now" movies={data.trendingMovies} seeAllHref="/danh-sach/phim-bo" />
        )}
        {data.singleMovies.length > 0 && (
          <MovieShelf title="Movies" movies={data.singleMovies} seeAllHref="/danh-sach/phim-le" />
        )}
        {data.seriesMovies.length > 0 && (
          <MovieShelf title="Series" movies={data.seriesMovies} seeAllHref="/danh-sach/phim-bo" />
        )}
        {data.animationMovies.length > 0 && (
          <MovieShelf title="Animation" movies={data.animationMovies} seeAllHref="/danh-sach/hoat-hinh" />
        )}
        {data.nguoncMovies.length > 0 && (
          <MovieShelf title="More Movies" movies={data.nguoncMovies} seeAllHref="/danh-sach/phim-le" />
        )}
        {data.moreToWatch.length > 0 && (
          <MovieShelf title="You May Like" movies={data.moreToWatch} seeAllHref="/danh-sach/tv-shows" />
        )}
      </div>
      <Footer />
    </div>
  );
}
