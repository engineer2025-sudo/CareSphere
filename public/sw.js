const SHELL_CACHE = "caresphere-shell-v5";
const ASSET_CACHE = "caresphere-assets-v5";
const SHELL_URL = new URL("/", self.location.origin).href;
const MEDIA_ASSETS = ["1F343", "1F33C", "1F426", "2600", "2601", "1F30A", "1F330", "1F344", "1F331", "1F333", "1F334", "1F335", "2615", "1F4D8", "1F4A1", "1FA91", "1F511", "1F570", "1F37D", "1F3E0", "1F6CF", "1F9F9", "1F9FC", "1F9F0", "1F34E", "1F35E", "1F350", "1F372", "1F35A", "1F34C", "1F955", "1F9C0", "1F33D", "1F34A", "1F353", "1F95A"].map((code) => `/media/openmoji/${code}.svg`).concat("/media/openmoji/ATTRIBUTION.txt");
const MAX_ASSETS = 80;
self.addEventListener("install", (event) => {
  event.waitUntil((async () => {
    const response = await fetch(new Request(SHELL_URL, { cache: "reload" }));
    if (!response.ok) throw new Error("App shell unavailable");
    const html = await response.clone().text();
    const assets = [...html.matchAll(/(?:src|href)=["'](\/assets\/[^"']+)["']/g)].map((match) => match[1]);
    if (!assets.length) throw new Error("No built application assets found");
    const cache = await caches.open(ASSET_CACHE);
    await cache.addAll([... new Set([...assets, ...MEDIA_ASSETS, "/manifest.webmanifest", "/care-icon.svg", "/icon-192.png", "/icon-512.png"])]);
    await (await caches.open(SHELL_CACHE)).put(SHELL_URL, response);
    await self.skipWaiting();
  })());
});
self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) => Promise.all(keys.filter((key) => key.startsWith("caresphere-") && ![SHELL_CACHE, ASSET_CACHE].includes(key)).map((key) => caches.delete(key)))).then(() => self.clients.claim())
  );
});
async function cacheAsset(request, response) {
  if (!response.ok || response.type === "opaque") return;
  const cache = await caches.open(ASSET_CACHE);
  await cache.put(request, response.clone());
  const keys = await cache.keys();
  if (keys.length > MAX_ASSETS) {
    await Promise.all(keys.slice(0, keys.length - MAX_ASSETS).map((key) => cache.delete(key)));
  }
}
self.addEventListener("fetch", (event) => {
  const request = event.request;
  if (request.method !== "GET") return;
  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;
  if (request.mode === "navigate") {
    event.respondWith((async () => {
      try {
        const response = await fetch(request);
        if (response.ok) {
          const cache = await caches.open(SHELL_CACHE);
          await cache.put(SHELL_URL, response.clone());
        }
        return response;
      } catch {
        const cache = await caches.open(SHELL_CACHE);
        return await cache.match(request.url) || await cache.match(SHELL_URL) || Response.error();
      }
    })());
    return;
  }
  const isAppAsset = url.pathname.startsWith("/assets/") || /^\/media\/openmoji\/(?:[0-9A-F]+\.svg|ATTRIBUTION\.txt)$/.test(url.pathname) || /^\/(?:care-icon\.svg|icon-\d+\.png|manifest\.webmanifest)$/.test(url.pathname);
  if (!isAppAsset) return;
  event.respondWith((async () => {
    const cache = await caches.open(ASSET_CACHE);
    const cached = await cache.match(request, { ignoreVary: true });
    if (cached) return cached;
    const response = await fetch(request);
    await cacheAsset(request, response);
    return response;
  })());
});
