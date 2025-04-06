<?php
include_once RACINE . '/classes/Etudiant.php';
include_once RACINE . '/connexion/Connexion.php';
include_once RACINE . '/dao/IDao.php';

class EtudiantService implements IDao {
    private $connexion;
    
    function __construct() {
        $this->connexion = new Connexion();
    }

    public function create($o) {
        $query = "INSERT INTO Etudiant (`id`, `nom`, `prenom`, `ville`, `sexe`, `date_naissance`, `photo`) 
                  VALUES (NULL, ?, ?, ?, ?, ?, ?)";
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute([
            $o->getNom(), 
            $o->getPrenom(),
            $o->getVille(),
            $o->getSexe(),
            $o->getDateNaissance(),
            $o->getPhoto()
        ]) or die('Erreur SQL');
    }

    public function delete($o) {
        $query = "DELETE FROM Etudiant WHERE id = ?";
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute([$o->getId()]) or die('Erreur SQL');
        
        // Supprimer aussi le fichier photo si existe
        if($o->getPhoto()) {
            @unlink('../uploads/' . $o->getPhoto());
        }
    }

    public function findAll() {
        $etds = array();
        $query = "SELECT * FROM Etudiant";
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute();
        while ($e = $req->fetch(PDO::FETCH_OBJ)) {
            $etds[] = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe, $e->date_naissance, $e->photo);
        }
        return $etds;
    }

    public function findById($id) {
        $query = "SELECT * FROM Etudiant WHERE id = ?";
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute([$id]);
        if ($e = $req->fetch(PDO::FETCH_OBJ)) {
            $etd = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe, $e->date_naissance, $e->photo);
        }
        return $etd;
    }

    public function update($o) {
        $query = "UPDATE Etudiant SET 
                 nom = ?, prenom = ?, ville = ?, sexe = ?, date_naissance = ?, photo = ?
                 WHERE id = ?";
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute([
            $o->getNom(),
            $o->getPrenom(),
            $o->getVille(),
            $o->getSexe(),
            $o->getDateNaissance(),
            $o->getPhoto(),
            $o->getId()
        ]) or die('Erreur SQL');
    }

    public function findAllApi() {
        $query = "SELECT * FROM Etudiant";
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute();
        return $req->fetchAll(PDO::FETCH_ASSOC);
    }
}