import { NextRequest, NextResponse } from "next/server";

const VALID_TYPES = ["phim-le", "phim-bo", "hoat-hinh", "tv-shows"];

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ type: string }> }
) {
  const { type } = await params;
  const searchParams = request.nextUrl.searchParams;
  const page = searchParams.get("page") || "1";

  if (!VALID_TYPES.includes(type)) {
    return NextResponse.json(
      { error: "Invalid type" },
      { status: 400 }
    );
  }

  try {
    const res = await fetch(
      `https://phimapi.com/v1/api/danh-sach/${type}?page=${page}`,
      { next: { revalidate: 300 } }
    );
    const data = await res.json();
    return NextResponse.json(data);
  } catch {
    return NextResponse.json(
      { error: "Failed to fetch movies" },
      { status: 500 }
    );
  }
}
