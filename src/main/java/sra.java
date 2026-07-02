import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import com.google.gson.*;

public class sra {
    public static void main(String[] args) throws Exception {

        SpotifyClient api = SpotifyClient.fromEnv();
        if (api == null) {
            System.err.println("Set SPOTIFY_CLIENT_ID and SPOTIFY_CLIENT_SECRET env vars.");
            return;
        }
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Soundtrack Recommender (Block-Safe) ===");
        System.out.println("Choose a mood:");
        for (int i = 0; i < Mood.values().length; i++) System.out.printf("[%d] %s%n", i + 1, Mood.values()[i]);
        System.out.print("Selection: ");
        int idx = safeInt(sc.nextLine(), 1);
        if (idx < 1 || idx > Mood.values().length) { System.out.println("Invalid selection."); return; }
        Mood mood = Mood.values()[idx - 1];
        System.out.print("How many?  ");
        int howMany = Math.max(1, Math.min(50, safeInt(sc.nextLine(), 10)));
        List<Track> recs = recommend(api, mood, howMany);
        if (recs.isEmpty()) { System.out.println("No matches. Try another mood."); return; }
        System.out.println("\nRecommendations:");
        for (int i = 0; i < recs.size(); i++) {
            Track t = recs.get(i);
            System.out.printf("%2d) %s — %s | album: %s | pop: %d | %s%n",
                    i + 1, t.name, String.join(", ", t.artists), t.albumName, t.popularity, t.externalUrl);
        }
    }
    static int safeInt(String s, int def) { try { return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }

    static List<Track> recommend(SpotifyClient api, Mood mood, int howMany) throws Exception {
        MoodProfile p = MoodProfile.forMood(mood);
        List<String> base = Arrays.asList("cinematic", "film score", "score", "suite", "theme", "soundtrack", "orchestral", "ost");
        List<String> queries = new ArrayList<>();
        queries.addAll(base);
        queries.addAll(p.seedArtists);
        for (String plus : p.positiveTerms) queries.add(plus + " score");

        Map<String, Track> bag = new LinkedHashMap<>();
        for (String q : queries) {
            for (Track t : api.searchTracks(q, 25)) bag.putIfAbsent(t.id, t);
            if (bag.size() > howMany * 6) break;
        }
        for (Track t : bag.values()) {
            double s = 0;
            String hay = (t.name + " " + t.albumName + " " + String.join(" ", t.artists)).toLowerCase();
            for (String w : p.positiveTerms) if (hay.contains(w)) s += 2.0;
            for (String w : Arrays.asList("score","suite","theme","ost","soundtrack","cinematic","orchestra","orchestral"))
                if (hay.contains(w)) s += 1.2;
            for (String a : p.seedArtists) if (hay.toLowerCase().contains(a.toLowerCase())) s += 2.5;
            for (String w : p.negativeTerms) if (hay.contains(w)) s -= 1.5;
            s += (t.popularity / 100.0);
            t.ruleScore = s;
        }
        List<Track> all = new ArrayList<>(bag.values());
        all.sort((a,b) -> Double.compare(b.ruleScore, a.ruleScore));
        return all.size() > howMany ? all.subList(0, howMany) : all;
    }
}

/* ---- Mood profiles ---- */
enum Mood { ACTION, ROMANTIC, EPIC, DRAMA, MYSTERY }

class MoodProfile {
    final List<String> positiveTerms, negativeTerms, seedArtists;
    private MoodProfile(List<String> pos, List<String> neg, List<String> artists) {
        this.positiveTerms = pos; this.negativeTerms = neg; this.seedArtists = artists;
    }
    static MoodProfile forMood(Mood m) {
        return switch (m) {
            case ACTION -> new MoodProfile(
                    Arrays.asList("action","intense","battle","epic","chase","dark","drive","power","rise"),
                    Arrays.asList("acoustic version","lullaby","lofi","remix","cover"),
                    Arrays.asList("Hans Zimmer","Junkie XL","Brian Tyler","Lorne Balfe","Steve Jablonsky")
            );
            case EPIC -> new MoodProfile(
                    Arrays.asList("epic","trailer","anthem","rise","immortal","legends","glory"),
                    Arrays.asList("lofi","piano cover","remix","karaoke"),
                    Arrays.asList("Two Steps From Hell","Audiomachine","Immediate Music","Thomas Bergersen")
            );
            case ROMANTIC -> new MoodProfile(
                    Arrays.asList("romance","love theme","love","tender","serenade","quiet"),
                    Arrays.asList("battle","chase","metal","dubstep","hard"),
                    Arrays.asList("Joe Hisaishi","Max Richter","Alexandre Desplat","Nicholas Britell")
            );
            case DRAMA -> new MoodProfile(
                    Arrays.asList("drama","melancholy","strings","piano","memoir","farewell","lament"),
                    Arrays.asList("club","dance","remix","metal"),
                    Arrays.asList("Hildur Guðnadóttir","Trent Reznor","Atticus Ross","Dario Marianelli")
            );
            case MYSTERY -> new MoodProfile(
                    Arrays.asList("mystery","noir","investigation","enigmatic","whisper","shadow","tense"),
                    Arrays.asList("happy","party","summer mix","dance"),
                    Arrays.asList("Clint Mansell","Ben Frost","Johann Johannsson","Ramin Djawadi")
            );
        };
    }
}

class Track {
    String id;
    String name;
    List<String> artists = new ArrayList<>();
    String albumName = "";
    int popularity = 0;
    int durationMs = 0;
    String externalUrl;
    double ruleScore;
}

class SpotifyClient {
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String API_BASE = "https://api.spotify.com/v1";
    private final String clientId;
    private final String clientSecret;
    private final HttpClient http = HttpClient.newHttpClient();
    private String accessToken;
    private Instant tokenExpiry = Instant.EPOCH;
    private final Gson gson = new Gson();

