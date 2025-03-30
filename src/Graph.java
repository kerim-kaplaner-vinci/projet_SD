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
import java.util.Set;
import java.util.stream.Stream;

public class Graph {

  HashMap<String, Artiste> nomToArtiste;
  HashMap<Integer, Artiste> idToArtiste;
  HashMap<Integer, Set<Mention>> artisteToMentions;
  private static final double SCORE_INEXISTANT = -1.0;

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

    reconstruireEtAfficherChemin(artisteDepart, artisteArrivee, arriveeToDepart);
  }


  public void trouverCheminMaxMentions(String nomArtisteDepart, String nomArtisteArrivee) {
    Artiste artisteDepart = nomToArtiste.get(nomArtisteDepart);
    Artiste artisteArrivee = nomToArtiste.get(nomArtisteArrivee);

    Map<Artiste, Double> maxMentions = new HashMap<>();
    Map<Artiste, Artiste> arriveeToDepart = new HashMap<>();
    Set<Artiste> dejaVisites = new HashSet<>();

    PriorityQueue<Noeud> fileTrieeArtistes = new PriorityQueue<>(
        Comparator.comparingDouble(
            (Noeud n) -> -n.score)
    );

    maxMentions.put(artisteDepart, 0.0);
    fileTrieeArtistes.add(new Noeud(artisteDepart, 0.0));

    while (!fileTrieeArtistes.isEmpty()) {
      Noeud nCourant = fileTrieeArtistes.poll();
      Artiste aCourant = nCourant.artiste;

      if (!dejaVisites.add(aCourant)) {
        continue;
      }

      for (Mention mention : artisteToMentions.getOrDefault(aCourant.getId(),
          Collections.emptySet())) {
        Artiste voisin = mention.getArtisteArrivee();

        if (dejaVisites.contains(voisin)) {
          continue;
        }

        double nvScore = maxMentions.get(aCourant) + 1.0 / mention.getNbMentions();
        double scoreActuelVoisin = maxMentions.getOrDefault(voisin, SCORE_INEXISTANT);

        if (nvScore > scoreActuelVoisin) {
          maxMentions.put(voisin, nvScore);
          arriveeToDepart.put(voisin, aCourant);
          fileTrieeArtistes.add(new Noeud(voisin, nvScore));
        }
      }
    }

    reconstruireEtAfficherChemin(artisteDepart, artisteArrivee, arriveeToDepart);
  }

  private void reconstruireEtAfficherChemin(
      Artiste depart, Artiste arrivee, Map<Artiste, Artiste> arriveeToDepart
  ) {
    if (!arriveeToDepart.containsKey(arrivee)) {
      throw new RuntimeException(
          "Aucun chemin entre " + depart.getNom() + " et " + arrivee.getNom());
    }

    LinkedList<Artiste> chemin = new LinkedList<>();
    for (Artiste at = arrivee; at != null; at = arriveeToDepart.get(at)) {
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
          artiste.getNom() + " (" + artiste.getCategorie() + ")\n");
    }
    System.out.println();
  }

  private double calculerCout(LinkedList<Artiste> chemin) {
    double cout = 0.0;
    for (int i = 0; i < chemin.size() - 1; i++) {
      Artiste from = chemin.get(i);
      Artiste to = chemin.get(i + 1);

      Mention mentionTrouvee = artisteToMentions
          .getOrDefault(from.getId(), Collections.emptySet())
          .stream()
          .filter(m -> m.getArtisteArrivee().equals(to))
          .findFirst()
          .orElse(null);

      if (mentionTrouvee == null) {
        throw new IllegalStateException();
      }
      cout += 1.0 / mentionTrouvee.getNbMentions();
    }
    return cout;
  }

  private static class Noeud {

    Artiste artiste;
    double score;

    Noeud(Artiste artiste, double score) {
      this.artiste = artiste;
      this.score = score;
    }
  }
}
