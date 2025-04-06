<?php
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once '../racine.php';
    include_once RACINE.'/service/EtudiantService.php';
    delete();
}

function delete() {
    $id = $_POST['id'];
    $es = new EtudiantService();
    $etudiant = $es->findById($id);
    
    // Supprimer la photo si elle existe
    if($etudiant->getPhoto()) {
        @unlink('../uploads/' . $etudiant->getPhoto());
    }
    
    $es->delete($etudiant);
    header('Content-Type: application/json');
    echo json_encode($es->findAllApi());
}