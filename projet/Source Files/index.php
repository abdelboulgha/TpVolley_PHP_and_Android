<?php
include_once './racine.php';
?>
<html>
<head>
    <meta charset="UTF-8">
    <title>Gestion des étudiants</title>
</head>
<body>
<form method="POST" action="controller/createEtudiant.php" enctype="multipart/form-data">
    <fieldset>
        <legend>Ajouter un nouveau étudiant</legend>
        <table border="0">
            <tr>
                <td>Nom :</td>
                <td><input type="text" name="nom" required></td>
            </tr>
            <tr>
                <td>Prénom :</td>
                <td><input type="text" name="prenom" required></td>
            </tr>
            <tr>
                <td>Ville :</td>
                <td>
                    <select name="ville" required>
                        <option value="Marrakech">Marrakech</option>
                        <option value="Rabat">Rabat</option>
                        <option value="Agadir">Agadir</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>Sexe :</td>
                <td>
                    M<input type="radio" name="sexe" value="homme" required>
                    F<input type="radio" name="sexe" value="femme">
                </td>
            </tr>
            <tr>
                <td>Date de naissance :</td>
                <td><input type="date" name="dateNaissance" required></td>
            </tr>
            <tr>
                <td>Photo :</td>
                <td><input type="file" name="photo" accept="image/*"></td>
            </tr>
            <tr>
                <td></td>
                <td>
                    <input type="submit" value="Envoyer">
                    <input type="reset" value="Effacer">
                </td>
            </tr>
        </table>
    </fieldset>
</form>

<table border="1">
    <thead>
    <tr>
        <th>ID</th>
        <th>Nom</th>
        <th>Prénom</th>
        <th>Ville</th>
        <th>Sexe</th>
        <th>Date Naissance</th>
        <th>Photo</th>
        <th>Supprimer</th>
        <th>Modifier</th>
    </tr>
    </thead>
    <tbody>
    <?php
    include_once RACINE . '/service/EtudiantService.php';
    $es = new EtudiantService();
    foreach ($es->findAll() as $e) {
        ?>
        <tr>
            <td><?= $e->getId() ?></td>
            <td><?= $e->getNom() ?></td>
            <td><?= $e->getPrenom() ?></td>
            <td><?= $e->getVille() ?></td>
            <td><?= $e->getSexe() ?></td>
            <td><?= $e->getDateNaissance() ?></td>
            <td>
                <?php if($e->getPhoto()): ?>
                    <img src="uploads/<?= $e->getPhoto() ?>" width="50">
                <?php endif; ?>
            </td>
            <td>
                <a href="controller/deleteEtudiant.php?id=<?= $e->getId() ?>">Supprimer</a>
            </td>
            <td>
                <a href="updateForm.php?id=<?= $e->getId() ?>">Modifier</a>
            </td>
        </tr>
    <?php } ?>
    </tbody>
</table>
</body>
</html>