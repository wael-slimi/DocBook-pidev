package org.docbook.services.medical;

import org.docbook.entities.records.Document;
import org.docbook.interfaces.IService;
import org.docbook.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DocumentService implements IService<Document> {

    @Override
    public void create(Document doc) throws Exception {
        add(doc);
    }

    @Override
    public Document readById(Integer id) throws Exception {
        return getById(id);
    }

    @Override
    public List<Document> readAll() throws Exception {
        return getAll();
    }

    @Override
    public void delete(Integer id) throws Exception {
        delete(id);
    }

    private Connection connection;
    public DocumentService() {
        try {
            // Initialize here where you can handle the exception
            this.connection = DBConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
            // Optionally: throw a runtime exception to stop the app if DB is required
            throw new RuntimeException("Database unavailable");
        }
    }
    /**
     * Insere un nouveau document dans la table {@code document}.
     * Cette methode renseigne aussi la date de creation et recupere l'identifiant
     * genere par la base pour le repropager dans l'objet {@code doc}.
     *
     * @param doc document a persister (dossierMedicalId, titre, type, date et contenu requis)
     */
    @Override
    public void add(Document doc) {
        String sql = "INSERT INTO document (dossier_medical_id, titre, type_document, date_document, contenu, fichier_path, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, doc.getDossierMedicalId());
            ps.setString(2, doc.getTitre());
            ps.setString(3, doc.getTypeDocument());
            ps.setDate(4, Date.valueOf(doc.getDateDocument()));
            ps.setString(5, doc.getContenu());
            ps.setString(6, doc.getFichierPath());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    doc.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding document: " + e.getMessage());
        }
    }

    /**
     * Met a jour un document existant via son identifiant.
     * Les champs metier sont ecrases par les valeurs courantes de {@code doc}
     * et la date de modification est fixee a l'instant present.
     *
     * @param doc document a mettre a jour (id valide requis)
     */
    @Override
    public void update(Document doc) {
        String sql = "UPDATE document SET titre=?, type_document=?, date_document=?, contenu=?, fichier_path=?, date_modification=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, doc.getTitre());
            ps.setString(2, doc.getTypeDocument());
            ps.setDate(3, Date.valueOf(doc.getDateDocument()));
            ps.setString(4, doc.getContenu());
            ps.setString(5, doc.getFichierPath());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(7, doc.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating document: " + e.getMessage());
        }
    }

    /**
     * Supprime physiquement un document de la base.
     *
     * @param id identifiant du document a supprimer
     */
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM document WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting document: " + e.getMessage());
        }
    }

    /**
     * Retourne tous les documents enregistres, sans filtrage.
     *
     * @return liste des documents, vide si aucun enregistrement ou en cas d'erreur SQL
     */
    @Override
    public List<Document> getAll() {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT * FROM document";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all documents: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retourne les documents rattaches a un dossier medical precis.
     *
     * @param dossierId identifiant du dossier medical
     * @return liste des documents du dossier, vide si aucun resultat ou erreur SQL
     */
    public List<Document> getByDossierId(int dossierId) {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT * FROM document WHERE dossier_medical_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, dossierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDocument(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching documents for dossier ID: " + e.getMessage());
        }
        return list;
    }

    /**
     * Recherche un document par sa cle primaire.
     *
     * @param id identifiant du document
     * @return document trouve, ou {@code null} si inexistant ou erreur SQL
     */
    @Override
    public Document getById(int id) {
        String sql = "SELECT * FROM document WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching document by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Mappe la ligne courante du {@link ResultSet} vers un objet {@link Document}.
     * Les colonnes de date/timestamp sont converties en types Java 8 si presentes.
     *
     * @param rs resultat SQL positionne sur une ligne valide
     * @return instance {@link Document} hydratee
     * @throws SQLException en cas d'acces colonne invalide ou de conversion SQL
     */
    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        Document doc = new Document();
        doc.setId(rs.getInt("id"));
        doc.setDossierMedicalId(rs.getInt("dossier_medical_id"));
        doc.setTitre(rs.getString("titre"));
        doc.setTypeDocument(rs.getString("type_document"));
        Date docDate = rs.getDate("date_document");
        if (docDate != null) doc.setDateDocument(docDate.toLocalDate());
        doc.setContenu(rs.getString("contenu"));
        doc.setFichierPath(rs.getString("fichier_path"));
        Timestamp creation = rs.getTimestamp("date_creation");
        if (creation != null) doc.setDateCreation(creation.toLocalDateTime());
        Timestamp modification = rs.getTimestamp("date_modification");
        if (modification != null) doc.setDateModification(modification.toLocalDateTime());
        return doc;
    }
}


