<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    include_once '../racine.php';
    include_once RACINE.'/service/EtudiantService.php';
    create();
}

function create(){
    $nom = $_POST['nom'];
    $prenom = $_POST['prenom'];
    $ville = $_POST['ville'];
    $sexe = $_POST['sexe'];
    $dateNaissance = $_POST['dateNaissance'];
    
    // Gestion de l'upload de la photo
    $photoName = null;
    
    // Vérifier si une photo encodée en Base64 est envoyée
    if(isset($_POST['photo']) && !empty($_POST['photo'])) {
        $uploadDir = '../uploads/';
        if(!is_dir($uploadDir)) {
            mkdir($uploadDir, 0777, true);
        }
        
        // Générer un nom pour l'image
        $photoName = isset($_POST['photoName']) ? $_POST['photoName'] : 'img_'.time().'.jpg';
        $uploadFile = $uploadDir . $photoName;
        
        // Décoder l'image Base64 et la sauvegarder
        $imageData = base64_decode(str_replace(' ', '+', $_POST['photo']));
        file_put_contents($uploadFile, $imageData);
    }
    // Vérifier si une photo est uploadée via $_FILES (méthode traditionnelle)
    else if(isset($_FILES['photo']) && $_FILES['photo']['error'] == 0) {
        $uploadDir = '../uploads/';
        if(!is_dir($uploadDir)) {
            mkdir($uploadDir, 0777, true);
        }
        
        $extension = pathinfo($_FILES['photo']['name'], PATHINFO_EXTENSION);
        $photoName = uniqid().'.'.$extension;
        $uploadFile = $uploadDir . $photoName;
        
        move_uploaded_file($_FILES['photo']['tmp_name'], $uploadFile);
    }
    
    $es = new EtudiantService();
    $es->create(new Etudiant(1, $nom, $prenom, $ville, $sexe, $dateNaissance, $photoName));
    
    // Retourner la réponse en JSON
    $response = array(
        'success' => true,
        'message' => 'Étudiant ajouté avec succès',
        'data' => $es->findAllApi()
    );
    
    header('Content-type: application/json');
    echo json_encode($response);
}