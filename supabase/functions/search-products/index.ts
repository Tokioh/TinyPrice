const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json; charset=utf-8",
};

const requestTimes = new Map<string, number[]>();
const MAX_REQUESTS_PER_MINUTE = 20;

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: corsHeaders });
}

function isRateLimited(request: Request) {
  const now = Date.now();
  const key = request.headers.get("x-forwarded-for")?.split(",")[0]?.trim() || "local-client";
  const recentRequests = (requestTimes.get(key) || []).filter((timestamp) => timestamp > now - 60_000);

  if (recentRequests.length >= MAX_REQUESTS_PER_MINUTE) {
    requestTimes.set(key, recentRequests);
    return true;
  }

  recentRequests.push(now);
  requestTimes.set(key, recentRequests);
  return false;
}

function offerFromResult(result: Record<string, unknown>, position: number, query: string) {
  const rawUrl = typeof result.url === "string" ? result.url : "";
  let sourceUrl = "";
  const siteName = typeof result.site_name === "string" ? result.site_name : "";
  let store = siteName;
  try {
    const parsedUrl = new URL(rawUrl);
    if (parsedUrl.protocol === "http:" || parsedUrl.protocol === "https:") {
      sourceUrl = parsedUrl.toString();
      store ||= parsedUrl.hostname.replace(/^www\./, "");
    }
  } catch {
    store ||= "Fuente web";
  }

  return {
    id: `${position}-${sourceUrl || query}`,
    store,
    productName: typeof result.title === "string" ? result.title : query,
    price: "Ver en fuente",
    originalPrice: null,
    shipping: "Consulta en tienda",
    delivery: "Disponibilidad por confirmar",
    rating: "—",
    sourceUrl,
    summary: typeof result.snippet === "string" ? result.snippet : "Resultado encontrado en la web.",
    tags: ["Resultado web"],
    isBestValue: false,
  };
}

export async function searchProducts(request: Request): Promise<Response> {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (request.method !== "POST") {
    return json({ error: "Usa una solicitud POST para buscar." }, 405);
  }

  let query = "";
  try {
    const body = await request.json();
    query = typeof body?.query === "string" ? body.query.trim() : "";
  } catch {
    return json({ error: "El cuerpo de la solicitud debe ser JSON válido." }, 400);
  }

  if (query.length < 2 || query.length > 200) {
    return json({ error: "La búsqueda debe tener entre 2 y 200 caracteres." }, 400);
  }
  if (isRateLimited(request)) {
    return json({ error: "Demasiadas búsquedas. Inténtalo en un minuto." }, 429);
  }

  const apiKey = Deno.env.get("TINYFISH_API_KEY");
  if (!apiKey) {
    return json({
      code: "TINYFISH_NOT_CONFIGURED",
      error: "La búsqueda en vivo aún no está configurada.",
    }, 503);
  }

  const searchUrl = new URL("https://api.search.tinyfish.ai");
  searchUrl.searchParams.set("query", query);
  searchUrl.searchParams.set("purpose", "Encontrar páginas de compra y ofertas relevantes para comparar productos.");
  searchUrl.searchParams.set("location", "EC");
  searchUrl.searchParams.set("language", "es");

  try {
    const tinyFishResponse = await fetch(searchUrl, {
      headers: { "X-API-Key": apiKey, Accept: "application/json" },
    });
    if (!tinyFishResponse.ok) {
      console.error("TinyFish search failed", { status: tinyFishResponse.status });
      return json({ error: "No fue posible consultar las fuentes web ahora." }, 502);
    }

    const payload = await tinyFishResponse.json();
    const results = Array.isArray(payload.results) ? payload.results : [];
    return json({
      query,
      offers: results.slice(0, 10).map((result, index) => offerFromResult(result, index + 1, query)),
      searchedAt: new Date().toISOString(),
    });
  } catch (error) {
    console.error("TinyFish request could not be completed", error);
    return json({ error: "La búsqueda web no está disponible en este momento." }, 502);
  }
}

Deno.serve(searchProducts);
