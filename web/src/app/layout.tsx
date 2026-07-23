import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import Navbar from "@/components/Navbar";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "WatchBox - Xem Phim Online",
  description: "Xem phim online miễn phí chất lượng cao",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body className={`${inter.className} bg-background min-h-screen`}>
        <Navbar />
        <main>{children}</main>
      </body>
    </html>
  );
}
