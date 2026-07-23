import Link from "next/link";

interface CategoryItem {
  _id?: string;
  name: string;
  slug: string;
}

interface CategoryGridProps {
  items: CategoryItem[];
  basePath: string;
  title: string;
}

export default function CategoryGrid({ items, basePath, title }: CategoryGridProps) {
  if (!items || items.length === 0) {
    return (
      <div className="text-center py-20">
        <p className="text-gray-400">No items found</p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl md:text-3xl font-bold text-white mb-8">{title}</h1>
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-3">
        {items.map((item) => (
          <Link
            key={item.slug}
            href={`${basePath}/${item.slug}`}
            className="group glass rounded-xl px-4 py-5 text-center hover:bg-white/10 hover:border-accent/30 transition-all duration-200 hover:scale-105"
          >
            <span className="text-sm font-medium text-gray-300 group-hover:text-accent transition-colors">
              {item.name}
            </span>
          </Link>
        ))}
      </div>
    </div>
  );
}
