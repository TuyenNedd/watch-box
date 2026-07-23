import { MovieListResponse, MovieDetail, SearchResponse } from "@/types/movie";

const BASE_URL = typeof window !== "undefined" ? "" : "http://localhost:3000";

// --- PhimAPI functions (primary) ---

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

export async function getMoviesByType(type: string, page: number = 1) {
  const res = await fetch(`${BASE_URL}/api/danh-sach/${type}?page=${page}`, {
    next: { revalidate: 300 },
  });
  if (!res.ok) throw new Error("Failed to fetch movies by type");
  return res.json();
}

export async function getGenres() {
  const res = await fetch(`${BASE_URL}/api/the-loai`, {
    next: { revalidate: 3600 },
  });
  if (!res.ok) throw new Error("Failed to fetch genres");
  return res.json();
}

export async function getMoviesByGenre(slug: string, page: number = 1) {
  const res = await fetch(`${BASE_URL}/api/the-loai/${slug}?page=${page}`, {
    next: { revalidate: 300 },
  });
  if (!res.ok) throw new Error("Failed to fetch movies by genre");
  return res.json();
}

export async function getCountries() {
  const res = await fetch(`${BASE_URL}/api/quoc-gia`, {
    next: { revalidate: 3600 },
  });
  if (!res.ok) throw new Error("Failed to fetch countries");
  return res.json();
}

export async function getMoviesByCountry(slug: string, page: number = 1) {
  const res = await fetch(`${BASE_URL}/api/quoc-gia/${slug}?page=${page}`, {
    next: { revalidate: 300 },
  });
  if (!res.ok) throw new Error("Failed to fetch movies by country");
  return res.json();
}

export function getImageUrl(path: string): string {
  if (!path) return "/placeholder.png";
  if (path.startsWith("http")) return path;
  return `https://phimimg.com/${path}`;
}

// --- NguonC functions (additional source) ---

export interface NguonCListResponse {
  status: string;
  paginate: {
    current_page: number;
    total_page: number;
    total_items: number;
    items_per_page: number;
  } | null;
  items: NguonCItem[];
}

export interface NguonCItem {
  name: string;
  slug: string;
  original_name: string;
  thumb_url: string;
  poster_url: string;
  description: string;
  total_episodes: number | null;
  current_episode: string;
  language: string;
  quality: string;
  year: number | null;
  category: { name: string; slug: string }[];
  country: { name: string; slug: string }[];
}

export interface NguonCDetailResponse {
  status: string;
  movie: {
    name: string;
    slug: string;
    original_name: string;
    thumb_url: string;
    poster_url: string;
    description: string;
    total_episodes: number | null;
    current_episode: string;
    language: string;
    quality: string;
    year: number | null;
    category: { name: string; slug: string }[];
    country: { name: string; slug: string }[];
    episodes: {
      server_name: string;
      items: { name: string; slug: string; embed: string; m3u8: string }[];
    }[];
  } | null;
}

export async function getNguonCMovieList(page: number = 1): Promise<NguonCListResponse> {
  try {
    const res = await fetch(`${BASE_URL}/api/nguonc/films/phim-moi-cap-nhat?page=${page}`, {
      next: { revalidate: 300 },
    });
    return res.json();
  } catch {
    return { status: "error", paginate: null, items: [] };
  }
}

export async function getNguonCMovieDetail(slug: string): Promise<NguonCDetailResponse> {
  try {
    const res = await fetch(`${BASE_URL}/api/nguonc/film/${slug}`, {
      next: { revalidate: 300 },
    });
    return res.json();
  } catch {
    return { status: "error", movie: null };
  }
}

export async function searchNguonCMovies(query: string): Promise<NguonCListResponse> {
  try {
    const res = await fetch(
      `${BASE_URL}/api/nguonc/films/search?keyword=${encodeURIComponent(query)}`,
      { next: { revalidate: 60 } }
    );
    return res.json();
  } catch {
    return { status: "error", paginate: null, items: [] };
  }
}

