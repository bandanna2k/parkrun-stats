<html>

<body>
<h1>Friends Table</h1>
</body>

<?php

include 'credentials.php';

$athleteId = $_POST["athleteId"];
$question = $_POST["question"];
$givenAnswer = $_POST["answer"];
$url = hash('sha256', 'V1' . $athleteId . $question);


// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// If url exists, goto url
echo "<p>Url does not exist. Proceeding</p>\n";

// 1. Insert into session (insert into athlete_id, eventQuestion, givenAnswer, url)
// 1a - Get actual answer
$query = "select * from event_volunteer
          join course_event_summary using (course_id, date)
          where athlete_id = " . $athleteId . "
          order by date asc limit 1;";
$result = $conn->query($query);
$row = $result->fetch_assoc();
$finishers = $row['finishers'];
$eventNumber = $row['event_number'];

echo "<p>Finishers: " . $finishers . "</p>\n";
echo "<p>Event number: " . $eventNumber . "</p>\n";

$actualAnswer = $finishers;

// 1b. Insert
$sqlToInsert = "insert into friends_session (athlete_id, question, given_answer, actual_answer, url)
values ('" . $athleteId . "', '" . $question . "', '" . $givenAnswer . "', '" . $actualAnswer . "', '" . $url . "')";
echo "<p>" . $sqlToInsert . "</p>\n";

if ($conn->query($sqlToInsert) === TRUE) {
  echo "New record created successfully";
} else {
  echo "Error: " . $sqlToInsert . "<br>" . $conn->error;
}



echo "<p>";

echo "Athlete ID:";
echo $athleteId;
echo ", ";

echo "Question:";
echo $question;
echo ", ";

echo "Given answer:";
echo $givenAnswer;
echo ", ";

echo "URL:";
echo $url;

echo "</p>\n";




// Update session table with actual answer (update with actual answer)
echo "<p>Updating session with actual answer: 100</p>\n";

// If actual <> given exit
echo "<p>";

echo "Given answer:";
echo $givenAnswer;
echo ", ";

echo "Actual answer:";
echo $finishers;

echo "</p>\n";

// Update session table with friends.
$friend1 = $_POST["athleteId1"];
$friend2 = $_POST["athleteId2"];
$friend3 = $_POST["athleteId3"];
$friend4 = $_POST["athleteId4"];
$friend5 = $_POST["athleteId5"];

$friend6 = $_POST["athleteId6"];
$friend7 = $_POST["athleteId7"];
$friend8 = $_POST["athleteId8"];
$friend9 = $_POST["athleteId9"];

echo "<p>";
echo "Updating session table with friends:";
echo $friend1;

echo ", " . $friend2;
echo ", " . $friend3;
echo ", " . $friend4;
echo ", " . $friend5;
echo ", " . $friend6;
echo ", " . $friend7;
echo ", " . $friend8;
echo ", " . $friend9;

echo "</p>\n";

// Process friends data.
echo "<p>Process friends</p>\n";

$conn->close();

?>

</html>
