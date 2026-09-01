import React from "react";
import "@/styles/globals.css";
import { AuthProvider } from "@/context/AuthContext";

export const metadata = {
  title: "AI Release Readiness Analyzer",
  description: "AI-powered automated release readiness and risk scoring analyzer",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-slate-50 text-slate-900 antialiased">
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}


