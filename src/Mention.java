public class Mention
{
  private Artiste artisteDepart;
  private Artiste artisteArrivee;
  private int nbMentions;

  public Mention(Artiste artisteDepart, Artiste artisteArrivee, int nbMentions)
  {
    this.artisteDepart = artisteDepart;
    this.artisteArrivee = artisteArrivee;
    this.nbMentions = nbMentions;
  }

  public Artiste getArtisteDepart()
  {
    return artisteDepart;
  }

  public Artiste getArtisteArrivee()
  {
    return artisteArrivee;
  }

  public int getNbMentions()
  {
    return nbMentions;
  }
}
