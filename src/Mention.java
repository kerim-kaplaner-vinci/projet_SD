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
}
