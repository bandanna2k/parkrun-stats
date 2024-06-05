<html>

<body>
<h1>Hello World!</h1>
</body>

<?php

include 'credentials.php';

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
die("Connection failed: " . $conn->connect_error);
}

// $sql="select count(*) as count from result where athlete_id = 414811";

// $result=mysqli_query($conn, $sql);
// $data=mysqli_fetch_assoc($result);echo $data['count'];

$query = "select course_long_name, date from result join course using (course_id) where course_id = 10 and athlete_id = 414811 order by date desc;";

$result = $conn->query($query);
while ($row = $result->fetch_assoc()) {
    $course_long_name = $row['course_long_name'];
    $date = $row['date'];
    echo "<p>" . $course_long_name . ", " . $date . "</p>\n";
}

class Fruit {
    // Properties
    public $name;
    public $colour;

    function __construct($name, $colour) {
        $this->name = $name;
        $this->colour = $colour;
    }
}

$orange = new Fruit("Blood Orange", "orange");

echo "<p>";
echo $orange->name . " " . $orange->colour;
echo "</p>";

?>
</html>
