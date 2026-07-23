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
      <div className="pt-24 flex flex-col items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-2 border-accent border-t-transparent rounded-full animate-spin" />
        <p className="text-gray-500 text-sm mt-4">Loading...</p>
      </div>
    );
  }

  if (!data || !data.movie) {
    return (
      <div className="pt-24 text-center min-h-[60vh] flex flex-col items-center justify-center">
        <div className="w-16 h-16 rounded-full bg-white/5 flex items-center justify-center mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-7 h-7 text-gray-500">
            <path strokeLinecap="round" strokeLinejoin="round" d="m15.75 10.5 4.72-4.72a.75.75 0 0 1 1.28.53v11.38a.75.75 0 0 1-1.28.53l-4.72-4.72M4.5 18.75h9a2.25 2.25 0 0 0 2.25-2.25v-9a2.25 2.25 0 0 0-2.25-2.25h-9A2.25 2.25 0 0 0 2.25 7.5v9a2.25 2.25 0 0 0 2.25 2.25Z" />
          </svg>
        </div>
        <p className="text-gray-400 text-lg">Movie not found</p>
        <Link href="/" className="text-accent hover:underline text-sm mt-2">
          Back to Home
        </Link>
      </div>
    );
  }

  const movie = data.movie;
  const episodes = data.episodes || [];
  const serverData = episodes[currentServer]?.server_data || [];
  const currentEpisode: EpisodeData | undefined = serverData[currentEp];
  const videoSrc = currentEpisode?.link_m3u8 || "";
  const hasNextEp = currentEp < serverData.length - 1;
  const hasPrevEp = currentEp > 0;

  return (
    <div className="pt-16 pb-12 min-h-screen bg-black/40">
      {/* Theater mode: dark overlay effect */}
      <div className="fixed inset-0 bg-black/60 -z-10" />

      {/* Video Player */}
      <div className="max-w-6xl mx-auto px-4 pt-4">
        <VideoPlayer
          src={videoSrc}
          poster={getImageUrl(movie.thumb_url)}
        />

        {/* Keyboard shortcuts hint */}
        <div className="flex items-center gap-4 mt-3 text-xs text-gray-600">
          <span className="flex items-center gap-1">
            <kbd className="px-1.5 py-0.5 rounded bg-white/5 border border-white/10 text-gray-500 font-mono">Space</kbd>
            Pause
          </span>
          <span className="flex items-center gap-1">
            <kbd className="px-1.5 py-0.5 rounded bg-white/5 border border-white/10 text-gray-500 font-mono">F</kbd>
            Fullscreen
          </span>
          <span className="flex items-center gap-1">
            <kbd className="px-1.5 py-0.5 rounded bg-white/5 border border-white/10 text-gray-500 font-mono">&larr; &rarr;</kbd>
            Seek
          </span>
        </div>
      </div>

      {/* Movie Info & Controls */}
      <div className="max-w-6xl mx-auto px-4 mt-6">
        <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
          <div>
            <h1 className="text-xl md:text-2xl font-bold text-white">
              {movie.name}
            </h1>
            <p className="text-gray-500 text-sm mt-1">
              {movie.origin_name}
              {currentEpisode && ` \u2014 ${currentEpisode.name}`}
            </p>
            <Link
              href={`/phim/${movie.slug}`}
              className="text-accent text-sm hover:underline mt-2 inline-flex items-center gap-1"
            >
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-4 h-4">
                <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5 3 12m0 0 7.5-7.5M3 12h18" />
              </svg>
              Back to Details
            </Link>
          </div>

          {/* Next/Prev Episode Buttons */}
          {serverData.length > 1 && (
            <div className="flex items-center gap-2">
              <button
                onClick={() => hasPrevEp && setCurrentEp(currentEp - 1)}
                disabled={!hasPrevEp}
                className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm transition-all ${
                  hasPrevEp
                    ? "glass hover:bg-white/10 text-white"
                    : "bg-white/5 text-gray-600 cursor-not-allowed"
                }`}
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-4 h-4">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" />
                </svg>
                Previous
              </button>
              <button
                onClick={() => hasNextEp && setCurrentEp(currentEp + 1)}
                disabled={!hasNextEp}
                className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm transition-all ${
                  hasNextEp
                    ? "bg-accent hover:bg-accent-hover text-white"
                    : "bg-white/5 text-gray-600 cursor-not-allowed"
                }`}
              >
                Next
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-4 h-4">
                  <path strokeLinecap="round" strokeLinejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" />
                </svg>
              </button>
            </div>
          )}
        </div>

        {/* Server Selector */}
        {episodes.length > 1 && (
          <div className="mt-6">
            <p className="text-sm text-gray-500 mb-2 font-medium">Server:</p>
            <div className="flex gap-2 flex-wrap">
              {episodes.map((server, idx) => (
                <button
                  key={idx}
                  onClick={() => {
                    setCurrentServer(idx);
                    setCurrentEp(0);
                  }}
                  className={`px-4 py-2 rounded-lg text-sm transition-all duration-200 ${
                    currentServer === idx
                      ? "bg-accent text-white shadow-lg shadow-accent/20"
                      : "glass text-gray-300 hover:bg-white/10"
                  }`}
                >
                  {server.server_name}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Episode Grid */}
        {serverData.length > 1 && (
          <div className="mt-6">
            <p className="text-sm text-gray-500 mb-3 font-medium">Select Episode:</p>
            <div className="flex flex-wrap gap-2 max-h-48 overflow-y-auto pr-2">
              {serverData.map((ep, idx) => (
                <button
                  key={idx}
                  onClick={() => setCurrentEp(idx)}
                  className={`px-4 py-2.5 rounded-lg text-sm transition-all duration-200 ${
                    currentEp === idx
                      ? "bg-accent text-white shadow-lg shadow-accent/20 scale-105"
                      : "bg-white/5 text-gray-300 hover:bg-white/10 border border-white/5 hover:border-white/20"
                  }`}
                >
                  {ep.name}
                </button>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