    private SpotifyClient(String id, String secret) { this.clientId = id; this.clientSecret = secret; }
    static SpotifyClient fromEnv() throws Exception {
        String id = System.getenv("SPOTIFY_CLIENT_ID");
        String secret = System.getenv("SPOTIFY_CLIENT_SECRET");
        if (id == null || id.isBlank() || secret == null || secret.isBlank()) return null;
        return new SpotifyClient(id, secret);
    }

    List<Track> searchTracks(String keyword, int limit) throws Exception {
        ensureToken();
        String q = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = API_BASE + "/search?q=" + q + "&type=track&limit=" + Math.min(50, Math.max(1, limit));
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + accessToken).GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 401) { accessToken = null; ensureToken(); return searchTracks(keyword, limit); }
        if (resp.statusCode() >= 300) throw new RuntimeException("Search error: " + resp);
        JsonObject root = gson.fromJson(resp.body(), JsonObject.class);
        JsonArray items = root.getAsJsonObject("tracks").getAsJsonArray("items");
        List<Track> out = new ArrayList<>();
        for (JsonElement el : items) {
            JsonObject o = el.getAsJsonObject();
            Track t = new Track();
            t.id = o.get("id").getAsString();
            t.name = o.get("name").getAsString();
            for (JsonElement a : o.getAsJsonArray("artists")) t.artists.add(a.getAsJsonObject().get("name").getAsString());
            JsonObject album = o.getAsJsonObject("album");
            if (album != null && album.has("name")) t.albumName = album.get("name").getAsString();
            if (o.has("popularity")) t.popularity = o.get("popularity").getAsInt();
            if (o.has("duration_ms")) t.durationMs = o.get("duration_ms").getAsInt();
            JsonObject ext = o.getAsJsonObject("external_urls");
            t.externalUrl = ext != null && ext.has("spotify") ? ext.get("spotify").getAsString() : "";
            out.add(t);
        }
        return out;
    }

    private void ensureToken() throws Exception {
        if (accessToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(30))) return;
        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.newBuilder(URI.create(TOKEN_URL))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) {
            throw new RuntimeException("Token error: (POST " + TOKEN_URL + ") "
                    + resp.statusCode() + " | body=" + resp.body());
        }
        JsonObject o = JsonParser.parseString(resp.body()).getAsJsonObject();
        accessToken = o.get("access_token").getAsString();
        tokenExpiry = Instant.now().plusSeconds(o.get("expires_in").getAsInt());
    }
}
