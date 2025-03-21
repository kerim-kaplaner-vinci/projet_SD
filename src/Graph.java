import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Graph
{

  HashMap<String, Artiste> nomToArtiste;
  HashMap<Integer, Artiste> idToArtiste;
  HashMap<Integer, Set<Mention>> artisteToMentions;

  public Graph(String fichierArtists, String fichierMentions)
  {
    initArtistes(fichierArtists);
    initMentions(fichierMentions);
  }

  private void initArtistes(String fichierArtists)
  {
    this.idToArtiste = new HashMap<>();
    this.nomToArtiste = new HashMap<>();

    Path filePath = Path.of(fichierArtists);

    try (Stream<String> lines = Files.lines(filePath, Charset.forName("Windows-1252")))
    {
      lines
          .map(line -> line.split(","))
          .filter(parts -> parts.length == 3)
          .forEach(parts -> {
            int id = Integer.parseInt(parts[0]);
            String name = parts[1];
            Artiste artiste = new Artiste(id, name, parts[2]);
            idToArtiste.put(id, artiste);
            nomToArtiste.put(name, artiste);
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

            Artiste artisteDepart = idToArtiste.get(idArtisteDepart);
            Artiste artisteArrivee = idToArtiste.get(idArtisteArrivee);
            ensembleArtiste.add(new Mention(artisteDepart, artisteArrivee, nbMentions));
          });
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void trouverCheminLePlusCourt(String nomArtisteDepart, String nomArtisteArrivee)
  {
    Artiste artisteDepart = nomToArtiste.get(nomArtisteDepart);
    Artiste artisteArrivee = nomToArtiste.get(nomArtisteArrivee);

    Deque<Artiste> queue = new LinkedList<>();
    Set<Artiste> visited = new HashSet<>();
    Map<Artiste, Artiste> predecesseur = new HashMap<>();

    queue.add(artisteDepart);
    visited.add(artisteDepart);

    while (!queue.isEmpty())
    {
      Artiste courant = queue.poll();

      if (courant.equals(artisteArrivee))
      {
        break;
      }

      Set<Mention> voisins = artisteToMentions.getOrDefault(courant.getId(), new HashSet<>());

      for (Mention mention : voisins)
      {
        Artiste voisin = mention.getArtisteArrivee();
        if (!visited.contains(voisin))
        {
          visited.add(voisin);
          predecesseur.put(voisin, courant);
          queue.add(voisin);
        }
      }
    }

    if (!predecesseur.containsKey(artisteArrivee))
    {
      throw new RuntimeException(
          "Aucun chemin entre " + nomArtisteDepart + " et " + nomArtisteArrivee);
    }

    List<Artiste> chemin = new LinkedList<>();
    Artiste courant = artisteArrivee;
    while (courant != null && !courant.equals(artisteDepart))
    {
      chemin.addFirst(courant);
      courant = predecesseur.get(courant);
    }
    chemin.addFirst(artisteDepart);

    afficher(chemin);
  }

  private void afficher(List<Artiste> chemin)
  {
    System.out.println("Longueur du chemin : " + (chemin.size() - 1));
    System.out.println("Coût total du chemin : " + calculerCout(chemin));
    System.out.print("Chemin :\n");
    for (Artiste artiste : chemin)
    {
      System.out.print(
          artiste.getId() + ": " + artiste.getNom() + " (" + artiste.getCategorie() + ")\n");
    }
    System.out.println();
  }

  private double calculerCout(List<Artiste> chemin)
  {
    double cout = 0.0;
    for (int i = 0; i < chemin.size() - 1; i++)
    {
      Artiste from = chemin.get(i);
      Artiste to = chemin.get(i + 1);
      Set<Mention> mentions = artisteToMentions.get(from.getId());
      for (Mention mention : mentions)
      {
        if (mention.getArtisteArrivee().equals(to))
        {
          System.out.println(from.getNom() + " - " + to.getNom() + " = " + cout);
          cout += 1.0 / mention.getNbMentions();
          break;
        }
      }
    }
    return cout;
  }

  public void trouverCheminMaxMentions(String nomArtisteDepart, String nomArtisteArrivee)
  {
    Artiste artisteDepart = nomToArtiste.get(nomArtisteDepart);
    Artiste artisteArrivee = nomToArtiste.get(nomArtisteArrivee);
    return;
  }
}
