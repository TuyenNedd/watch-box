import CategoryGrid from "@/components/CategoryGrid";
import Footer from "@/components/Footer";

interface Country {
  _id: string;
  name: string;
  slug: string;
}

async function getCountries(): Promise<Country[]> {
  try {
    const res = await fetch("https://phimapi.com/quoc-gia", {
      next: { revalidate: 3600 },
    });
    const data = await res.json();
    return Array.isArray(data) ? data : [];
  } catch {
    return [];
  }
}

export default async function CountriesPage() {
  const countries = await getCountries();

  return (
    <div className="pt-16 min-h-screen">
      <div className="max-w-7xl mx-auto px-4 py-10">
        <CategoryGrid items={countries} basePath="/quoc-gia" title="All Countries" />
      </div>
      <Footer />
    </div>
  );
}
