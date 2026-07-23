"use client";

import Link from "next/link";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  basePath: string;
}

export default function Pagination({ currentPage, totalPages, basePath }: PaginationProps) {
  if (totalPages <= 1) return null;

  const getPageNumbers = (): (number | "...")[] => {
    const pages: (number | "...")[] = [];
    const maxVisible = 5;

    if (totalPages <= maxVisible + 2) {
      for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else {
      pages.push(1);
      if (currentPage > 3) pages.push("...");

      const start = Math.max(2, currentPage - 1);
      const end = Math.min(totalPages - 1, currentPage + 1);

      for (let i = start; i <= end; i++) pages.push(i);

      if (currentPage < totalPages - 2) pages.push("...");
      pages.push(totalPages);
    }
    return pages;
  };

  const getHref = (page: number) => {
    return `${basePath}?page=${page}`;
  };

  const pageNumbers = getPageNumbers();

  return (
    <nav className="flex items-center justify-center gap-2 mt-10" aria-label="Pagination">
      {/* Previous */}
      {currentPage > 1 ? (
        <Link
          href={getHref(currentPage - 1)}
          className="px-4 py-2 text-sm font-medium text-gray-300 glass rounded-lg hover:bg-white/10 transition-colors"
        >
          Previous
        </Link>
      ) : (
        <span className="px-4 py-2 text-sm font-medium text-gray-600 glass rounded-lg cursor-not-allowed opacity-50">
          Previous
        </span>
      )}

      {/* Page numbers */}
      <div className="hidden sm:flex items-center gap-1">
        {pageNumbers.map((page, idx) =>
          page === "..." ? (
            <span key={`ellipsis-${idx}`} className="px-3 py-2 text-gray-500 text-sm">
              ...
            </span>
          ) : (
            <Link
              key={page}
              href={getHref(page)}
              className={`px-3.5 py-2 text-sm font-medium rounded-lg transition-colors ${
                page === currentPage
                  ? "bg-accent text-white"
                  : "text-gray-300 hover:bg-white/10"
              }`}
            >
              {page}
            </Link>
          )
        )}
      </div>

      {/* Mobile page indicator */}
      <span className="sm:hidden text-sm text-gray-400">
        Page {currentPage} of {totalPages}
      </span>

      {/* Next */}
      {currentPage < totalPages ? (
        <Link
          href={getHref(currentPage + 1)}
          className="px-4 py-2 text-sm font-medium text-gray-300 glass rounded-lg hover:bg-white/10 transition-colors"
        >
          Next
        </Link>
      ) : (
        <span className="px-4 py-2 text-sm font-medium text-gray-600 glass rounded-lg cursor-not-allowed opacity-50">
          Next
        </span>
      )}
    </nav>
  );
}
