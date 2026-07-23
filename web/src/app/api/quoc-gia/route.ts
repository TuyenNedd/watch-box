import { NextResponse } from "next/server";

export async function GET() {
  try {
    const res = await fetch("https://phimapi.com/quoc-gia", {
      next: { revalidate: 3600 },
    });
    const data = await res.json();
    return NextResponse.json(data);
  } catch {
    return NextResponse.json(
      { error: "Failed to fetch countries" },
      { status: 500 }
    );
  }
}
