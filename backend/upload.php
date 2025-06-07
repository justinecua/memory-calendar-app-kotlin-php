<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

require 'db.php';

if (
    !isset($_POST['user_id']) || 
    !isset($_POST['memory_date']) || 
    !isset($_FILES['image'])
) {
    http_response_code(400);
    echo json_encode(["error" => "Missing required fields."]);
    exit;
}

$user_id = intval($_POST['user_id']);
$memory_date = $_POST['memory_date'];
$description = isset($_POST['description']) ? $_POST['description'] : null;

$image = $_FILES['image'];

// Validate uploaded file
if ($image['error'] !== UPLOAD_ERR_OK) {
    http_response_code(400);
    echo json_encode(["error" => "File upload error: " . $image['error']]);
    exit;
}

// Validate image type (allow jpeg and png for example)
$allowed_types = ['image/jpeg', 'image/png'];
if (!in_array($image['type'], $allowed_types)) {
    http_response_code(400);
    echo json_encode(["error" => "Invalid image type. Only JPEG and PNG allowed."]);
    exit;
}

// Create uploads directory if not exists
$upload_dir = __DIR__ . '/uploads/';
if (!is_dir($upload_dir)) {
    mkdir($upload_dir, 0755, true);
}

// Generate a unique filename to avoid conflicts
$ext = pathinfo($image['name'], PATHINFO_EXTENSION);
$filename = uniqid('img_', true) . '.' . $ext;
$filepath = $upload_dir . $filename;

// Move uploaded file to uploads folder
if (!move_uploaded_file($image['tmp_name'], $filepath)) {
    http_response_code(500);
    echo json_encode(["error" => "Failed to move uploaded file."]);
    exit;
}

// Prepare path for database (relative path)
$image_path_db = "uploads/" . $filename;

// Insert into database
try {
    $stmt = $pdo->prepare("INSERT INTO memories (user_id, image_path, memory_date, description, created_at) VALUES (?, ?, ?, ?, NOW())");
    $stmt->execute([$user_id, $image_path_db, $memory_date, $description]);

    echo json_encode([
        "success" => true,
        "message" => "Image uploaded successfully.",
        "image_path" => $image_path_db
    ]);
} catch (PDOException $e) {
    // Delete the uploaded file if DB insert fails
    unlink($filepath);

    http_response_code(500);
    echo json_encode(["error" => "Database insert failed: " . $e->getMessage()]);
}
?>

