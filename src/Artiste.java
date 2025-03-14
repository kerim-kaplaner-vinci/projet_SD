public class Artiste
{
  private int id;
  private String nom;
  private String categorie;

  public Artiste(int id, String nom, String categorie)
  {
    this.id = id;
    this.nom = nom;
    this.categorie = categorie;
  }

  public int getId()
  {
    return id;
  }

  public String getNom()
  {
    return nom;
  }

  public String getCategorie()
  {
    return categorie;
  }
}
