import { NextResponse } from "next/server";

export async function GET() {
  try {
    const res = await fetch("https://phimapi.com/the-loai", {
      next: { revalidate: 3600 },
    });
    const data = await res.json();
    return NextResponse.json(data);
  } catch {
    return NextResponse.json(
      { error: "Failed to fetch genres" },
      { status: 500 }
    );
  }
}
