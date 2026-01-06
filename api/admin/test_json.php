<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

// Simple JSON response to test if server is delivering JSON correctly
echo json_encode([
    "success" => true,
    "message" => "Hello JSON",
    "timestamp" => time()
]);
?>
