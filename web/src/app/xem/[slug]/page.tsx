"use client";

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import VideoPlayer from "@/components/VideoPlayer";
import { MovieDetail, EpisodeData } from "@/types/movie";
import { getImageUrl } from "@/lib/api";

interface WatchPageProps {
  params: Promise<{ slug: string }>;
}

export default function WatchPage({ params }: WatchPageProps) {
  const searchParams = useSearchParams();
  const [slug, setSlug] = useState<string>("");
  const [data, setData] = useState<MovieDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [currentEp, setCurrentEp] = useState(0);
  const [currentServer, setCurrentServer] = useState(0);

  useEffect(() => {
    params.then((p) => setSlug(p.slug));
  }, [params]);

  useEffect(() => {
    if (!slug) return;

    const epParam = searchParams.get("ep");
    const svParam = searchParams.get("sv");
    if (epParam) setCurrentEp(parseInt(epParam));
    if (svParam) setCurrentServer(parseInt(svParam));

    fetch(`/api/phim/${slug}`)
      .then((res) => res.json())
      .then((d) => {
        setData(d);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [slug, searchParams]);

  if (loading) {
    return (
      <div className="pt-24 flex justify-center">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!data || !data.movie) {
    return (
      <div className="pt-24 text-center text-gray-400">
        Không tìm thấy phim
      </div>
    );
  }

  const movie = data.movie;
  const episodes = data.episodes || [];
  const serverData = episodes[currentServer]?.server_data || [];
  const currentEpisode: EpisodeData | undefined = serverData[currentEp];
  const videoSrc = currentEpisode?.link_m3u8 || "";

  return (
    <div className="pt-20 pb-12 px-4 max-w-7xl mx-auto">
      {/* Video Player */}
      <VideoPlayer
        src={videoSrc}
        poster={getImageUrl(movie.thumb_url)}
      />

      {/* Movie Info */}
      <div className="mt-6">
        <h1 className="text-xl md:text-2xl font-bold text-white">
          {movie.name}
        </h1>
        <p className="text-gray-400 text-sm mt-1">
          {movie.origin_name}
          {currentEpisode && ` - ${currentEpisode.name}`}
        </p>
        <Link
          href={`/phim/${movie.slug}`}
          className="text-accent text-sm hover:underline mt-2 inline-block"
        >
          ← Về trang chi tiết
        </Link>
      </div>

      {/* Server Selector */}
      {episodes.length > 1 && (
        <div className="mt-6">
          <p className="text-sm text-gray-400 mb-2">Server:</p>
          <div className="flex gap-2 flex-wrap">
            {episodes.map((server, idx) => (
              <button
                key={idx}
                onClick={() => {
                  setCurrentServer(idx);
                  setCurrentEp(0);
                }}
                className={`px-4 py-2 rounded-lg text-sm transition-colors ${
                  currentServer === idx
                    ? "bg-accent text-white"
                    : "bg-card text-gray-300 hover:bg-card-hover border border-white/10"
                }`}
              >
                {server.server_name}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Episode Selector */}
      {serverData.length > 1 && (
        <div className="mt-6">
          <p className="text-sm text-gray-400 mb-2">Chọn tập:</p>
          <div className="flex flex-wrap gap-2">
            {serverData.map((ep, idx) => (
              <button
                key={idx}
                onClick={() => setCurrentEp(idx)}
                className={`px-4 py-2 rounded-lg text-sm transition-colors ${
                  currentEp === idx
                    ? "bg-accent text-white"
                    : "bg-card text-gray-300 hover:bg-card-hover border border-white/10"
                }`}
              >
                {ep.name}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
