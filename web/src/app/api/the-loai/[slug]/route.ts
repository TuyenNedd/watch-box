import { NextRequest, NextResponse } from "next/server";

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ slug: string }> }
) {
  const { slug } = await params;
  const searchParams = request.nextUrl.searchParams;
  const page = searchParams.get("page") || "1";

  try {
    const res = await fetch(
      `https://phimapi.com/v1/api/the-loai/${slug}?page=${page}`,
      { next: { revalidate: 300 } }
    );
    const data = await res.json();
    return NextResponse.json(data);
  } catch {
    return NextResponse.json(
      { error: "Failed to fetch movies by genre" },
      { status: 500 }
    );
  }
}
