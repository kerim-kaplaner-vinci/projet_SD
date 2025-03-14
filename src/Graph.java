import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Graph
{

  HashMap<Integer, Artiste> idToArtistes;
  HashMap<Integer, Set<Mention>> artisteToMentions;

  public Graph(String fichierArtists, String fichierMentions)
  {
    initArtistes(fichierArtists);
    initMentions(fichierMentions);
  }

  private void initArtistes(String fichierArtists)
  {
    this.idToArtistes = new HashMap<>();

    Path filePath = Path.of(fichierArtists);

    try (Stream<String> lines = Files.lines(filePath, Charset.forName("Windows-1252")))
    {
      lines
          .map(line -> line.split(","))
          .filter(parts -> parts.length == 3)
          .forEach(parts -> {
            int id = Integer.parseInt(parts[0]);
            Artiste artiste = new Artiste(id, parts[1], parts[2]);
            idToArtistes.put(id, artiste);
          });
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }

  }

  private void initMentions(String fichierMentions)
  {
    this.artisteToMentions = new HashMap<>();

    Path filePath = Path.of(fichierMentions);

    try (Stream<String> lines = Files.lines(filePath, Charset.forName("Windows-1252")))
    {
      lines
          .map(line -> line.split(","))
          .filter(parts -> parts.length == 3)
          .forEach(parts -> {
            int idArtisteDepart = Integer.parseInt(parts[0]);
            int idArtisteArrivee = Integer.parseInt(parts[1]);
            int nbMentions = Integer.parseInt(parts[2]);

            artisteToMentions.putIfAbsent(idArtisteDepart, new HashSet<>());
            Set<Mention> ensembleArtiste = artisteToMentions.get(idArtisteDepart);

            Artiste artisteDepart = idToArtistes.get(idArtisteDepart);
            Artiste artisteArrivee = idToArtistes.get(idArtisteArrivee);
            ensembleArtiste.add(new Mention(artisteDepart, artisteArrivee, nbMentions));
          });
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void trouverCheminLePlusCourt(String artists1, String artists2)
  {
    return;
  }

  public void trouverCheminMaxMentions(String artists1, String artists2)
  {
    return;
  }
}
