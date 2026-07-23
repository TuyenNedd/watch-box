import CategoryGrid from "@/components/CategoryGrid";
import Footer from "@/components/Footer";

interface Genre {
  _id: string;
  name: string;
  slug: string;
}

async function getGenres(): Promise<Genre[]> {
  try {
    const res = await fetch("https://phimapi.com/the-loai", {
      next: { revalidate: 3600 },
    });
    const data = await res.json();
    return Array.isArray(data) ? data : [];
  } catch {
    return [];
  }
}

export default async function GenresPage() {
  const genres = await getGenres();

  return (
    <div className="pt-16 min-h-screen">
      <div className="max-w-7xl mx-auto px-4 py-10">
        <CategoryGrid items={genres} basePath="/the-loai" title="All Genres" />
      </div>
      <Footer />
    </div>
  );
}
