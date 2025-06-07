<?php
header("Content-Type: application/json; charset=UTF-8");

// Include your database connection
require_once 'db.php';

// Get userId from GET parameter
$userId = isset($_GET['userId']) ? intval($_GET['userId']) : 0;

if ($userId <= 0) {
    echo json_encode(["success" => false, "message" => "Invalid user ID"]);
    exit;
}

try {
    $sql = "SELECT id, user_id, image_path, memory_date, description, created_at 
            FROM memories 
            WHERE user_id = :userId 
            ORDER BY created_at DESC";
    
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
    $stmt->execute();

    $memories = $stmt->fetchAll();

    if (count($memories) > 0) {
        echo json_encode(["success" => true, "memories" => $memories]);
    } else {
        echo json_encode(["success" => false, "message" => "No memories found"]);
    }
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database error: " . $e->getMessage()]);
}