export async function getNguonCByGenre(slug: string, page: number = 1): Promise<NguonCListResponse> {
  try {
    const res = await fetch(`${BASE_URL}/api/nguonc/films/the-loai/${slug}?page=${page}`, {
      next: { revalidate: 300 },
    });
    return res.json();
  } catch {
    return { status: "error", paginate: null, items: [] };
  }
}

export async function getNguonCByCountry(slug: string, page: number = 1): Promise<NguonCListResponse> {
  try {
    const res = await fetch(`${BASE_URL}/api/nguonc/films/quoc-gia/${slug}?page=${page}`, {
      next: { revalidate: 300 },
    });
    return res.json();
  } catch {
    return { status: "error", paginate: null, items: [] };
  }
}

export async function getNguonCByCategory(slug: string, page: number = 1): Promise<NguonCListResponse> {
  try {
    const res = await fetch(`${BASE_URL}/api/nguonc/films/danh-sach/${slug}?page=${page}`, {
      next: { revalidate: 300 },
    });
    return res.json();
  } catch {
    return { status: "error", paginate: null, items: [] };
  }
}

// --- OPhim functions (additional source) ---

export interface OPhimListResponse {
  status: string;
  items?: OPhimItem[];
  data?: {
    items?: OPhimItem[];
  };
  paginate?: {
    current_page: number;
    total_page: number;
    total_items: number;
    items_per_page: number;
  };
  pagination?: {
    totalItems: number;
    totalItemsPerPage: number;
    currentPage: number;
    totalPages: number;
  };
}

export interface OPhimItem {
  _id: string;
  name: string;
  slug: string;
  origin_name: string;
  thumb_url: string;
  poster_url: string;
  type: string;
  year: number;
  quality: string;
  lang: string;
  episode_current: string;
  category: { id: string; name: string; slug: string }[];
  country: { id: string; name: string; slug: string }[];
}

export interface OPhimDetailResponse {
  status: boolean | string;
  movie?: {
    _id: string;
    name: string;
    slug: string;
    origin_name: string;
    content: string;
    type: string;
    status: string;
    poster_url: string;
    thumb_url: string;
    sub_docquyen: boolean;
    time: string;
    episode_current: string;
    episode_total: string;
    quality: string;
    lang: string;
    year: number;
    actor: string[];
    director: string[];
    category: { id: string; name: string; slug: string }[];
    country: { id: string; name: string; slug: string }[];
    trailer_url: string;
  };
  episodes?: {
    server_name: string;
    server_data: {
      name: string;
      slug: string;
      filename: string;
      link_embed: string;
      link_m3u8: string;
    }[];
  }[];
}

export async function getOPhimMovieList(page: number = 1): Promise<OPhimListResponse> {
  try {
    const res = await fetch(`${BASE_URL}/api/ophim/danh-sach/phim-moi-cap-nhat?page=${page}`, {
      next: { revalidate: 300 },
    });
    return res.json();
  } catch {
    return { status: "error", items: [] };
  }
}

export async function getOPhimMovieDetail(slug: string): Promise<OPhimDetailResponse> {
  try {
    const res = await fetch(`${BASE_URL}/api/ophim/phim/${slug}`, {
      next: { revalidate: 300 },
    });
    return res.json();
  } catch {
    return { status: "error" };
  }
}

export async function searchOPhimMovies(query: string): Promise<OPhimListResponse> {
  try {
    const res = await fetch(
      `${BASE_URL}/api/ophim/v1/api/tim-kiem?keyword=${encodeURIComponent(query)}`,
      { next: { revalidate: 60 } }
    );
    return res.json();
  } catch {
    return { status: "error", items: [] };
  }
}

// --- Fallback utility ---

/**
 * Try fetching from PhimAPI first; if it fails or returns empty,
 * fall back to NguonC.
 */
export async function fetchWithFallback<T>(
  primaryFn: () => Promise<T>,
  fallbackFn: () => Promise<T>,
  isEmpty: (result: T) => boolean
): Promise<T> {
  try {
    const primary = await primaryFn();
    if (!isEmpty(primary)) return primary;
  } catch {
    // primary failed, try fallback
  }
  try {
    return await fallbackFn();
  } catch {
    throw new Error("All sources failed");
  }
}
