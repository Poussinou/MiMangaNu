package ar.rulosoft.mimanganu.servers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.utils.Util;
import ar.rulosoft.navegadores.Navigator;

@Deprecated
public class TusMangasOnlineCom extends ServerBase {

    private final static int TIMEOUT = 20000;

    private static String[] generos = new String[]{
            "#", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z",
            "Acción", "Artes Marciales", "Aventura", "Ciencia Ficción", "Comedia",
            "Deportes", "Drama", "Ecchi", "Fantasía", "Harem", "Histórico", "Horror",
            "Josei", "Magia", "Mecha", "Misterio", "Psicológico", "Recuentos de la vida",
            "Romance", "Seinen", "Shoujo", "Shonen", "Shonen-ai", "Shoujo-ai",
            "Sobrenatural", "Suspense", "Tragedia", "Vida escolar", "Yuri"
    };
    private static String[] generosV = new String[]{
            "tipo=2&filter=1", "tipo=2&filter=A", "tipo=2&filter=B", "tipo=2&filter=C",
            "tipo=2&filter=D", "tipo=2&filter=E", "tipo=2&filter=F", "tipo=2&filter=G",
            "tipo=2&filter=H", "tipo=2&filter=I", "tipo=2&filter=J", "tipo=2&filter=K",
            "tipo=2&filter=L", "tipo=2&filter=M", "tipo=2&filter=N", "tipo=2&filter=O",
            "tipo=2&filter=P", "tipo=2&filter=Q", "tipo=2&filter=R", "tipo=2&filter=S",
            "tipo=2&filter=T", "tipo=2&filter=U", "tipo=2&filter=V", "tipo=2&filter=W",
            "tipo=2&filter=X", "tipo=2&filter=y", "tipo=2&filter=Z",
            "tipo=3&filter=Acci%C3%B3n",
            "tipo=3&filter=Artes+Marciales",
            "tipo=3&filter=Aventura",
            "tipo=3&filter=Ciencia+Ficci%C3%B3n",
            "tipo=3&filter=Comedia",
            "tipo=3&filter=Deportes",
            "tipo=3&filter=Drama", "tipo=3&filter=Ecchi",
            "tipo=3&filter=Fantas%C3%ADa",
            "tipo=3&filter=Harem",
            "tipo=3&filter=Hist%C3%B3rico",
            "tipo=3&filter=Horror",
            "tipo=3&filter=Josei", "tipo=3&filter=Magia",
            "tipo=3&filter=Mecha",
            "tipo=3&filter=Misterio",
            "tipo=3&filter=Psicol%C3%B3gico",
            "tipo=3&filter=Recuentos+de+la+vida",
            "tipo=3&filter=Romance",
            "tipo=3&filter=Seinen",
            "tipo=3&filter=Sh%C5%8Djo",
            "tipo=3&filter=Sh%C5%8Dnen",
            "tipo=3&filter=Shonen-ai",
            "tipo=3&filter=Shoujo-ai",
            "tipo=3&filter=Sobrenatural",
            "tipo=3&filter=Suspense",
            "tipo=3&filter=Tragedia",
            "tipo=3&filter=Vida+escolar",
            "tipo=3&filter=Yuri"
    };

    private static String HOST = "http://www.tumangaonline.com";


    public TusMangasOnlineCom() {
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.tumangaonline_icon);
        this.setServerName("TusMangasOnline");
        setServerID(ServerBase.TUSMANGAS);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        return null;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavigatorAndFlushParameters().get(manga.getPath(), TIMEOUT);
        // sinopsis
        String sinopsis = getFirstMatchDefault("(<p itemprop=\"description\".+?</p></div>)",
                source, "Sin sinopsis");
        manga.setSynopsis(Util.getInstance().fromHtml(sinopsis).toString());
        // portada
        manga.setImages(getFirstMatchDefault("src=\"([^\"]+TMOmanga[^\"]+)\"", source, ""));
        // estado
        manga.setFinished(!(getFirstMatchDefault("<td><strong>Estado:(.+?)</td>", source, "").contains("En Curso")));
        // autor
        manga.setAuthor(getFirstMatchDefault("5&amp;filter=.+?>(.+?)<", source, ""));
        // genero
        manga.setGenre(Util.getInstance().fromHtml(getFirstMatchDefault("<tr><td><strong>G&eacute;neros(.+?)</tr>", source, "")).toString());
        // capitulos
        ArrayList<Chapter> caps = new ArrayList<>();
        Pattern p = Pattern.compile("<h5><a[^C]+Click=\"listaCapitulos\\((.+?),(.+?)\\)\".+?>(.+?)</a");
        Matcher ma = p.matcher(source);
        while (ma.find()) {
            Chapter c = new Chapter(Util.getInstance().fromHtml(
                    ma.group(3)).toString().replace("0 0",""),
                    HOST + "/index.php?option=com_controlmanga&view=capitulos&format=raw&idManga=" +
                            ma.group(1) + "&idCapitulo=" + ma.group(2));
            caps.add(0, c);
        }
        manga.setChapters(caps);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return null;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2 || !chapter.getExtra().contains("|")) {
            if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
                getExtraWeb(chapter);
            }
            String source = getNavigatorAndFlushParameters().get(chapter.getExtra(), TIMEOUT);
            Pattern p = Pattern.compile(
                    "<input id=\"\\d+\" hidden=\"true\" value=\"(.+?);(.+?);(.+?);(.+?);(.+?)\"");
            Matcher m = p.matcher(source);
            String imgBase;
            String imgStrip;
            String imagenes = "";
            if (m.find()) {
                imgBase = "http://img1.tumangaonline.com/subidas/" +
                        m.group(1) + "/" + m.group(2) + "/" + m.group(3) + "/";
                imgStrip = m.group(4);
            } else {
                throw new Exception("Error obteniendo Imagenes");
            }
            String[] strip = imgStrip.split("%");
            for (String s : strip) {
                imagenes = imagenes + "|" + imgBase + s;
            }
            chapter.setExtra(imagenes);
        }
        String[] imagenes = chapter.getExtra().split("\\|");
        return imagenes[page];
    }

    private void getExtraWeb(Chapter c) throws Exception {
        String cId = getFirstMatch("idCapitulo=([^&]+)", c.getPath(), "Error al iniciar Capítulo");
        String mId = getFirstMatch("idManga=([^&]+)", c.getPath(), "Error al iniciar Capítulo");
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("idManga", mId);
        nav.addPost("idCapitulo", cId);
        String source = nav.post("http://www.tumangaonline.com/index.php?option=com_controlmanga&view=capitulos&format=raw");
        String fs = getFirstMatch("(http://www.tumangaonline.com/visor/.+?)\"",
                source, "Error al iniciar Capítulo");
        c.setExtra(fs);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        if (!(chapter.getExtra() != null && chapter.getExtra().length() > 1)) {
            getExtraWeb(chapter);
        }
        String source = getNavigatorAndFlushParameters().get(chapter.getExtra());
        String paginas = getFirstMatch(
                "<input id=\"totalPaginas\" hidden=\"true\" value=\"(\\d+)\">",
                source, "Error al iniciar Capítulo");
        chapter.setPages(Integer.parseInt(paginas));
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public boolean needRefererForImages() {
        return false;//give 403 error
    }
}
