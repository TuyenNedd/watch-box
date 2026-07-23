export function SkeletonCard() {
  return (
    <div className="flex-shrink-0 w-36 md:w-44">
      <div className="relative aspect-[2/3] rounded-xl overflow-hidden skeleton" />
      <div className="mt-2 px-1">
        <div className="h-4 w-3/4 rounded skeleton" />
        <div className="h-3 w-1/2 rounded mt-1 skeleton" />
      </div>
    </div>
  );
}

export function SkeletonShelf() {
  return (
    <section className="mb-10">
      <div className="h-7 w-48 rounded skeleton mb-4 mx-4 md:mx-0" />
      <div className="flex gap-4 overflow-hidden px-4 md:px-0 pb-4">
        {Array.from({ length: 7 }).map((_, i) => (
          <SkeletonCard key={i} />
        ))}
      </div>
    </section>
  );
}

export function SkeletonGrid() {
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 mt-8">
      {Array.from({ length: 12 }).map((_, i) => (
        <div key={i}>
          <div className="relative aspect-[2/3] rounded-xl overflow-hidden skeleton" />
          <div className="mt-2 px-1">
            <div className="h-4 w-3/4 rounded skeleton" />
            <div className="h-3 w-1/2 rounded mt-1 skeleton" />
          </div>
        </div>
      ))}
    </div>
  );
}
