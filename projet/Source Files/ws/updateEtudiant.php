<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    include_once '../racine.php';
    include_once RACINE.'/service/EtudiantService.php';
    update();
}

function update(){
    // Récupération des données du formulaire
    $id = $_POST['id'];
    $nom = $_POST['nom'];
    $prenom = $_POST['prenom'];
    $ville = $_POST['ville'];
    $sexe = $_POST['sexe'];
    $dateNaissance = $_POST['dateNaissance'];
    
    $es = new EtudiantService();
    $etudiant = $es->findById($id);
    $photoName = $etudiant->getPhoto();
    
    // Gestion de l'upload de la photo
    // Si une photo est envoyée en Base64 depuis l'application Android
    if(isset($_POST['photo']) && !empty($_POST['photo'])) {
        $uploadDir = '../uploads/';
        if(!is_dir($uploadDir)) {
            mkdir($uploadDir, 0777, true);
        }
        
        // Supprimer l'ancienne photo si elle existe
        if($photoName && file_exists($uploadDir . $photoName)) {
            @unlink($uploadDir . $photoName);
        }
        
        // Générer un nom pour la nouvelle image
        $photoName = isset($_POST['photoName']) ? $_POST['photoName'] : 'img_'.time().'.jpg';
        $uploadFile = $uploadDir . $photoName;
        
        // Décoder et sauvegarder l'image Base64
        $imageData = base64_decode(str_replace(' ', '+', $_POST['photo']));
        file_put_contents($uploadFile, $imageData);
    }
    // Si une photo est uploadée via $_FILES (formulaire web traditionnel)
    else if(isset($_FILES['photo']) && $_FILES['photo']['error'] == 0) {
        $uploadDir = '../uploads/';
        if(!is_dir($uploadDir)) {
            mkdir($uploadDir, 0777, true);
        }
        
        // Supprimer l'ancienne photo si elle existe
        if($photoName && file_exists($uploadDir . $photoName)) {
            @unlink($uploadDir . $photoName);
        }
        
        $extension = pathinfo($_FILES['photo']['name'], PATHINFO_EXTENSION);
        $photoName = uniqid().'.'.$extension;
        $uploadFile = $uploadDir . $photoName;
        
        move_uploaded_file($_FILES['photo']['tmp_name'], $uploadFile);
    }
    // Si aucune nouvelle photo, conserver l'ancienne
    else if(isset($_POST['photo_existante'])) {
        $photoName = $_POST['photo_existante'];
    }
    
    $etudiant->setNom($nom);
    $etudiant->setPrenom($prenom);
    $etudiant->setVille($ville);
    $etudiant->setSexe($sexe);
    $etudiant->setDateNaissance($dateNaissance);
    $etudiant->setPhoto($photoName);
    
    $es->update($etudiant);
    
    // Si la requête vient de l'application mobile, retourner JSON
    if(isset($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) === 'xmlhttprequest') {
        $response = array(
            'success' => true,
            'message' => 'Étudiant mis à jour avec succès',
            'data' => $es->findAllApi()
        );
        header('Content-type: application/json');
        echo json_encode($response);
    } else {
        // Sinon, rediriger vers la page d'accueil (pour le web)
        header("location:../index.php");
    }
}