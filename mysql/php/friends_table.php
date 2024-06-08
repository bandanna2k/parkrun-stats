<!DOCTYPE html>
<html>
<title>
New Zealand parkrun Most Events and Attendance Records for Events on and prior to 25/05/2024</title>
<head>
</head>
<script src="https://www.kryogenix.org/code/browser/sorttable/sorttable.js">
</script>
<body>

<style>
body
{
    font-family:'Arial';
    background:#f2f2f2;
    text-align:center;
}
table
{
    border-spacing:0px;
    margin:0px;
    margin-left:auto;
    margin-right:auto;
}
table a
{
    color:inherit;
    text-decoration:none;
}
th
{
    padding:10px;
    text-align:left;
    background-color:#f0f7f7;
    color:black;
}
td
{
    border:0px;
    padding:8px;
    text-align:center;
}

.td_zero { background-color:#fff; }
.td0 { background-color:#e5e5e5; }
.td1 { background-color:#ccc; }
.td2 { background-color:#b2b2b2; }
.td3 { background-color:#999; }
.td4 { background-color:#7f7f7f; }

.td5 { background-color:#666; color:white; }
.td6 { background-color:#4c4c4c; color:white; }
.td7 { background-color:#333; color:white; }
.td8 { background-color:#191919; color:white; }
.td9 { background-color:#000; color:white; }

th {
    color:black;
    font-weight:700;
    text-align:right;
    writing-mode:vertical-rl;
    transform:rotate(180deg);
    vertical-align:center;
    text-align:left;
}
th:nth-child(even) {
    background-color:white
}
th:nth-child(odd) {
    background-color:#f0f0f0
}
td:nth-child(1) {
    color:black;
    font-weight:700;
    text-align:right;
}
tr:nth-child(2n) > td:nth-child(1) {
    background-color:#f0f0f0;
}
tr:nth-child(2n+1) > td:nth-child(1) {
    background-color:white;
}
</style>
<h1>Pairs Table</h1>
<table><thead><th>
</th>
<th>
<a href="https://parkrun.co.nz/parkrunner/6459026/all/" target="6459026">Nathan HEAVER</a>
</th>
<th>
<a href="https://parkrun.co.nz/parkrunner/4225353/all/" target="4225353">Dan JOE</a>
</th>
<th>
<a href="https://parkrun.co.nz/parkrunner/796322/all/" target="796322">Tim ROBBINS</a>
</th>
</thead>

<tr>
    <td>

<?php

include 'credentials.php';

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$athleteSet = new \Ds\Set();

$query = 'SELECT fc.*
          FROM friend_session fs
          LEFT JOIN friend_counts fc USING (friend_session_id)
          WHERE fs.athlete_id = 414811
          ORDER BY fc.athlete_id1, fc.athlete_id2;'

$result = $conn->query($query);
while ($row = $result->fetch_assoc()) {

    $athleteId1 = $row['athlete_id1'];
    $athleteId2 = $row['athlete_id2'];

    $athleteSet.add();
}

while ($row = $result->fetch_assoc()) {

    $athleteId1 = $row['athlete_id1'];
    $athleteId2 = $row['athlete_id2'];

    $athleteSet.add();
}

?>

    </td>
</tr>

<tr><td>
<a href="https://parkrun.co.nz/parkrunner/291411/all/" target="291411">Martin O'SULLIVAN</a>
</td>
<td class="td6">
7</td>
<td class="td9">
46</td>
<td class="td6">
10</td>
</tr>
<tr><td>
<a href="https://parkrun.co.nz/parkrunner/796322/all/" target="796322">Tim ROBBINS</a>
</td>
<td class="td2">
1</td>
<td class="td7">
12</td>
</tr>
<tr><td>
<a href="https://parkrun.co.nz/parkrunner/4225353/all/" target="4225353">Dan JOE</a>
</td>
<td class="td6">
9</td>
</tr>
</table>
<p>* This table shows how many events runner A, has run with runner B.</p></body>
</html>
