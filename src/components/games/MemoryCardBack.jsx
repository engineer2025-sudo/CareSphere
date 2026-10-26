import React from "react";
export default function MemoryCardBack({ free = false }) {
  const accent = free ? "#e7c15c" : "#d9bf79";
  const center = free ? "#e7c15c" : "#183d35";
  return <span className={`memory-card-back ${free ? "is-free" : ""}`} aria-hidden="true">
    <svg viewBox="0 0 80 80" focusable="false">
      <rect x="8" y="8" width="64" height="64" rx="13" fill="none" stroke={accent} strokeWidth="2" />
      <path d="M40 14 66 40 40 66 14 40Z" fill="none" stroke={accent} strokeWidth="3" />
      <path d="M40 24 56 40 40 56 24 40Z" fill="none" stroke={accent} strokeWidth="2" />
      <circle cx="40" cy="40" r="8" fill={center} stroke={accent} strokeWidth="3" />
      <path d="M40 4v13M40 63v13M4 40h13M63 40h13" stroke={accent} strokeWidth="2" strokeLinecap="round" />
    </svg>
  </span>;
}
