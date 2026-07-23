"use client";

import { createContext, useContext, useState, useEffect, ReactNode } from "react";

export type SourceType = "all" | "phimapi" | "ophim" | "nguonc";

interface SourceContextValue {
  source: SourceType;
  setSource: (source: SourceType) => void;
}

const SourceContext = createContext<SourceContextValue>({
  source: "all",
  setSource: () => {},
});

export function SourceProvider({ children }: { children: ReactNode }) {
  const [source, setSourceState] = useState<SourceType>("all");

  useEffect(() => {
    const saved = localStorage.getItem("watchbox_source") as SourceType | null;
    if (saved && ["all", "phimapi", "ophim", "nguonc"].includes(saved)) {
      setSourceState(saved);
    }
  }, []);

  const setSource = (newSource: SourceType) => {
    setSourceState(newSource);
    localStorage.setItem("watchbox_source", newSource);
  };

  return (
    <SourceContext.Provider value={{ source, setSource }}>
      {children}
    </SourceContext.Provider>
  );
}

export function useSource() {
  return useContext(SourceContext);
}
