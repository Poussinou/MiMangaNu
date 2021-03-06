package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;

/**
 * Created by Raul on 05/04/2016.
 */
public class TuMangaOnline extends ServerBase {

    public static String[] type = new String[]{
            "Todos", "Manga", "Manhua", "Manhwa", "Novela", "Propio", "Otro"
    };
    private static String[] genres = new String[]{
            "Acción", "Apocalíptico", "Artes Marciales", "Aventura", "Ciencia Ficción",
            "Comedia", "Cyberpunk", "Demonios", "Deportes", "Drama", "Ecchi", "Fantasía",
            "Gender Bender", "Gore", "Harem", "Histórico", "Horror", "Magia", "Mecha", "Militar",
            "Misterio", "Musical", "Parodia", "Policial", "Psicológico", "Realidad Virtual",
            "Recuentos de la vida", "Reencarnación", "Romance", "Samurai", "Sobrenatural",
            "Super Poderes", "Supervivencia", "Suspense", "Thiller", "Tragedia", "Vampiros",
            "Vida Escolar", "Yuri"
    };
    private static String[] genresValues = new String[]{
            "1", "24", "33", "2", "14", "3", "37", "41", "16", "4", "6", "7", "35", "23", "19",
            "27", "10", "8", "20", "28", "11", "38", "39", "40", "12", "36", "5", "22", "13", "34",
            "9", "31", "21", "15", "30", "25", "32", "26", "17"
    };
    private static String[] demografia = {
            "Todos", "Seinen", "Shoujo", "Shounen", "Josei", "Kodomo", "Otros"
    };
    private static String[] demografiaV = {
            "", "Seinen", "Shoujo", "Shounen", "Josei", "Kodomo", "Otros"
    };
    private static String[] estado = {
            "Todos", "Activo", "Abandonado", "Finalizado"
    };
    private static String[] estadoV = {
            "", "Activo", "Abandonado", "Finalizado"
    };
    private static String[] typeV = new String[]{
            "", "Manga", "Manhua", "Manhwa", "Novela", "Propio", "Otro"
    };

    private static String[] categoria = new String[]{
            "Dōjinshi", "One-Shot", "Webtoon", "Yonkoma"
    };

    private static String[] categoriaV = new String[]{
            "2", "1", "3", "4"
    };

    private static String[] sortBy = new String[]{
            "Alfabetico ↓", "Ranking ↓", "Número de lecturas ↓", "Fecha de creacion ↓",
            "Alfabetico ↑", "Ranking ↑", "Número de lecturas ↑", "Fecha de creacion ↑"
    };

    private static String[] sortByValues = new String[]{
            "&sortDir=asc&sortedBy=nombre", "&sortDir=desc&sortedBy=puntuacion",
            "&sortDir=desc&sortedBy=numVistos", "&sortDir=desc&sortedBy=fechaCreacion",
            "&sortDir=desc&sortedBy=nombre", "&sortDir=asc&sortedBy=puntuacion",
            "&sortDir=asc&sortedBy=numVistos", "&sortDir=asc&sortedBy=fechaCreacion"
    };

    private static int lastPage = 10000;

