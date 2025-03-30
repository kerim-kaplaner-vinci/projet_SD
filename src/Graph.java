import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

public class Graph {

  HashMap<String, Artiste> nomToArtiste;
  HashMap<Integer, Artiste> idToArtiste;
  HashMap<Integer, Set<Mention>> artisteToMentions;

  public Graph(String fichierArtists, String fichierMentions) {
    initArtistes(fichierArtists);
    initMentions(fichierMentions);
  }

  private void initArtistes(String fichierArtists) {
    this.idToArtiste = new HashMap<>();
    this.nomToArtiste = new HashMap<>();

    Path filePath = Path.of(fichierArtists);
    try (Stream<String> lines = Files.lines(filePath, Charset.forName("Windows-1252"))) {
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private void initMentions(String fichierMentions) {
    this.artisteToMentions = new HashMap<>();

    Path filePath = Path.of(fichierMentions);
    try (Stream<String> lines = Files.lines(filePath, Charset.forName("Windows-1252"))) {
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void trouverCheminLePlusCourt(String nomArtisteDepart, String nomArtisteArrivee) {
    Artiste artisteDepart = nomToArtiste.get(nomArtisteDepart);
    Artiste artisteArrivee = nomToArtiste.get(nomArtisteArrivee);

    Deque<Artiste> fileArtistes = new LinkedList<>();
    Map<Artiste, Artiste> arriveeToDepart = new HashMap<>();
    fileArtistes.add(artisteDepart);
    arriveeToDepart.put(artisteDepart, null);

    while (!fileArtistes.isEmpty()) {
      Artiste courant = fileArtistes.poll();
      if (courant.equals(artisteArrivee)) {
        break;
      }

      for (Mention mention : artisteToMentions.getOrDefault(courant.getId(),
          Collections.emptySet())) {
        Artiste voisin = mention.getArtisteArrivee();
        if (!arriveeToDepart.containsKey(voisin)) {
          arriveeToDepart.put(voisin, courant);
          fileArtistes.add(voisin);
        }
      }
    }

    throwsError(nomArtisteDepart, nomArtisteArrivee, artisteArrivee, arriveeToDepart);
  }


  public void trouverCheminMaxMentions(String nomArtisteDepart, String nomArtisteArrivee) {
    Artiste artisteDepart = nomToArtiste.get(nomArtisteDepart);
    Artiste artisteArrivee = nomToArtiste.get(nomArtisteArrivee);

    Map<Artiste, Double> maxMentions = new HashMap<>();
    Map<Artiste, Artiste> arriveeToDepart = new HashMap<>();
    PriorityQueue<Artiste> fileTrieeArtistes = new PriorityQueue<>(
        Comparator.comparingDouble(
            (Artiste a) -> -maxMentions.getOrDefault(a, Double.NEGATIVE_INFINITY))
    );

    maxMentions.put(artisteDepart, 0.0);
    fileTrieeArtistes.add(artisteDepart);

    while (!fileTrieeArtistes.isEmpty()) {
      Artiste courant = fileTrieeArtistes.poll();

      for (Mention mention : artisteToMentions.getOrDefault(courant.getId(),
          Collections.emptySet())) {
        Artiste voisin = mention.getArtisteArrivee();
        double score = maxMentions.get(courant) + 1.0 / mention.getNbMentions();

        if (score > maxMentions.getOrDefault(voisin, 0.0)) {
          maxMentions.put(voisin, score);
          arriveeToDepart.put(voisin, courant);
          fileTrieeArtistes.add(voisin);
        }
      }
    }

    throwsError(nomArtisteDepart, nomArtisteArrivee, artisteArrivee, arriveeToDepart);
  }

  private void throwsError(String nomArtisteDepart, String nomArtisteArrivee,
      Artiste artisteArrivee, Map<Artiste, Artiste> arriveeToDepart) {
    if (!arriveeToDepart.containsKey(artisteArrivee)) {
      throw new RuntimeException(
          "Aucun chemin entre " + nomArtisteDepart + " et " + nomArtisteArrivee);
    }

    LinkedList<Artiste> chemin = new LinkedList<>();

    for (Artiste at = artisteArrivee; at != null; at = arriveeToDepart.get(at)) {
      chemin.addFirst(at);
    }

    afficher(chemin);
  }

  private void afficher(LinkedList<Artiste> chemin) {
    System.out.println("Longueur du chemin : " + (chemin.size() - 1));
    System.out.println("Coût total du chemin : " + calculerCout(chemin));
    System.out.print("Chemin :\n");
    for (Artiste artiste : chemin) {
      System.out.print(
          artiste.getId() + ": " + artiste.getNom() + " (" + artiste.getCategorie() + ")\n");
    }
    System.out.println();
  }

  private double calculerCout(LinkedList<Artiste> chemin) {
    double cout = 0.0;
    for (int i = 0; i < chemin.size() - 1; i++) {
      Artiste from = chemin.get(i);
      Artiste to = chemin.get(i + 1);
      Set<Mention> mentions = artisteToMentions.get(from.getId());
      for (Mention mention : mentions) {
        if (mention.getArtisteArrivee().equals(to)) {
          System.out.println(from.getNom() + " - " + to.getNom() + " = " + cout);
          cout += 1.0 / mention.getNbMentions();
          break;
        }
      }
    }
    return cout;
  }
}
