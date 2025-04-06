<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once '../racine.php';
    include_once RACINE . '/service/EtudiantService.php';
    loadAll();
}
function loadAll() {
    $es = new EtudiantService();
    header('Content-type: application/json');
    echo json_encode($es->findAllApi());
}