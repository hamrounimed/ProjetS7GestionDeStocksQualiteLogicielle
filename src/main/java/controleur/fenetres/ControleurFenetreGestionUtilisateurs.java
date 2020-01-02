package controleur.fenetres;

import controleur.Connexion;
import controleur.ProduitDAO;
import controleur.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import modele.*;
import modele.tables.ProduitsTableClass;
import modele.tables.UtilisateursTableClass;
import vue.FenetrePrincipale;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControleurFenetreGestionUtilisateurs implements Initializable {

    //Défintion des boutons
    @FXML Button boutonCreer;
    @FXML Button boutonModifier;
    @FXML Button boutonSupprimer;
    @FXML Button boutonRestreindre;
    @FXML Button boutonTransformer;

    //L'utilisateur actuellement connecté
    private static Utilisateur utilisateurConnecte;

    //Table et colonnes de la table des rayons d'un magasin :
    @FXML private TableView<UtilisateursTableClass> utilisateursTable;
    @FXML private TableColumn<UtilisateursTableClass, String> colonneNomCompte;
    @FXML private TableColumn<UtilisateursTableClass, String> colonneNom;
    @FXML private TableColumn<UtilisateursTableClass, String> colonnePrenom;
    @FXML private TableColumn<UtilisateursTableClass, String> colonneTypeCompte;
    @FXML private TableColumn<UtilisateursTableClass, String> colonneAcces;
    @FXML private TableColumn<UtilisateursTableClass, String> colonneRayonDirige;
    @FXML private TableColumn<UtilisateursTableClass, String> colonneMagasin;
    private static ObservableList<UtilisateursTableClass> dataTableUtilisateurs;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //================================ Définition des actions des boutons ================================

        boutonCreer.setOnAction( event -> {
            ControleurFenetreCreerUtilisateur.setUtilisateurConnecte(utilisateurConnecte);
            Parent root;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fenetreCreerUtilisateur.fxml"));
                root = loader.load();
                Stage stage = new Stage();
                stage.getIcons().add(new Image(FenetrePrincipale.class.getResourceAsStream( "/icon.png" )));
                stage.setTitle("Créer utilisateur");
                stage.setScene(new Scene(root, 350, 600));
                stage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });

        boutonModifier.setOnAction( event -> {
            //TODO
        });

        boutonSupprimer.setOnAction( event -> {
            //On vérifie que l'utilisateur a sélectionné un ligne du tableau :
            UtilisateursTableClass utilisateurSelectionne = utilisateursTable.getSelectionModel().getSelectedItem();
            if(utilisateursTable != null){
                //Si l'utilisateur peut voir les utilisateurs, c'est qu'il a le droit de les administrer, donc pas de vérification à faire ici :
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer " + utilisateurSelectionne.getPrenom() + " " + utilisateurSelectionne.getNom() + " ?");
                Optional<ButtonType> choose = alert.showAndWait();
                if(choose.get() == ButtonType.OK){
                    UtilisateurDAO.supprimerUtilisateur(utilisateurSelectionne.getIdUtilisateur());
                    miseAJourTable();
                }
            }
            event.consume();
        });

        boutonRestreindre.setOnAction( event -> {
            //TODO
        });

        boutonTransformer.setOnAction( event -> {
            //TODO
        });

        //================================ Initialisation des tables ================================

        //On fait le lien entre les lignes déclarées ici et celles du fichier .fxml et les éléments du modèle TableClass :
        colonneNomCompte.setCellValueFactory( new PropertyValueFactory<>("nomCompte") );
        colonneNom.setCellValueFactory( new PropertyValueFactory<>("nom") );
        colonnePrenom.setCellValueFactory( new PropertyValueFactory<>("prenom") );
        colonneTypeCompte.setCellValueFactory( new PropertyValueFactory<>("typeCompte") );
        colonneAcces.setCellValueFactory( new PropertyValueFactory<>("acces") );
        colonneRayonDirige.setCellValueFactory( new PropertyValueFactory<>("rayonDirige") );
        colonneMagasin.setCellValueFactory( new PropertyValueFactory<>("magasin") );

        dataTableUtilisateurs = FXCollections.observableArrayList();
        utilisateursTable.setItems(dataTableUtilisateurs);

        miseAJourTable();
    }

    /**
     * Cette fonction met à jour la table avec les dernières données de la BDD.
     */
    public static void miseAJourTable(){
        List<Utilisateur> utilisateurs = UtilisateurDAO.tousLesUtilisateurs();

        dataTableUtilisateurs.clear();

        EntityManager em = Connexion.getEntityManager();

        for(Utilisateur u : utilisateurs){
            u = em.find(Utilisateur.class, u.getIdUtilisateur());

            //On ajoute pas l'utilisateur si :
            // - C'est un super admin (personne ne peut administrer les super admin)
            // - L'utilisateur connecté est un admin et on veut ajouter un admin.
            // - L'utilisateur connecté est un admin et on veut ajouter un utilisateur qui n'est pas du même magasin
            boolean ajouterUtilisateur = true;
            if( (u.getTypeDeCompte().equals(TypeDeCompte.SUPER_ADMINISTRATEUR))
                    || (utilisateurConnecte.getTypeDeCompte().equals(TypeDeCompte.ADMINISTRATEUR) && u.getTypeDeCompte().equals(TypeDeCompte.ADMINISTRATEUR))
                    || (utilisateurConnecte.getTypeDeCompte().equals(TypeDeCompte.ADMINISTRATEUR) && !u.getMagasin().equals(utilisateurConnecte.getMagasin())) ){
                ajouterUtilisateur = false;
            }

            if(ajouterUtilisateur){

                String typeCompte;
                switch(u.getTypeDeCompte()){
                    case UTILISATEUR:
                        typeCompte = "Utilisateur";
                        break;
                    case ADMINISTRATEUR:
                        typeCompte = "Administrateur";
                        break;
                    case SUPER_ADMINISTRATEUR:
                        typeCompte = "Super-administrateur";
                        break;
                    default:
                        typeCompte = "non défini";
                        break;
                }

                String restreint;
                if(u.isRestreint()){
                    restreint = "Restreint";
                }
                else{
                    restreint = "Normal";
                }

                String nomMagasin = "Aucun";
                if(u.getMagasin() != null){
                    nomMagasin = u.getMagasin().getNomMagasin();
                }

                String nomRayonDirige = "Aucun";
                if(u.getRayonDirige() != null){
                    nomRayonDirige = u.getRayonDirige().getNomRayon();
                }
                dataTableUtilisateurs.add( new UtilisateursTableClass(u.getNomDeCompte(), u.getNom(), u.getPrenom(), typeCompte, restreint, nomRayonDirige, nomMagasin, u.getIdUtilisateur()) );
            }

        }

        em.close();

    }

    /**
     * Cette fonction statique et publique permet de définir l'utilisateur connecté à l'application depuis la fenêtre de connexion.
     * @param utilisateur : l'utilisateur connecté.
     */
    public static void setUtilisateurConnecte(Utilisateur utilisateur){
        utilisateurConnecte = utilisateur;
    }

}