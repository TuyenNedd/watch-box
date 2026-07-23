import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "phimimg.com",
        pathname: "/**",
      },
      {
        protocol: "https",
        hostname: "img.ophim.live",
        pathname: "/**",
      },
    ],
  },
};

export default nextConfig;