    public TuMangaOnline() {
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.tumangaonline_icon);
        this.setServerName("TuMangaOnline");
        setServerID(ServerBase.TUMANGAONLINE);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        JSONObject jsonObject = new JSONObject(getNavigatorAndFlushParameters().get("http://www.tumangaonline.com/api/v1/mangas?categorias=%5B%5D&generos=%5B%5D&itemsPerPage=20&nameSearch=" + URLEncoder.encode(term, "UTF-8") + "&page=1&puntuacion=0&searchBy=nombre&sortDir=asc&sortedBy=nombre"));
        return getMangasJsonArray(jsonObject.getJSONArray("data"));
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadChapters(manga, forceReload, false);
    }

    public void loadChapters(Manga manga, boolean forceReload, boolean last) throws Exception {
        ArrayList<Chapter> result = new ArrayList<>();
        String data = getNavigatorAndFlushParameters().get("http://www.tumangaonline.com/api/v1/mangas/" + manga.getPath() + "/capitulos?page=" + 1 + "&tomo=-1");
        if (data != null && data.length() > 3) {
            JSONObject object = new JSONObject(data);
            int last_page = object.getInt("last_page");
            result.addAll(0, getChaptersJsonArray(object.getJSONArray("data"), manga.getPath()));
            if (!last)
                for (int i = 2; i <= last_page; i++) {
                    try {
                        data = getNavigatorAndFlushParameters().get("http://www.tumangaonline.com/api/v1/mangas/" + manga.getPath() + "/capitulos?page=" + i + "&tomo=-1");
                        if (data != null && data.length() > 3) {
                            object = new JSONObject(data);
                            result.addAll(0, getChaptersJsonArray(object.getJSONArray("data"), manga.getPath()));
                        }
                    } catch (Exception ignore) {
                    }
                }
        }
        manga.setChapters(result);
    }

    private ArrayList<Chapter> getChaptersJsonArray(JSONArray jsonArray, String mid) {
        ArrayList<Chapter> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                Chapter c = new Chapter("Capítulo " + object.getString("numCapitulo") + " " + (object.getString("nombre").equalsIgnoreCase("null") ? "" : object.getString("nombre")), getServerID() + "_" + mid + "_" + object.getString("numCapitulo"));
                c.setExtra(mid +
                        "/" + object.getString("numCapitulo") + "/" + object.getJSONArray("subidas").getJSONObject(0).getString("idScan") + "|" +
                        object.getJSONArray("subidas").getJSONObject(0).getString("imagenes"));
                result.add(0, c);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        JSONObject object = new JSONObject(getNavigatorAndFlushParameters().get("http://www.tumangaonline.com/api/v1/mangas/" + manga.getPath()));
        manga.setImages("http://img1.tumangaonline.com/" + object.getString("imageUrl"));
        manga.setSynopsis(object.getJSONObject("info").getString("sinopsis"));
        if (object.getJSONArray("autores").length() != 0) {
            manga.setAuthor(object.getJSONArray("autores").getJSONObject(0).getString("autor"));
        }
        {
            String genres = "";
            JSONArray array = object.getJSONArray("generos");
            for (int i = 0; i < array.length(); i++) {
                if (genres.equals("")) {
                    genres = array.getJSONObject(i).getString("genero");
                } else {
                    genres += ", " + array.getJSONObject(i).getString("genero");
                }
            }
            manga.setGenre(genres);
        }
        manga.setFinished(!object.getString("estado").contains("Activo"));
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String[] d1 = chapter.getExtra().split("\\|");
        return "http://img1.tumangaonline.com/subidas/" + d1[page];
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String[] d1 = chapter.getExtra().split("\\|");
        String[] d2 = (d1[1].replace("[", "").replace("]", "").replaceAll("\"", "")).split(",");
        chapter.setPages(d2.length);
        String images = "";
        for (String d : d2) {
            images = images + "|" + d1[0] + "/" + d;
        }
        chapter.setExtra(images);
    }

    ArrayList<Manga> getMangasJsonArray(JSONArray jsonArray) {
        ArrayList<Manga> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                Manga m = new Manga(getServerID(), object.getString("nombre"), object.getString("id"), "Finalizado".contains(object.getString("estado")));
                m.setImages("http://img1.tumangaonline.com/" + object.getString("imageUrl").replaceAll("\\\\", ""));
                result.add(m);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public int searchForNewChapters(int id, Context context, boolean fast) throws Exception {//TODO FAST
        int returnValue = 0;
        Manga mangaDb = Database.getFullManga(context, id);
        Manga manga = new Manga(mangaDb.getServerId(), mangaDb.getTitle(), mangaDb.getPath(), false);
        manga.setId(mangaDb.getId());
        this.loadMangaInformation(manga, true);
        loadChapters(manga, false, true);

        ArrayList<Chapter> notAdd = new ArrayList<>();

        for (Chapter c : manga.getChapters()) {
            for (Chapter csl : mangaDb.getChapters()) {
                if (c.getPath().equalsIgnoreCase(csl.getPath())) {
                    notAdd.add(c);
                    break;
                }
            }
        }

        manga.getChapters().removeAll(notAdd);

        for (Chapter chapter : manga.getChapters()) {
            chapter.setMangaID(mangaDb.getId());
            chapter.setReadStatus(Chapter.NEW);
            Database.addChapter(context, chapter, mangaDb.getId());
        }

        if (manga.getChapters().size() > 0) {
            Database.updateMangaRead(context, mangaDb.getId());
            Database.updateNewMangas(context, mangaDb, manga.getChapters().size());
        }

        returnValue = manga.getChapters().size();

        if (returnValue > 0)
            new CreateGroupByMangaNotificationsTask(manga.getChapters(), manga, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        boolean changes = false;
        if (!mangaDb.getAuthor().equals(manga.getAuthor()) &&
                manga.getAuthor().length() > 2) {
            mangaDb.setAuthor(manga.getAuthor());
            changes = true;
        }

        if (!mangaDb.getImages().equals(manga.getImages()) &&
                manga.getImages().length() > 2) {
            mangaDb.setImages(manga.getImages());
            changes = true;
        }

        if (!mangaDb.getSynopsis().equals(manga.getSynopsis()) &&
                manga.getSynopsis().length() > 2) {
            mangaDb.setSynopsis(manga.getSynopsis());
            changes = true;
        }

        if (!mangaDb.getGenre().equals(manga.getGenre()) &&
                manga.getGenre().length() > 2) {
            mangaDb.setGenre(manga.getGenre());
            changes = true;
        }
        if (mangaDb.isFinished() != manga.isFinished()) {
            mangaDb.setFinished(manga.isFinished());
            changes = true;
        }

        if (changes) Database.updateManga(context, mangaDb, false);

        return returnValue;
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public boolean needRefererForImages() {
        return false;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter("Tipo", type, ServerFilter.FilterType.SINGLE),//0
                new ServerFilter("Demografia", demografia, ServerFilter.FilterType.SINGLE),//1
                new ServerFilter("Generos", genres, ServerFilter.FilterType.MULTI),//2
                new ServerFilter("Estado", estado, ServerFilter.FilterType.SINGLE),//3
                new ServerFilter("Categoria", categoria, ServerFilter.FilterType.MULTI),//4
                new ServerFilter("Ordenado por", sortBy, ServerFilter.FilterType.SINGLE)//5
        };
    }

    //http://www.tumangaonline.com/api/v1/mangas?categorias=%5B3%5D&defecto=1&demografia=Seinen
    // &estado=Activo&generos=%5B1%5D&itemsPerPage=20&page=1&puntuacion=0&searchBy=nombre
    // &sortDir=asc&sortedBy=nombre&tipo=MANGA

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        if (pageNumber == 1)
            lastPage = 10000;
        if (pageNumber <= lastPage) {
            String gens = "";
            for (int i = 0; i < filters[2].length; i++) {
                gens = gens + genresValues[filters[2][i]] + ",";
            }
            if (gens.length() > 0)
                gens = gens.substring(0, gens.length() - 1);

            String cats = "";
            for (int i = 0; i < filters[4].length; i++) {
                cats = cats + categoriaV[filters[4][i]] + ",";
            }
            if (cats.length() > 0)
                cats = cats.substring(0, cats.length() - 1);

            String web = "http://www.tumangaonline.com/api/v1/mangas?categorias=%5B" + cats +
                    "%5D&defecto=1&demografia=" + demografiaV[filters[1][0]] + "&estado=" +
                    estadoV[filters[3][0]] + "&generos=%5B" + gens + "%5D&itemsPerPage=20&page=" +
                    pageNumber + "&puntuacion=0&searchBy=nombre" + sortByValues[filters[5][0]] +
                    "&tipo=" + typeV[filters[0][0]];

            JSONObject jsonObject = new JSONObject(getNavigatorAndFlushParameters().get(web));
            lastPage = jsonObject.getInt("last_page");
            return getMangasJsonArray(jsonObject.getJSONArray("data"));
        } else {
            return new ArrayList<>();
        }
    }
}
