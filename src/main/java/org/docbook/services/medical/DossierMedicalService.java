package org.docbook.services.medical;

import org.docbook.entities.records.DossierMedical;
import org.docbook.interfaces.IService;
import org.docbook.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DossierMedicalService implements IService<DossierMedical> {

    private Connection connection;
    public DossierMedicalService(){
        try {
            this.connection = DBConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
            throw new RuntimeException("Database unavailable");
        }
    }
    /**
     * Insere un dossier medical dans la table {@code dossier_medical}.
     * La date de creation est fixee automatiquement et l'identifiant genere
     * est reinjecte dans l'objet {@code dm}.
     *
     * @param dm dossier medical a creer (champs patient requis)
     */
    @Override
    public void add(DossierMedical dm) {
        String sql = "INSERT INTO dossier_medical (numero_dossier, patient_nom, patient_prenom, date_naissance, genre, email, telephone, adresse, remarques, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dm.getNumeroDossier());
            ps.setString(2, dm.getPatientNom());
            ps.setString(3, dm.getPatientPrenom());
            ps.setDate(4, Date.valueOf(dm.getDateNaissance()));
            ps.setString(5, dm.getGenre());
            ps.setString(6, dm.getEmail());
            ps.setString(7, dm.getTelephone());
            ps.setString(8, dm.getAdresse());
            ps.setString(9, dm.getRemarques());
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    dm.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding dossier medical: " + e.getMessage());
        }
    }

    /**
     * Met a jour un dossier medical existant par son identifiant.
     * Tous les champs metier sont remplaces et la date de modification est actualisee.
     *
     * @param dm dossier medical a modifier (id valide requis)
     */
    @Override
    public void update(DossierMedical dm) {
        String sql = "UPDATE dossier_medical SET numero_dossier=?, patient_nom=?, patient_prenom=?, date_naissance=?, genre=?, email=?, telephone=?, adresse=?, remarques=?, date_modification=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, dm.getNumeroDossier());
            ps.setString(2, dm.getPatientNom());
            ps.setString(3, dm.getPatientPrenom());
            ps.setDate(4, Date.valueOf(dm.getDateNaissance()));
            ps.setString(5, dm.getGenre());
            ps.setString(6, dm.getEmail());
            ps.setString(7, dm.getTelephone());
            ps.setString(8, dm.getAdresse());
            ps.setString(9, dm.getRemarques());
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(11, dm.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating dossier medical: " + e.getMessage());
        }
    }

    /**
     * Supprime definitivement un dossier medical en base.
     *
     * @param id identifiant du dossier a supprimer
     */
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM dossier_medical WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting dossier medical: " + e.getMessage());
        }
    }

    /**
     * Recupere tous les dossiers medicaux.
     *
     * @return liste complete, vide si aucun enregistrement ou erreur SQL
     */
    @Override
    public List<DossierMedical> getAll() {
        List<DossierMedical> list = new ArrayList<>();
        String sql = "SELECT * FROM dossier_medical";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToDossier(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all dossier medical files: " + e.getMessage());
        }
        return list;
    }

    /**
     * Recherche un dossier medical par sa cle primaire.
     *
     * @param id identifiant du dossier
     * @return dossier trouve, ou {@code null} si absent ou erreur SQL
     */
    @Override
    public DossierMedical getById(int id) {
        String sql = "SELECT * FROM dossier_medical WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDossier(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching dossier medical by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Convertit la ligne courante du {@link ResultSet} en {@link DossierMedical}.
     * Les dates SQL sont converties vers les types Java ({@code LocalDate}/{@code LocalDateTime}).
     *
     * @param rs resultat SQL positionne sur une ligne valide
     * @return dossier medical hydrate depuis la ligne courante
     * @throws SQLException en cas d'erreur de lecture SQL
     */
    private DossierMedical mapResultSetToDossier(ResultSet rs) throws SQLException {
        DossierMedical dm = new DossierMedical();
        dm.setId(rs.getInt("id"));
        dm.setNumeroDossier(rs.getString("numero_dossier"));
        dm.setPatientNom(rs.getString("patient_nom"));
        dm.setPatientPrenom(rs.getString("patient_prenom"));
        Date birthDate = rs.getDate("date_naissance");
        if (birthDate != null) dm.setDateNaissance(birthDate.toLocalDate());
        dm.setGenre(rs.getString("genre"));
        dm.setEmail(rs.getString("email"));
        dm.setTelephone(rs.getString("telephone"));
        dm.setAdresse(rs.getString("adresse"));
        dm.setRemarques(rs.getString("remarques"));
        Timestamp creation = rs.getTimestamp("date_creation");
        if (creation != null) dm.setDateCreation(creation.toLocalDateTime());
        Timestamp modification = rs.getTimestamp("date_modification");
        if (modification != null) dm.setDateModification(modification.toLocalDateTime());
        return dm;
    }
}


